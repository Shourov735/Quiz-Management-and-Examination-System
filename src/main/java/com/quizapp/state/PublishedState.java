package com.quizapp.state;

import com.quizapp.model.Quiz;
import com.quizapp.model.QuizStatus;

/**
 * Represents the {@code PUBLISHED} lifecycle state of a {@link Quiz}.
 * <p>
 * A published quiz is visible to students but not yet open for attempts.
 * Editing is locked to protect the exam integrity.  Allowed transitions:
 * unpublish (→ DRAFT), activate (→ ACTIVE), archive (→ ARCHIVED).
 * </p>
 */
public class PublishedState implements QuizState {

    private static final String STATE_NAME = "PUBLISHED";

    // -------------------------------------------------------------------------
    // QuizState transitions
    // -------------------------------------------------------------------------

    /**
     * @throws IllegalStateException always — the quiz is already published
     */
    @Override
    public void publish(Quiz quiz) {
        throw new IllegalStateException(
                "Cannot publish a quiz that is already in PUBLISHED state.");
    }

    /**
     * Reverts the quiz to {@code DRAFT} so it can be edited again.
     *
     * @param quiz the quiz to unpublish
     */
    @Override
    public void unpublish(Quiz quiz) {
        quiz.setStatus(QuizStatus.DRAFT);
    }

    /**
     * Archives the quiz, moving it from {@code PUBLISHED} to {@code ARCHIVED}.
     *
     * @param quiz the quiz to archive
     */
    @Override
    public void archive(Quiz quiz) {
        quiz.setStatus(QuizStatus.ARCHIVED);
    }

    /**
     * Activates the quiz, opening it for student attempts.
     *
     * @param quiz the quiz to activate
     */
    @Override
    public void activate(Quiz quiz) {
        quiz.setStatus(QuizStatus.ACTIVE);
    }

    /**
     * @throws IllegalStateException always — the quiz must go through ACTIVE before COMPLETED
     */
    @Override
    public void complete(Quiz quiz) {
        throw new IllegalStateException(
                "Cannot complete a PUBLISHED quiz. Activate it first to open it for attempts.");
    }

    // -------------------------------------------------------------------------
    // State properties
    // -------------------------------------------------------------------------

    @Override
    public String getStateName() { return STATE_NAME; }

    /**
     * @return {@code true} — published quizzes are visible and can be attempted
     *         (if the system enforces a start date, that logic sits above this layer)
     */
    @Override
    public boolean canBeAttempted() { return true; }

    /** @return {@code false} — quiz metadata is locked once published */
    @Override
    public boolean canBeEdited() { return false; }

    /** @return {@code false} — no new questions can be added once published */
    @Override
    public boolean canAddQuestions() { return false; }

    @Override
    public String toString() { return STATE_NAME; }
}
