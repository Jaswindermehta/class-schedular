# class-scheduler

A Java Spring Boot 3 + PostgreSQL backend engineering project for a **global live-learning class booking system**.

This service coordinates academic courses, teacher scheduling, parent enrollment bookings, and timezone projections. It ensures strict data consistency under high concurrent load.

---

## 1. Architecture Overview

The system is structured as a **focused, modular monolith** designed for code clarity, straightforward testing, and transparent execution:

- **REST Controller Layer**: Handles HTTP requests, parameter mapping, and input validation using standard validation annotations.
- **Service Layer**: Houses all core business workflows, timezone logic, database interactions, and transaction boundaries. Direct classes are used instead of service interfaces to keep the codebase simple and maintainable.
- **Injectable Mapping Layer**: Declares manual Entity-to-Response-DTO mapping classes as `@Component` beans. This maintains a clean boundary between persistence and presentation states, preventing circular JSON serialization and lazy-loading exceptions.
- **Global Error Handling Advice**: Catches custom exceptions (`ResourceNotFoundException`, `ConflictException`, `BadRequestException`) and formats them into structured JSON error payloads.

---

## 2. Database Design

The relational database schema is managed sequentially in `V1__initial_schema.sql` by **Flyway**:

```text
+--------------+        +--------------+        +--------------+
|   teachers   |        |   courses    |        |   parents    |
+--------------+        +--------------+        +--------------+
| id (PK)      |        | id (PK)      |        | id (PK)      |
| name         |        | title        |        | name         |
| email (UQ)   |        | description  |        | email (UQ)   |
| timezone     |        | created_at   |        | timezone     |
| created_at   |        +--------------+        | created_at   |
+-------+------+               |                +-------+------+
        |                      |                        |
        |  +--------------+    |                        |
        +--+  offerings   +----+                        |
           +--------------+                             |
           | id (PK)      |                             |
           | course_id(FK)|                             |
           | teacher_id(FK)                             |
           | name         |                             |
           | timezone     |                             |
           | created_at   |                             |
           +---+-----+----+                             |
               |     |                                  |
               |     +------------------+               |
               |                        |               |
        +------+-------+        +-------+------+        |
        |   sessions   |        |   bookings   +--------+
        +--------------+        +--------------+
        | id (PK)      |        | id (PK)      |
        | offering_id  |        | parent_id(FK)|
        | start_time   |        | offering_id  |
        | end_time     |        | booked_at    |
        | created_at   |        +--------------+
        +--------------+        | UNIQUE(p, o) |
                                +--------------+
```

- **`teachers` & `parents`**: Store regional timezone IDs (e.g. `Europe/London`) to project schedule displays dynamically.
- **`sessions`**: Implements a database CHECK constraint ensuring `end_time_utc > start_time_utc`.
- **`bookings`**: Implements a database-level unique constraint `UNIQUE(parent_id, offering_id)` preventing duplicate student enrollments in the same offering cohort.

---

## 3. Timezone Handling Strategy

To coordinate scheduling across multiple global timezones, the system separates internal storage from API presentation:

1. **Internal Storage**: All session times are normalized and persisted in UTC using PostgreSQL `TIMESTAMP WITH TIME ZONE` columns. This ensures simple time comparison logic and keeps indexes performant.
2. **Ingestion**: Teachers input schedules in their local timezone context as `LocalDateTime`. The service fetches the offering timezone, maps a `ZonedDateTime` at that location, and converts it to a UTC `OffsetDateTime` before database insertion.
3. **Presentation**: Timestamps are converted into local times (`startTime`, `endTime`) projected in the parent's preferred timezone during Response DTO assembly.

---

## 4. Concurrency Handling Approach

To prevent race conditions when a parent triggers concurrent booking transactions (e.g., submitting parallel requests from multiple open tabs), the system implements database-level pessimistic locking:

- **Row-level Locks**: The transaction acquires a lock on the target parent record using `@Lock(LockModeType.PESSIMISTIC_WRITE)` during lookup.
- **Blocking parallel transactions**: Concurrent transactions attempting to register bookings for the same parent block until the active transaction commits or rolls back.
- **Why JVM `synchronized` was avoided**: JVM-level locks only secure code within a single server instance. Distributed systems scaling to multiple server instances will bypass JVM locks. Database-level locking guarantees thread safety across all nodes.

---

## 5. Booking Conflict Detection (Overlap)

Overlap checking is executed dynamically at the **session level** inside `BookingService`:

- **Decoupled Cohort Scheduling**: Two course offerings might meet on different days or hours (e.g. Tuesdays vs. Thursdays). Evaluating overlaps at the offering level would prevent parents from booking multiple concurrent courses.
- **Session-Level Comparison**: The service retrieves all sessions of the requested offering and compares them with the sessions of all offerings the parent has already booked.
- **Overlap Logic**: Two sessions overlap if:
  `start1 < end2 AND end1 > start2`
  *(Evaluated directly using UTC timestamps to eliminate regional offset conversion math during comparison).*

---

## 6. Tradeoffs & Assumptions

Intentional design boundaries were defined for this scope of work:
- **Authentication**: Omitted to keep the project focused on scheduling and concurrency models.
- **Capacity Management**: Student capacity limits and seat allocations are omitted to focus on schedule overlap mechanics.
- **Booking Cancellation**: Booking deletions and transaction refunds are deferred to keep API contracts minimal.
- **Modular Monolith**: Selected over microservices to avoid network latencies, distributed transaction complexities (like Sagas), and operations overhead.

---

## 7. Running Locally

### Step 1: Spin Up the Database
Run a PostgreSQL 16 instance inside a container:
```bash
docker compose up -d
```

### Step 2: Build & Run Tests
Compile and verify all components:
```bash
mvn clean install
```

### Step 3: Start the Application
Launch the Spring Boot server:
```bash
mvn spring-boot:run
```
Access the Swagger UI dashboard at: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

---

## 8. Walkthrough API Example Flow

### 1. Register a Teacher (New York)
`POST /teachers`
```json
{
  "name": "Sarah Miller",
  "email": "sarah@example.com",
  "timezone": "America/New_York"
}
```

### 2. Register a Parent (London)
`POST /parents`
```json
{
  "name": "John Watson",
  "email": "john@example.com",
  "timezone": "Europe/London"
}
```

### 3. Register a Course Catalog Entry
`POST /courses`
```json
{
  "title": "Intro to Java",
  "description": "Learn object-oriented principles."
}
```

### 4. Create an Offering Cohort
`POST /offerings`
```json
{
  "courseId": 1,
  "teacherId": 1,
  "name": "Intro Java - Mon Cohort",
  "timezone": "America/New_York"
}
```

### 5. Add Sessions in Bulk
`POST /offerings/1/sessions`
*Note: Ingests New York local time (9:00 AM NY translates to 13:00 UTC).*
```json
[
  {
    "startTime": "2026-06-01T09:00:00",
    "endTime": "2026-06-01T10:30:00"
  }
]
```

### 6. Query Available Offerings (Projected in London Time)
`GET /offerings?timezone=Europe/London`
*Returns the sessions projected into John Watson's local timezone (13:00 UTC projects to 14:00 Europe/London offset in June).*
```json
[
  {
    "id": 1,
    "name": "Intro Java - Mon Cohort",
    "timezone": "America/New_York",
    "courseId": 1,
    "courseTitle": "Intro to Java",
    "sessions": [
      {
        "id": 1,
        "offeringId": 1,
        "startTime": "2026-06-01T14:00:00+01:00",
        "endTime": "2026-06-01T15:30:00+01:00",
        "timezone": "Europe/London"
      }
    ]
  }
]
```

### 7. Book the Offering
`POST /bookings`
```json
{
  "parentId": 1,
  "offeringId": 1
}
```
*If you submit an overlapping request or duplicate registration, the system returns a detailed `409 Conflict` error.*
