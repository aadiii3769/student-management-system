package com.sims.util;

import io.github.cdimascio.dotenv.Dotenv;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton database connection manager.
 *
 * <h2>Design Pattern – Creational: Singleton</h2>
 * <p><b>Academic Justification</b>: Opening a new JDBC database connection for every
 * operation creates significant overhead (TCP handshakes, process allocation,
 * Oracle session lifecycle). The Singleton pattern guarantees that a single
 * active {@link Connection} instance is shared across the entire desktop application
 * lifecycle, providing a centralized, thread-safe access point.</p>
 */
public final class DBConnection {

    private static volatile DBConnection instance;
    private Connection connection;

    private String connectedUrl;

    private DBConnection() throws SQLException {
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();

        String configuredUrl = dotenv.get("DB_URL", "jdbc:oracle:thin:@localhost:1522/FREEPDB1");
        String user = dotenv.get("DB_USER", "system");
        String password = dotenv.get("DB_PASSWORD", "admin");

        try {
            this.connection = DriverManager.getConnection(configuredUrl, user, password);
            this.connectedUrl = configuredUrl;
        } catch (SQLException primaryEx) {
            // Automatic fallback if port 1522 was configured but user runs on default 1521
            if (configuredUrl.contains(":1522/")) {
                String fallbackUrl = configuredUrl.replace(":1522/", ":1521/");
                try {
                    this.connection = DriverManager.getConnection(fallbackUrl, user, password);
                    this.connectedUrl = fallbackUrl;
                } catch (SQLException fallbackEx) {
                    throw primaryEx; // throw initial exception
                }
            } else {
                throw primaryEx;
            }
        }

        this.connection.setAutoCommit(false); // explicit manual transaction demarcation
    }

    /**
     * Returns the singleton {@code DBConnection} instance.
     *
     * @return the singleton instance
     * @throws RuntimeException if database connection fails
     */
    public static DBConnection getInstance() {
        if (instance == null) {
            synchronized (DBConnection.class) {
                if (instance == null) {
                    try {
                        instance = new DBConnection();
                    } catch (SQLException e) {
                        throw new RuntimeException("Failed to establish Oracle Database connection: " + e.getMessage(), e);
                    }
                }
            }
        }
        return instance;
    }

    /**
     * Returns the underlying shared JDBC {@link Connection}.
     *
     * @return active JDBC connection with autoCommit = false
     */
    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                synchronized (DBConnection.class) {
                    if (connection == null || connection.isClosed()) {
                        instance = new DBConnection();
                        return instance.connection;
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database connection check failed: " + e.getMessage(), e);
        }
        return connection;
    }

    public String getConnectedUrl() {
        return connectedUrl;
    }

    /**
     * Closes the active connection on application shutdown.
     */
    public void close() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
    }
}
