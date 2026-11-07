package com.shubham.flightbooking.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** A ticket booking, shown together with its flight details. */
public record Booking(
        int id,
        String pnr,
        int passengerId,
        int flightId,
        int seats,
        BigDecimal totalFare,
        BookingStatus status,
        LocalDateTime bookedAt,
        Flight flight) {
}
