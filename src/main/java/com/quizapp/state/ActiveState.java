package com.quizapp.state;

import com.quizapp.model.Quiz;
import com.quizapp.model.QuizStatus;

/**
 * Represents the {@code ACTIVE} lifecycle state of a {@link Quiz}.
 * <p>
 * The quiz is currently open for student attempts.  Once active, no
 * modifications are permitted except closing the quiz ({@code complete}).
 * The only allowed transition out of this state is complete (→ COMPLETED).
 * </p>
 */
public class ActiveState implements QuizState {

    private static final String STATE_NAME = "ACTIVE";

    // -------------------------------------------------------------------------
    // QuizState transitions
    // -------------------------------------------------------------------------

    /**
     * @throws IllegalStateException always — cannot publish an already active quiz
     */
    @Override
    public void publish(Quiz quiz) {
        throw new IllegalStateException(
                "Cannot publish an ACTIVE quiz. The quiz is currently open for attempts.");
    }

    /**
     * @throws IllegalStateException always — cannot move directly from ACTIVE to DRAFT
     */
    @Override
    public void unpublish(Quiz quiz) {
        throw new IllegalStateException(
                "Cannot unpublish an ACTIVE quiz. Complete the quiz first.");
    }

    /**
     * @throws IllegalStateException always — an active quiz must be completed before archiving
     */
    @Override
    public void archive(Quiz quiz) {
        throw new IllegalStateException(
                "Cannot archive an ACTIVE quiz. Complete the quiz first, then archive it.");
    }

    /**
     * @throws IllegalStateException always — the quiz is already active
     */
    @Override
    public void activate(Quiz quiz) {
        throw new IllegalStateException(
                "Cannot activate a quiz that is already in ACTIVE state.");
    }

    /**
     * Closes the quiz for new attempts, transitioning it to {@code COMPLETED}.
     *
     * @param quiz the quiz to complete
     */
    @Override
    public void complete(Quiz quiz) {
        quiz.setStatus(QuizStatus.COMPLETED);
    }

    // -------------------------------------------------------------------------
    // State properties
    // -------------------------------------------------------------------------

    @Override
    public String getStateName() { return STATE_NAME; }

    /** @return {@code true} — students may submit attempts while the quiz is active */
    @Override
    public boolean canBeAttempted() { return true; }

    /** @return {@code false} — quiz is locked while active */
    @Override
    public boolean canBeEdited() { return false; }

    /** @return {@code false} — no new questions can be added while active */
    @Override
    public boolean canAddQuestions() { return false; }

    @Override
    public String toString() { return STATE_NAME; }
}
