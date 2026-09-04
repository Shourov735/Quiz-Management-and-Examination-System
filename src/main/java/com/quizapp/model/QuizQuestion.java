package com.quizapp.model;

import java.util.Objects;

/**
 * Join entity that links a {@link Question} to a {@link Quiz} and tracks the
 * question's display order within that quiz.
 *
 * <p>The {@link #question} field is populated by the repository layer when
 * the quiz is loaded with its questions (via a JOIN query).
 */
public class QuizQuestion {

    // -------------------------------------------------------
    // Fields
    // -------------------------------------------------------

    private int      id;
    private int      quizId;
    private int      questionId;
    private int      questionOrder;

    /** Fully populated question object (set on JOIN fetch). */
    private Question question;

    // -------------------------------------------------------
    // Constructors
    // -------------------------------------------------------

    /** No-argument constructor. */
    public QuizQuestion() {
    }

    /**
     * Constructor for creating a link between a quiz and a question.
     *
     * @param quizId        FK to {@code quizzes.id}
     * @param questionId    FK to {@code questions.id}
     * @param questionOrder 1-based display order within the quiz
     */
    public QuizQuestion(int quizId, int questionId, int questionOrder) {
        this.quizId        = quizId;
        this.questionId    = questionId;
        this.questionOrder = questionOrder;
    }

    /**
     * Full constructor.
     *
     * @param id            database primary key
     * @param quizId        FK to {@code quizzes.id}
     * @param questionId    FK to {@code questions.id}
     * @param questionOrder 1-based display order within the quiz
     */
    public QuizQuestion(int id, int quizId, int questionId, int questionOrder) {
        this.id            = id;
        this.quizId        = quizId;
        this.questionId    = questionId;
        this.questionOrder = questionOrder;
    }

    // -------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------

    /** @return database primary key */
    public int getId() { return id; }

    /** @param id database primary key */
    public void setId(int id) { this.id = id; }

    /** @return FK to parent quiz */
    public int getQuizId() { return quizId; }

    /** @param quizId FK to parent quiz */
    public void setQuizId(int quizId) { this.quizId = quizId; }

    /** @return FK to the associated question */
    public int getQuestionId() { return questionId; }

    /** @param questionId FK to the associated question */
    public void setQuestionId(int questionId) { this.questionId = questionId; }

    /** @return 1-based display order within the quiz */
    public int getQuestionOrder() { return questionOrder; }

    /** @param questionOrder display order (1-based) */
    public void setQuestionOrder(int questionOrder) { this.questionOrder = questionOrder; }

    /**
     * Returns the fully populated {@link Question} object (set by the
     * repository layer on JOIN fetch).
     *
     * @return question, or {@code null} if not yet populated
     */
    public Question getQuestion() { return question; }

    /**
     * Sets the populated question object.
     *
     * @param question the associated question
     */
    public void setQuestion(Question question) { this.question = question; }

    // -------------------------------------------------------
    // Identity & Presentation
    // -------------------------------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof QuizQuestion other)) return false;
        return quizId == other.quizId && questionId == other.questionId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(quizId, questionId);
    }

    @Override
    public String toString() {
        return "QuizQuestion{id=" + id
                + ", quizId=" + quizId
                + ", questionId=" + questionId
                + ", questionOrder=" + questionOrder
                + '}';
    }
}
