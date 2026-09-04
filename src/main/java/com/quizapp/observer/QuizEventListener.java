package com.quizapp.observer;

/**
 * Observer interface for the Quiz event publish-subscribe system.
 * <p>
 * Any component interested in quiz session events must implement this interface
 * and register itself with a {@link QuizEventPublisher}.
 * </p>
 *
 * @see QuizEventPublisher
 * @see QuizEvent
 */
public interface QuizEventListener {

    /**
     * Called by the {@link QuizEventPublisher} when a subscribed event occurs.
     *
     * @param event the event that was fired
     * @param data  optional payload associated with the event (may be {@code null});
     *              the concrete type depends on the event — e.g. an {@code Answer}
     *              for {@link QuizEvent#ANSWER_SUBMITTED}, a {@code Double} for
     *              {@link QuizEvent#SCORE_UPDATED}, etc.
     */
    void onEvent(QuizEvent event, Object data);
}
