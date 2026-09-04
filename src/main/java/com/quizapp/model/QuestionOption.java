package com.quizapp.model;

import java.util.Objects;

/**
 * Represents a single selectable option belonging to a {@link Question}.
 *
 * <p>Used by {@link QuestionType#MCQ}, {@link QuestionType#TRUE_FALSE}, and
 * {@link QuestionType#MULTIPLE_ANSWER} question types. The {@link #isCorrect}
 * flag identifies which option(s) are the correct answer(s).
 */
public class QuestionOption {

    // -------------------------------------------------------
    // Fields
    // -------------------------------------------------------

    private int     id;
    private int     questionId;
    private String  optionText;
    private boolean isCorrect;
    private int     optionOrder;

    // -------------------------------------------------------
    // Constructors
    // -------------------------------------------------------

    /** No-argument constructor. */
    public QuestionOption() {
    }

    /**
     * Full constructor.
     *
     * @param id          database primary key
     * @param questionId  FK to {@code questions.id}
     * @param optionText  display text for this option
     * @param isCorrect   {@code true} if this option is a correct answer
     * @param optionOrder display ordering index (1-based)
     */
    public QuestionOption(int id, int questionId, String optionText,
                          boolean isCorrect, int optionOrder) {
        this.id          = id;
        this.questionId  = questionId;
        this.optionText  = optionText;
        this.isCorrect   = isCorrect;
        this.optionOrder = optionOrder;
    }

    // -------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------

    /** @return database primary key */
    public int getId() { return id; }

    /** @param id database primary key */
    public void setId(int id) { this.id = id; }

    /** @return FK to parent question */
    public int getQuestionId() { return questionId; }

    /** @param questionId FK to parent question */
    public void setQuestionId(int questionId) { this.questionId = questionId; }

    /** @return display text for this option */
    public String getOptionText() { return optionText; }

    /** @param optionText display text */
    public void setOptionText(String optionText) { this.optionText = optionText; }

    /** @return {@code true} if this option is a correct answer */
    public boolean isCorrect() { return isCorrect; }

    /** @param correct whether this option is correct */
    public void setCorrect(boolean correct) { isCorrect = correct; }

    /** @return display ordering index */
    public int getOptionOrder() { return optionOrder; }

    /** @param optionOrder display ordering index */
    public void setOptionOrder(int optionOrder) { this.optionOrder = optionOrder; }

    /** @return alias for optionOrder */
    public int getDisplayOrder() { return optionOrder; }

    /** @param displayOrder alias for optionOrder */
    public void setDisplayOrder(int displayOrder) { this.optionOrder = displayOrder; }

    // -------------------------------------------------------
    // Identity & Presentation
    // -------------------------------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof QuestionOption other)) return false;
        return id == other.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "QuestionOption{id=" + id
                + ", questionId=" + questionId
                + ", optionText='" + optionText + '\''
                + ", isCorrect=" + isCorrect
                + ", optionOrder=" + optionOrder
                + '}';
    }
}
