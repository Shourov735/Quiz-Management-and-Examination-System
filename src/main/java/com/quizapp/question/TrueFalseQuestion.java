package com.quizapp.question;

import com.quizapp.model.Question;
import com.quizapp.model.QuestionOption;
import com.quizapp.model.QuestionType;

/**
 * True / False question type.
 * <p>
 * Options are always {@code "True"} and {@code "False"}.  The student submits
 * either string; validation is case-insensitive.
 * </p>
 */
public class TrueFalseQuestion extends Question {

    /** Canonical text for the correct answer — set at construction time. */
    private boolean correctValue; // true → answer is "True", false → "False"

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /** Constructs a TrueFalseQuestion and sets the type discriminator. */
    public TrueFalseQuestion() {
        this.questionType = QuestionType.TRUE_FALSE;
    }

    // -------------------------------------------------------------------------
    // Question contract
    // -------------------------------------------------------------------------

    /**
     * Validates the student's answer.
     * <p>
     * Accepts {@code "true"} or {@code "false"} (case-insensitive).  Then
     * checks whether that value matches the correct option in the options list
     * (the option with {@link QuestionOption#isCorrect()} == {@code true}).
     * </p>
     *
     * @param answer the student's submitted answer ("true" / "false" / "True" / "False")
     * @return {@code true} if the answer is correct
     */
    @Override
    public boolean validateAnswer(String answer) {
        if (answer == null || answer.isBlank()) {
            return false;
        }
        String normalised = answer.trim().toLowerCase();
        if (!normalised.equals("true") && !normalised.equals("false")) {
            return false;
        }

        // Find the option whose text matches what the student said
        for (QuestionOption option : getOptions()) {
            if (option.getOptionText() != null
                    && option.getOptionText().equalsIgnoreCase(normalised)
                    && option.isCorrect()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns {@code "True"} or {@code "False"} based on the correct option.
     *
     * @return correct answer display text
     */
    @Override
    public String getCorrectAnswerDisplay() {
        return getOptions().stream()
                .filter(QuestionOption::isCorrect)
                .map(QuestionOption::getOptionText)
                .findFirst()
                .orElse("True");
    }

    // -------------------------------------------------------------------------
    // Convenience helpers
    // -------------------------------------------------------------------------

    /**
     * Returns {@code true} if the correct answer is "True", {@code false} if "False".
     * Determined by examining the options list.
     *
     * @return correct boolean value
     */
    public boolean isCorrectValueTrue() {
        return getOptions().stream()
                .filter(QuestionOption::isCorrect)
                .map(o -> "true".equalsIgnoreCase(o.getOptionText()))
                .findFirst()
                .orElse(true);
    }
}
