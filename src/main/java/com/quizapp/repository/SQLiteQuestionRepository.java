package com.quizapp.repository;

import com.quizapp.database.DatabaseConnection;
import com.quizapp.factory.QuestionFactory;
import com.quizapp.model.Question;
import com.quizapp.model.QuestionOption;
import com.quizapp.model.QuestionType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * SQLite-backed implementation of {@link QuestionRepository}.
 *
 * <p>Questions are stored in the {@code questions} table; their selectable options
 * are stored in {@code question_options}. The many-to-many mapping between quizzes
 * and questions is managed through the {@code quiz_questions} join table, which
 * carries an explicit {@code question_order} column.</p>
 *
 * <p>All question rows are hydrated through {@link QuestionFactory#createFromResultSet(ResultSet)},
 * which instantiates the correct concrete subclass ({@code MCQQuestion},
 * {@code TrueFalseQuestion}, etc.) based on the {@code question_type} discriminator.</p>
 *
 * <p>Every method catches {@link SQLException} and re-throws it as a
 * {@link RuntimeException} with a descriptive message.</p>
 */
public class SQLiteQuestionRepository implements QuestionRepository {

    // -------------------------------------------------------------------------
    // SQL constants — questions table
    // -------------------------------------------------------------------------

    private static final String INSERT_QUESTION =
            "INSERT INTO questions (question_text, question_type, marks, display_order, quiz_id) " +
            "VALUES (?, ?, ?, ?, ?)";

    private static final String SELECT_QUESTION_BASE =
            "SELECT id, question_text, question_type, marks, display_order, quiz_id FROM questions";

    private static final String SELECT_BY_ID =
            SELECT_QUESTION_BASE + " WHERE id = ?";

    private static final String SELECT_ALL =
            SELECT_QUESTION_BASE;

    private static final String SELECT_BY_TYPE =
            SELECT_QUESTION_BASE + " WHERE question_type = ?";

    private static final String SELECT_BY_CATEGORY =
            SELECT_QUESTION_BASE + " WHERE category_id = ?";

    private static final String SELECT_BY_CREATED_BY =
            SELECT_QUESTION_BASE + " WHERE created_by = ?";

    private static final String SELECT_BY_QUIZ =
            "SELECT q.id, q.question_text, q.question_type, q.marks, q.display_order, q.quiz_id " +
            "FROM questions q " +
            "JOIN quiz_questions qq ON qq.question_id = q.id " +
            "WHERE qq.quiz_id = ? " +
            "ORDER BY qq.question_order ASC";

    private static final String UPDATE_QUESTION =
            "UPDATE questions SET question_text = ?, question_type = ?, marks = ?, display_order = ? WHERE id = ?";

    private static final String DELETE_QUESTION =
            "DELETE FROM questions WHERE id = ?";

    // -------------------------------------------------------------------------
    // SQL constants — question_options table
    // -------------------------------------------------------------------------

    private static final String INSERT_OPTION =
            "INSERT INTO question_options (question_id, option_text, is_correct, display_order) VALUES (?, ?, ?, ?)";

    private static final String SELECT_OPTIONS =
            "SELECT id, question_id, option_text, is_correct, display_order FROM question_options " +
            "WHERE question_id = ? ORDER BY display_order ASC";

    private static final String DELETE_OPTIONS_FOR_QUESTION =
            "DELETE FROM question_options WHERE question_id = ?";

    // -------------------------------------------------------------------------
    // SQL constants — quiz_questions join table
    // -------------------------------------------------------------------------

    private static final String INSERT_QUIZ_QUESTION =
            "INSERT INTO quiz_questions (quiz_id, question_id, question_order) VALUES (?, ?, ?)";

    private static final String DELETE_QUIZ_QUESTION =
            "DELETE FROM quiz_questions WHERE quiz_id = ? AND question_id = ?";

    private static final String MAX_ORDER_FOR_QUIZ =
            "SELECT MAX(question_order) FROM quiz_questions WHERE quiz_id = ?";

    // -------------------------------------------------------------------------
    // QuestionRepository implementation
    // -------------------------------------------------------------------------

    /**
     * Persists a new question together with all of its options.
     *
     * <p>The operation is <em>not</em> wrapped in a transaction by this method; if
     * the caller requires atomicity it should manage the connection-level transaction
     * itself.</p>
     *
     * @param question the question to save; its {@code id} is updated upon return
     * @return the auto-generated question id
     * @throws RuntimeException on any SQL failure
     */
    @Override
    public int save(Question question) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_QUESTION, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, question.getQuestionText());
            ps.setString(2, question.getQuestionType() != null ? question.getQuestionType().name() : null);
            ps.setDouble(3, question.getMarks());
            ps.setInt(4, question.getDisplayOrder());
            if (question.getQuizId() > 0) {
                ps.setInt(5, question.getQuizId());
            } else {
                ps.setNull(5, Types.INTEGER);
            }

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new RuntimeException("save(Question) - no rows inserted");
            }

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int generatedId = keys.getInt(1);
                    question.setId(generatedId);
                    saveOptions(conn, generatedId, question.getOptions());
                    return generatedId;
                }
                throw new RuntimeException("save(Question) - no generated key returned");
            }
        } catch (SQLException e) {
            throw new RuntimeException("save(Question) failed: " + e.getMessage(), e);
        }
    }

    /**
     * Finds a question by primary key and loads its options.
     *
     * @param id the question id
     * @return an {@link Optional} containing the question, or empty if not found
     */
    @Override
    public Optional<Question> findById(int id) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Question q = QuestionFactory.createFromResultSet(rs);
                    loadOptions(conn, q);
                    return Optional.of(q);
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("findById(Question) failed for id=" + id + ": " + e.getMessage(), e);
        }
    }

    /**
     * Returns all questions with their options loaded.
     *
     * @return list of all questions; empty list if none
     */
    @Override
    public List<Question> findAll() {
        return queryList(SELECT_ALL, null, "findAll(Question)");
    }

    /**
     * Returns all questions of the given type.
     *
     * @param type the question type discriminator
     * @return matching questions; empty list if none
     */
    @Override
    public List<Question> findByType(QuestionType type) {
        return queryList(SELECT_BY_TYPE, type.name(), "findByType(Question)");
    }

    /**
     * Returns all questions associated with the given category.
     *
     * <p>Requires a {@code category_id} column in the {@code questions} table.</p>
     *
     * @param categoryId the category id
     * @return matching questions; empty list if none
     */
    @Override
    public List<Question> findByCategoryId(int categoryId) {
        List<Question> questions = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_CATEGORY)) {

            ps.setInt(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Question q = QuestionFactory.createFromResultSet(rs);
                    loadOptions(conn, q);
                    questions.add(q);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                    "findByCategoryId(Question) failed for categoryId=" + categoryId + ": " + e.getMessage(), e);
        }
        return questions;
    }

    /**
     * Returns all questions created by the given teacher.
     *
     * <p>Requires a {@code created_by} column in the {@code questions} table.</p>
     *
     * @param teacherId the teacher's user id
     * @return matching questions; empty list if none
     */
    @Override
    public List<Question> findByCreatedBy(int teacherId) {
        List<Question> questions = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_CREATED_BY)) {

            ps.setInt(1, teacherId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Question q = QuestionFactory.createFromResultSet(rs);
                    loadOptions(conn, q);
                    questions.add(q);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                    "findByCreatedBy(Question) failed for teacherId=" + teacherId + ": " + e.getMessage(), e);
        }
        return questions;
    }

    /**
     * Searches questions using optional filters combined with AND.
     *
     * <p>All parameters are optional:
     * <ul>
     *   <li>{@code textKeyword} — null/blank to skip; matched with {@code LIKE %keyword%}</li>
     *   <li>{@code type} — null to skip</li>
     *   <li>{@code categoryId} — 0 to skip</li>
     *   <li>{@code difficulty} — null/blank to skip</li>
     * </ul>
     * A parameterised {@link PreparedStatement} is built dynamically.</p>
     *
     * @param textKeyword case-insensitive substring matched against question text
     * @param type        question type filter, or null
     * @param categoryId  category filter, or 0
     * @param difficulty  difficulty filter, or null/blank
     * @return matching questions with options loaded; empty list if none
     */
    @Override
    public List<Question> search(String textKeyword, QuestionType type, int categoryId, String difficulty) {
        List<Question> questions = new ArrayList<>();
        StringBuilder sql = new StringBuilder(SELECT_QUESTION_BASE + " WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (textKeyword != null && !textKeyword.isBlank()) {
            sql.append(" AND question_text LIKE ?");
            params.add("%" + textKeyword + "%");
        }
        if (type != null) {
            sql.append(" AND question_type = ?");
            params.add(type.name());
        }
        if (categoryId > 0) {
            sql.append(" AND category_id = ?");
            params.add(categoryId);
        }
        if (difficulty != null && !difficulty.isBlank()) {
            sql.append(" AND difficulty = ?");
            params.add(difficulty);
        }

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                Object p = params.get(i);
                if (p instanceof String s) {
                    ps.setString(i + 1, s);
                } else if (p instanceof Integer iv) {
                    ps.setInt(i + 1, iv);
                } else {
                    ps.setObject(i + 1, p);
                }
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Question q = QuestionFactory.createFromResultSet(rs);
                    loadOptions(conn, q);
                    questions.add(q);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("search(Question) failed: " + e.getMessage(), e);
        }
        return questions;
    }

    /**
     * Returns all questions belonging to a quiz, ordered by their {@code question_order}
     * in the {@code quiz_questions} join table.
     *
     * @param quizId the quiz id
     * @return ordered list of questions with options; empty list if none
     */
    @Override
    public List<Question> findByQuizId(int quizId) {
        List<Question> questions = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_QUIZ)) {

            ps.setInt(1, quizId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Question q = QuestionFactory.createFromResultSet(rs);
                    loadOptions(conn, q);
                    questions.add(q);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                    "findByQuizId(Question) failed for quizId=" + quizId + ": " + e.getMessage(), e);
        }
        return questions;
    }

    /**
     * Updates the question's text, type, marks, and display order, then replaces
     * all its existing options with the current option list from the {@link Question}.
     *
     * @param question the question to update; must have a valid {@code id}
     * @return {@code true} if the question row was updated
     */
    @Override
    public boolean update(Question question) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {

            boolean updated;
            try (PreparedStatement ps = conn.prepareStatement(UPDATE_QUESTION)) {
                ps.setString(1, question.getQuestionText());
                ps.setString(2, question.getQuestionType() != null ? question.getQuestionType().name() : null);
                ps.setDouble(3, question.getMarks());
                ps.setInt(4, question.getDisplayOrder());
                ps.setInt(5, question.getId());
                updated = ps.executeUpdate() > 0;
            }

            // Refresh options: delete all existing, then re-insert
            try (PreparedStatement del = conn.prepareStatement(DELETE_OPTIONS_FOR_QUESTION)) {
                del.setInt(1, question.getId());
                del.executeUpdate();
            }
            saveOptions(conn, question.getId(), question.getOptions());

            return updated;
        } catch (SQLException e) {
            throw new RuntimeException(
                    "update(Question) failed for id=" + question.getId() + ": " + e.getMessage(), e);
        }
    }

    /**
     * Deletes the question with the given id.
     *
     * <p>Options and quiz-question mappings are expected to be removed by
     * ON DELETE CASCADE constraints defined in the schema.</p>
     *
     * @param id the question id
     * @return {@code true} if a row was deleted
     */
    @Override
    public boolean delete(int id) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE_QUESTION)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("delete(Question) failed for id=" + id + ": " + e.getMessage(), e);
        }
    }

    /**
     * Adds a question to a quiz with the specified order position.
     *
     * @param quizId     the quiz id
     * @param questionId the question id
     * @param order      the ordering position within the quiz
     * @return {@code true} if the join row was inserted
     */
    @Override
    public boolean addToQuiz(int quizId, int questionId, int order) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_QUIZ_QUESTION)) {

            ps.setInt(1, quizId);
            ps.setInt(2, questionId);
            ps.setInt(3, order);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(
                    "addToQuiz(Question) failed for quizId=" + quizId + ", questionId=" + questionId
                    + ": " + e.getMessage(), e);
        }
    }

    /**
     * Removes the question-to-quiz mapping for the given quiz and question.
     *
     * @param quizId     the quiz id
     * @param questionId the question id
     * @return {@code true} if a row was deleted
     */
    @Override
    public boolean removeFromQuiz(int quizId, int questionId) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE_QUIZ_QUESTION)) {

            ps.setInt(1, quizId);
            ps.setInt(2, questionId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(
                    "removeFromQuiz(Question) failed for quizId=" + quizId + ", questionId=" + questionId
                    + ": " + e.getMessage(), e);
        }
    }

    /**
     * Returns the next available order position for a question within the specified quiz.
     *
     * <p>Returns {@code MAX(question_order) + 1}, or {@code 1} if the quiz has no
     * questions yet.</p>
     *
     * @param quizId the quiz id
     * @return the next order value
     */
    @Override
    public int getNextOrderForQuiz(int quizId) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(MAX_ORDER_FOR_QUIZ)) {

            ps.setInt(1, quizId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int max = rs.getInt(1);
                    return rs.wasNull() ? 1 : max + 1;
                }
            }
            return 1;
        } catch (SQLException e) {
            throw new RuntimeException(
                    "getNextOrderForQuiz(Question) failed for quizId=" + quizId + ": " + e.getMessage(), e);
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Loads and attaches the {@link QuestionOption}s for the given question.
     *
     * @param conn the active JDBC connection (reused to avoid opening a second connection)
     * @param q    the question to populate
     * @throws SQLException if the option query fails
     */
    private void loadOptions(Connection conn, Question q) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SELECT_OPTIONS)) {
            ps.setInt(1, q.getId());
            try (ResultSet rs = ps.executeQuery()) {
                List<QuestionOption> options = new ArrayList<>();
                while (rs.next()) {
                    QuestionOption opt = new QuestionOption();
                    opt.setId(rs.getInt("id"));
                    opt.setQuestionId(rs.getInt("question_id"));
                    opt.setOptionText(rs.getString("option_text"));
                    opt.setCorrect(rs.getInt("is_correct") == 1);
                    opt.setDisplayOrder(rs.getInt("display_order"));
                    options.add(opt);
                }
                q.setOptions(options);
            }
        }
    }

    /**
     * Inserts a list of {@link QuestionOption}s linked to the given question id.
     *
     * @param conn       the active JDBC connection
     * @param questionId the parent question id
     * @param options    the options to insert; may be null or empty
     * @throws SQLException if any insert fails
     */
    private void saveOptions(Connection conn, int questionId, List<QuestionOption> options) throws SQLException {
        if (options == null || options.isEmpty()) {
            return;
        }
        try (PreparedStatement ps = conn.prepareStatement(INSERT_OPTION, Statement.RETURN_GENERATED_KEYS)) {
            for (QuestionOption opt : options) {
                ps.setInt(1, questionId);
                ps.setString(2, opt.getOptionText());
                ps.setInt(3, opt.isCorrect() ? 1 : 0);
                ps.setInt(4, opt.getDisplayOrder());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    /**
     * Executes the given SQL query with a single optional string parameter, loads
     * options for each row, and returns the result as a list.
     *
     * @param sql         the SELECT statement (0 or 1 {@code ?} parameter)
     * @param param       the string parameter value, or {@code null} for no parameter
     * @param methodLabel label used in the exception message
     * @return populated list of questions
     */
    private List<Question> queryList(String sql, String param, String methodLabel) {
        List<Question> questions = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (param != null) {
                ps.setString(1, param);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Question q = QuestionFactory.createFromResultSet(rs);
                    loadOptions(conn, q);
                    questions.add(q);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(methodLabel + " failed: " + e.getMessage(), e);
        }
        return questions;
    }
}
