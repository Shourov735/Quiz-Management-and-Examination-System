package com.quizapp.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Records a single attempt by a {@link UserRole#STUDENT} on a {@link Quiz}.
 *
 * <p>An attempt transitions through the following states:
 * <ol>
 *   <li>{@link AttemptStatus#IN_PROGRESS} – created when the student starts.</li>
 *   <li>{@link AttemptStatus#SUBMITTED}   – student clicks "Submit".</li>
 *   <li>{@link AttemptStatus#TIMED_OUT}   – time limit expired automatically.</li>
 * </ol>
 *
 * <p>The {@link #answers} list is populated from the {@code answers} table
 * after the attempt is submitted and graded.
 */
public class QuizAttempt {

    // -------------------------------------------------------
    // Fields
    // -------------------------------------------------------

    private int           id;
    private int           quizId;
    private int           studentId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private AttemptStatus status;
    private double        score;
    private double        totalMarks;

    /** All answers recorded for this attempt. */
    private List<Answer> answers = new ArrayList<>();

    // -------------------------------------------------------
    // Constructors
    // -------------------------------------------------------

    /** No-argument constructor. */
    public QuizAttempt() {
        this.status = AttemptStatus.IN_PROGRESS;
    }

    /**
     * Constructor for starting a new attempt.
     *
     * @param quizId    FK to {@code quizzes.id}
     * @param studentId FK to {@code users.id} of the student
     * @param startTime timestamp when the attempt was started
     */
    public QuizAttempt(int quizId, int studentId, LocalDateTime startTime) {
        this.quizId    = quizId;
        this.studentId = studentId;
        this.startTime = startTime;
        this.status    = AttemptStatus.IN_PROGRESS;
    }

    /**
     * Full constructor.
     *
     * @param id         database primary key
     * @param quizId     FK to {@code quizzes.id}
     * @param studentId  FK to {@code users.id}
     * @param startTime  start timestamp
     * @param endTime    end timestamp (null while in progress)
     * @param status     current attempt status
     * @param score      marks earned by the student
     * @param totalMarks total possible marks for the quiz
     */
    public QuizAttempt(int id, int quizId, int studentId,
                       LocalDateTime startTime, LocalDateTime endTime,
                       AttemptStatus status, double score, double totalMarks) {
        this.id         = id;
        this.quizId     = quizId;
        this.studentId  = studentId;
        this.startTime  = startTime;
        this.endTime    = endTime;
        this.status     = status;
        this.score      = score;
        this.totalMarks = totalMarks;
    }

    // -------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------

    /** @return database primary key */
    public int getId() { return id; }

    /** @param id database primary key */
    public void setId(int id) { this.id = id; }

    /** @return FK to the quiz being attempted */
    public int getQuizId() { return quizId; }

    /** @param quizId FK to quizzes */
    public void setQuizId(int quizId) { this.quizId = quizId; }

    /** @return FK to the student user */
    public int getStudentId() { return studentId; }

    /** @param studentId FK to users */
    public void setStudentId(int studentId) { this.studentId = studentId; }

    /** @return timestamp when the attempt started */
    public LocalDateTime getStartTime() { return startTime; }

    /** @param startTime attempt start timestamp */
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    /** @return timestamp when the attempt ended, or {@code null} if still in progress */
    public LocalDateTime getEndTime() { return endTime; }

    /** @param endTime attempt end timestamp */
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    /** @return current attempt status */
    public AttemptStatus getStatus() { return status; }

    /** @param status attempt status */
    public void setStatus(AttemptStatus status) { this.status = status; }

    /** @return marks earned by the student */
    public double getScore() { return score; }

    /** @param score marks earned */
    public void setScore(double score) { this.score = score; }

    /** @return total possible marks for this quiz */
    public double getTotalMarks() { return totalMarks; }

    /** @param totalMarks total possible marks */
    public void setTotalMarks(double totalMarks) { this.totalMarks = totalMarks; }

    /**
     * Returns the mutable list of answers recorded for this attempt.
     *
     * @return list of {@link Answer}; never {@code null}
     */
    public List<Answer> getAnswers() { return answers; }

    /**
     * Replaces the answers list.
     *
     * @param answers new list of answers
     */
    public void setAnswers(List<Answer> answers) {
        this.answers = (answers != null) ? answers : new ArrayList<>();
    }

    /**
     * Appends a single answer to the list.
     *
     * @param answer the answer to add
     */
    public void addAnswer(Answer answer) {
        if (answer != null) {
            this.answers.add(answer);
        }
    }

    /**
     * Convenience method: returns the percentage score (0–100).
     *
     * @return percentage score, or 0 if total marks is 0
     */
    public double getPercentageScore() {
        if (totalMarks == 0) return 0.0;
        return (score / totalMarks) * 100.0;
    }

    // -------------------------------------------------------
    // Identity & Presentation
    // -------------------------------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof QuizAttempt other)) return false;
        return id == other.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "QuizAttempt{id=" + id
                + ", quizId=" + quizId
                + ", studentId=" + studentId
                + ", status=" + status
                + ", score=" + score
                + ", totalMarks=" + totalMarks
                + ", startTime=" + startTime
                + '}';
    }
}
