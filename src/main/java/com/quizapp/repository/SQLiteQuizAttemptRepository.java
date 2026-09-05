package com.quizapp.repository;

import com.quizapp.database.DatabaseConnection;
import com.quizapp.model.AttemptStatus;
import com.quizapp.model.QuizAttempt;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * SQLite-backed implementation of {@link QuizAttemptRepository}.
 *
 * <p>An attempt row tracks the full lifecycle of a student's single try on a quiz:
 * from {@code IN_PROGRESS} through to {@code SUBMITTED} or {@code TIMED_OUT}.
 * Score statistics only consider <em>completed</em> attempts (status {@code SUBMITTED}
 * or {@code TIMED_OUT}).</p>
 *
 * <p>All resources are managed with try-with-resources, and every
 * {@link SQLException} is converted to a descriptive {@link RuntimeException}.</p>
 */
public class SQLiteQuizAttemptRepository implements QuizAttemptRepository {

    // -------------------------------------------------------------------------
    // SQL constants
    // -------------------------------------------------------------------------

    private static final String INSERT_ATTEMPT =
            "INSERT INTO quiz_attempts (quiz_id, student_id, start_time, end_time, status, score, total_marks) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String SELECT_BASE =
            "SELECT id, quiz_id, student_id, start_time, end_time, status, score, total_marks FROM quiz_attempts";

    private static final String SELECT_BY_ID =
            SELECT_BASE + " WHERE id = ?";

    private static final String SELECT_BY_QUIZ =
            SELECT_BASE + " WHERE quiz_id = ?";

    private static final String SELECT_BY_STUDENT =
            SELECT_BASE + " WHERE student_id = ?";

    private static final String SELECT_BY_STUDENT_AND_QUIZ =
            SELECT_BASE + " WHERE student_id = ? AND quiz_id = ?";

    private static final String COUNT_COMPLETED =
            "SELECT COUNT(*) FROM quiz_attempts " +
            "WHERE student_id = ? AND quiz_id = ? AND status IN ('SUBMITTED','TIMED_OUT')";

    private static final String BEST_SCORE =
            "SELECT MAX(score) FROM quiz_attempts " +
            "WHERE student_id = ? AND quiz_id = ? AND status IN ('SUBMITTED','TIMED_OUT')";

    private static final String AVG_SCORE =
            "SELECT AVG(score) FROM quiz_attempts " +
            "WHERE student_id = ? AND quiz_id = ? AND status IN ('SUBMITTED','TIMED_OUT')";

    private static final String UPDATE_ATTEMPT =
            "UPDATE quiz_attempts SET status = ?, score = ?, end_time = ?, total_marks = ? WHERE id = ?";

    private static final String SELECT_IN_PROGRESS =
            SELECT_BASE + " WHERE student_id = ? AND quiz_id = ? AND status = 'IN_PROGRESS'";

    // -------------------------------------------------------------------------
    // QuizAttemptRepository implementation
    // -------------------------------------------------------------------------

    /**
     * Persists a new quiz attempt and returns the auto-generated id.
     *
     * @param attempt the attempt to save; its {@code id} is updated upon return
     * @return the generated database id
     * @throws RuntimeException on SQL failure
     */
    @Override
    public int save(QuizAttempt attempt) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_ATTEMPT, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, attempt.getQuizId());
            ps.setInt(2, attempt.getStudentId());
            ps.setString(3, attempt.getStartTime() != null ? attempt.getStartTime().toString() : null);
            ps.setString(4, attempt.getEndTime() != null ? attempt.getEndTime().toString() : null);
            ps.setString(5, attempt.getStatus() != null ? attempt.getStatus().name() : AttemptStatus.IN_PROGRESS.name());
            ps.setDouble(6, attempt.getScore());
            ps.setDouble(7, attempt.getTotalMarks());

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new RuntimeException("save(QuizAttempt) - no rows inserted");
            }

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int generatedId = keys.getInt(1);
                    attempt.setId(generatedId);
                    return generatedId;
                }
                throw new RuntimeException("save(QuizAttempt) - no generated key returned");
            }
        } catch (SQLException e) {
            throw new RuntimeException("save(QuizAttempt) failed: " + e.getMessage(), e);
        }
    }

    /**
     * Finds a quiz attempt by its primary key.
     *
     * @param id the attempt id
     * @return an {@link Optional} containing the attempt, or empty if not found
     */
    @Override
    public Optional<QuizAttempt> findById(int id) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("findById(QuizAttempt) failed for id=" + id + ": " + e.getMessage(), e);
        }
    }

    /**
     * Returns all attempts for the given quiz.
     *
     * @param quizId the quiz id
     * @return list of attempts; empty list if none
     */
    @Override
    public List<QuizAttempt> findByQuizId(int quizId) {
        return queryList(SELECT_BY_QUIZ, quizId, "findByQuizId(QuizAttempt)");
    }

    /**
     * Returns all attempts by the given student.
     *
     * @param studentId the student's user id
     * @return list of attempts; empty list if none
     */
    @Override
    public List<QuizAttempt> findByStudentId(int studentId) {
        return queryList(SELECT_BY_STUDENT, studentId, "findByStudentId(QuizAttempt)");
    }

    /**
     * Returns all attempts by a student on a specific quiz (all statuses).
     *
     * @param studentId the student's user id
     * @param quizId    the quiz id
     * @return list of matching attempts; empty list if none
     */
    @Override
    public List<QuizAttempt> findByStudentAndQuiz(int studentId, int quizId) {
        List<QuizAttempt> attempts = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_STUDENT_AND_QUIZ)) {

            ps.setInt(1, studentId);
            ps.setInt(2, quizId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    attempts.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                    "findByStudentAndQuiz(QuizAttempt) failed for studentId=" + studentId
                    + ", quizId=" + quizId + ": " + e.getMessage(), e);
        }
        return attempts;
    }

    /**
     * Counts the number of completed (SUBMITTED or TIMED_OUT) attempts by a student on a quiz.
     *
     * @param studentId the student's user id
     * @param quizId    the quiz id
     * @return count of completed attempts
     */
    @Override
    public int countCompletedAttempts(int studentId, int quizId) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(COUNT_COMPLETED)) {

            ps.setInt(1, studentId);
            ps.setInt(2, quizId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                    "countCompletedAttempts(QuizAttempt) failed for studentId=" + studentId
                    + ", quizId=" + quizId + ": " + e.getMessage(), e);
        }
    }

    /**
     * Returns the highest score a student achieved on a specific quiz
     * across all completed attempts.
     *
     * @param studentId the student's user id
     * @param quizId    the quiz id
     * @return best score, or {@code 0.0} if no completed attempts exist
     */
    @Override
    public double getBestScore(int studentId, int quizId) {
        return queryDoubleAggregate(BEST_SCORE, studentId, quizId, "getBestScore(QuizAttempt)");
    }

    /**
     * Returns the average score a student achieved on a specific quiz
     * across all completed attempts.
     *
     * @param studentId the student's user id
     * @param quizId    the quiz id
     * @return average score, or {@code 0.0} if no completed attempts exist
     */
    @Override
    public double getAverageScore(int studentId, int quizId) {
        return queryDoubleAggregate(AVG_SCORE, studentId, quizId, "getAverageScore(QuizAttempt)");
    }

    /**
     * Updates the {@code status}, {@code score}, {@code end_time}, and {@code total_marks}
     * of an existing attempt.
     *
     * @param attempt the attempt to update; must have a valid {@code id}
     * @return {@code true} if a row was updated
     */
    @Override
    public boolean update(QuizAttempt attempt) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_ATTEMPT)) {

            ps.setString(1, attempt.getStatus() != null ? attempt.getStatus().name() : null);
            ps.setDouble(2, attempt.getScore());
            ps.setString(3, attempt.getEndTime() != null ? attempt.getEndTime().toString() : null);
            ps.setDouble(4, attempt.getTotalMarks());
            ps.setInt(5, attempt.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(
                    "update(QuizAttempt) failed for id=" + attempt.getId() + ": " + e.getMessage(), e);
        }
    }

    /**
     * Finds a currently in-progress attempt for the given student on the given quiz.
     *
     * <p>Business logic guarantees at most one IN_PROGRESS attempt per student-quiz pair,
     * but this method returns the first result if multiple somehow exist.</p>
     *
     * @param studentId the student's user id
     * @param quizId    the quiz id
     * @return an {@link Optional} containing the in-progress attempt, or empty if none
     */
    @Override
    public Optional<QuizAttempt> findInProgress(int studentId, int quizId) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_IN_PROGRESS)) {

            ps.setInt(1, studentId);
            ps.setInt(2, quizId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException(
                    "findInProgress(QuizAttempt) failed for studentId=" + studentId
                    + ", quizId=" + quizId + ": " + e.getMessage(), e);
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Executes a SELECT with a single integer parameter and returns a list of attempts.
     *
     * @param sql         the query
     * @param param       the integer parameter value
     * @param methodLabel label for exception messages
     * @return list of attempts
     */
    private List<QuizAttempt> queryList(String sql, int param, String methodLabel) {
        List<QuizAttempt> attempts = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    attempts.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(methodLabel + " failed for param=" + param + ": " + e.getMessage(), e);
        }
        return attempts;
    }

    /**
     * Executes an aggregate (MAX or AVG) query with two integer parameters (studentId, quizId).
     *
     * @param sql         the aggregate query
     * @param studentId   first parameter
     * @param quizId      second parameter
     * @param methodLabel label for exception messages
     * @return the aggregate double value, or {@code 0.0} if null/empty
     */
    private double queryDoubleAggregate(String sql, int studentId, int quizId, String methodLabel) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            ps.setInt(2, quizId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double value = rs.getDouble(1);
                    return rs.wasNull() ? 0.0 : value;
                }
            }
            return 0.0;
        } catch (SQLException e) {
            throw new RuntimeException(
                    methodLabel + " failed for studentId=" + studentId + ", quizId=" + quizId
                    + ": " + e.getMessage(), e);
        }
    }

    /**
     * Maps the current row of the given {@link ResultSet} to a {@link QuizAttempt}.
     *
     * @param rs an open, positioned {@link ResultSet}
     * @return a populated {@link QuizAttempt}
     * @throws SQLException if any column access fails
     */
    private QuizAttempt mapResultSet(ResultSet rs) throws SQLException {
        QuizAttempt attempt = new QuizAttempt();
        attempt.setId(rs.getInt("id"));
        attempt.setQuizId(rs.getInt("quiz_id"));
        attempt.setStudentId(rs.getInt("student_id"));
        attempt.setScore(rs.getDouble("score"));
        attempt.setTotalMarks(rs.getDouble("total_marks"));

        String statusStr = rs.getString("status");
        if (statusStr != null && !statusStr.isBlank()) {
            try {
                attempt.setStatus(AttemptStatus.valueOf(statusStr));
            } catch (IllegalArgumentException ex) {
                attempt.setStatus(AttemptStatus.IN_PROGRESS);
            }
        }

        attempt.setStartTime(parseDateTime(rs.getString("start_time")));
        attempt.setEndTime(parseDateTime(rs.getString("end_time")));

        return attempt;
    }

    /**
     * Parses an ISO-8601 {@link LocalDateTime} string, returning {@code null} on failure.
     *
     * @param raw the raw timestamp string from the database column
     * @return the parsed {@link LocalDateTime}, or {@code null}
     */
    private LocalDateTime parseDateTime(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(raw);
        } catch (Exception ex) {
            return null;
        }
    }
}
