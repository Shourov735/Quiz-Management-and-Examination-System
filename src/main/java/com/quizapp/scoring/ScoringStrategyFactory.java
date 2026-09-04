package com.quizapp.scoring;

/**
 * Factory that maps a strategy name string to a concrete {@link ScoringStrategy}.
 * <p>
 * This follows the Factory Method pattern: callers obtain a strategy by name
 * without knowing the concrete implementation class.
 * </p>
 *
 * <p>Supported strategy names (case-insensitive):</p>
 * <ul>
 *   <li>{@code "STANDARD"} – {@link StandardScoring}</li>
 *   <li>{@code "NEGATIVE_MARKING"} – {@link NegativeMarkingScoring}</li>
 *   <li>{@code "TIME_BASED"} – {@link TimeBasedScoring}</li>
 * </ul>
 *
 * <p>Unrecognised names fall back to {@link StandardScoring}.</p>
 */
public class ScoringStrategyFactory {

    // -------------------------------------------------------------------------
    // Constants
    // -------------------------------------------------------------------------

    /** Name key for standard (no-penalty) scoring. */
    public static final String STRATEGY_STANDARD = "STANDARD";

    /** Name key for negative-marking scoring. */
    public static final String STRATEGY_NEGATIVE_MARKING = "NEGATIVE_MARKING";

    /** Name key for time-based bonus scoring. */
    public static final String STRATEGY_TIME_BASED = "TIME_BASED";

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /** Private constructor — this is a utility / factory class. */
    private ScoringStrategyFactory() {}

    // -------------------------------------------------------------------------
    // Factory method
    // -------------------------------------------------------------------------

    /**
     * Returns the {@link ScoringStrategy} that corresponds to the given name.
     * <p>
     * The comparison is case-insensitive and trims surrounding whitespace.
     * If the name is {@code null}, blank, or unknown the default
     * {@link StandardScoring} strategy is returned.
     * </p>
     *
     * @param name the strategy name (e.g. {@code "NEGATIVE_MARKING"})
     * @return a concrete {@link ScoringStrategy} instance; never {@code null}
     */
    public static ScoringStrategy getStrategy(String name) {
        if (name == null || name.isBlank()) {
            return new StandardScoring();
        }

        return switch (name.trim().toUpperCase()) {
            case STRATEGY_NEGATIVE_MARKING -> new NegativeMarkingScoring();
            case STRATEGY_TIME_BASED       -> new TimeBasedScoring();
            default                        -> new StandardScoring();
        };
    }

    /**
     * Returns a {@link NegativeMarkingScoring} with a custom penalty fraction.
     *
     * @param penaltyFraction fraction (0.0 – 1.0) deducted per wrong answer
     * @return configured {@link NegativeMarkingScoring}
     */
    public static ScoringStrategy getNegativeMarkingStrategy(double penaltyFraction) {
        return new NegativeMarkingScoring(penaltyFraction);
    }
}
