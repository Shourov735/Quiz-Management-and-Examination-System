package com.quizapp.scoring;

import com.quizapp.model.Answer;

import java.util.List;

/**
 * Strategy interface for quiz scoring algorithms.
 * <p>
 * Different implementations can apply standard scoring, negative marking,
 * time-based bonuses, etc.  The appropriate implementation is selected at
 * runtime via {@link ScoringStrategyFactory}.
 * </p>
 *
 * @see StandardScoring
 * @see NegativeMarkingScoring
 * @see TimeBasedScoring
 */
public interface ScoringStrategy {

    /**
     * Calculates the score for a completed quiz attempt.
     *
     * @param answers           list of evaluated answers
     * @param totalMarks        total possible marks for the quiz
     * @param timeLimitSeconds  total time allowed (0 if not timed)
     * @param timeSpentSeconds  actual time spent by the student
     * @return calculated score (never negative)
     */
    double calculateScore(List<Answer> answers, double totalMarks,
                          int timeLimitSeconds, int timeSpentSeconds);

    /**
     * Returns the unique name identifying this strategy.
     * Used by {@link ScoringStrategyFactory} for lookup.
     *
     * @return strategy name (e.g. {@code "STANDARD"})
     */
    String getStrategyName();

    /**
     * Returns a human-readable description of how this strategy works.
     *
     * @return description string
     */
    String getDescription();
}
