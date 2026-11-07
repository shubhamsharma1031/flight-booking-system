package com.shubham.flightbooking.dao;

import com.shubham.flightbooking.model.Booking;
import com.shubham.flightbooking.model.BookingStatus;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Data access for the bookings table. Bookings are always read together with their flight (JOIN). */
public class BookingDao {

    private static final String SELECT_WITH_FLIGHT =
            "SELECT b.booking_id, b.pnr, b.passenger_id, b.flight_id, b.seats, b.total_fare, b.status, b.booked_at, "
            + "f.flight_id, f.flight_number, f.airline, f.source, f.destination, f.departure_time, f.arrival_time, "
            + "f.total_seats, f.available_seats, f.fare "
            + "FROM bookings b JOIN flights f ON f.flight_id = b.flight_id ";

    public int insert(Connection conn, String pnr, int passengerId, int flightId, int seats, BigDecimal totalFare)
            throws SQLException {
        String sql = "INSERT INTO bookings (pnr, passenger_id, flight_id, seats, total_fare, status) "
                + "VALUES (?, ?, ?, ?, ?, 'CONFIRMED')";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, pnr);
            ps.setInt(2, passengerId);
            ps.setInt(3, flightId);
            ps.setInt(4, seats);
            ps.setBigDecimal(5, totalFare);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    public boolean pnrExists(Connection conn, String pnr) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM bookings WHERE pnr = ?")) {
            ps.setString(1, pnr);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** Reads a booking and locks it so it cannot be cancelled twice at the same time. */
    public Optional<Booking> findByPnrForUpdate(Connection conn, String pnr) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SELECT_WITH_FLIGHT + "WHERE b.pnr = ? FOR UPDATE")) {
            ps.setString(1, pnr.trim().toUpperCase());
            List<Booking> result = mapAll(ps.executeQuery());
            return result.stream().findFirst();
        }
    }

    public Optional<Booking> findByPnr(Connection conn, String pnr) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SELECT_WITH_FLIGHT + "WHERE b.pnr = ?")) {
            ps.setString(1, pnr.trim().toUpperCase());
            return mapAll(ps.executeQuery()).stream().findFirst();
        }
    }

    public List<Booking> findByPassenger(Connection conn, int passengerId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                SELECT_WITH_FLIGHT + "WHERE b.passenger_id = ? ORDER BY f.departure_time")) {
            ps.setInt(1, passengerId);
            return mapAll(ps.executeQuery());
        }
    }

    public void updateStatus(Connection conn, int bookingId, BookingStatus status) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("UPDATE bookings SET status = ? WHERE booking_id = ?")) {
            ps.setString(1, status.name());
            ps.setInt(2, bookingId);
            ps.executeUpdate();
        }
    }

    private List<Booking> mapAll(ResultSet rs) throws SQLException {
        List<Booking> bookings = new ArrayList<>();
        try (rs) {
            while (rs.next()) {
                bookings.add(new Booking(
                        rs.getInt("booking_id"),
                        rs.getString("pnr"),
                        rs.getInt("passenger_id"),
                        rs.getInt("flight_id"),
                        rs.getInt("seats"),
                        rs.getBigDecimal("total_fare"),
                        BookingStatus.valueOf(rs.getString("status")),
                        rs.getTimestamp("booked_at").toLocalDateTime(),
                        FlightDao.map(rs)));
            }
        }
        return bookings;
    }
}
