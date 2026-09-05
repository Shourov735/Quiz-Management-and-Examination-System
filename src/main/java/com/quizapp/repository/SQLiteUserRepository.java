package com.quizapp.repository;

import com.quizapp.database.DatabaseConnection;
import com.quizapp.model.User;
import com.quizapp.model.UserRole;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * SQLite-backed implementation of {@link UserRepository}.
 *
 * <p>All database access goes through
 * {@code DatabaseConnection.getInstance().getConnection()} and uses
 * {@link PreparedStatement} with try-with-resources to prevent resource leaks.
 * Every {@link SQLException} is wrapped in a {@link RuntimeException} with a
 * descriptive message so callers do not need to handle checked SQL exceptions.</p>
 *
 * <p><strong>Security note:</strong> Passwords are currently stored and compared
 * as plain text. Production code MUST replace this with a strong hashing
 * algorithm (e.g. BCrypt) before deployment.</p>
 */
public class SQLiteUserRepository implements UserRepository {

    // -------------------------------------------------------------------------
    // SQL constants
    // -------------------------------------------------------------------------

    private static final String INSERT_USER =
            "INSERT INTO users (name, email, password, role, created_at) VALUES (?, ?, ?, ?, ?)";

    private static final String SELECT_BY_ID =
            "SELECT id, name, email, password, role, created_at FROM users WHERE id = ?";

    private static final String SELECT_BY_EMAIL =
            "SELECT id, name, email, password, role, created_at FROM users WHERE email = ?";

    private static final String SELECT_BY_EMAIL_AND_PASSWORD =
            // TODO: Replace plain-text password comparison with hashed comparison (e.g., BCrypt).
            "SELECT id, name, email, password, role, created_at FROM users WHERE email = ? AND password = ?";

    private static final String SELECT_BY_ROLE =
            "SELECT id, name, email, password, role, created_at FROM users WHERE role = ?";

    private static final String SELECT_ALL =
            "SELECT id, name, email, password, role, created_at FROM users";

    private static final String UPDATE_USER =
            "UPDATE users SET name = ?, email = ?, password = ? WHERE id = ?";

    private static final String DELETE_USER =
            "DELETE FROM users WHERE id = ?";

    private static final String COUNT_EMAIL =
            "SELECT COUNT(*) FROM users WHERE email = ?";

    // -------------------------------------------------------------------------
    // UserRepository implementation
    // -------------------------------------------------------------------------

    /**
     * Inserts a new {@link User} row and returns the auto-generated primary key.
     *
     * @param user the user to persist; its {@code id} field will be ignored
     * @return the generated database id
     * @throws RuntimeException if a SQL error occurs
     */
    @Override
    public int save(User user) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_USER, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword()); // TODO: hash before storing
            ps.setString(4, user.getRole() != null ? user.getRole().name() : UserRole.STUDENT.name());
            ps.setString(5, user.getCreatedAt() != null ? user.getCreatedAt().toString() : null);

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new RuntimeException("save(User) - no rows inserted for user: " + user.getEmail());
            }

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int generatedId = keys.getInt(1);
                    user.setId(generatedId);
                    return generatedId;
                }
                throw new RuntimeException("save(User) - no generated key returned for user: " + user.getEmail());
            }
        } catch (SQLException e) {
            throw new RuntimeException("save(User) failed for email='" + user.getEmail() + "': " + e.getMessage(), e);
        }
    }

    /**
     * Finds a {@link User} by primary key.
     *
     * @param id the user id
     * @return an {@link Optional} containing the user, or empty if not found
     */
    @Override
    public Optional<User> findById(int id) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("findById(User) failed for id=" + id + ": " + e.getMessage(), e);
        }
    }

    /**
     * Finds a {@link User} by their unique email address.
     *
     * @param email the email to look up
     * @return an {@link Optional} containing the user, or empty if not found
     */
    @Override
    public Optional<User> findByEmail(String email) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_EMAIL)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("findByEmail(User) failed for email='" + email + "': " + e.getMessage(), e);
        }
    }

    /**
     * Authenticates a user by matching email and password.
     *
     * <p><strong>Security note:</strong> Currently performs plain-text comparison.
     * Must be replaced with BCrypt or equivalent before production use.</p>
     *
     * @param email    the user's email
     * @param password the user's plain-text password
     * @return an {@link Optional} containing the authenticated user, or empty if credentials are invalid
     */
    @Override
    public Optional<User> findByEmailAndPassword(String email, String password) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_EMAIL_AND_PASSWORD)) {

            ps.setString(1, email);
            ps.setString(2, password); // TODO: hash password before comparison
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException(
                    "findByEmailAndPassword(User) failed for email='" + email + "': " + e.getMessage(), e);
        }
    }

    /**
     * Returns all users with the given {@link UserRole}.
     *
     * @param role the role to filter by
     * @return list of matching users; empty list if none found
     */
    @Override
    public List<User> findByRole(UserRole role) {
        List<User> users = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_ROLE)) {

            ps.setString(1, role.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    users.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("findByRole(User) failed for role=" + role + ": " + e.getMessage(), e);
        }
        return users;
    }

    /**
     * Returns all users in the system.
     *
     * @return list of all users; empty list if none exist
     */
    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                users.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("findAll(User) failed: " + e.getMessage(), e);
        }
        return users;
    }

    /**
     * Updates the {@code name}, {@code email}, and {@code password} fields of an existing user.
     *
     * @param user the user to update; must have a valid {@code id}
     * @return {@code true} if at least one row was updated
     */
    @Override
    public boolean update(User user) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_USER)) {

            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword()); // TODO: hash before storing
            ps.setInt(4, user.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("update(User) failed for id=" + user.getId() + ": " + e.getMessage(), e);
        }
    }

    /**
     * Deletes the user row with the given id.
     *
     * @param id the user id to delete
     * @return {@code true} if a row was deleted
     */
    @Override
    public boolean delete(int id) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE_USER)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("delete(User) failed for id=" + id + ": " + e.getMessage(), e);
        }
    }

    /**
     * Checks whether any user row has the given email address.
     *
     * @param email the email to check
     * @return {@code true} if the email already exists in the database
     */
    @Override
    public boolean emailExists(String email) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(COUNT_EMAIL)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("emailExists(User) failed for email='" + email + "': " + e.getMessage(), e);
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Maps the current row of the given {@link ResultSet} to a {@link User} object.
     *
     * <p>Assumes the cursor is already positioned on a valid row (i.e. after
     * {@link ResultSet#next()} has returned {@code true}).</p>
     *
     * @param rs an open, positioned {@link ResultSet}
     * @return a fully-populated {@link User}
     * @throws SQLException if any column access fails
     */
    private User mapResultSet(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setName(rs.getString("name"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));

        String roleStr = rs.getString("role");
        if (roleStr != null && !roleStr.isBlank()) {
            try {
                user.setRole(UserRole.valueOf(roleStr));
            } catch (IllegalArgumentException ex) {
                user.setRole(UserRole.STUDENT);
            }
        }

        String createdAtStr = rs.getString("created_at");
        if (createdAtStr != null && !createdAtStr.isBlank()) {
            try {
                user.setCreatedAt(java.time.LocalDateTime.parse(createdAtStr));
            } catch (Exception ex) {
                // Leave createdAt as null if unparseable
            }
        }

        return user;
    }
}
