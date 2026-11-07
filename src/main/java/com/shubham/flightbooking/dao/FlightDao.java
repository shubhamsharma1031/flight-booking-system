package com.shubham.flightbooking.dao;

import com.shubham.flightbooking.model.Flight;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Data access for the flights table. Every query uses PreparedStatement to prevent SQL injection. */
public class FlightDao {

    private static final String COLUMNS = "flight_id, flight_number, airline, source, destination, "
            + "departure_time, arrival_time, total_seats, available_seats, fare";

    public List<Flight> findUpcoming(Connection conn) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM flights WHERE departure_time > NOW() ORDER BY departure_time";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            return mapAll(ps.executeQuery());
        }
    }

    /** Search by route; the travel date is optional (null = any upcoming date). */
    public List<Flight> search(Connection conn, String source, String destination, LocalDate date) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT " + COLUMNS + " FROM flights "
                + "WHERE LOWER(source) = LOWER(?) AND LOWER(destination) = LOWER(?) AND departure_time > NOW()");
        if (date != null) {
            sql.append(" AND DATE(departure_time) = ?");
        }
        sql.append(" ORDER BY departure_time");
        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setString(1, source.trim());
            ps.setString(2, destination.trim());
            if (date != null) {
                ps.setObject(3, date);
            }
            return mapAll(ps.executeQuery());
        }
    }

    public Optional<Flight> findByNumber(Connection conn, String flightNumber) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM flights WHERE flight_number = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, flightNumber.trim().toUpperCase());
            List<Flight> result = mapAll(ps.executeQuery());
            return result.stream().findFirst();
        }
    }

    /**
     * Reads a flight and locks its row until the transaction ends (SELECT ... FOR UPDATE).
     * This stops two bookings from taking the same last seats at the same time.
     */
    public Optional<Flight> findByIdForUpdate(Connection conn, int flightId) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM flights WHERE flight_id = ? FOR UPDATE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, flightId);
            return mapAll(ps.executeQuery()).stream().findFirst();
        }
    }

    public void updateAvailableSeats(Connection conn, int flightId, int availableSeats) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE flights SET available_seats = ? WHERE flight_id = ?")) {
            ps.setInt(1, availableSeats);
            ps.setInt(2, flightId);
            ps.executeUpdate();
        }
    }

    private List<Flight> mapAll(ResultSet rs) throws SQLException {
        List<Flight> flights = new ArrayList<>();
        try (rs) {
            while (rs.next()) {
                flights.add(map(rs));
            }
        }
        return flights;
    }

    static Flight map(ResultSet rs) throws SQLException {
        return new Flight(
                rs.getInt("flight_id"),
                rs.getString("flight_number"),
                rs.getString("airline"),
                rs.getString("source"),
                rs.getString("destination"),
                toLocal(rs.getTimestamp("departure_time")),
                toLocal(rs.getTimestamp("arrival_time")),
                rs.getInt("total_seats"),
                rs.getInt("available_seats"),
                rs.getBigDecimal("fare"));
    }

    private static java.time.LocalDateTime toLocal(Timestamp ts) {
        return ts == null ? null : ts.toLocalDateTime();
    }
}
