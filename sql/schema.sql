-- ============================================================
-- Flight Booking System: database schema (MySQL 8.0+)
-- ============================================================
CREATE DATABASE IF NOT EXISTS flight_booking;
USE flight_booking;

DROP TABLE IF EXISTS bookings;
DROP TABLE IF EXISTS passengers;
DROP TABLE IF EXISTS flights;

CREATE TABLE flights (
    flight_id        INT AUTO_INCREMENT PRIMARY KEY,
    flight_number    VARCHAR(10)   NOT NULL UNIQUE,
    airline          VARCHAR(50)   NOT NULL,
    source           VARCHAR(50)   NOT NULL,
    destination      VARCHAR(50)   NOT NULL,
    departure_time   DATETIME      NOT NULL,
    arrival_time     DATETIME      NOT NULL,
    total_seats      INT           NOT NULL,
    available_seats  INT           NOT NULL,
    fare             DECIMAL(10,2) NOT NULL,
    CONSTRAINT chk_seats CHECK (available_seats BETWEEN 0 AND total_seats),
    CONSTRAINT chk_route CHECK (source <> destination),
    CONSTRAINT chk_times CHECK (arrival_time > departure_time)
);

CREATE TABLE passengers (
    passenger_id  INT AUTO_INCREMENT PRIMARY KEY,
    full_name     VARCHAR(100) NOT NULL,
    email         VARCHAR(100) NOT NULL UNIQUE,
    phone         VARCHAR(15)  NOT NULL,
    age           INT          NOT NULL,
    created_at    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_age CHECK (age BETWEEN 1 AND 120)
);

CREATE TABLE bookings (
    booking_id    INT AUTO_INCREMENT PRIMARY KEY,
    pnr           CHAR(6)       NOT NULL UNIQUE,
    passenger_id  INT           NOT NULL,
    flight_id     INT           NOT NULL,
    seats         INT           NOT NULL,
    total_fare    DECIMAL(10,2) NOT NULL,
    status        ENUM('CONFIRMED', 'CANCELLED') NOT NULL DEFAULT 'CONFIRMED',
    booked_at     TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_booking_passenger FOREIGN KEY (passenger_id) REFERENCES passengers(passenger_id),
    CONSTRAINT fk_booking_flight    FOREIGN KEY (flight_id)    REFERENCES flights(flight_id),
    CONSTRAINT chk_booking_seats CHECK (seats BETWEEN 1 AND 6)
);

CREATE INDEX idx_flights_route ON flights (source, destination, departure_time);
