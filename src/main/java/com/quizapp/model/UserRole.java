package com.quizapp.model;

/**
 * Represents the role of a user within the Quiz Management System.
 *
 * <ul>
 *   <li>{@link #TEACHER} – can create, edit, and manage quizzes and questions.</li>
 *   <li>{@link #STUDENT} – can attempt published quizzes and view their results.</li>
 * </ul>
 */
public enum UserRole {

    /** A user who creates and manages quizzes. */
    TEACHER,

    /** A user who takes quizzes. */
    STUDENT
}
