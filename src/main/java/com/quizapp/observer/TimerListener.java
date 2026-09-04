package com.quizapp.observer;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Observer that monitors quiz timer state during an attempt.
 * <p>
 * Subscribes to {@link QuizEvent#TIME_EXPIRED} and {@link QuizEvent#ATTEMPT_STARTED}
 * events.  When {@link QuizEvent#TIME_EXPIRED} fires the internal flag is set to
 * {@code true}, which callers can check via {@link #isExpired()}.
 * </p>
 *
 * <p>Elapsed time is tracked in milliseconds.  The {@link QuizEvent#SCORE_UPDATED}
 * and {@link QuizEvent#ANSWER_SUBMITTED} events can carry the current elapsed-time
 * as a {@link Number} payload; the tracker records the maximum value seen so that
 * callers can always obtain a meaningful elapsed time even if they subscribe late.</p>
 *
 * <p>All state is managed via atomic types for safe concurrent access.</p>
 *
 * <p>Usage:</p>
 * <pre>{@code
 * TimerListener timer = new TimerListener();
 * publisher.subscribe(QuizEvent.ATTEMPT_STARTED, timer);
 * publisher.subscribe(QuizEvent.TIME_EXPIRED,    timer);
 *
 * // In the quiz UI loop:
 * if (timer.isExpired()) { /* submit and close attempt *\/ }
 * }</pre>
 */
public class TimerListener implements QuizEventListener {

    /**
     * Whether the timer has expired for the current attempt.
     * Set to {@code true} on {@link QuizEvent#TIME_EXPIRED}; reset on
     * {@link QuizEvent#ATTEMPT_STARTED}.
     */
    private final AtomicBoolean expired = new AtomicBoolean(false);

    /**
     * Elapsed time in milliseconds, updated whenever a numeric payload is
     * received from time-related events.
     */
    private final AtomicLong elapsedMillis = new AtomicLong(0L);

    /** Timestamp (millis since epoch) at which the current attempt started. */
    private final AtomicLong startTimestampMillis = new AtomicLong(0L);

    // -------------------------------------------------------------------------
    // QuizEventListener
    // -------------------------------------------------------------------------

    /**
     * Handles quiz session events relevant to timing.
     *
     * <ul>
     *   <li>{@link QuizEvent#ATTEMPT_STARTED} — resets the expired flag, elapsed time,
     *       and records the current system time as the start timestamp</li>
     *   <li>{@link QuizEvent#TIME_EXPIRED} — sets the expired flag to {@code true};
     *       if {@code data} is a {@link Number} it is stored as the final elapsed millis</li>
     * </ul>
     *
     * @param event the fired event
     * @param data  optional payload (used as elapsed millis when it is a {@link Number})
     */
    @Override
    public void onEvent(QuizEvent event, Object data) {
        switch (event) {
            case ATTEMPT_STARTED -> {
                expired.set(false);
                elapsedMillis.set(0L);
                startTimestampMillis.set(System.currentTimeMillis());
            }
            case TIME_EXPIRED -> {
                expired.set(true);
                if (data instanceof Number number) {
                    elapsedMillis.set(number.longValue());
                } else {
                    // Compute elapsed from start timestamp if no payload provided
                    long start = startTimestampMillis.get();
                    if (start > 0) {
                        elapsedMillis.set(System.currentTimeMillis() - start);
                    }
                }
            }
            default -> {
                // Update elapsed time from numeric payloads on other events
                if (data instanceof Number number) {
                    elapsedMillis.set(number.longValue());
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    /**
     * Returns {@code true} if the timer has expired for the current attempt.
     *
     * @return whether the time limit has been reached
     */
    public boolean isExpired() {
        return expired.get();
    }

    /**
     * Returns the elapsed time in milliseconds recorded for the current attempt.
     * <p>
     * If no payload was provided with the events, this value is computed from
     * the attempt start timestamp on expiry.
     * </p>
     *
     * @return elapsed time in milliseconds
     */
    public long getElapsedMillis() {
        return elapsedMillis.get();
    }

    /**
     * Returns the elapsed time in <em>seconds</em> (truncated).
     *
     * @return elapsed time in seconds
     */
    public long getElapsedSeconds() {
        return elapsedMillis.get() / 1000L;
    }

    /**
     * Resets this listener to its initial state (non-expired, zero elapsed time).
     * <p>
     * This can be called manually; it is also invoked automatically when an
     * {@link QuizEvent#ATTEMPT_STARTED} event is received.
     * </p>
     */
    public void reset() {
        expired.set(false);
        elapsedMillis.set(0L);
        startTimestampMillis.set(0L);
    }
}
