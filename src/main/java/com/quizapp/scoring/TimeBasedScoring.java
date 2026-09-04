package com.quizapp.scoring;

import com.quizapp.model.Answer;

import java.util.List;

/**
 * Time-based scoring strategy that rewards students who finish quickly.
 * <p>
 * The base score is calculated identically to {@link StandardScoring}.
 * A speed bonus multiplier is then applied according to the ratio of time
 * spent to the time limit:
 * </p>
 *
 * <pre>
 *   timeRatio = timeSpentSeconds / timeLimitSeconds
 *
 *   timeRatio &lt; 0.50  →  score × 1.10  (10 % bonus)
 *   timeRatio &lt; 0.75  →  score × 1.05  (5 % bonus)
 *   otherwise         →  no bonus
 *
 *   final score is capped at totalMarks
 * </pre>
 *
 * <p>If the quiz has no time limit ({@code timeLimitSeconds == 0}) the
 * strategy falls back to standard scoring.</p>
 */
public class TimeBasedScoring implements ScoringStrategy {

    private static final String NAME = "TIME_BASED";

    private static final double FAST_BONUS_MULTIPLIER = 1.10;
    private static final double FAST_BONUS_THRESHOLD  = 0.50;

    private static final double MEDIUM_BONUS_MULTIPLIER = 1.05;
    private static final double MEDIUM_BONUS_THRESHOLD  = 0.75;

    /** Delegate for the base score calculation. */
    private final StandardScoring standardScoring = new StandardScoring();

    // -------------------------------------------------------------------------
    // ScoringStrategy
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     *
     * <p>Applies the speed bonus (if the quiz is timed) and caps at {@code totalMarks}.</p>
     */
    @Override
    public double calculateScore(List<Answer> answers, double totalMarks,
                                 int timeLimitSeconds, int timeSpentSeconds) {
        double baseScore = standardScoring.calculateScore(
                answers, totalMarks, timeLimitSeconds, timeSpentSeconds);

        // No bonus if the quiz is untimed
        if (timeLimitSeconds <= 0) {
            return baseScore;
        }

        double timeRatio = (double) timeSpentSeconds / timeLimitSeconds;
        double multiplier;

        if (timeRatio < FAST_BONUS_THRESHOLD) {
            multiplier = FAST_BONUS_MULTIPLIER;
        } else if (timeRatio < MEDIUM_BONUS_THRESHOLD) {
            multiplier = MEDIUM_BONUS_MULTIPLIER;
        } else {
            multiplier = 1.0; // no bonus
        }

        double bonusScore = baseScore * multiplier;

        // Final score must not exceed the quiz's total marks
        return Math.min(bonusScore, totalMarks);
    }

    /** @return {@code "TIME_BASED"} */
    @Override
    public String getStrategyName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Time-based scoring: awards a 10% bonus for finishing in under 50% of the "
                + "time limit, a 5% bonus for under 75%, and no bonus otherwise. "
                + "Score is capped at total marks.";
    }
}
