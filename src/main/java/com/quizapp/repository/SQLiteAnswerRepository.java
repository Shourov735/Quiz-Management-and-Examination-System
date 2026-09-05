package com.quizapp.repository;

import com.quizapp.database.DatabaseConnection;
import com.quizapp.model.Answer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * SQLite-backed implementation of {@link AnswerRepository}.
 *
 * <p>Answers represent a student's evaluated response to a single question within
 * a quiz attempt. They are stored in the {@code answers} table with the columns:
 * {@code id}, {@code attempt_id}, {@code question_id}, {@code answer_value} (the
 * student's raw submission), {@code is_correct}, and {@code marks_awarded}.</p>
 *
 * <p>The {@link #saveOrUpdate} method uses SQLite's {@code INSERT OR REPLACE}
 * semantics, which requires a {@code UNIQUE} constraint on
 * {@code (attempt_id, question_id)} in the database schema.</p>
 *
 * <p>All resources are closed via try-with-resources. Every {@link SQLException}
 * is caught and re-thrown as a descriptive {@link RuntimeException}.</p>
 */
public class SQLiteAnswerRepository implements AnswerRepository {

    // -------------------------------------------------------------------------
    // SQL constants
    // -------------------------------------------------------------------------

    private static final String INSERT_ANSWER =
            "INSERT INTO answers (attempt_id, question_id, answer_value, is_correct, marks_awarded) " +
            "VALUES (?, ?, ?, ?, ?)";

    /**
     * Upsert using SQLite's INSERT OR REPLACE.
     * Requires a UNIQUE constraint on (attempt_id, question_id) in the schema.
     */
    private static final String UPSERT_ANSWER =
            "INSERT OR REPLACE INTO answers (attempt_id, question_id, answer_value, is_correct, marks_awarded) " +
            "VALUES (?, ?, ?, ?, ?)";

    private static final String SELECT_BASE =
            "SELECT id, attempt_id, question_id, answer_value, is_correct, marks_awarded FROM answers";

    private static final String SELECT_BY_ID =
            SELECT_BASE + " WHERE id = ?";

    private static final String SELECT_BY_ATTEMPT =
            SELECT_BASE + " WHERE attempt_id = ?";

    private static final String SELECT_BY_ATTEMPT_AND_QUESTION =
            SELECT_BASE + " WHERE attempt_id = ? AND question_id = ?";

    private static final String UPDATE_ANSWER =
            "UPDATE answers SET answer_value = ?, is_correct = ?, marks_awarded = ? WHERE id = ?";

    private static final String DELETE_BY_ATTEMPT =
            "DELETE FROM answers WHERE attempt_id = ?";

    private static final String COUNT_CORRECT =
            "SELECT COUNT(*) FROM answers WHERE attempt_id = ? AND is_correct = 1";

    private static final String COUNT_INCORRECT =
            "SELECT COUNT(*) FROM answers WHERE attempt_id = ? AND is_correct = 0";

    // -------------------------------------------------------------------------
    // AnswerRepository implementation
    // -------------------------------------------------------------------------

    /**
     * Inserts a new answer row and returns the auto-generated id.
     *
     * @param answer the answer to save; its {@code id} is updated upon return
     * @return the generated database id
     * @throws RuntimeException on SQL failure
     */
    @Override
    public int save(Answer answer) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_ANSWER, Statement.RETURN_GENERATED_KEYS)) {

            bindAnswerParams(ps, answer);

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new RuntimeException("save(Answer) - no rows inserted for attemptId="
                        + answer.getAttemptId() + ", questionId=" + answer.getQuestionId());
            }

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int generatedId = keys.getInt(1);
                    answer.setId(generatedId);
                    return generatedId;
                }
                throw new RuntimeException("save(Answer) - no generated key returned");
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                    "save(Answer) failed for attemptId=" + answer.getAttemptId()
                    + ", questionId=" + answer.getQuestionId() + ": " + e.getMessage(), e);
        }
    }

    /**
     * Inserts or replaces an answer using SQLite's {@code INSERT OR REPLACE} semantics.
     *
     * <p>This requires a {@code UNIQUE} constraint on {@code (attempt_id, question_id)}
     * in the {@code answers} table. If a row already exists for that pair, it is
     * deleted and re-inserted with the new values, resetting the {@code id}.</p>
     *
     * @param answer the answer to upsert; the returned id may differ from the original
     * @return the generated (or replacement) database id
     * @throws RuntimeException on SQL failure
     */
    @Override
    public int saveOrUpdate(Answer answer) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(UPSERT_ANSWER, Statement.RETURN_GENERATED_KEYS)) {

            bindAnswerParams(ps, answer);

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int generatedId = keys.getInt(1);
                    answer.setId(generatedId);
                    return generatedId;
                }
                // For a REPLACE that updated an existing row, the old id is still valid
                return answer.getId();
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                    "saveOrUpdate(Answer) failed for attemptId=" + answer.getAttemptId()
                    + ", questionId=" + answer.getQuestionId() + ": " + e.getMessage(), e);
        }
    }

    /**
     * Finds an answer by its primary key.
     *
     * @param id the answer id
     * @return an {@link Optional} containing the answer, or empty if not found
     */
    @Override
    public Optional<Answer> findById(int id) {
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
            throw new RuntimeException("findById(Answer) failed for id=" + id + ": " + e.getMessage(), e);
        }
    }

    /**
     * Returns all answers submitted within a given attempt.
     *
     * @param attemptId the attempt id
     * @return list of answers; empty list if none
     */
    @Override
    public List<Answer> findByAttemptId(int attemptId) {
        List<Answer> answers = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_ATTEMPT)) {

            ps.setInt(1, attemptId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    answers.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                    "findByAttemptId(Answer) failed for attemptId=" + attemptId + ": " + e.getMessage(), e);
        }
        return answers;
    }

    /**
     * Finds a student's answer to a specific question within a specific attempt.
     *
     * @param attemptId  the attempt id
     * @param questionId the question id
     * @return an {@link Optional} containing the answer, or empty if not found
     */
    @Override
    public Optional<Answer> findByAttemptAndQuestion(int attemptId, int questionId) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_ATTEMPT_AND_QUESTION)) {

            ps.setInt(1, attemptId);
            ps.setInt(2, questionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException(
                    "findByAttemptAndQuestion(Answer) failed for attemptId=" + attemptId
                    + ", questionId=" + questionId + ": " + e.getMessage(), e);
        }
    }

    /**
     * Updates the {@code answer_value}, {@code is_correct}, and {@code marks_awarded}
     * fields of an existing answer.
     *
     * @param answer the answer to update; must have a valid {@code id}
     * @return {@code true} if a row was updated
     */
    @Override
    public boolean update(Answer answer) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_ANSWER)) {

            ps.setString(1, answer.getSubmittedAnswer());
            ps.setInt(2, answer.isCorrect() ? 1 : 0);
            ps.setDouble(3, answer.getMarksAwarded());
            ps.setInt(4, answer.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(
                    "update(Answer) failed for id=" + answer.getId() + ": " + e.getMessage(), e);
        }
    }

    /**
     * Deletes all answer rows for the given attempt.
     *
     * @param attemptId the attempt id
     * @return {@code true} if at least one row was deleted
     */
    @Override
    public boolean deleteByAttemptId(int attemptId) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE_BY_ATTEMPT)) {

            ps.setInt(1, attemptId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(
                    "deleteByAttemptId(Answer) failed for attemptId=" + attemptId + ": " + e.getMessage(), e);
        }
    }

    /**
     * Counts the number of correct answers within the given attempt.
     *
     * @param attemptId the attempt id
     * @return count of correct answers
     */
    @Override
    public int countCorrect(int attemptId) {
        return countByAttempt(COUNT_CORRECT, attemptId, "countCorrect(Answer)");
    }

    /**
     * Counts the number of incorrect answers within the given attempt.
     *
     * @param attemptId the attempt id
     * @return count of incorrect answers
     */
    @Override
    public int countIncorrect(int attemptId) {
        return countByAttempt(COUNT_INCORRECT, attemptId, "countIncorrect(Answer)");
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Binds the standard five answer fields onto a prepared statement in positions 1–5.
     *
     * @param ps     the prepared statement to bind
     * @param answer the source answer values
     * @throws SQLException if binding fails
     */
    private void bindAnswerParams(PreparedStatement ps, Answer answer) throws SQLException {
        ps.setInt(1, answer.getAttemptId());
        ps.setInt(2, answer.getQuestionId());
        ps.setString(3, answer.getSubmittedAnswer());
        ps.setInt(4, answer.isCorrect() ? 1 : 0);
        ps.setDouble(5, answer.getMarksAwarded());
    }

    /**
     * Executes a COUNT query with a single attempt-id parameter.
     *
     * @param sql         the COUNT query
     * @param attemptId   the attempt id
     * @param methodLabel label for exception messages
     * @return the count result
     */
    private int countByAttempt(String sql, int attemptId, String methodLabel) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, attemptId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                    methodLabel + " failed for attemptId=" + attemptId + ": " + e.getMessage(), e);
        }
    }

    /**
     * Maps the current row of the given {@link ResultSet} to an {@link Answer} object.
     *
     * @param rs an open, positioned {@link ResultSet}
     * @return a populated {@link Answer}
     * @throws SQLException if any column access fails
     */
    private Answer mapResultSet(ResultSet rs) throws SQLException {
        Answer answer = new Answer();
        answer.setId(rs.getInt("id"));
        answer.setAttemptId(rs.getInt("attempt_id"));
        answer.setQuestionId(rs.getInt("question_id"));
        answer.setSubmittedAnswer(rs.getString("answer_value"));
        answer.setCorrect(rs.getInt("is_correct") == 1);
        answer.setMarksAwarded(rs.getDouble("marks_awarded"));
        return answer;
    }
}
