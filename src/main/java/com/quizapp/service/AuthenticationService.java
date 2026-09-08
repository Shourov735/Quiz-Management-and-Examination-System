package com.quizapp.service;

import com.quizapp.model.User;
import com.quizapp.model.UserRole;
import com.quizapp.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Service responsible for user authentication and registration.
 *
 * <p>Manages the lifecycle of the currently authenticated session:
 * login, logout, and registration.  All validation failures throw
 * {@link IllegalArgumentException}; business-rule violations throw
 * {@link IllegalStateException}.</p>
 *
 * <p>This class is intentionally session-scoped — a single instance
 * is shared for the lifetime of the application.</p>
 */
public class AuthenticationService {

    // -------------------------------------------------------------------------
    // Fields
    // -------------------------------------------------------------------------

    /** Underlying persistence layer for User entities. */
    private final UserRepository userRepo;

    /** The currently authenticated user; {@code null} when no session is active. */
    private User currentUser;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * Creates an {@code AuthenticationService} with the given user repository.
     *
     * @param userRepo the repository used for user lookups and persistence;
     *                 must not be {@code null}
     */
    public AuthenticationService(UserRepository userRepo) {
        if (userRepo == null) {
            throw new IllegalArgumentException("UserRepository must not be null.");
        }
        this.userRepo = userRepo;
    }

    // -------------------------------------------------------------------------
    // Authentication
    // -------------------------------------------------------------------------

    /**
     * Attempts to authenticate a user with the given credentials.
     *
     * <p>On success the returned {@link User} is stored as the current session
     * user; {@link #isLoggedIn()} will then return {@code true}.</p>
     *
     * @param email    the user's email address; must be non-null and non-blank
     * @param password the user's plain-text password; must be non-null and non-blank
     * @return an {@link Optional} containing the authenticated {@link User},
     *         or {@link Optional#empty()} if credentials do not match
     * @throws IllegalArgumentException if {@code email} or {@code password} is blank
     */
    public Optional<User> login(String email, String password) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email must not be blank.");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password must not be blank.");
        }

        Optional<User> found = userRepo.findByEmailAndPassword(email.trim(), password);
        found.ifPresent(u -> this.currentUser = u);
        return found;
    }

    /**
     * Terminates the current user session.
     * After this call {@link #isLoggedIn()} returns {@code false}.
     */
    public void logout() {
        this.currentUser = null;
    }

    // -------------------------------------------------------------------------
    // Session queries
    // -------------------------------------------------------------------------

    /**
     * Returns the currently logged-in user.
     *
     * @return an {@link Optional} containing the current {@link User},
     *         or empty if no session is active
     */
    public Optional<User> getCurrentUser() {
        return Optional.ofNullable(currentUser);
    }

    /**
     * Returns {@code true} if a user session is currently active.
     *
     * @return {@code true} when logged in
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * Returns {@code true} if the current user has the {@link UserRole#TEACHER} role.
     *
     * @return {@code true} for a teacher session
     */
    public boolean isTeacher() {
        return currentUser != null && currentUser.getRole() == UserRole.TEACHER;
    }

    /**
     * Returns {@code true} if the current user has the {@link UserRole#STUDENT} role.
     *
     * @return {@code true} for a student session
     */
    public boolean isStudent() {
        return currentUser != null && currentUser.getRole() == UserRole.STUDENT;
    }

    // -------------------------------------------------------------------------
    // Registration
    // -------------------------------------------------------------------------

    /**
     * Registers a new user account.
     *
     * <p>Validates that all fields are non-blank, that the email address is not
     * already taken, then persists the new {@link User} record.</p>
     *
     * @param name     display name; must be non-blank
     * @param email    email address (unique); must be non-blank
     * @param password plain-text password; must be non-blank
     * @param role     the user's role; must not be {@code null}
     * @return the newly created and persisted {@link User} (with its generated id set)
     * @throws IllegalArgumentException if any field is blank, or if the email is
     *                                  already registered
     */
    public User register(String name, String email, String password, UserRole role) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name must not be blank.");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email must not be blank.");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password must not be blank.");
        }
        if (role == null) {
            throw new IllegalArgumentException("User role must not be null.");
        }

        String normalizedEmail = email.trim().toLowerCase();
        if (userRepo.emailExists(normalizedEmail)) {
            throw new IllegalArgumentException(
                    "An account with the email address '" + normalizedEmail + "' already exists.");
        }

        User user = new User();
        user.setName(name.trim());
        user.setEmail(normalizedEmail);
        user.setPassword(password);
        user.setRole(role);
        user.setCreatedAt(LocalDateTime.now());

        int generatedId = userRepo.save(user);
        user.setId(generatedId);
        return user;
    }
}
