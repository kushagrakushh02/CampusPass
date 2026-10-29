-- CampusPass : Event Registration and Ticketing System
-- Run once:  mysql -u root -p < database/schema.sql
-- Needs MySQL 8.0.16+ (CHECK constraints and generated columns are enforced)

DROP DATABASE IF EXISTS campuspass;
CREATE DATABASE campuspass CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE campuspass;

CREATE TABLE users (
    user_id        INT AUTO_INCREMENT PRIMARY KEY,
    name           VARCHAR(80)  NOT NULL,
    email          VARCHAR(120) NOT NULL UNIQUE,
    password_hash  VARCHAR(255) NOT NULL,          -- PBKDF2 hash, never the plain password
    role           ENUM('USER','ADMIN') NOT NULL DEFAULT 'USER',
    created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE events (
    event_id    INT AUTO_INCREMENT PRIMARY KEY,
    title       VARCHAR(150) NOT NULL,
    category    VARCHAR(40)  NOT NULL DEFAULT 'General',
    venue       VARCHAR(150) NOT NULL,
    event_date  DATETIME     NOT NULL,
    description TEXT,
    status      ENUM('ACTIVE','CANCELLED') NOT NULL DEFAULT 'ACTIVE',
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_events_date (event_date)
) ENGINE=InnoDB;

CREATE TABLE ticket_types (
    type_id     INT AUTO_INCREMENT PRIMARY KEY,
    event_id    INT           NOT NULL,
    label       VARCHAR(60)   NOT NULL,
    price       DECIMAL(10,2) NOT NULL DEFAULT 0,
    total_seats INT           NOT NULL,
    seats_left  INT           NOT NULL,
    CONSTRAINT fk_tt_event  FOREIGN KEY (event_id) REFERENCES events(event_id) ON DELETE CASCADE,
    -- last line of defence: seats can never go negative or above capacity
    CONSTRAINT chk_seats    CHECK (seats_left >= 0 AND seats_left <= total_seats),
    CONSTRAINT chk_price    CHECK (price >= 0)
) ENGINE=InnoDB;

CREATE TABLE registrations (
    reg_id       INT AUTO_INCREMENT PRIMARY KEY,
    user_id      INT         NOT NULL,
    event_id     INT         NOT NULL,
    type_id      INT         NOT NULL,
    quantity     INT         NOT NULL,
    ticket_code  VARCHAR(20) NOT NULL UNIQUE,
    status       ENUM('CONFIRMED','CANCELLED') NOT NULL DEFAULT 'CONFIRMED',
    booked_at    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    cancelled_at TIMESTAMP   NULL,
    -- One live booking per person per event. The value is NULL once a booking is
    -- cancelled, and MySQL lets many NULLs share a UNIQUE index, so history is kept.
    active_key   VARCHAR(40) GENERATED ALWAYS AS
                 (IF(status = 'CONFIRMED', CONCAT(user_id, '-', event_id), NULL)) STORED,
    CONSTRAINT fk_reg_user  FOREIGN KEY (user_id)  REFERENCES users(user_id),
    CONSTRAINT fk_reg_event FOREIGN KEY (event_id) REFERENCES events(event_id),
    CONSTRAINT fk_reg_type  FOREIGN KEY (type_id)  REFERENCES ticket_types(type_id),
    CONSTRAINT chk_qty      CHECK (quantity BETWEEN 1 AND 10),
    UNIQUE KEY uq_one_active_booking (active_key),
    INDEX idx_reg_user (user_id),
    INDEX idx_reg_event (event_id)
) ENGINE=InnoDB;
