package com.quizapp.observer;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Thread-safe publish-subscribe event bus for quiz session events.
 * <p>
 * Listeners register interest in specific {@link QuizEvent} types.  When an
 * event is published all registered listeners for that event are notified
 * synchronously in the order they were subscribed.
 * </p>
 *
 * <p>Thread safety is achieved with a {@link ReentrantReadWriteLock}: multiple
 * threads may read (publish) concurrently while subscribe/unsubscribe hold the
 * write lock exclusively.</p>
 *
 * <p>Usage example:</p>
 * <pre>{@code
 * QuizEventPublisher publisher = new QuizEventPublisher();
 * ProgressTracker tracker = new ProgressTracker(totalQuestions);
 * publisher.subscribe(QuizEvent.ANSWER_SUBMITTED, tracker);
 * publisher.subscribe(QuizEvent.SCORE_UPDATED, tracker);
 *
 * // Later, during the attempt:
 * publisher.publish(QuizEvent.ANSWER_SUBMITTED, answer);
 * }</pre>
 */
public class QuizEventPublisher {

    /**
     * Map of event → registered listeners.
     * Initialised with an entry for every {@link QuizEvent} to avoid
     * null-checks during publish.
     */
    private final Map<QuizEvent, List<QuizEventListener>> listenerMap;

    /** Guards all access to {@link #listenerMap}. */
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /** Constructs an empty {@code QuizEventPublisher}. */
    public QuizEventPublisher() {
        listenerMap = new EnumMap<>(QuizEvent.class);
        for (QuizEvent event : QuizEvent.values()) {
            listenerMap.put(event, new ArrayList<>());
        }
    }

    // -------------------------------------------------------------------------
    // Subscribe / Unsubscribe
    // -------------------------------------------------------------------------

    /**
     * Registers a listener for a specific event.
     * <p>
     * A listener may be registered for multiple events by calling this method
     * multiple times.  Duplicate registrations for the same event are ignored.
     * </p>
     *
     * @param event    the event to subscribe to; must not be {@code null}
     * @param listener the listener to register; must not be {@code null}
     * @throws IllegalArgumentException if either argument is {@code null}
     */
    public void subscribe(QuizEvent event, QuizEventListener listener) {
        if (event == null) {
            throw new IllegalArgumentException("Event must not be null");
        }
        if (listener == null) {
            throw new IllegalArgumentException("Listener must not be null");
        }
        lock.writeLock().lock();
        try {
            List<QuizEventListener> listeners = listenerMap.get(event);
            if (!listeners.contains(listener)) {
                listeners.add(listener);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Removes a listener from a specific event.
     * <p>
     * If the listener was not registered for the event, this method is a no-op.
     * </p>
     *
     * @param event    the event to unsubscribe from; must not be {@code null}
     * @param listener the listener to remove; must not be {@code null}
     */
    public void unsubscribe(QuizEvent event, QuizEventListener listener) {
        if (event == null || listener == null) {
            return;
        }
        lock.writeLock().lock();
        try {
            listenerMap.get(event).remove(listener);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Removes a listener from <em>all</em> events it is subscribed to.
     *
     * @param listener the listener to remove completely
     */
    public void unsubscribeAll(QuizEventListener listener) {
        if (listener == null) {
            return;
        }
        lock.writeLock().lock();
        try {
            for (List<QuizEventListener> listeners : listenerMap.values()) {
                listeners.remove(listener);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    // -------------------------------------------------------------------------
    // Publish
    // -------------------------------------------------------------------------

    /**
     * Notifies all listeners registered for the given event.
     * <p>
     * Listeners are called synchronously in subscription order.  Exceptions
     * thrown by individual listeners are caught and logged to
     * {@link System#err} so that one failing listener does not prevent others
     * from being notified.
     * </p>
     *
     * @param event the event to publish; must not be {@code null}
     * @param data  optional payload (may be {@code null})
     */
    public void publish(QuizEvent event, Object data) {
        if (event == null) {
            throw new IllegalArgumentException("Event must not be null");
        }

        // Take a snapshot under the read lock so listeners can safely call
        // subscribe/unsubscribe during notification without deadlock.
        List<QuizEventListener> snapshot;
        lock.readLock().lock();
        try {
            snapshot = new ArrayList<>(listenerMap.get(event));
        } finally {
            lock.readLock().unlock();
        }

        for (QuizEventListener listener : snapshot) {
            try {
                listener.onEvent(event, data);
            } catch (Exception ex) {
                System.err.println("[QuizEventPublisher] Listener " + listener.getClass().getSimpleName()
                        + " threw an exception on event " + event + ": " + ex.getMessage());
            }
        }
    }

    // -------------------------------------------------------------------------
    // Utility
    // -------------------------------------------------------------------------

    /**
     * Returns the number of listeners currently subscribed to the given event.
     *
     * @param event the event to query
     * @return listener count (0 if event is {@code null} or unknown)
     */
    public int listenerCount(QuizEvent event) {
        if (event == null) {
            return 0;
        }
        lock.readLock().lock();
        try {
            return listenerMap.get(event).size();
        } finally {
            lock.readLock().unlock();
        }
    }
}
