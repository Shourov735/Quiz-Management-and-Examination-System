package com.quizapp.observer;

/**
 * Enumeration of events that can be published during a quiz attempt session.
 * <p>
 * These events are used by the Observer pattern implementation
 * ({@link QuizEventPublisher}) to notify registered {@link QuizEventListener}s
 * of state changes in a quiz session.
 * </p>
 */
public enum QuizEvent {

    /** Fired when a student submits an answer to a question. */
    ANSWER_SUBMITTED,

    /** Fired when the running score for an attempt is recalculated. */
    SCORE_UPDATED,

    /** Fired when the student completes all questions or submits the quiz. */
    QUIZ_COMPLETED,

    /** Fired when the allocated time for an attempt runs out. */
    TIME_EXPIRED,

    /** Fired when a student begins a new quiz attempt. */
    ATTEMPT_STARTED
}
