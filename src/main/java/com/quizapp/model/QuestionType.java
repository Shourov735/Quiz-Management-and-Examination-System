package com.quizapp.model;

/**
 * Supported question formats in the Quiz Management System.
 *
 * <ul>
 *   <li>{@link #MCQ}             – single-answer multiple choice.</li>
 *   <li>{@link #TRUE_FALSE}      – binary true/false question.</li>
 *   <li>{@link #FILL_BLANK}      – student types a free-text answer.</li>
 *   <li>{@link #MULTIPLE_ANSWER} – one or more options may be correct.</li>
 * </ul>
 */
public enum QuestionType {

    /** Multiple-choice question with exactly one correct answer. */
    MCQ,

    /** True or false question. */
    TRUE_FALSE,

    /** Fill-in-the-blank free-text question. */
    FILL_BLANK,

    /** Multiple-answer question where more than one option may be correct. */
    MULTIPLE_ANSWER
}
