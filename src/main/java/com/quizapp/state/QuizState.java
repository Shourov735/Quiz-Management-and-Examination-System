package com.quizapp.state;

import com.quizapp.model.Quiz;

/**
 * State interface for the Quiz lifecycle State pattern.
 * <p>
 * Each concrete state ({@link DraftState}, {@link PublishedState},
 * {@link ActiveState}, {@link CompletedState}, {@link ArchivedState}) implements
 * this interface and decides which transitions are legal from that state.
 * Illegal transitions throw {@link IllegalStateException} with a descriptive
 * message.
 * </p>
 *
 * <p>Lifecycle diagram:</p>
 * <pre>
 *   DRAFT ──publish──▶ PUBLISHED ──activate──▶ ACTIVE ──complete──▶ COMPLETED
 *     │                    │                                             │
 *     └──archive──▶ ARCHIVED ◀──archive──────────────────────────archive─┘
 *                          ▲
 *                  (also from PUBLISHED via unpublish back to DRAFT)
 * </pre>
 */
public interface QuizState {

    /**
     * Transitions the quiz to the {@code PUBLISHED} state.
     *
     * @param quiz the quiz to transition
     * @throws IllegalStateException if this transition is not allowed from the current state
     */
    void publish(Quiz quiz) throws IllegalStateException;

    /**
     * Transitions the quiz back to the {@code DRAFT} state.
     *
     * @param quiz the quiz to transition
     * @throws IllegalStateException if this transition is not allowed from the current state
     */
    void unpublish(Quiz quiz) throws IllegalStateException;

    /**
     * Transitions the quiz to the {@code ARCHIVED} state.
     *
     * @param quiz the quiz to transition
     * @throws IllegalStateException if this transition is not allowed from the current state
     */
    void archive(Quiz quiz) throws IllegalStateException;

    /**
     * Transitions the quiz to the {@code ACTIVE} state (open for attempts).
     *
     * @param quiz the quiz to transition
     * @throws IllegalStateException if this transition is not allowed from the current state
     */
    void activate(Quiz quiz) throws IllegalStateException;

    /**
     * Transitions the quiz to the {@code COMPLETED} state (attempts closed).
     *
     * @param quiz the quiz to transition
     * @throws IllegalStateException if this transition is not allowed from the current state
     */
    void complete(Quiz quiz) throws IllegalStateException;

    /**
     * Returns the display name of this state.
     *
     * @return state name string
     */
    String getStateName();

    /**
     * Indicates whether students may submit attempts while the quiz is in this state.
     *
     * @return {@code true} if the quiz can be attempted
     */
    boolean canBeAttempted();

    /**
     * Indicates whether the quiz metadata (title, description, etc.) can be edited.
     *
     * @return {@code true} if the quiz can be edited
     */
    boolean canBeEdited();

    /**
     * Indicates whether new questions can be added to the quiz.
     *
     * @return {@code true} if questions can be added
     */
    boolean canAddQuestions();
}
