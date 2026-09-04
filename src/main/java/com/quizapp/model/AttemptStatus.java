package com.quizapp.model;

/**
 * Status of a {@link QuizAttempt}.
 *
 * <ul>
 *   <li>{@link #IN_PROGRESS} – the student has started but not yet submitted.</li>
 *   <li>{@link #SUBMITTED}   – the student voluntarily submitted the attempt.</li>
 *   <li>{@link #TIMED_OUT}   – the time limit expired before submission.</li>
 * </ul>
 */
public enum AttemptStatus {

    /** Attempt is currently in progress. */
    IN_PROGRESS,

    /** Attempt was submitted by the student. */
    SUBMITTED,

    /** Attempt ended because the time limit was exceeded. */
    TIMED_OUT;

    /** @return true if the attempt has concluded (SUBMITTED or TIMED_OUT) */
    public boolean isTerminal() {
        return this == SUBMITTED || this == TIMED_OUT;
    }
}
