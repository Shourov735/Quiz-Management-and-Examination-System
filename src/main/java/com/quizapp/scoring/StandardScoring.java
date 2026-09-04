package com.quizapp.scoring;

import com.quizapp.model.Answer;

import java.util.List;

/**
 * Standard (no-penalty) scoring strategy.
 * <p>
 * The final score is simply the sum of {@link Answer#getMarksAwarded()} for
 * every answer in the attempt.  Wrong or skipped answers contribute zero marks.
 * </p>
 */
public class StandardScoring implements ScoringStrategy {

    private static final String NAME = "STANDARD";

    // -------------------------------------------------------------------------
    // ScoringStrategy
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     *
     * <p>Score = Σ marksAwarded for all answers.</p>
     *
     * @param answers           evaluated answers for the attempt
     * @param totalMarks        total possible marks (unused by this strategy)
     * @param timeLimitSeconds  unused by this strategy
     * @param timeSpentSeconds  unused by this strategy
     * @return sum of marks awarded across all answers
     */
    @Override
    public double calculateScore(List<Answer> answers, double totalMarks,
                                 int timeLimitSeconds, int timeSpentSeconds) {
        if (answers == null || answers.isEmpty()) {
            return 0.0;
        }
        return answers.stream()
                .mapToDouble(Answer::getMarksAwarded)
                .sum();
    }

    /** @return {@code "STANDARD"} */
    @Override
    public String getStrategyName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Standard scoring: each correct answer awards its full marks; "
                + "incorrect or skipped answers award zero marks.";
    }
}
