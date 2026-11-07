package com.shubham.flightbooking.model;

/** A registered passenger. id is 0 until the passenger is saved. */
public record Passenger(int id, String fullName, String email, String phone, int age) {
}
