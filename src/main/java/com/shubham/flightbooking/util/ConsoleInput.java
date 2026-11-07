package com.shubham.flightbooking.util;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/** Reads and validates user input from the console, asking again until the input is valid. */
public class ConsoleInput {

    private final Scanner scanner = new Scanner(System.in);

    public String readLine(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();
            if (!value.isEmpty()) {
                return value;
            }
            System.out.println("  This field cannot be empty.");
        }
    }

    public int readInt(String prompt, int min, int max) {
        while (true) {
            String value = readLine(prompt);
            try {
                int number = Integer.parseInt(value);
                if (number >= min && number <= max) {
                    return number;
                }
            } catch (NumberFormatException ignored) {
                // fall through to the message below
            }
            System.out.printf("  Please enter a number from %d to %d.%n", min, max);
        }
    }

    /** Returns null when the user presses Enter without typing a date. */
    public LocalDate readOptionalDate(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();
            if (value.isEmpty()) {
                return null;
            }
            try {
                return LocalDate.parse(value);
            } catch (DateTimeParseException e) {
                System.out.println("  Please use the format YYYY-MM-DD, for example 2026-01-15.");
            }
        }
    }

    public boolean confirm(String prompt) {
        String value = readLine(prompt + " (y/n): ");
        return value.equalsIgnoreCase("y") || value.equalsIgnoreCase("yes");
    }
}
