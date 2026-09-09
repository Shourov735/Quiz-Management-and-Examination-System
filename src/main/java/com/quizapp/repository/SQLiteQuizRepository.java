package com.quizapp.repository;

import com.quizapp.database.DatabaseConnection;
import com.quizapp.model.Difficulty;
import com.quizapp.model.Quiz;
import com.quizapp.model.QuizStatus;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * SQLite-backed implementation of {@link QuizRepository}.
 *
 * <p>Uses parameterised {@link PreparedStatement}s throughout — including the
 * dynamic {@link #search} query — to prevent SQL injection. All resources are
 * closed via try-with-resources, and every {@link SQLException} is wrapped in a
 * descriptive {@link RuntimeException}.</p>
 *
 * <p>Score statistics (average, highest, lowest) only consider attempts whose
 * status is {@code SUBMITTED} or {@code TIMED_OUT}, i.e. attempts that have
 * produced a final result.</p>
 */
public class SQLiteQuizRepository implements QuizRepository {

    // -------------------------------------------------------------------------
    // SQL constants
    // -------------------------------------------------------------------------

    private static final String INSERT_QUIZ =
            "INSERT INTO quizzes (title, description, category_id, difficulty, time_limit_minutes, " +
            "max_attempts, scoring_strategy, status, created_by, created_at, updated_at, shuffle_questions) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SELECT_BASE =
            "SELECT id, title, description, category_id, difficulty, time_limit_minutes, " +
            "max_attempts, scoring_strategy, status, created_by, created_at, updated_at, shuffle_questions FROM quizzes";

    private static final String SELECT_BY_ID =
            SELECT_BASE + " WHERE id = ?";

    private static final String SELECT_ALL =
            SELECT_BASE;

    private static final String SELECT_BY_STATUS =
            SELECT_ALL + " WHERE status = ?";

    private static final String UPDATE_QUIZ =
            "UPDATE quizzes SET title = ?, description = ?, category_id = ?, difficulty = ?, " +
            "time_limit_minutes = ?, max_attempts = ?, scoring_strategy = ?, status = ?, " +
            "created_by = ?, updated_at = ?, shuffle_questions = ? WHERE id = ?";

    private static final String DELETE_QUIZ =
            "DELETE FROM quizzes WHERE id = ?";

    private static final String COUNT_ATTEMPTS =
            "SELECT COUNT(*) FROM quiz_attempts WHERE quiz_id = ? AND status IN ('SUBMITTED','TIMED_OUT')";

    private static final String AVG_SCORE =
            "SELECT AVG(score) FROM quiz_attempts WHERE quiz_id = ? AND status IN ('SUBMITTED','TIMED_OUT')";

    private static final String MAX_SCORE =
            "SELECT MAX(score) FROM quiz_attempts WHERE quiz_id = ? AND status IN ('SUBMITTED','TIMED_OUT')";

    private static final String MIN_SCORE =
            "SELECT MIN(score) FROM quiz_attempts WHERE quiz_id = ? AND status IN ('SUBMITTED','TIMED_OUT')";

    // -------------------------------------------------------------------------
    // QuizRepository implementation
    // -------------------------------------------------------------------------

    /**
     * Persists a new quiz and returns the generated primary key.
     *
     * @param quiz the quiz to save; its {@code id} is ignored and updated on return
     * @return the auto-generated database id
     * @throws RuntimeException on SQL failure
     */
    @Override
    public int save(Quiz quiz) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_QUIZ, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, quiz.getTitle());
            ps.setString(2, quiz.getDescription());
            if (quiz.getCategoryId() > 0) {
                ps.setInt(3, quiz.getCategoryId());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            ps.setString(4, quiz.getDifficulty() != null ? quiz.getDifficulty().name() : null);
            ps.setInt(5, quiz.getTimeLimitMinutes());
            ps.setInt(6, quiz.getMaxAttempts());
            ps.setString(7, quiz.getScoringStrategy() != null ? quiz.getScoringStrategy() : "STANDARD");
            ps.setString(8, quiz.getStatus() != null ? quiz.getStatus().name() : QuizStatus.DRAFT.name());
            if (quiz.getCreatedBy() > 0) {
                ps.setInt(9, quiz.getCreatedBy());
            } else {
                ps.setNull(9, Types.INTEGER);
            }
            ps.setString(10, quiz.getCreatedAt() != null ? quiz.getCreatedAt().toString() : LocalDateTime.now().toString());
            ps.setString(11, quiz.getUpdatedAt() != null ? quiz.getUpdatedAt().toString() : LocalDateTime.now().toString());
            ps.setInt(12, quiz.isShuffleQuestions() ? 1 : 0);

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new RuntimeException("save(Quiz) - no rows inserted for quiz: " + quiz.getTitle());
            }

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int generatedId = keys.getInt(1);
                    quiz.setId(generatedId);
                    return generatedId;
                }
                throw new RuntimeException("save(Quiz) - no generated key returned for quiz: " + quiz.getTitle());
            }
        } catch (SQLException e) {
            throw new RuntimeException("save(Quiz) failed for title='" + quiz.getTitle() + "': " + e.getMessage(), e);
        }
    }

    /**
     * Finds a quiz by its primary key.
     *
     * @param id the quiz id
     * @return an {@link Optional} containing the quiz, or empty if not found
     */
    @Override
    public Optional<Quiz> findById(int id) {
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
            throw new RuntimeException("findById(Quiz) failed for id=" + id + ": " + e.getMessage(), e);
        }
    }

    /**
     * Returns all quizzes in the database.
     *
     * @return list of all quizzes; empty list if none
     */
    @Override
    public List<Quiz> findAll() {
        List<Quiz> quizzes = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                quizzes.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("findAll(Quiz) failed: " + e.getMessage(), e);
        }
        return quizzes;
    }

    /**
     * Returns all quizzes with the given lifecycle status.
     *
     * @param status the status to filter by
     * @return matching quizzes; empty list if none
     */
    @Override
    public List<Quiz> findByStatus(QuizStatus status) {
        List<Quiz> quizzes = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_STATUS)) {

            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    quizzes.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("findByStatus(Quiz) failed for status=" + status + ": " + e.getMessage(), e);
        }
        return quizzes;
    }

    /**
     * Returns all quizzes created by the specified teacher.
     *
     * <p>The {@code created_by} column must exist in the {@code quizzes} table for
     * this filter to work correctly.</p>
     *
     * @param teacherId the teacher's user id
     * @return matching quizzes; empty list if none
     */
    @Override
    public List<Quiz> findByCreatedBy(int teacherId) {
        List<Quiz> quizzes = new ArrayList<>();
        String sql = SELECT_ALL + " WHERE created_by = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, teacherId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    quizzes.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("findByCreatedBy(Quiz) failed for teacherId=" + teacherId + ": " + e.getMessage(), e);
        }
        return quizzes;
    }

    /**
     * Returns all quizzes belonging to a given category.
     *
     * <p>Requires a {@code category_id} column in the {@code quizzes} table.</p>
     *
     * @param categoryId the category id
     * @return matching quizzes; empty list if none
     */
    @Override
    public List<Quiz> findByCategoryId(int categoryId) {
        List<Quiz> quizzes = new ArrayList<>();
        String sql = SELECT_ALL + " WHERE category_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    quizzes.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                    "findByCategoryId(Quiz) failed for categoryId=" + categoryId + ": " + e.getMessage(), e);
        }
        return quizzes;
    }

    /**
     * Searches quizzes using optional filters. Filters are combined with AND.
     *
     * <p>All filter parameters are optional:
     * <ul>
     *   <li>{@code titleKeyword} — {@code null} or blank to skip; matched with {@code LIKE %keyword%}</li>
     *   <li>{@code categoryId} — {@code 0} to skip</li>
     *   <li>{@code difficulty} — {@code null} or blank to skip</li>
     *   <li>{@code status} — {@code null} to skip</li>
     * </ul>
     * Parameterised {@link PreparedStatement}s are built dynamically; no string concatenation
     * of user input is performed.</p>
     *
     * @param titleKeyword case-insensitive substring to match against the title, or null/blank
     * @param categoryId   category constraint, or 0 to skip
     * @param difficulty   difficulty level string, or null/blank to skip
     * @param status       quiz lifecycle status constraint, or null to skip
     * @return matching quizzes; empty list if none
     */
    @Override
    public List<Quiz> search(String titleKeyword, int categoryId, String difficulty, QuizStatus status) {
        List<Quiz> quizzes = new ArrayList<>();

        // Build the WHERE clause dynamically, collecting parameters in order
        StringBuilder sql = new StringBuilder(
                SELECT_BASE + " WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (titleKeyword != null && !titleKeyword.isBlank()) {
            sql.append(" AND title LIKE ?");
            params.add("%" + titleKeyword + "%");
        }
        if (categoryId > 0) {
            sql.append(" AND category_id = ?");
            params.add(categoryId);
        }
        if (difficulty != null && !difficulty.isBlank()) {
            sql.append(" AND difficulty = ?");
            params.add(difficulty);
        }
        if (status != null) {
            sql.append(" AND status = ?");
            params.add(status.name());
        }

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                Object param = params.get(i);
                if (param instanceof String s) {
                    ps.setString(i + 1, s);
                } else if (param instanceof Integer iv) {
                    ps.setInt(i + 1, iv);
                } else {
                    ps.setObject(i + 1, param);
                }
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    quizzes.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("search(Quiz) failed: " + e.getMessage(), e);
        }
        return quizzes;
    }

    /**
     * Updates all mutable fields of an existing quiz.
     *
     * @param quiz the quiz to update; must have a valid {@code id}
     * @return {@code true} if a row was updated
     */
    @Override
    public boolean update(Quiz quiz) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_QUIZ)) {

            ps.setString(1, quiz.getTitle());
            ps.setString(2, quiz.getDescription());
            if (quiz.getCategoryId() > 0) {
                ps.setInt(3, quiz.getCategoryId());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            ps.setString(4, quiz.getDifficulty() != null ? quiz.getDifficulty().name() : null);
            ps.setInt(5, quiz.getTimeLimitMinutes());
            ps.setInt(6, quiz.getMaxAttempts());
            ps.setString(7, quiz.getScoringStrategy() != null ? quiz.getScoringStrategy() : "STANDARD");
            ps.setString(8, quiz.getStatus() != null ? quiz.getStatus().name() : QuizStatus.DRAFT.name());
            if (quiz.getCreatedBy() > 0) {
                ps.setInt(9, quiz.getCreatedBy());
            } else {
                ps.setNull(9, Types.INTEGER);
            }
            ps.setString(10, LocalDateTime.now().toString());
            ps.setInt(11, quiz.isShuffleQuestions() ? 1 : 0);
            ps.setInt(12, quiz.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("update(Quiz) failed for id=" + quiz.getId() + ": " + e.getMessage(), e);
        }
    }

    /**
     * Deletes the quiz row with the given id.
     *
     * @param id the quiz id
     * @return {@code true} if a row was deleted
     */
    @Override
    public boolean delete(int id) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE_QUIZ)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("delete(Quiz) failed for id=" + id + ": " + e.getMessage(), e);
        }
    }

    /**
     * Counts the number of completed (SUBMITTED or TIMED_OUT) attempts for the quiz.
     *
     * @param quizId the quiz id
     * @return count of completed attempts
     */
    @Override
    public int countAttempts(int quizId) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(COUNT_ATTEMPTS)) {

            ps.setInt(1, quizId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("countAttempts(Quiz) failed for quizId=" + quizId + ": " + e.getMessage(), e);
        }
    }

    /**
     * Computes the average score across all completed attempts for the quiz.
     *
     * @param quizId the quiz id
     * @return average score, or {@code 0.0} if no completed attempts exist
     */
    @Override
    public double getAverageScore(int quizId) {
        return querySingleDouble(AVG_SCORE, quizId, "getAverageScore(Quiz)");
    }

    /**
     * Returns the highest score achieved across all completed attempts for the quiz.
     *
     * @param quizId the quiz id
     * @return highest score, or {@code 0.0} if no completed attempts exist
     */
    @Override
    public double getHighestScore(int quizId) {
        return querySingleDouble(MAX_SCORE, quizId, "getHighestScore(Quiz)");
    }

    /**
     * Returns the lowest score achieved across all completed attempts for the quiz.
     *
     * @param quizId the quiz id
     * @return lowest score, or {@code 0.0} if no completed attempts exist
     */
    @Override
    public double getLowestScore(int quizId) {
        return querySingleDouble(MIN_SCORE, quizId, "getLowestScore(Quiz)");
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Executes a single-column {@code DOUBLE} aggregate query parameterised by a quiz id.
     *
     * @param sql         the query string (one {@code ?} placeholder for the quiz id)
     * @param quizId      the quiz id
     * @param methodLabel label used in the exception message
     * @return the aggregate result, or {@code 0.0} if the result set is empty or NULL
     */
    private double querySingleDouble(String sql, int quizId, String methodLabel) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, quizId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double value = rs.getDouble(1);
                    return rs.wasNull() ? 0.0 : value;
                }
            }
            return 0.0;
        } catch (SQLException e) {
            throw new RuntimeException(methodLabel + " failed for quizId=" + quizId + ": " + e.getMessage(), e);
        }
    }

    /**
     * Maps the current row of the given {@link ResultSet} to a {@link Quiz} object.
     *
     * @param rs an open, positioned {@link ResultSet}
     * @return a populated {@link Quiz}
     * @throws SQLException if any column access fails
     */
    private Quiz mapResultSet(ResultSet rs) throws SQLException {
        Quiz quiz = new Quiz();
        quiz.setId(rs.getInt("id"));
        quiz.setTitle(rs.getString("title"));
        quiz.setDescription(rs.getString("description"));
        quiz.setCategoryId(rs.getInt("category_id"));
        String diffStr = rs.getString("difficulty");
        if (diffStr != null && !diffStr.isBlank()) {
            try {
                quiz.setDifficulty(Difficulty.valueOf(diffStr));
            } catch (IllegalArgumentException ex) {
                // leave null
            }
        }
        quiz.setTimeLimitMinutes(rs.getInt("time_limit_minutes"));
        quiz.setMaxAttempts(rs.getInt("max_attempts"));
        quiz.setScoringStrategy(rs.getString("scoring_strategy"));

        String statusStr = rs.getString("status");
        if (statusStr != null && !statusStr.isBlank()) {
            try {
                quiz.setStatus(QuizStatus.valueOf(statusStr));
            } catch (IllegalArgumentException ex) {
                quiz.setStatus(QuizStatus.DRAFT);
            }
        }

        quiz.setCreatedBy(rs.getInt("created_by"));
        parseAndSetDateTime(rs, "created_at", quiz);
        parseAndSetUpdatedAt(rs, "updated_at", quiz);
        quiz.setShuffleQuestions(rs.getInt("shuffle_questions") == 1);

        return quiz;
    }

    /** Parses the {@code created_at} column and sets it on the quiz if parseable. */
    private void parseAndSetDateTime(ResultSet rs, String column, Quiz quiz) throws SQLException {
        String raw = rs.getString(column);
        if (raw != null && !raw.isBlank()) {
            try {
                quiz.setCreatedAt(LocalDateTime.parse(raw));
            } catch (Exception ex) {
                // Leave null if unparseable
            }
        }
    }

    /** Parses the {@code updated_at} column and sets it on the quiz if parseable. */
    private void parseAndSetUpdatedAt(ResultSet rs, String column, Quiz quiz) throws SQLException {
        String raw = rs.getString(column);
        if (raw != null && !raw.isBlank()) {
            try {
                quiz.setUpdatedAt(LocalDateTime.parse(raw));
            } catch (Exception ex) {
                // Leave null if unparseable
            }
        }
    }
}
