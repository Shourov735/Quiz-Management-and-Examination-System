package com.quizapp.state;

import com.quizapp.model.Quiz;

/**
 * Represents the {@code ARCHIVED} lifecycle state of a {@link Quiz}.
 * <p>
 * An archived quiz is read-only and considered permanently closed.
 * No lifecycle transitions are permitted from this state.
 * </p>
 */
public class ArchivedState implements QuizState {

    private static final String STATE_NAME = "ARCHIVED";

    // -------------------------------------------------------------------------
    // QuizState transitions — all disallowed
    // -------------------------------------------------------------------------

    /**
     * @throws IllegalStateException always — archived quizzes cannot be re-published
     */
    @Override
    public void publish(Quiz quiz) {
        throw new IllegalStateException(
                "Cannot publish an ARCHIVED quiz. Archived quizzes are permanently closed.");
    }

    /**
     * @throws IllegalStateException always — archived quizzes cannot be reverted to draft
     */
    @Override
    public void unpublish(Quiz quiz) {
        throw new IllegalStateException(
                "Cannot unpublish an ARCHIVED quiz. Archived quizzes are permanently closed.");
    }

    /**
     * @throws IllegalStateException always — already archived
     */
    @Override
    public void archive(Quiz quiz) {
        throw new IllegalStateException(
                "Cannot archive a quiz that is already in ARCHIVED state.");
    }

    /**
     * @throws IllegalStateException always — archived quizzes cannot be re-activated
     */
    @Override
    public void activate(Quiz quiz) {
        throw new IllegalStateException(
                "Cannot activate an ARCHIVED quiz. Archived quizzes are permanently closed.");
    }

    /**
     * @throws IllegalStateException always — archived quizzes cannot be completed again
     */
    @Override
    public void complete(Quiz quiz) {
        throw new IllegalStateException(
                "Cannot complete an ARCHIVED quiz. Archived quizzes are permanently closed.");
    }

    // -------------------------------------------------------------------------
    // State properties
    // -------------------------------------------------------------------------

    @Override
    public String getStateName() { return STATE_NAME; }

    /** @return {@code false} — archived quizzes cannot be attempted */
    @Override
    public boolean canBeAttempted() { return false; }

    /** @return {@code false} — archived quizzes are read-only */
    @Override
    public boolean canBeEdited() { return false; }

    /** @return {@code false} — no questions can be added to an archived quiz */
    @Override
    public boolean canAddQuestions() { return false; }

    @Override
    public String toString() { return STATE_NAME; }
}
