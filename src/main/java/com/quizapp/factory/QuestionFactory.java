package com.quizapp.factory;

import com.quizapp.model.Question;
import com.quizapp.model.QuestionOption;
import com.quizapp.model.QuestionType;
import com.quizapp.question.FillBlankQuestion;
import com.quizapp.question.MCQQuestion;
import com.quizapp.question.MultipleAnswerQuestion;
import com.quizapp.question.TrueFalseQuestion;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Factory class responsible for creating {@link Question} instances.
 * <p>
 * This implements the Factory Method pattern by centralising the construction
 * logic for every question type.  Callers never need to {@code new} a concrete
 * question class directly.
 * </p>
 *
 * <p>Three factory entry points are provided:</p>
 * <ul>
 *   <li>{@link #createQuestion(QuestionType)} – blank instance by type</li>
 *   <li>{@link #createFromResultSet(ResultSet)} – populate from a JDBC row</li>
 *   <li>{@link #loadOptions(Question, List)} – attach options to any question</li>
 * </ul>
 */
public class QuestionFactory {

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /** Private constructor — utility / factory class. */
    private QuestionFactory() {}

    // -------------------------------------------------------------------------
    // Factory methods
    // -------------------------------------------------------------------------

    /**
     * Creates a new, blank {@link Question} instance of the requested type.
     * <p>
     * The returned object has its {@code questionType} field set but no other
     * fields populated.  The caller is responsible for setting text, marks,
     * options, etc.
     * </p>
     *
     * @param type the {@link QuestionType} to create
     * @return a concrete {@link Question} subclass instance
     * @throws IllegalArgumentException if {@code type} is {@code null}
     */
    public static Question createQuestion(QuestionType type) {
        if (type == null) {
            throw new IllegalArgumentException("QuestionType must not be null");
        }
        return switch (type) {
            case MCQ             -> new MCQQuestion();
            case TRUE_FALSE      -> new TrueFalseQuestion();
            case FILL_BLANK      -> new FillBlankQuestion();
            case MULTIPLE_ANSWER -> new MultipleAnswerQuestion();
        };
    }

    /**
     * Creates and populates a {@link Question} from a JDBC {@link ResultSet} row.
     * <p>
     * The cursor is assumed to already be positioned on the row to read
     * (i.e. {@code rs.next()} has been called by the caller).
     * </p>
     *
     * <p>Expected columns in the result set:</p>
     * <ul>
     *   <li>{@code id}            – INT</li>
     *   <li>{@code quiz_id}       – INT</li>
     *   <li>{@code question_text} – TEXT</li>
     *   <li>{@code question_type} – TEXT (matches {@link QuestionType} name)</li>
     *   <li>{@code marks}         – REAL</li>
     *   <li>{@code display_order} – INT</li>
     *   <li>{@code correct_answer}– TEXT (only used for FILL_BLANK; may be null)</li>
     * </ul>
     *
     * @param rs the positioned {@link ResultSet}
     * @return a populated concrete {@link Question} subclass instance
     * @throws SQLException             if a column cannot be read
     * @throws IllegalArgumentException if the {@code question_type} value is unknown
     */
    public static Question createFromResultSet(ResultSet rs) throws SQLException {
        String typeStr = rs.getString("question_type");
        QuestionType type = QuestionType.valueOf(typeStr.toUpperCase());

        Question question = createQuestion(type);
        question.setId(rs.getInt("id"));
        question.setQuestionText(rs.getString("question_text"));
        question.setMarks(rs.getDouble("marks"));

        try {
            String diff = rs.getString("difficulty");
            if (diff != null && !diff.isBlank()) {
                question.setDifficulty(com.quizapp.model.Difficulty.valueOf(diff.toUpperCase()));
            }
        } catch (Exception ignored) {}

        try {
            question.setCategoryId(rs.getInt("category_id"));
        } catch (SQLException ignored) {}

        try {
            question.setCreatedBy(rs.getInt("created_by"));
        } catch (SQLException ignored) {}

        try {
            question.setQuizId(rs.getInt("quiz_id"));
        } catch (SQLException ignored) {}

        try {
            question.setDisplayOrder(rs.getInt("question_order"));
        } catch (SQLException ignored) {}

        try {
            question.setDisplayOrder(rs.getInt("display_order"));
        } catch (SQLException ignored) {}

        if (type == QuestionType.FILL_BLANK) {
            try {
                String correctAnswer = rs.getString("correct_answer");
                if (correctAnswer != null) {
                    ((FillBlankQuestion) question).setCorrectAnswer(correctAnswer);
                }
            } catch (SQLException ignored) {}
        }

        return question;
    }

    /**
     * Attaches a list of {@link QuestionOption}s to the given question.
     * <p>
     * Replaces any previously set options. This helper is called after the
     * options have been loaded from the database (typically in a separate query).
     * </p>
     *
     * @param question the question to load options into; must not be {@code null}
     * @param options  the list of options to attach; may be empty but not {@code null}
     * @throws IllegalArgumentException if {@code question} or {@code options} is {@code null}
     */
    public static void loadOptions(Question question, List<QuestionOption> options) {
        if (question == null) {
            throw new IllegalArgumentException("Question must not be null");
        }
        if (options == null) {
            throw new IllegalArgumentException("Options list must not be null");
        }
        question.setOptions(options);
        if (question instanceof FillBlankQuestion fb) {
            for (QuestionOption opt : options) {
                if (opt.isCorrect()) {
                    fb.setCorrectAnswer(opt.getOptionText());
                    break;
                }
            }
        }
    }
}
