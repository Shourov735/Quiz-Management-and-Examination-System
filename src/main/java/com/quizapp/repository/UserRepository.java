package com.quizapp.repository;

import com.quizapp.model.User;
import com.quizapp.model.UserRole;

import java.util.List;
import java.util.Optional;

/**
 * Repository abstraction for User persistence operations.
 * Follows the Repository pattern to decouple persistence from business logic.
 */
public interface UserRepository {

    /** Persist a new user and return the generated id. */
    int save(User user);

    /** Find user by primary key. */
    Optional<User> findById(int id);

    /** Find user by email (unique). */
    Optional<User> findByEmail(String email);

    /** Find user by email and password (for authentication). */
    Optional<User> findByEmailAndPassword(String email, String password);

    /** Return all users with a given role. */
    List<User> findByRole(UserRole role);

    /** Return all users. */
    List<User> findAll();

    /** Update an existing user. */
    boolean update(User user);

    /** Delete a user by id. */
    boolean delete(int id);

    /** Check if an email already exists. */
    boolean emailExists(String email);
}
