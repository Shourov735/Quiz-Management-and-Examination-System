package com.quizapp.model;

/**
 * Lifecycle status of a {@link Quiz}.
 *
 * <ul>
 *   <li>{@link #DRAFT}     – created but not yet visible to students.</li>
 *   <li>{@link #PUBLISHED} – visible and available for students to attempt.</li>
 *   <li>{@link #ACTIVE}    – currently in progress (timed window open).</li>
 *   <li>{@link #COMPLETED} – all attempts have been evaluated; no new attempts accepted.</li>
 *   <li>{@link #ARCHIVED}  – retained for record-keeping but no longer active.</li>
 * </ul>
 */
public enum QuizStatus {

    /** Quiz is in draft; not visible to students. */
    DRAFT,

    /** Quiz has been published and is open for attempts. */
    PUBLISHED,

    /** Quiz session is currently active. */
    ACTIVE,

    /** Quiz is closed; all attempts have been graded. */
    COMPLETED,

    /** Quiz is archived and read-only. */
    ARCHIVED
}
