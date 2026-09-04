package com.quizapp.observer;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Observer that tracks a student's progress through a quiz attempt.
 * <p>
 * Subscribes to {@link QuizEvent#ANSWER_SUBMITTED}, {@link QuizEvent#SCORE_UPDATED},
 * and {@link QuizEvent#ATTEMPT_STARTED} events published by a
 * {@link QuizEventPublisher}.
 * </p>
 *
 * <p>All fields are updated atomically so instances can safely be shared across
 * threads (e.g. a UI thread reading while a timer thread fires events).</p>
 *
 * <p>Usage:</p>
 * <pre>{@code
 * ProgressTracker tracker = new ProgressTracker(totalQuestions);
 * publisher.subscribe(QuizEvent.ATTEMPT_STARTED,  tracker);
 * publisher.subscribe(QuizEvent.ANSWER_SUBMITTED, tracker);
 * publisher.subscribe(QuizEvent.SCORE_UPDATED,    tracker);
 * }</pre>
 */
public class ProgressTracker implements QuizEventListener {

    /** Total number of questions in the attempt — used to compute percent complete. */
    private final int totalQuestions;

    /** Number of questions for which an answer has been submitted. */
    private final AtomicInteger answeredCount = new AtomicInteger(0);

    /** Running score; stored as a boxed Double for atomic CAS updates. */
    private final AtomicReference<Double> currentScore = new AtomicReference<>(0.0);

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * Constructs a {@code ProgressTracker} for an attempt with the given number of questions.
     *
     * @param totalQuestions total questions in this attempt (must be ≥ 1)
     * @throws IllegalArgumentException if {@code totalQuestions} is less than 1
     */
    public ProgressTracker(int totalQuestions) {
        if (totalQuestions < 1) {
            throw new IllegalArgumentException(
                    "totalQuestions must be at least 1, got: " + totalQuestions);
        }
        this.totalQuestions = totalQuestions;
    }

    // -------------------------------------------------------------------------
    // QuizEventListener
    // -------------------------------------------------------------------------

    /**
     * Handles quiz session events.
     *
     * <ul>
     *   <li>{@link QuizEvent#ATTEMPT_STARTED} — resets answered count and score to zero</li>
     *   <li>{@link QuizEvent#ANSWER_SUBMITTED} — increments {@link #answeredCount}</li>
     *   <li>{@link QuizEvent#SCORE_UPDATED} — updates {@link #currentScore} if {@code data}
     *       is a {@link Number}</li>
     * </ul>
     *
     * @param event the fired event
     * @param data  optional payload associated with the event
     */
    @Override
    public void onEvent(QuizEvent event, Object data) {
        switch (event) {
            case ATTEMPT_STARTED -> reset();
            case ANSWER_SUBMITTED -> answeredCount.incrementAndGet();
            case SCORE_UPDATED -> {
                if (data instanceof Number number) {
                    currentScore.set(number.doubleValue());
                }
            }
            default -> { /* other events not handled by this tracker */ }
        }
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    /**
     * Returns the number of questions answered so far in this attempt.
     *
     * @return answered question count (0 – totalQuestions)
     */
    public int getAnsweredCount() {
        return answeredCount.get();
    }

    /**
     * Returns the current running score for this attempt.
     *
     * @return current score
     */
    public double getCurrentScore() {
        return currentScore.get();
    }

    /**
     * Returns the completion percentage as a value between 0.0 and 100.0.
     *
     * @return percent complete
     */
    public double getPercentComplete() {
        return (answeredCount.get() / (double) totalQuestions) * 100.0;
    }

    /**
     * Returns the total number of questions in this attempt.
     *
     * @return total question count
     */
    public int getTotalQuestions() {
        return totalQuestions;
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    /**
     * Resets the tracker to its initial state.
     * Called automatically on {@link QuizEvent#ATTEMPT_STARTED}.
     */
    private void reset() {
        answeredCount.set(0);
        currentScore.set(0.0);
    }
}
