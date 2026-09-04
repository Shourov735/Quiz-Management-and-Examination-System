package com.quizapp.question;

import com.quizapp.model.Question;
import com.quizapp.model.QuestionOption;
import com.quizapp.model.QuestionType;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Multiple-answer question — the student must select <em>all</em> correct options.
 * <p>
 * The student submits a comma-separated list of option texts.  The answer is
 * correct only when the selected set exactly equals the set of correct options
 * (no extra selections, no missing selections).
 * </p>
 *
 * <p>Matching is case-insensitive and extra whitespace around each token is
 * ignored.</p>
 */
public class MultipleAnswerQuestion extends Question {

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /** Constructs a MultipleAnswerQuestion and sets the type discriminator. */
    public MultipleAnswerQuestion() {
        this.questionType = QuestionType.MULTIPLE_ANSWER;
    }

    // -------------------------------------------------------------------------
    // Question contract
    // -------------------------------------------------------------------------

    /**
     * Validates a comma-separated answer string.
     * <p>
     * The answer must contain exactly the texts of all correct options — no
     * more, no fewer.  Order and case do not matter.
     * </p>
     *
     * @param answer comma-separated option texts submitted by the student
     * @return {@code true} if the selected set exactly matches the correct options
     */
    @Override
    public boolean validateAnswer(String answer) {
        if (answer == null || answer.isBlank()) {
            return false;
        }

        // Parse the student's selections into a normalised set
        Set<String> selected = Arrays.stream(answer.split(","))
                .map(s -> s.trim().toLowerCase())
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());

        // Build the set of correct option texts
        Set<String> correctTexts = getOptions().stream()
                .filter(QuestionOption::isCorrect)
                .map(o -> o.getOptionText().trim().toLowerCase())
                .collect(Collectors.toSet());

        if (correctTexts.isEmpty()) {
            return false;
        }

        return selected.equals(correctTexts);
    }

    /**
     * Returns a comma-separated string of all correct option texts.
     *
     * @return display text for all correct answers
     */
    @Override
    public String getCorrectAnswerDisplay() {
        List<String> correctTexts = getOptions().stream()
                .filter(QuestionOption::isCorrect)
                .map(QuestionOption::getOptionText)
                .collect(Collectors.toList());
        return String.join(", ", correctTexts);
    }
}
