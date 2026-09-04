package com.quizapp.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a quiz created by a {@link UserRole#TEACHER}.
 *
 * <p>A quiz aggregates an ordered set of {@link Question}s (via
 * {@link QuizQuestion}) and tracks attempt limits, scoring strategy,
 * lifecycle status, and optional time constraints.
 */
public class Quiz {

    // -------------------------------------------------------
    // Fields
    // -------------------------------------------------------

    private int           id;
    private String        title;
    private String        description;
    private int           categoryId;
    private Difficulty    difficulty;
    /** Maximum allowed minutes; 0 or negative means no time limit. */
    private int           timeLimitMinutes;
    /** Maximum number of attempts per student; 0 means unlimited. */
    private int           maxAttempts;
    /** Name of the scoring strategy (e.g. "STANDARD", "NEGATIVE_MARKING"). */
    private String        scoringStrategy;
    private QuizStatus    status;
    private int           createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    /** When {@code true}, questions are presented in a random order. */
    private boolean       shuffleQuestions;
    /** Total potential marks of all questions in the quiz. */
    private double        totalMarks;

    // -------------------------------------------------------
    // Constructors
    // -------------------------------------------------------

    /** No-argument constructor. */
    public Quiz() {
        this.status          = QuizStatus.DRAFT;
        this.scoringStrategy = "STANDARD";
    }

    /**
     * Full constructor.
     *
     * @param id               database primary key
     * @param title            quiz title
     * @param description      optional description
     * @param categoryId       FK to {@code categories.id}
     * @param difficulty       difficulty level
     * @param timeLimitMinutes time limit in minutes (0 = unlimited)
     * @param maxAttempts      max attempts per student (0 = unlimited)
     * @param scoringStrategy  name of the scoring strategy
     * @param status           current lifecycle status
     * @param createdBy        FK to {@code users.id} of the creating teacher
     * @param createdAt        creation timestamp
     * @param updatedAt        last-update timestamp
     * @param shuffleQuestions whether questions should be shuffled
     */
    public Quiz(int id, String title, String description, int categoryId,
                Difficulty difficulty, int timeLimitMinutes, int maxAttempts,
                String scoringStrategy, QuizStatus status, int createdBy,
                LocalDateTime createdAt, LocalDateTime updatedAt, boolean shuffleQuestions) {
        this.id               = id;
        this.title            = title;
        this.description      = description;
        this.categoryId       = categoryId;
        this.difficulty       = difficulty;
        this.timeLimitMinutes = timeLimitMinutes;
        this.maxAttempts      = maxAttempts;
        this.scoringStrategy  = scoringStrategy;
        this.status           = status;
        this.createdBy        = createdBy;
        this.createdAt        = createdAt;
        this.updatedAt        = updatedAt;
        this.shuffleQuestions = shuffleQuestions;
    }

    // -------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------

    /** @return database primary key */
    public int getId() { return id; }

    /** @param id database primary key */
    public void setId(int id) { this.id = id; }

    /** @return quiz title */
    public String getTitle() { return title; }

    /** @param title quiz title */
    public void setTitle(String title) { this.title = title; }

    /** @return optional description */
    public String getDescription() { return description; }

    /** @param description optional description */
    public void setDescription(String description) { this.description = description; }

    /** @return FK to {@code categories.id} */
    public int getCategoryId() { return categoryId; }

    /** @param categoryId FK to {@code categories.id} */
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    /** @return difficulty level */
    public Difficulty getDifficulty() { return difficulty; }

    /** @param difficulty difficulty level */
    public void setDifficulty(Difficulty difficulty) { this.difficulty = difficulty; }

    /** @return time limit in minutes (0 = unlimited) */
    public int getTimeLimitMinutes() { return timeLimitMinutes; }

    /** @param timeLimitMinutes time limit in minutes */
    public void setTimeLimitMinutes(int timeLimitMinutes) {
        this.timeLimitMinutes = timeLimitMinutes;
    }

    /** @return max attempts per student (0 = unlimited) */
    public int getMaxAttempts() { return maxAttempts; }

    /** @param maxAttempts max attempts per student */
    public void setMaxAttempts(int maxAttempts) { this.maxAttempts = maxAttempts; }

    /** @return name of the scoring strategy */
    public String getScoringStrategy() { return scoringStrategy; }

    /** @param scoringStrategy name of the scoring strategy */
    public void setScoringStrategy(String scoringStrategy) {
        this.scoringStrategy = scoringStrategy;
    }

    /** @return current lifecycle status */
    public QuizStatus getStatus() { return status; }

    /** @param status lifecycle status */
    public void setStatus(QuizStatus status) { this.status = status; }

    /** @return FK to {@code users.id} of the creating teacher */
    public int getCreatedBy() { return createdBy; }

    /** @param createdBy FK to creating teacher's user id */
    public void setCreatedBy(int createdBy) { this.createdBy = createdBy; }

    /** @return creation timestamp */
    public LocalDateTime getCreatedAt() { return createdAt; }

    /** @param createdAt creation timestamp */
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    /** @return last-update timestamp */
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    /** @param updatedAt last-update timestamp */
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    /** @return {@code true} if questions should be shuffled per attempt */
    public boolean isShuffleQuestions() { return shuffleQuestions; }

    /** @param shuffleQuestions whether to shuffle questions */
    public void setShuffleQuestions(boolean shuffleQuestions) {
        this.shuffleQuestions = shuffleQuestions;
    }

    /** @return total potential marks of all questions in this quiz */
    public double getTotalMarks() {
        return totalMarks;
    }

    /** @param totalMarks total marks for the quiz */
    public void setTotalMarks(double totalMarks) {
        this.totalMarks = totalMarks;
    }

    /** @return time limit in seconds (for timer computations) */
    public int getTimeLimitSeconds() {
        return timeLimitMinutes * 60;
    }

    /** @param seconds time limit in seconds */
    public void setTimeLimitSeconds(int seconds) {
        this.timeLimitMinutes = Math.max(0, seconds / 60);
    }

    /** @return true if students are allowed to attempt this quiz */
    public boolean canBeAttempted() {
        return status == QuizStatus.PUBLISHED || status == QuizStatus.ACTIVE;
    }

    // -------------------------------------------------------
    // Identity & Presentation
    // -------------------------------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Quiz other)) return false;
        return id == other.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Quiz{id=" + id
                + ", title='" + title + '\''
                + ", status=" + status
                + ", difficulty=" + difficulty
                + ", categoryId=" + categoryId
                + ", createdBy=" + createdBy
                + '}';
    }
}
