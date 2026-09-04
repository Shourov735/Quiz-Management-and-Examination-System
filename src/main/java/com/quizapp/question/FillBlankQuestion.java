package com.quizapp.question;

import com.quizapp.model.Question;
import com.quizapp.model.QuestionType;

/**
 * Fill-in-the-blank question type.
 * <p>
 * The student types a free-text answer.  Validation is a case-insensitive,
 * trimmed string comparison against the stored {@link #correctAnswer}.
 * </p>
 */
public class FillBlankQuestion extends Question {

    /**
     * The expected correct answer string (case-insensitive comparison is used).
     */
    private String correctAnswer;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /** Constructs a FillBlankQuestion and sets the type discriminator. */
    public FillBlankQuestion() {
        this.questionType = QuestionType.FILL_BLANK;
    }

    // -------------------------------------------------------------------------
    // Question contract
    // -------------------------------------------------------------------------

    /**
     * Validates the student's answer with a case-insensitive, trimmed comparison.
     *
     * @param answer the student's submitted answer
     * @return {@code true} if the answer matches the correct answer
     */
    @Override
    public boolean validateAnswer(String answer) {
        if (answer == null || correctAnswer == null) {
            return false;
        }
        return answer.trim().equalsIgnoreCase(correctAnswer.trim());
    }

    /**
     * Returns the correct answer as stored.
     *
     * @return the correct answer string
     */
    @Override
    public String getCorrectAnswerDisplay() {
        return correctAnswer != null ? correctAnswer : "";
    }

    // -------------------------------------------------------------------------
    // Getters / Setters
    // -------------------------------------------------------------------------

    /**
     * Returns the expected correct answer.
     *
     * @return correct answer string
     */
    public String getCorrectAnswer() {
        return correctAnswer;
    }

    /**
     * Sets the expected correct answer.
     *
     * @param correctAnswer the correct answer string
     */
    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }
}
