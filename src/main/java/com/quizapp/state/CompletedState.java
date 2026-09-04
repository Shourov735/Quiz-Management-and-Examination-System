package com.quizapp.state;

import com.quizapp.model.Quiz;
import com.quizapp.model.QuizStatus;

/**
 * Represents the {@code COMPLETED} lifecycle state of a {@link Quiz}.
 * <p>
 * The quiz has ended and results are available.  The only permitted transition
 * is to archive the quiz.  No attempts may be started, and no edits are allowed.
 * </p>
 */
public class CompletedState implements QuizState {

    private static final String STATE_NAME = "COMPLETED";

    // -------------------------------------------------------------------------
    // QuizState transitions
    // -------------------------------------------------------------------------

    /**
     * @throws IllegalStateException always — cannot publish a completed quiz
     */
    @Override
    public void publish(Quiz quiz) {
        throw new IllegalStateException(
                "Cannot publish a COMPLETED quiz. Archive it or leave it as-is.");
    }

    /**
     * @throws IllegalStateException always — cannot revert a completed quiz to draft
     */
    @Override
    public void unpublish(Quiz quiz) {
        throw new IllegalStateException(
                "Cannot unpublish a COMPLETED quiz.");
    }

    /**
     * Archives the quiz, moving it from {@code COMPLETED} to {@code ARCHIVED}.
     *
     * @param quiz the quiz to archive
     */
    @Override
    public void archive(Quiz quiz) {
        quiz.setStatus(QuizStatus.ARCHIVED);
    }

    /**
     * @throws IllegalStateException always — cannot re-activate a completed quiz
     */
    @Override
    public void activate(Quiz quiz) {
        throw new IllegalStateException(
                "Cannot activate a COMPLETED quiz.");
    }

    /**
     * @throws IllegalStateException always — the quiz is already completed
     */
    @Override
    public void complete(Quiz quiz) {
        throw new IllegalStateException(
                "Cannot complete a quiz that is already in COMPLETED state.");
    }

    // -------------------------------------------------------------------------
    // State properties
    // -------------------------------------------------------------------------

    @Override
    public String getStateName() { return STATE_NAME; }

    /** @return {@code false} — no new attempts may be started on a completed quiz */
    @Override
    public boolean canBeAttempted() { return false; }

    /** @return {@code false} — editing is not allowed */
    @Override
    public boolean canBeEdited() { return false; }

    /** @return {@code false} — questions cannot be added */
    @Override
    public boolean canAddQuestions() { return false; }

    @Override
    public String toString() { return STATE_NAME; }
}
