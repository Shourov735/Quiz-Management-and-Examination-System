package com.quizapp.model;

import java.util.Objects;

/**
 * Represents a student's answer to a single {@link Question} within a
 * {@link QuizAttempt}.
 *
 * <p>The {@link #answerValue} is a string whose format depends on the
 * question type:
 * <ul>
 *   <li><b>MCQ / TRUE_FALSE</b> – the id or text of the selected option.</li>
 *   <li><b>FILL_BLANK</b>       – the free-text response typed by the student.</li>
 *   <li><b>MULTIPLE_ANSWER</b>  – comma-separated option ids/texts.</li>
 * </ul>
 */
public class Answer {

    // -------------------------------------------------------
    // Fields
    // -------------------------------------------------------

    private int    id;
    private int    attemptId;
    private int    questionId;
    private String answerValue;
    private boolean isCorrect;
    private double  marksAwarded;

    // -------------------------------------------------------
    // Constructors
    // -------------------------------------------------------

    /** No-argument constructor. */
    public Answer() {
    }

    /**
     * Convenience constructor for recording an answer during a quiz attempt
     * (before grading, so {@code isCorrect} and {@code marksAwarded} default).
     *
     * @param attemptId   FK to {@code quiz_attempts.id}
     * @param questionId  FK to {@code questions.id}
     * @param answerValue the student's answer string
     */
    public Answer(int attemptId, int questionId, String answerValue) {
        this.attemptId   = attemptId;
        this.questionId  = questionId;
        this.answerValue = answerValue;
    }

    /**
     * Full constructor.
     *
     * @param id           database primary key
     * @param attemptId    FK to {@code quiz_attempts.id}
     * @param questionId   FK to {@code questions.id}
     * @param answerValue  the student's answer string
     * @param isCorrect    whether the answer was marked correct
     * @param marksAwarded marks awarded for this answer
     */
    public Answer(int id, int attemptId, int questionId, String answerValue,
                  boolean isCorrect, double marksAwarded) {
        this.id           = id;
        this.attemptId    = attemptId;
        this.questionId   = questionId;
        this.answerValue  = answerValue;
        this.isCorrect    = isCorrect;
        this.marksAwarded = marksAwarded;
    }

    // -------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------

    /** @return database primary key */
    public int getId() { return id; }

    /** @param id database primary key */
    public void setId(int id) { this.id = id; }

    /** @return FK to the parent quiz attempt */
    public int getAttemptId() { return attemptId; }

    /** @param attemptId FK to quiz_attempts */
    public void setAttemptId(int attemptId) { this.attemptId = attemptId; }

    /** @return FK to the answered question */
    public int getQuestionId() { return questionId; }

    /** @param questionId FK to questions */
    public void setQuestionId(int questionId) { this.questionId = questionId; }

    /** @return the student's answer string */
    public String getAnswerValue() { return answerValue; }

    /** @param answerValue the student's answer string */
    public void setAnswerValue(String answerValue) { this.answerValue = answerValue; }

    /** @return alias for answerValue */
    public String getSubmittedAnswer() { return answerValue; }

    /** @param submittedAnswer alias for answerValue */
    public void setSubmittedAnswer(String submittedAnswer) { this.answerValue = submittedAnswer; }

    /** @return {@code true} if this answer was graded as correct */
    public boolean isCorrect() { return isCorrect; }

    /** @param correct whether the answer is correct */
    public void setCorrect(boolean correct) { isCorrect = correct; }

    /** @return marks awarded for this answer */
    public double getMarksAwarded() { return marksAwarded; }

    /** @param marksAwarded marks to award */
    public void setMarksAwarded(double marksAwarded) { this.marksAwarded = marksAwarded; }

    // -------------------------------------------------------
    // Identity & Presentation
    // -------------------------------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Answer other)) return false;
        return id == other.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Answer{id=" + id
                + ", attemptId=" + attemptId
                + ", questionId=" + questionId
                + ", answerValue='" + answerValue + '\''
                + ", isCorrect=" + isCorrect
                + ", marksAwarded=" + marksAwarded
                + '}';
    }
}
