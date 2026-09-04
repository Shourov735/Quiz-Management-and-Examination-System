package com.quizapp.database;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Singleton that manages a single shared SQLite {@link Connection} for the
 * Quiz Management System.
 *
 * <p>Using a singleton is appropriate here because:
 * <ul>
 *   <li>SQLite allows only one write-connection at a time per file.</li>
 *   <li>All application components share the same in-process database file.</li>
 *   <li>Connection creation is expensive; reusing one instance improves performance.</li>
 * </ul>
 *
 * <p>On first access the singleton runs {@code schema.sql} (DDL) followed by
 * {@code seed.sql} (initial data) only when the {@code users} table does not
 * yet exist, preventing double-seeding across restarts.
 *
 * <p><b>Thread safety:</b> {@link #getInstance()} uses double-checked locking
 * with a {@code volatile} field, which is safe under Java 5+ memory model.
 * Individual database operations must still be serialised by callers because
 * SQLite itself is not thread-safe in WAL-less mode.
 */
public final class DatabaseConnection {

    private static final Logger LOGGER = Logger.getLogger(DatabaseConnection.class.getName());

    /** Relative path to the SQLite database file. */
    private static final String DB_URL = "jdbc:sqlite:quiz_management.db";

    /** Classpath location of the DDL script. */
    private static final String SCHEMA_RESOURCE = "/database/schema.sql";

    /** Classpath location of the seed-data script. */
    private static final String SEED_RESOURCE = "/database/seed.sql";

    /** The single shared connection. */
    private Connection connection;

    // -------------------------------------------------------
    // Singleton wiring
    // -------------------------------------------------------

    /** Holder for the lazily-created singleton instance. */
    private static volatile DatabaseConnection instance;

    /**
     * Private constructor – obtains the JDBC connection and bootstraps the
     * database when necessary.
     *
     * @throws RuntimeException if the JDBC driver cannot be loaded or the
     *                          connection cannot be established.
     */
    private DatabaseConnection() {
        try {
            // Ensure the SQLite JDBC driver is registered
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(DB_URL);
            enableForeignKeys();
            initializeDatabase();
            LOGGER.info("DatabaseConnection initialised successfully.");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("SQLite JDBC driver not found on the classpath.", e);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to establish a database connection: " + e.getMessage(), e);
        }
    }

    /**
     * Returns the singleton {@link DatabaseConnection} instance, creating it
     * on the first call (double-checked locking).
     *
     * @return the singleton instance
     */
    public static DatabaseConnection getInstance() {
        if (instance == null) {
            synchronized (DatabaseConnection.class) {
                if (instance == null) {
                    instance = new DatabaseConnection();
                }
            }
        }
        return instance;
    }

    // -------------------------------------------------------
    // Public API
    // -------------------------------------------------------

    /**
     * Returns the underlying {@link Connection}.
     *
     * <p>If the connection has been closed (e.g. after a call to
     * {@link #closeConnection()}), a new connection is transparently reopened.
     *
     * @return a valid, open JDBC {@link Connection}
     * @throws SQLException if the connection cannot be re-established
     */
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            LOGGER.warning("Connection was closed – reopening.");
            connection = DriverManager.getConnection(DB_URL);
            enableForeignKeys();
        }
        return connection;
    }

    /**
     * Closes the underlying JDBC connection and clears the singleton so that
     * the next call to {@link #getInstance()} creates a fresh one.
     */
    public void closeConnection() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                    LOGGER.info("Database connection closed.");
                }
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Error while closing connection.", e);
            } finally {
                connection = null;
                instance = null;
            }
        }
    }

    // -------------------------------------------------------
    // Initialisation helpers
    // -------------------------------------------------------

    /**
     * Enables SQLite foreign-key constraint enforcement for this connection.
     *
     * @throws SQLException if the PRAGMA cannot be executed
     */
    private void enableForeignKeys() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON");
        }
    }

    /**
     * Runs {@code schema.sql} unconditionally, then runs {@code seed.sql} only
     * when the {@code users} table is empty (first-time setup).
     *
     * @throws SQLException if a SQL error occurs during initialisation
     */
    public void initializeDatabase() throws SQLException {
        runScript(SCHEMA_RESOURCE);
        LOGGER.info("Schema applied.");

        if (isDatabaseEmpty()) {
            runScript(SEED_RESOURCE);
            LOGGER.info("Seed data inserted.");
        } else {
            LOGGER.info("Database already contains data – skipping seed.");
        }
    }

    /**
     * Returns {@code true} when the {@code users} table contains no rows,
     * indicating that the database has not yet been seeded.
     *
     * @return {@code true} if the users table is empty
     * @throws SQLException if the query fails
     */
    private boolean isDatabaseEmpty() throws SQLException {
        String sql = "SELECT COUNT(*) FROM users";
        try (Statement stmt = connection.createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {
            return rs.next() && rs.getInt(1) == 0;
        } catch (SQLException e) {
            // Table may not exist yet – treat as empty
            return true;
        }
    }

    /**
     * Loads a SQL script from the classpath and executes each statement.
     *
     * <p>Statements are split on {@code ";"} after stripping single-line
     * ({@code --}) comments so that multi-statement scripts work correctly
     * with JDBC's {@link Statement#execute(String)}.
     *
     * @param resourcePath classpath path to the {@code .sql} file
     * @throws SQLException if any statement fails to execute
     */
    private void runScript(String resourcePath) throws SQLException {
        String sql = loadResource(resourcePath);

        // Remove single-line comments and split on semicolons
        String[] statements = sql
                .replaceAll("--[^\n]*", "")  // strip -- comments
                .split(";");

        try (Statement stmt = connection.createStatement()) {
            for (String statement : statements) {
                String trimmed = statement.strip();
                if (!trimmed.isEmpty()) {
                    stmt.execute(trimmed);
                }
            }
        }
    }

    /**
     * Reads the contents of a classpath resource into a {@link String}.
     *
     * @param resourcePath the classpath path (must start with {@code /})
     * @return the full content of the resource file
     * @throws RuntimeException if the resource cannot be found or read
     */
    private String loadResource(String resourcePath) {
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new RuntimeException("Classpath resource not found: " + resourcePath);
            }
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(is, StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read resource: " + resourcePath, e);
        }
    }
}
