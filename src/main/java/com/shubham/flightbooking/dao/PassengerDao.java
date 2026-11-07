package com.shubham.flightbooking.dao;

import com.shubham.flightbooking.model.Passenger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

/** Data access for the passengers table. */
public class PassengerDao {

    public Passenger insert(Connection conn, Passenger p) throws SQLException {
        String sql = "INSERT INTO passengers (full_name, email, phone, age) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.fullName());
            ps.setString(2, p.email());
            ps.setString(3, p.phone());
            ps.setInt(4, p.age());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return new Passenger(keys.getInt(1), p.fullName(), p.email(), p.phone(), p.age());
            }
        }
    }

    public Optional<Passenger> findByEmail(Connection conn, String email) throws SQLException {
        String sql = "SELECT passenger_id, full_name, email, phone, age FROM passengers WHERE email = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email.trim().toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(new Passenger(
                        rs.getInt("passenger_id"),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getInt("age")));
            }
        }
    }
}
