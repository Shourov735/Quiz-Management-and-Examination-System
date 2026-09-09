package com.quizapp.util;

import com.quizapp.model.User;

/**
 * Thread-local session context holding the currently authenticated user.
 *
 * <p>This is a simple holder class used to pass the authenticated user between
 * JavaFX controllers without coupling them directly. It is not a Singleton in
 * the design-pattern sense — it holds no business logic — but provides a
 * convenient application-level access point for the current user identity.</p>
 */
public final class SessionContext {

    /** The authenticated user for the current session. */
    private static User currentUser;

    /** The current quiz attempt ID being taken by a student. */
    private static int currentAttemptId;

    /** The current quiz ID selected for viewing or attempting. */
    private static int selectedQuizId;

    /** The current question ID being reviewed. */
    private static int selectedQuestionId;

    private SessionContext() {
        // Utility class — not instantiable
    }

    // -------------------------------------------------------------------------
    // Current user
    // -------------------------------------------------------------------------

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static void clearSession() {
        currentUser = null;
        currentAttemptId = 0;
        selectedQuizId = 0;
        selectedQuestionId = 0;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    // -------------------------------------------------------------------------
    // Navigation context
    // -------------------------------------------------------------------------

    public static int getCurrentAttemptId() {
        return currentAttemptId;
    }

    public static void setCurrentAttemptId(int id) {
        currentAttemptId = id;
    }

    public static int getSelectedQuizId() {
        return selectedQuizId;
    }

    public static void setSelectedQuizId(int id) {
        selectedQuizId = id;
    }

    public static int getSelectedQuestionId() {
        return selectedQuestionId;
    }

    public static void setSelectedQuestionId(int id) {
        selectedQuestionId = id;
    }
}
