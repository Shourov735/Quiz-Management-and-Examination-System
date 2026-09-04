package com.quizapp.scoring;

import com.quizapp.model.Answer;

import java.util.List;

/**
 * Negative-marking scoring strategy.
 * <p>
 * A fraction of the average marks-per-question is deducted for each wrong
 * answer.  The final score is floored at zero.
 * </p>
 *
 * <pre>
 *   correctMarks   = Σ marksAwarded (correct answers)
 *   wrongAnswers   = count of answers where isCorrect() == false
 *   avgMarks       = totalMarks / totalQuestions
 *   penalty        = wrongAnswers × penaltyFraction × avgMarks
 *   score          = max(0, correctMarks − penalty)
 * </pre>
 */
public class NegativeMarkingScoring implements ScoringStrategy {

    private static final String NAME = "NEGATIVE_MARKING";

    /**
     * Fraction of the average marks-per-question deducted per wrong answer.
     * Defaults to 0.25 (one-quarter).
     */
    private final double penaltyFraction;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /** Constructs a {@code NegativeMarkingScoring} with a 0.25 penalty fraction. */
    public NegativeMarkingScoring() {
        this(0.25);
    }

    /**
     * Constructs a {@code NegativeMarkingScoring} with a custom penalty fraction.
     *
     * @param penaltyFraction fraction of average-marks-per-question to deduct
     *                        per wrong answer (e.g. 0.25 = 25 %)
     * @throws IllegalArgumentException if {@code penaltyFraction} is not in [0, 1]
     */
    public NegativeMarkingScoring(double penaltyFraction) {
        if (penaltyFraction < 0.0 || penaltyFraction > 1.0) {
            throw new IllegalArgumentException(
                    "penaltyFraction must be between 0.0 and 1.0, got: " + penaltyFraction);
        }
        this.penaltyFraction = penaltyFraction;
    }

    // -------------------------------------------------------------------------
    // ScoringStrategy
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     *
     * <p>Applies a penalty for each wrong answer then clamps the result to 0.</p>
     */
    @Override
    public double calculateScore(List<Answer> answers, double totalMarks,
                                 int timeLimitSeconds, int timeSpentSeconds) {
        if (answers == null || answers.isEmpty()) {
            return 0.0;
        }

        int totalQuestions = answers.size();
        double avgMarksPerQuestion = (totalQuestions > 0)
                ? totalMarks / totalQuestions
                : 0.0;

        double correctMarks = 0.0;
        long wrongAnswers = 0;

        for (Answer answer : answers) {
            if (answer.isCorrect()) {
                correctMarks += answer.getMarksAwarded();
            } else {
                wrongAnswers++;
            }
        }

        double penalty = wrongAnswers * penaltyFraction * avgMarksPerQuestion;
        double score = correctMarks - penalty;

        // Score cannot go below zero
        return Math.max(0.0, score);
    }

    /** @return {@code "NEGATIVE_MARKING"} */
    @Override
    public String getStrategyName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return String.format(
                "Negative marking scoring: deducts %.0f%% of the average marks per question "
                        + "for each wrong answer. Minimum score is 0.",
                penaltyFraction * 100);
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    /**
     * Returns the penalty fraction applied per wrong answer.
     *
     * @return penalty fraction (0.0 – 1.0)
     */
    public double getPenaltyFraction() {
        return penaltyFraction;
    }
}
