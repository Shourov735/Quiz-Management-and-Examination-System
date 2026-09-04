package com.quizapp.state;

import com.quizapp.model.Quiz;
import com.quizapp.model.QuizStatus;

/**
 * Represents the {@code DRAFT} lifecycle state of a {@link Quiz}.
 * <p>
 * In this state the quiz is being authored.  It is not visible to students and
 * can be freely edited.  Allowed transitions: publish, archive.
 * </p>
 */
public class DraftState implements QuizState {

    private static final String STATE_NAME = "DRAFT";

    // -------------------------------------------------------------------------
    // QuizState transitions
    // -------------------------------------------------------------------------

    /**
     * Publishes the quiz, moving it from {@code DRAFT} to {@code PUBLISHED}.
     *
     * @param quiz the quiz to publish
     */
    @Override
    public void publish(Quiz quiz) {
        quiz.setStatus(QuizStatus.PUBLISHED);
    }

    /**
     * @throws IllegalStateException always — the quiz is already in draft
     */
    @Override
    public void unpublish(Quiz quiz) {
        throw new IllegalStateException(
                "Cannot unpublish a quiz that is already in DRAFT state.");
    }

    /**
     * Archives the quiz, moving it from {@code DRAFT} to {@code ARCHIVED}.
     *
     * @param quiz the quiz to archive
     */
    @Override
    public void archive(Quiz quiz) {
        quiz.setStatus(QuizStatus.ARCHIVED);
    }

    /**
     * @throws IllegalStateException always — a quiz must be PUBLISHED before it can be ACTIVE
     */
    @Override
    public void activate(Quiz quiz) {
        throw new IllegalStateException(
                "Cannot activate a quiz in DRAFT state. Publish the quiz first.");
    }

    /**
     * @throws IllegalStateException always — cannot complete a quiz that has never been active
     */
    @Override
    public void complete(Quiz quiz) {
        throw new IllegalStateException(
                "Cannot complete a quiz in DRAFT state. The quiz must be ACTIVE first.");
    }

    // -------------------------------------------------------------------------
    // State properties
    // -------------------------------------------------------------------------

    @Override
    public String getStateName() { return STATE_NAME; }

    /** @return {@code false} — students cannot attempt a draft quiz */
    @Override
    public boolean canBeAttempted() { return false; }

    /** @return {@code true} — quiz metadata may be edited in draft */
    @Override
    public boolean canBeEdited() { return true; }

    /** @return {@code true} — questions may be added in draft */
    @Override
    public boolean canAddQuestions() { return true; }

    @Override
    public String toString() { return STATE_NAME; }
}
