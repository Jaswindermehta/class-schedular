# Video Demo Walkthrough Script: Global Class Scheduler

This document serves as your complete step-by-step recording script and copy-paste database for your project walkthrough video.

---

## 🎬 Pre-Recording Checklist
1. **Docker**: Ensure Docker Desktop is running.
2. **Terminal**: Open a terminal (CMD or PowerShell) in `E:\Projects\class-scheduler`.
3. **Browser**: Open Chrome/Firefox to **[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)** (once started).

---

## 📽️ Scene 1: Project Setup & Startup
* **Visuals on screen**: Open terminal/CMD.
* **Your action**: Start the Docker database container and run the Spring Boot application.

### 🎙️ Narration (What to say):
> *"Hello! In this video, I will demonstrate our global live-learning class booking system built with Spring Boot 3, Java 21, and PostgreSQL. We will cover project setup, timezone conversion, conflict detection, and concurrent locking.*
>
> *First, let's spin up our PostgreSQL 16 database container using Docker Compose. As you can see, the database is running successfully. Next, we start our Spring Boot application using Maven. The application initializes, automatically applies all Flyway database migrations, and boots our web server on port 8080. Now, let's open our web browser to access our fully interactive Swagger API documentation."*

### 💻 Commands to run on screen:
```bash
# 1. Spin up the PostgreSQL Database
docker compose up -d

# 2. Boot the Spring Boot application (Uses IntelliJ embedded JDK 21)
powershell -Command "$env:JAVA_HOME='C:\Program Files\JetBrains\IntelliJ IDEA 2025.2.1\jbr'; mvn spring-boot:run"
```

---

## 📽️ Scene 2: Creating Base Entities (Teachers & Parents)
* **Visuals on screen**: Switch to Swagger UI and open the **`Teachers`** and **`Parents`** sections.
* **Your action**: Create a teacher and a parent.

### 🎙️ Narration (What to say):
> *"Now that the application is running, let's register our teachers and parents. To show our robust timezone projection logic, we will register a teacher, Sarah Miller, residing in the America/New_York timezone, and a parent, John Watson, residing in the Europe/London timezone. Let's send these POST requests."*

### 📥 Payloads to send in Swagger:

#### 1. Register Teacher (New York)
* **Endpoint**: `POST /teachers`
* **JSON Payload**:
```json
{
  "name": "Sarah Miller",
  "email": "sarah.miller@example.com",
  "timezone": "America/New_York"
}
```
* **Expected Response**: Teacher created with `id = 1` (or next ID).

#### 2. Register Parent (London)
* **Endpoint**: `POST /parents`
* **JSON Payload**:
```json
{
  "name": "John Watson",
  "email": "john.watson@example.com",
  "timezone": "Europe/London"
}
```
* **Expected Response**: Parent created with `id = 1` (or next ID).

---

## 📽️ Scene 3: Course Catalog & Creating Offerings
* **Visuals on screen**: Switch to **`Courses`** and **`Offerings`** sections in Swagger.
* **Your action**: Create a course and a cohort offering.

### 🎙️ Narration (What to say):
> *"Next, we publish a course catalog entry for 'Intro to Java'. Using the new Course ID and our Teacher ID, we create an offering cohort named 'Intro Java - Monday Cohort', taught in Sarah's America/New_York timezone context."*

### 📥 Payloads to send in Swagger:

#### 1. Publish Course Catalog Entry
* **Endpoint**: `POST /courses`
* **JSON Payload**:
```json
{
  "title": "Intro to Java",
  "description": "Learn variables, classes, and object-oriented principles."
}
```
* **Expected Response**: Course created with `id = 1`.

#### 2. Create Course Offering Cohort
* **Endpoint**: `POST /offerings`
* **JSON Payload**:
```json
{
  "courseId": 1,
  "teacherId": 1,
  "name": "Intro Java - Mon Cohort",
  "timezone": "America/New_York"
}
```
* **Expected Response**: Offering created with `id = 1`.

---

## 📽️ Scene 4: Adding Scheduled Sessions
* **Visuals on screen**: Expand **`POST /offerings/{offeringId}/sessions`** in Swagger.
* **Your action**: Publish a bulk session in Sarah's local wall-clock time.

### 🎙️ Narration (What to say):
> *"Now we publish scheduled weekly sessions for this cohort. Our system ingests session dates and times in the teacher's local wall-clock context. Sarah schedules a Monday session from 9:00 AM to 10:30 AM in New York. Under the hood, our service converts this into a normalized UTC offset of 13:00 to 14:30 and persists it as a TIMESTAMP WITH TIME ZONE in PostgreSQL."*

### 📥 Payloads to send in Swagger:
* **Endpoint**: `POST /offerings/1/sessions`
* **JSON Payload**:
```json
[
  {
    "startTime": "2026-06-01T09:00:00",
    "endTime": "2026-06-01T10:30:00"
  }
]
```
* **Expected Response**: A JSON array containing the session projected in UTC offset (`2026-06-01T13:00:00Z`).

---

## 📽️ Scene 5: Timezone Projection & Viewing Offerings
* **Visuals on screen**: Expand **`GET /offerings`** in Swagger.
* **Your action**: Send a GET request with `timezone = Europe/London`.

### 🎙️ Narration (What to say):
> *"Let's see our timezone conversion handling in action. When parent John Watson, residing in London, browses catalog offerings, he wants to view schedules projected in his local timezone. If we query available offerings with the parameter 'Europe/London', the system dynamically reads John's local timezone offset (which is British Summer Time UTC+1 in June) and projects the session. As you can see, Sarah's 9:00 AM New York session is projected beautifully as 2:00 PM for John Watson in London."*

### 📥 Request Details in Swagger:
* **Endpoint**: `GET /offerings?timezone=Europe/London`
* **Expected Response Session block**:
```json
"sessions": [
  {
    "startTime": "2026-06-01T14:00:00+01:00",
    "endTime": "2026-06-01T15:30:00+01:00",
    "timezone": "Europe/London"
  }
]
```

---

## 📽️ Scene 6: Booking Success & Conflict Detection (Overlapping)
* **Visuals on screen**: Expand **`POST /bookings`** in Swagger.
* **Your action**: Book Offering 1 successfully, then create an overlapping Offering 2, and try booking it to show conflict detection.

### 🎙️ Narration (What to say):
> *"Let's test parent booking and schedule conflict detection. First, parent John Watson books the 'Intro Java - Monday Cohort' offering successfully. 
> 
> Next, let's register an alternate Monday cohort, Offering B, taught by Sarah. We'll publish a session that overlaps on Monday from 9:30 AM to 11:00 AM NY time. Since the parent is already booked in Cohort A which runs from 9:00 AM to 10:30 AM, these sessions overlap by one hour.
>
> If John Watson attempts to book this overlapping cohort, the system runs a session-level overlap comparison directly in UTC, blocks the booking, and returns a detailed 409 Conflict exception explaining the schedule overlap. The conflict detection works flawlessly!"*

### 📥 Payloads to send in Swagger:

#### 1. Book Cohort A successfully
* **Endpoint**: `POST /bookings`
* **JSON Payload**:
```json
{
  "parentId": 1,
  "offeringId": 1
}
```
* **Expected Response**: `201 Created` containing booking confirmation.

#### 2. Create Alternate Overlapping Cohort B
* **Endpoint**: `POST /offerings`
* **JSON Payload**:
```json
{
  "courseId": 1,
  "teacherId": 1,
  "name": "Intro Java - Mon Alternate",
  "timezone": "America/New_York"
}
```
* **Expected Response**: Offering created with `id = 2`.

#### 3. Add Overlapping Session to Cohort B
* **Endpoint**: `POST /offerings/2/sessions`
* **JSON Payload**:
```json
[
  {
    "startTime": "2026-06-01T09:30:00",
    "endTime": "2026-06-01T11:00:00"
  }
]
```

#### 4. Attempt Overlapping Booking (Must Fail)
* **Endpoint**: `POST /bookings`
* **JSON Payload**:
```json
{
  "parentId": 1,
  "offeringId": 2
}
```
* **Expected Response**: `409 Conflict` containing the message:
`"Schedule conflict: Session overlaps with session (ID: 1) in offering (ID: 1)"`

---

## 📽️ Scene 7: Concurrent Booking & Pessimistic Write Locks
* **Visuals on screen**: Open code file **`BookingService.java`** in your IDE at line 55.
* **Your action**: Highlight the `@Lock(LockModeType.PESSIMISTIC_WRITE)` repository call or `parentRepository.findByIdForUpdate`.

### 🎙️ Narration (What to say):
> *"To ensure strict data consistency under high concurrent load, we implemented a robust database-level pessimistic locking mechanism. When a parent initiates a booking request, we lock the target parent record using `@Lock(LockModeType.PESSIMISTIC_WRITE)` in JPA. 
> 
> If a parent opens multiple browser tabs and clicks 'Book' concurrently, parallel threads attempting to register bookings for the same parent are blocked and queued at the database level. This guarantees that double bookings or duplicate overlapping registrations are physically impossible in a distributed environment, ensuring absolute consistency.*
>
> *This concludes our demonstration of our robust, concurrent, timezone-aware global live-learning booking system. Thank you!"*
