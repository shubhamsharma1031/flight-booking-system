package com.shubham.flightbooking.service;

import com.shubham.flightbooking.dao.BookingDao;
import com.shubham.flightbooking.dao.FlightDao;
import com.shubham.flightbooking.dao.PassengerDao;
import com.shubham.flightbooking.db.DatabaseConnection;
import com.shubham.flightbooking.model.Booking;
import com.shubham.flightbooking.model.BookingStatus;
import com.shubham.flightbooking.model.Flight;
import com.shubham.flightbooking.model.Passenger;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Business logic: validation, booking and cancellation.
 * Booking and cancelling run inside a database transaction, so seats and bookings never get out of sync.
 */
public class BookingService {

    public static final int MAX_SEATS_PER_BOOKING = 6;

    private static final Pattern EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");
    private static final Pattern PHONE = Pattern.compile("^[6-9]\\d{9}$");   // Indian mobile number
    private static final Pattern NAME = Pattern.compile("^[A-Za-z][A-Za-z .'-]{1,99}$");
    private static final String PNR_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // no 0/O or 1/I confusion
    private static final SecureRandom RANDOM = new SecureRandom();

    private final FlightDao flightDao = new FlightDao();
    private final PassengerDao passengerDao = new PassengerDao();
    private final BookingDao bookingDao = new BookingDao();

    // ---------- Flights ----------

    public List<Flight> listUpcomingFlights() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return flightDao.findUpcoming(conn);
        }
    }

    public List<Flight> searchFlights(String source, String destination, LocalDate date) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return flightDao.search(conn, source, destination, date);
        }
    }

    // ---------- Passengers ----------

    public Passenger registerPassenger(String fullName, String email, String phone, int age)
            throws SQLException, BookingException {
        String cleanName = fullName == null ? "" : fullName.trim().replaceAll("\\s+", " ");
        String cleanEmail = email == null ? "" : email.trim().toLowerCase();
        String cleanPhone = phone == null ? "" : phone.trim();

        if (!NAME.matcher(cleanName).matches()) {
            throw new BookingException("Please enter a valid name (letters and spaces only).");
        }
        if (!EMAIL.matcher(cleanEmail).matches()) {
            throw new BookingException("Please enter a valid email address.");
        }
        if (!PHONE.matcher(cleanPhone).matches()) {
            throw new BookingException("Please enter a valid 10-digit mobile number.");
        }
        if (age < 1 || age > 120) {
            throw new BookingException("Age must be between 1 and 120.");
        }
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (passengerDao.findByEmail(conn, cleanEmail).isPresent()) {
                throw new BookingException("A passenger with this email is already registered.");
            }
            return passengerDao.insert(conn, new Passenger(0, cleanName, cleanEmail, cleanPhone, age));
        }
    }

    public Passenger getPassenger(String email) throws SQLException, BookingException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return passengerDao.findByEmail(conn, email)
                    .orElseThrow(() -> new BookingException("No passenger found with email " + email));
        }
    }

    // ---------- Bookings ----------

    public Booking bookTicket(String email, String flightNumber, int seats) throws SQLException, BookingException {
        if (seats < 1 || seats > MAX_SEATS_PER_BOOKING) {
            throw new BookingException("You can book between 1 and " + MAX_SEATS_PER_BOOKING + " seats.");
        }
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Passenger passenger = passengerDao.findByEmail(conn, email)
                        .orElseThrow(() -> new BookingException("Please register first. No passenger with email " + email));
                Flight found = flightDao.findByNumber(conn, flightNumber)
                        .orElseThrow(() -> new BookingException("Flight " + flightNumber + " does not exist."));
                // lock the flight row so the seat count cannot change until we commit
                Flight flight = flightDao.findByIdForUpdate(conn, found.id()).orElseThrow();

                if (!flight.departureTime().isAfter(LocalDateTime.now())) {
                    throw new BookingException("This flight has already departed.");
                }
                if (flight.availableSeats() < seats) {
                    throw new BookingException("Only " + flight.availableSeats() + " seat(s) left on " + flight.flightNumber() + ".");
                }

                BigDecimal totalFare = flight.fare().multiply(BigDecimal.valueOf(seats));
                String pnr = newPnr(conn);
                bookingDao.insert(conn, pnr, passenger.id(), flight.id(), seats, totalFare);
                flightDao.updateAvailableSeats(conn, flight.id(), flight.availableSeats() - seats);
                conn.commit();
                return bookingDao.findByPnr(conn, pnr).orElseThrow();
            } catch (SQLException | BookingException | RuntimeException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public List<Booking> getBookings(String email) throws SQLException, BookingException {
        Passenger passenger = getPassenger(email);
        try (Connection conn = DatabaseConnection.getConnection()) {
            return bookingDao.findByPassenger(conn, passenger.id());
        }
    }

    public Booking cancelBooking(String pnr) throws SQLException, BookingException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Booking booking = bookingDao.findByPnrForUpdate(conn, pnr)
                        .orElseThrow(() -> new BookingException("No booking found with PNR " + pnr));
                if (booking.status() == BookingStatus.CANCELLED) {
                    throw new BookingException("Booking " + booking.pnr() + " is already cancelled.");
                }
                Flight flight = flightDao.findByIdForUpdate(conn, booking.flightId()).orElseThrow();
                if (!flight.departureTime().isAfter(LocalDateTime.now())) {
                    throw new BookingException("This flight has already departed and cannot be cancelled.");
                }
                bookingDao.updateStatus(conn, booking.id(), BookingStatus.CANCELLED);
                flightDao.updateAvailableSeats(conn, flight.id(), flight.availableSeats() + booking.seats());
                conn.commit();
                return bookingDao.findByPnr(conn, booking.pnr()).orElseThrow();
            } catch (SQLException | BookingException | RuntimeException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    /** Generates a unique 6-character PNR such as "K7MX2Q". */
    private String newPnr(Connection conn) throws SQLException {
        String pnr;
        do {
            StringBuilder sb = new StringBuilder(6);
            for (int i = 0; i < 6; i++) {
                sb.append(PNR_CHARS.charAt(RANDOM.nextInt(PNR_CHARS.length())));
            }
            pnr = sb.toString();
        } while (bookingDao.pnrExists(conn, pnr));
        return pnr;
    }
}
