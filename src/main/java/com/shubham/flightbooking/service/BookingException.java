package com.shubham.flightbooking.service;

/** Thrown when a business rule is broken, for example not enough seats. */
public class BookingException extends Exception {
    private static final long serialVersionUID = 1L;

    public BookingException(String message) {
        super(message);
    }
}
