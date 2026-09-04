package com.quizapp.state;

import com.quizapp.model.Quiz;
import com.quizapp.model.QuizStatus;

/**
 * Context class that manages a {@link Quiz} and its current {@link QuizState}.
 * <p>
 * {@code QuizStateManager} encapsulates the state-transition logic and provides
 * a simple, type-safe API for the rest of the application.  The correct
 * {@link QuizState} implementation is resolved from the quiz's
 * {@link QuizStatus} at construction time and updated automatically whenever
 * a successful transition occurs.
 * </p>
 *
 * <p>Usage example:</p>
 * <pre>{@code
 * Quiz quiz = quizRepository.findById(id);
 * QuizStateManager manager = new QuizStateManager(quiz);
 * manager.publish();   // DRAFT → PUBLISHED
 * manager.activate();  // PUBLISHED → ACTIVE
 * manager.complete();  // ACTIVE → COMPLETED
 * }</pre>
 */
public class QuizStateManager {

    /** The quiz whose lifecycle is managed. */
    private final Quiz quiz;

    /** Current state; updated after each successful transition. */
    private QuizState currentState;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * Constructs a {@code QuizStateManager} for the given quiz.
     * <p>
     * The initial {@link QuizState} is derived from {@link Quiz#getStatus()}.
     * </p>
     *
     * @param quiz the quiz to manage; must not be {@code null}
     * @throws IllegalArgumentException if {@code quiz} is {@code null}
     */
    public QuizStateManager(Quiz quiz) {
        if (quiz == null) {
            throw new IllegalArgumentException("Quiz must not be null");
        }
        this.quiz = quiz;
        this.currentState = resolveState(quiz.getStatus());
    }

    // -------------------------------------------------------------------------
    // Transition methods
    // -------------------------------------------------------------------------

    /**
     * Attempts to publish the quiz.
     *
     * @throws IllegalStateException if the current state does not permit publishing
     */
    public void publish() {
        currentState.publish(quiz);
        currentState = resolveState(quiz.getStatus());
    }

    /**
     * Attempts to unpublish the quiz (revert to DRAFT).
     *
     * @throws IllegalStateException if the current state does not permit unpublishing
     */
    public void unpublish() {
        currentState.unpublish(quiz);
        currentState = resolveState(quiz.getStatus());
    }

    /**
     * Attempts to archive the quiz.
     *
     * @throws IllegalStateException if the current state does not permit archiving
     */
    public void archive() {
        currentState.archive(quiz);
        currentState = resolveState(quiz.getStatus());
    }

    /**
     * Attempts to activate the quiz (open for attempts).
     *
     * @throws IllegalStateException if the current state does not permit activation
     */
    public void activate() {
        currentState.activate(quiz);
        currentState = resolveState(quiz.getStatus());
    }

    /**
     * Attempts to complete the quiz (close for new attempts).
     *
     * @throws IllegalStateException if the current state does not permit completion
     */
    public void complete() {
        currentState.complete(quiz);
        currentState = resolveState(quiz.getStatus());
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    /**
     * Returns the current {@link QuizState}.
     *
     * @return current state; never {@code null}
     */
    public QuizState getState() {
        return currentState;
    }

    /**
     * Returns the managed {@link Quiz}.
     *
     * @return the quiz; never {@code null}
     */
    public Quiz getQuiz() {
        return quiz;
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Maps a {@link QuizStatus} enum value to its corresponding {@link QuizState} instance.
     *
     * @param status the current quiz status
     * @return appropriate state object
     */
    private QuizState resolveState(QuizStatus status) {
        if (status == null) {
            return new DraftState();
        }
        return switch (status) {
            case DRAFT     -> new DraftState();
            case PUBLISHED -> new PublishedState();
            case ACTIVE    -> new ActiveState();
            case COMPLETED -> new CompletedState();
            case ARCHIVED  -> new ArchivedState();
        };
    }

    /** Convenience static method to publish a quiz. */
    public static void publish(Quiz quiz) {
        new QuizStateManager(quiz).publish();
    }

    /** Convenience static method to unpublish a quiz. */
    public static void unpublish(Quiz quiz) {
        new QuizStateManager(quiz).unpublish();
    }

    /** Convenience static method to archive a quiz. */
    public static void archive(Quiz quiz) {
        new QuizStateManager(quiz).archive();
    }
}
