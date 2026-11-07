package com.shubham.flightbooking;

import com.shubham.flightbooking.model.Booking;
import com.shubham.flightbooking.model.Flight;
import com.shubham.flightbooking.model.Passenger;
import com.shubham.flightbooking.service.BookingException;
import com.shubham.flightbooking.service.BookingService;
import com.shubham.flightbooking.util.ConsoleInput;

import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/** Console menu for the Flight Booking System. */
public class Main {

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");
    private static final NumberFormat RUPEES = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-IN"));

    private final BookingService service = new BookingService();
    private final ConsoleInput input = new ConsoleInput();

    public static void main(String[] args) {
        new Main().run();
    }

    private void run() {
        System.out.println("==============================================");
        System.out.println("          FLIGHT BOOKING SYSTEM");
        System.out.println("==============================================");
        while (true) {
            printMenu();
            int choice = input.readInt("Choose an option: ", 0, 7);
            System.out.println();
            try {
                switch (choice) {
                    case 1 -> showFlights(service.listUpcomingFlights());
                    case 2 -> searchFlights();
                    case 3 -> registerPassenger();
                    case 4 -> showPassenger();
                    case 5 -> bookTicket();
                    case 6 -> showBookings();
                    case 7 -> cancelBooking();
                    case 0 -> {
                        System.out.println("Thank you for using the Flight Booking System. Goodbye!");
                        return;
                    }
                    default -> System.out.println("Invalid option.");
                }
            } catch (BookingException e) {
                System.out.println("  [X] " + e.getMessage());
            } catch (SQLException e) {
                System.out.println("  [X] Database error: " + e.getMessage());
                System.out.println("    Check that MySQL is running and the settings in db.properties are correct.");
            } catch (IllegalStateException e) {
                System.out.println("  [X] " + e.getMessage());
            }
            System.out.println();
        }
    }

    private void printMenu() {
        System.out.println("----------------------------------------------");
        System.out.println(" 1. View all upcoming flights");
        System.out.println(" 2. Search flights");
        System.out.println(" 3. Register as a passenger");
        System.out.println(" 4. View passenger details");
        System.out.println(" 5. Book a ticket");
        System.out.println(" 6. View my bookings");
        System.out.println(" 7. Cancel a booking");
        System.out.println(" 0. Exit");
        System.out.println("----------------------------------------------");
    }

    private void searchFlights() throws SQLException {
        String from = input.readLine("From (city): ");
        String to = input.readLine("To (city): ");
        LocalDate date = input.readOptionalDate("Travel date YYYY-MM-DD (press Enter for any date): ");
        showFlights(service.searchFlights(from, to, date));
    }

    private void registerPassenger() throws SQLException, BookingException {
        String name = input.readLine("Full name: ");
        String email = input.readLine("Email: ");
        String phone = input.readLine("Mobile number (10 digits): ");
        int age = input.readInt("Age: ", 1, 120);
        Passenger p = service.registerPassenger(name, email, phone, age);
        System.out.println("  [OK] Registered successfully. Passenger ID: " + p.id());
    }

    private void showPassenger() throws SQLException, BookingException {
        Passenger p = service.getPassenger(input.readLine("Email: "));
        System.out.printf("  ID: %d%n  Name: %s%n  Email: %s%n  Phone: %s%n  Age: %d%n",
                p.id(), p.fullName(), p.email(), p.phone(), p.age());
    }

    private void bookTicket() throws SQLException, BookingException {
        String email = input.readLine("Your registered email: ");
        String flightNumber = input.readLine("Flight number (e.g. SW101): ");
        int seats = input.readInt("Number of seats (1-" + BookingService.MAX_SEATS_PER_BOOKING + "): ",
                1, BookingService.MAX_SEATS_PER_BOOKING);
        if (!input.confirm("Confirm booking")) {
            System.out.println("  Booking cancelled by user.");
            return;
        }
        Booking b = service.bookTicket(email, flightNumber, seats);
        System.out.println("  [OK] Booking confirmed!");
        printBooking(b);
    }

    private void showBookings() throws SQLException, BookingException {
        List<Booking> bookings = service.getBookings(input.readLine("Your registered email: "));
        if (bookings.isEmpty()) {
            System.out.println("  You have no bookings yet.");
            return;
        }
        bookings.forEach(this::printBooking);
    }

    private void cancelBooking() throws SQLException, BookingException {
        String pnr = input.readLine("PNR: ");
        if (!input.confirm("Are you sure you want to cancel " + pnr.toUpperCase())) {
            return;
        }
        Booking b = service.cancelBooking(pnr);
        System.out.println("  [OK] Booking " + b.pnr() + " cancelled. " + b.seats() + " seat(s) released.");
    }

    // ---------- Output helpers ----------

    private void showFlights(List<Flight> flights) {
        if (flights.isEmpty()) {
            System.out.println("  No flights found.");
            return;
        }
        String row = "%-7s %-10s %-10s %-10s %-19s %-7s %6s %12s%n";
        System.out.printf(row, "Flight", "Airline", "From", "To", "Departure", "Time", "Seats", "Fare");
        System.out.println("-".repeat(88));
        for (Flight f : flights) {
            System.out.printf(row, f.flightNumber(), f.airline(), f.source(), f.destination(),
                    f.departureTime().format(DATE_TIME), formatDuration(f.duration()),
                    f.availableSeats(), RUPEES.format(f.fare()));
        }
        System.out.println(flights.size() + " flight(s) found.");
    }

    private void printBooking(Booking b) {
        Flight f = b.flight();
        System.out.println("  +---------------------------------------------");
        System.out.printf("  | PNR: %s   Status: %s%n", b.pnr(), b.status());
        System.out.printf("  | %s (%s)  %s -> %s%n", f.flightNumber(), f.airline(), f.source(), f.destination());
        System.out.printf("  | Departs: %s%n", f.departureTime().format(DATE_TIME));
        System.out.printf("  | Seats: %d   Total fare: %s%n", b.seats(), RUPEES.format(b.totalFare()));
        System.out.println("  +---------------------------------------------");
    }

    private static String formatDuration(Duration d) {
        return d.toHours() + "h" + String.format("%02d", d.toMinutesPart()) + "m";
    }
}
