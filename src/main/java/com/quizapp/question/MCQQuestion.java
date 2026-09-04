package com.quizapp.question;

import com.quizapp.model.Question;
import com.quizapp.model.QuestionOption;
import com.quizapp.model.QuestionType;

/**
 * Multiple Choice Question (MCQ) — exactly one correct option.
 * <p>
 * A student's answer is accepted if it matches either the
 * {@link QuestionOption#getOptionText() text} or the string form of the
 * {@link QuestionOption#getId() ID} of a correct option (case-insensitive).
 * </p>
 */
public class MCQQuestion extends Question {

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /** Constructs an MCQQuestion and sets the type discriminator. */
    public MCQQuestion() {
        this.questionType = QuestionType.MCQ;
    }

    // -------------------------------------------------------------------------
    // Question contract
    // -------------------------------------------------------------------------

    /**
     * Validates the student's answer against the correct option.
     * <p>
     * The answer matches if it equals (case-insensitively, trimmed) the
     * option's text <em>or</em> the string representation of the option's id.
     * </p>
     *
     * @param answer the student's submitted answer
     * @return {@code true} if the answer identifies a correct option
     */
    @Override
    public boolean validateAnswer(String answer) {
        if (answer == null || answer.isBlank()) {
            return false;
        }
        String normalised = answer.trim();
        for (QuestionOption option : getOptions()) {
            if (option.isCorrect()) {
                boolean matchesText = option.getOptionText() != null
                        && option.getOptionText().equalsIgnoreCase(normalised);
                boolean matchesId   = String.valueOf(option.getId()).equals(normalised);
                if (matchesText || matchesId) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns the text of the correct option, or an empty string if none is set.
     *
     * @return display text of the correct option
     */
    @Override
    public String getCorrectAnswerDisplay() {
        return getOptions().stream()
                .filter(QuestionOption::isCorrect)
                .map(QuestionOption::getOptionText)
                .findFirst()
                .orElse("");
    }
}
