package com.shubham.flightbooking.db;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Creates JDBC connections to MySQL.
 *
 * Settings are read from src/main/resources/db.properties. Environment variables
 * DB_URL, DB_USER and DB_PASSWORD override the file, so a password never has to be committed.
 */
public final class DatabaseConnection {

    private static final Properties PROPS = loadProperties();

    private DatabaseConnection() {
    }

    private static Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream in = DatabaseConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not read db.properties", e);
        }
        return props;
    }

    private static String setting(String envName, String propName) {
        String value = System.getenv(envName);
        if (value == null || value.isBlank()) {
            value = PROPS.getProperty(propName);
        }
        if (value == null) {
            throw new IllegalStateException("Missing database setting '" + propName
                    + "'. Copy db.properties.example to db.properties or set " + envName + ".");
        }
        return value;
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                setting("DB_URL", "db.url"),
                setting("DB_USER", "db.user"),
                setting("DB_PASSWORD", "db.password"));
    }
}
