package com.quizapp.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Abstract base class for all question types in the Quiz Management System.
 *
 * <p>Concrete subclasses implement the two abstract methods that define
 * type-specific answer validation and display:
 * <ul>
 *   <li>{@link #validateAnswer(String)} – returns {@code true} when the
 *       student's answer string is correct for this question.</li>
 *   <li>{@link #getCorrectAnswerDisplay()} – returns a human-readable
 *       representation of the expected correct answer(s).</li>
 * </ul>
 *
 * <p>The {@link #options} list is populated by the repository layer when
 * the question is fetched with its options (e.g. for display during a quiz).
 */
public abstract class Question {

    // -------------------------------------------------------
    // Fields
    // -------------------------------------------------------

    private int              id;
    private String           questionText;
    protected QuestionType   questionType;
    private double           marks;
    private Difficulty       difficulty;
    private int              categoryId;
    private int              createdBy;
    private LocalDateTime    createdAt;
    private int              quizId;
    private int              displayOrder;

    /** Options associated with this question (populated on JOIN). */
    private List<QuestionOption> options = new ArrayList<>();

    // -------------------------------------------------------
    // Constructors
    // -------------------------------------------------------

    /** No-argument constructor. */
    protected Question() {
    }

    /**
     * Full constructor used by subclasses and mapping code.
     *
     * @param id           database primary key
     * @param questionText the question body text
     * @param questionType the type of this question
     * @param marks        marks awarded for a fully correct answer
     * @param difficulty   difficulty level
     * @param categoryId   FK to {@code categories.id}
     * @param createdBy    FK to {@code users.id} of the creator
     * @param createdAt    creation timestamp
     */
    protected Question(int id, String questionText, QuestionType questionType,
                       double marks, Difficulty difficulty,
                       int categoryId, int createdBy, LocalDateTime createdAt) {
        this.id           = id;
        this.questionText = questionText;
        this.questionType = questionType;
        this.marks        = marks;
        this.difficulty   = difficulty;
        this.categoryId   = categoryId;
        this.createdBy    = createdBy;
        this.createdAt    = createdAt;
    }

    // -------------------------------------------------------
    // Abstract Methods
    // -------------------------------------------------------

    /**
     * Validates a student's answer string against the correct answer(s) for
     * this question.
     *
     * <p>The format of {@code answer} is type-specific:
     * <ul>
     *   <li><b>MCQ / TRUE_FALSE</b>: the id or text of the chosen option.</li>
     *   <li><b>FILL_BLANK</b>: the student's free-text response.</li>
     *   <li><b>MULTIPLE_ANSWER</b>: comma-separated option ids/texts.</li>
     * </ul>
     *
     * @param answer the student-supplied answer string
     * @return {@code true} if the answer is correct
     */
    public abstract boolean validateAnswer(String answer);

    /**
     * Returns a human-readable string describing the correct answer(s) for
     * this question, suitable for display in result screens.
     *
     * @return correct-answer display string
     */
    public abstract String getCorrectAnswerDisplay();

    // -------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------

    /** @return database primary key */
    public int getId() { return id; }

    /** @param id database primary key */
    public void setId(int id) { this.id = id; }

    /** @return the question body text */
    public String getQuestionText() { return questionText; }

    /** @param questionText the question body text */
    public void setQuestionText(String questionText) { this.questionText = questionText; }

    /** @return the question type */
    public QuestionType getQuestionType() { return questionType; }

    /** @param questionType the question type */
    public void setQuestionType(QuestionType questionType) { this.questionType = questionType; }

    /** @return marks awarded for a fully correct answer */
    public double getMarks() { return marks; }

    /** @param marks marks for a fully correct answer */
    public void setMarks(double marks) { this.marks = marks; }

    /** @return difficulty level */
    public Difficulty getDifficulty() { return difficulty; }

    /** @param difficulty difficulty level */
    public void setDifficulty(Difficulty difficulty) { this.difficulty = difficulty; }

    /** @return FK to {@code categories.id} */
    public int getCategoryId() { return categoryId; }

    /** @param categoryId FK to categories table */
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    /** @return FK to {@code users.id} of the creator */
    public int getCreatedBy() { return createdBy; }

    /** @param createdBy FK to creator user id */
    public void setCreatedBy(int createdBy) { this.createdBy = createdBy; }

    /** @return creation timestamp */
    public LocalDateTime getCreatedAt() { return createdAt; }

    /** @param createdAt creation timestamp */
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    /** @return associated quizId when loaded in the context of a quiz */
    public int getQuizId() { return quizId; }

    /** @param quizId associated quiz id */
    public void setQuizId(int quizId) { this.quizId = quizId; }

    /** @return display order within a quiz */
    public int getDisplayOrder() { return displayOrder; }

    /** @param displayOrder order index within a quiz */
    public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }

    /**
     * Returns the mutable list of options associated with this question.
     *
     * @return list of {@link QuestionOption}; never {@code null}
     */
    public List<QuestionOption> getOptions() { return options; }

    /**
     * Replaces the option list.
     *
     * @param options new list of options
     */
    public void setOptions(List<QuestionOption> options) {
        this.options = (options != null) ? options : new ArrayList<>();
    }

    /**
     * Appends a single option to the existing option list.
     *
     * @param option the option to add
     */
    public void addOption(QuestionOption option) {
        if (option != null) {
            this.options.add(option);
        }
    }

    // -------------------------------------------------------
    // Identity & Presentation
    // -------------------------------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Question other)) return false;
        return id == other.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()
                + "{id=" + id
                + ", type=" + questionType
                + ", marks=" + marks
                + ", difficulty=" + difficulty
                + ", text='" + questionText + '\''
                + '}';
    }
}
