-- V1__initial_schema.sql
-- Redesigned Database Schema for the Global Live-Learning Class Booking System

-- 1. Create Teachers Table
CREATE TABLE teachers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    timezone VARCHAR(100) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 2. Create Parents Table
CREATE TABLE parents (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    timezone VARCHAR(100) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 3. Create Courses Table
CREATE TABLE courses (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 4. Create Offerings Table (A course offering led by a specific teacher)
CREATE TABLE offerings (
    id BIGSERIAL PRIMARY KEY,
    course_id BIGINT NOT NULL,
    teacher_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    timezone VARCHAR(100) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_offering_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    CONSTRAINT fk_offering_teacher FOREIGN KEY (teacher_id) REFERENCES teachers(id) ON DELETE CASCADE
);

-- 5. Create Sessions Table (Multiple granular scheduled sessions per offering)
CREATE TABLE sessions (
    id BIGSERIAL PRIMARY KEY,
    offering_id BIGINT NOT NULL,
    start_time_utc TIMESTAMP WITH TIME ZONE NOT NULL,
    end_time_utc TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_session_offering FOREIGN KEY (offering_id) REFERENCES offerings(id) ON DELETE CASCADE,
    CONSTRAINT chk_session_times CHECK (end_time_utc > start_time_utc)
);

-- 6. Create Bookings Table (Junction representing parent enrollment in a full course offering)
CREATE TABLE bookings (
    id BIGSERIAL PRIMARY KEY,
    parent_id BIGINT NOT NULL,
    offering_id BIGINT NOT NULL,
    booked_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_booking_parent FOREIGN KEY (parent_id) REFERENCES parents(id) ON DELETE CASCADE,
    CONSTRAINT fk_booking_offering FOREIGN KEY (offering_id) REFERENCES offerings(id) ON DELETE CASCADE,
    CONSTRAINT uq_parent_offering UNIQUE (parent_id, offering_id)
);

-- 7. Optimized Secondary Indexes
CREATE INDEX idx_sessions_offering ON sessions (offering_id);
CREATE INDEX idx_sessions_start_time ON sessions (start_time_utc);
CREATE INDEX idx_bookings_parent ON bookings (parent_id);
CREATE INDEX idx_bookings_offering ON bookings (offering_id);
