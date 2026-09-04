package com.quizapp.model;

/**
 * Difficulty level applicable to both {@link Quiz} and {@link Question} entities.
 *
 * <ul>
 *   <li>{@link #EASY}   – suitable for beginners.</li>
 *   <li>{@link #MEDIUM} – requires intermediate knowledge.</li>
 *   <li>{@link #HARD}   – designed for advanced learners.</li>
 * </ul>
 */
public enum Difficulty {

    /** Beginner-level difficulty. */
    EASY,

    /** Intermediate-level difficulty. */
    MEDIUM,

    /** Advanced-level difficulty. */
    HARD
}
