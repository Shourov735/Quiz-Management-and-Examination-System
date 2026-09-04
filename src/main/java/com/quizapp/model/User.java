package com.quizapp.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents an application user, who is either a {@link UserRole#TEACHER}
 * or a {@link UserRole#STUDENT}.
 *
 * <p>Equality is based solely on {@link #id} so that detached and persisted
 * instances with the same identity compare as equal.
 */
public class User {

    // -------------------------------------------------------
    // Fields
    // -------------------------------------------------------

    private int           id;
    private String        name;
    private String        email;
    private String        password;
    private UserRole      role;
    private LocalDateTime createdAt;

    // -------------------------------------------------------
    // Constructors
    // -------------------------------------------------------

    /** No-argument constructor required by mapping frameworks. */
    public User() {
    }

    /**
     * Full constructor.
     *
     * @param id        database primary key
     * @param name      display name
     * @param email     unique e-mail address
     * @param password  hashed or plain password
     * @param role      {@link UserRole#TEACHER} or {@link UserRole#STUDENT}
     * @param createdAt timestamp of account creation
     */
    public User(int id, String name, String email, String password,
                UserRole role, LocalDateTime createdAt) {
        this.id        = id;
        this.name      = name;
        this.email     = email;
        this.password  = password;
        this.role      = role;
        this.createdAt = createdAt;
    }

    // -------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------

    /**
     * Returns the primary-key identifier.
     *
     * @return user id
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the primary-key identifier.
     *
     * @param id user id
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Returns the user's display name.
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the user's display name.
     *
     * @param name display name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the user's unique e-mail address.
     *
     * @return email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the user's e-mail address.
     *
     * @param email unique e-mail address
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Returns the stored password (hashed or plain).
     *
     * @return password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password.
     *
     * @param password hashed or plain password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Returns the user's role.
     *
     * @return {@link UserRole}
     */
    public UserRole getRole() {
        return role;
    }

    /**
     * Sets the user's role.
     *
     * @param role {@link UserRole}
     */
    public void setRole(UserRole role) {
        this.role = role;
    }

    /**
     * Returns the account creation timestamp.
     *
     * @return creation timestamp
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the account creation timestamp.
     *
     * @param createdAt creation timestamp
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // -------------------------------------------------------
    // Identity
    // -------------------------------------------------------

    /**
     * Two {@code User} objects are equal when they share the same {@link #id}.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User other)) return false;
        return id == other.id;
    }

    /** Hash code consistent with {@link #equals(Object)} (based on id). */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Returns a human-readable representation suitable for logging.
     * The password is deliberately omitted.
     */
    @Override
    public String toString() {
        return "User{id=" + id
                + ", name='" + name + '\''
                + ", email='" + email + '\''
                + ", role=" + role
                + ", createdAt=" + createdAt
                + '}';
    }
}
