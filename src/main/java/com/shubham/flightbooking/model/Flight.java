package com.shubham.flightbooking.model;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

/** A scheduled flight. */
public record Flight(
        int id,
        String flightNumber,
        String airline,
        String source,
        String destination,
        LocalDateTime departureTime,
        LocalDateTime arrivalTime,
        int totalSeats,
        int availableSeats,
        BigDecimal fare) {

    public Duration duration() {
        return Duration.between(departureTime, arrivalTime);
    }
}
