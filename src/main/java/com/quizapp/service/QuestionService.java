package com.quizapp.service;

import com.quizapp.model.Difficulty;
import com.quizapp.model.Question;
import com.quizapp.model.QuestionOption;
import com.quizapp.model.QuestionType;
import com.quizapp.model.QuizStatus;
import com.quizapp.repository.CategoryRepository;
import com.quizapp.repository.QuestionRepository;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing {@link Question} entities throughout their lifecycle.
 *
 * <p>Questions are created via the {@link com.quizapp.factory.QuestionFactory},
 * validated, then persisted through a {@link QuestionRepository}.  Validation
 * failures raise {@link IllegalArgumentException}; business-rule violations
 * raise {@link IllegalStateException}.</p>
 */
public class QuestionService {

    // -------------------------------------------------------------------------
    // Fields
    // -------------------------------------------------------------------------

    /** Persistence layer for Question entities. */
    private final QuestionRepository questionRepo;

    /** Persistence layer for Category entities (used to validate category ids). */
    private final CategoryRepository categoryRepo;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * Creates a {@code QuestionService} with the supplied repositories.
     *
     * @param questionRepo the question repository; must not be {@code null}
     * @param categoryRepo the category repository; must not be {@code null}
     */
    public QuestionService(QuestionRepository questionRepo, CategoryRepository categoryRepo) {
        if (questionRepo == null) {
            throw new IllegalArgumentException("QuestionRepository must not be null.");
        }
        if (categoryRepo == null) {
            throw new IllegalArgumentException("CategoryRepository must not be null.");
        }
        this.questionRepo = questionRepo;
        this.categoryRepo = categoryRepo;
    }

    // -------------------------------------------------------------------------
    // Create
    // -------------------------------------------------------------------------

    /**
     * Creates a new {@link Question} of the specified type and persists it.
     *
     * <p>The concrete question instance is obtained from the
     * {@link com.quizapp.factory.QuestionFactory}.  All field-level and
     * option-level validation is performed before the record is saved.</p>
     *
     * @param type        the question type; must not be {@code null}
     * @param text        the question text / prompt; must be non-blank
     * @param marks       marks awarded for a correct answer; must be &gt; 0
     * @param difficulty  the difficulty level; must not be {@code null}
     * @param categoryId  the owning category id (0 to leave uncategorised)
     * @param createdBy   the teacher user id who is creating this question
     * @param options     the list of answer options (required for MCQ / MULTIPLE_ANSWER)
     * @return the persisted {@link Question} with its generated id set
     * @throws IllegalArgumentException if any validation rule is violated
     */
    public Question createQuestion(QuestionType type, String text, double marks,
                                   Difficulty difficulty, int categoryId, int createdBy,
                                   List<QuestionOption> options) {
        if (type == null) {
            throw new IllegalArgumentException("Question type must not be null.");
        }
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Question text must not be blank.");
        }
        if (marks <= 0) {
            throw new IllegalArgumentException("Marks must be greater than zero.");
        }
        if (difficulty == null) {
            throw new IllegalArgumentException("Difficulty must not be null.");
        }

        // Build the concrete question via the Factory
        Question question = com.quizapp.factory.QuestionFactory.createQuestion(type);
        question.setQuestionText(text.trim());
        question.setMarks(marks);
        question.setOptions(options != null ? options : List.of());

        // Set category/creator metadata via reflection-safe setters that concrete
        // Question subclasses are expected to expose (they inherit from Question's
        // state fields via the factory-produced subclass).
        setCategoryId(question, categoryId);
        setCreatedBy(question, createdBy);
        setDifficulty(question, difficulty);

        // Validate completeness before persisting
        validateQuestion(question);

        int generatedId = questionRepo.save(question);
        question.setId(generatedId);
        return question;
    }

    // -------------------------------------------------------------------------
    // Read
    // -------------------------------------------------------------------------

    /**
     * Finds a question by its primary key.
     *
     * @param id the question identifier
     * @return an {@link Optional} containing the question, or empty if not found
     */
    public Optional<Question> findById(int id) {
        return questionRepo.findById(id);
    }

    /**
     * Returns all persisted questions.
     *
     * @return list of all questions; never {@code null}
     */
    public List<Question> findAll() {
        return questionRepo.findAll();
    }

    /**
     * Returns all questions of a given type.
     *
     * @param type the question type to filter by; must not be {@code null}
     * @return matching questions; never {@code null}
     */
    public List<Question> findByType(QuestionType type) {
        if (type == null) {
            throw new IllegalArgumentException("QuestionType must not be null.");
        }
        return questionRepo.findByType(type);
    }

    /**
     * Returns all questions created by a given teacher.
     *
     * @param teacherId the teacher user id
     * @return questions created by that teacher; never {@code null}
     */
    public List<Question> findByTeacher(int teacherId) {
        return questionRepo.findByCreatedBy(teacherId);
    }

    /**
     * Searches for questions using optional filters.
     *
     * @param keyword    text to search in question body (pass {@code null} or blank to skip)
     * @param type       question type filter (pass {@code null} to skip)
     * @param categoryId category id filter (pass {@code 0} to skip)
     * @param difficulty difficulty name filter (pass {@code null} or blank to skip)
     * @return matching questions; never {@code null}
     */
    public List<Question> search(String keyword, QuestionType type,
                                 int categoryId, String difficulty) {
        return questionRepo.search(keyword, type, categoryId, difficulty);
    }

    // -------------------------------------------------------------------------
    // Update
    // -------------------------------------------------------------------------

    /**
     * Updates an existing {@link Question}.
     *
     * <p>Full validation is performed before the repository is called.</p>
     *
     * @param question the question to update; must not be {@code null}
     * @return the updated question
     * @throws IllegalArgumentException if validation fails
     * @throws IllegalStateException    if the question does not exist in the repository
     */
    public Question updateQuestion(Question question) {
        if (question == null) {
            throw new IllegalArgumentException("Question must not be null.");
        }

        if (questionRepo.findById(question.getId()).isEmpty()) {
            throw new IllegalStateException(
                    "Question with id " + question.getId() + " does not exist.");
        }

        validateQuestion(question);

        boolean updated = questionRepo.update(question);
        if (!updated) {
            throw new IllegalStateException(
                    "Failed to update question with id " + question.getId() + ".");
        }
        return question;
    }

    // -------------------------------------------------------------------------
    // Delete
    // -------------------------------------------------------------------------

    /**
     * Deletes a question by its primary key.
     *
     * <p>A question may not be deleted if it currently belongs to a
     * {@link QuizStatus#ACTIVE} or {@link QuizStatus#PUBLISHED} quiz.</p>
     *
     * @param id the question identifier
     * @return {@code true} if deleted, {@code false} if not found
     * @throws IllegalStateException if the question is attached to active quizzes
     */
    public boolean deleteQuestion(int id) {
        // Delegate the guard to the repository; the SQLite implementation will
        // check the quiz_questions join table and reject if the question is in
        // an ACTIVE/PUBLISHED quiz. We propagate any IllegalStateException raised
        // by the repository.
        return questionRepo.delete(id);
    }

    // -------------------------------------------------------------------------
    // Validation
    // -------------------------------------------------------------------------

    /**
     * Validates that a {@link Question} is fully and correctly formed.
     *
     * <p>Rules:
     * <ul>
     *   <li>Text must be non-blank.</li>
     *   <li>Marks must be &gt; 0.</li>
     *   <li>MCQ and MULTIPLE_ANSWER questions must have at least one correct option.</li>
     *   <li>TRUE_FALSE questions must have exactly two options (one correct, one not).</li>
     * </ul>
     * </p>
     *
     * @param question the question to validate; must not be {@code null}
     * @throws IllegalArgumentException if any rule is violated
     */
    public void validateQuestion(Question question) {
        if (question == null) {
            throw new IllegalArgumentException("Question must not be null.");
        }
        if (question.getQuestionText() == null || question.getQuestionText().isBlank()) {
            throw new IllegalArgumentException("Question text must not be blank.");
        }
        if (question.getMarks() <= 0) {
            throw new IllegalArgumentException("Question marks must be greater than zero.");
        }

        QuestionType type = question.getQuestionType();

        if (type == QuestionType.MCQ || type == QuestionType.MULTIPLE_ANSWER) {
            if (question.getOptions() == null || question.getOptions().isEmpty()) {
                throw new IllegalArgumentException(
                        type + " questions must have at least one option.");
            }
            long correctCount = question.getOptions().stream()
                    .filter(QuestionOption::isCorrect)
                    .count();
            if (correctCount < 1) {
                throw new IllegalArgumentException(
                        type + " questions must have at least one correct option.");
            }
            if (type == QuestionType.MCQ && correctCount > 1) {
                throw new IllegalArgumentException(
                        "MCQ questions must have exactly one correct option.");
            }
        }

        if (type == QuestionType.TRUE_FALSE) {
            if (question.getOptions() == null || question.getOptions().size() != 2) {
                throw new IllegalArgumentException(
                        "True/False questions must have exactly two options.");
            }
            long correctCount = question.getOptions().stream()
                    .filter(QuestionOption::isCorrect)
                    .count();
            if (correctCount != 1) {
                throw new IllegalArgumentException(
                        "True/False questions must have exactly one correct option.");
            }
        }

        if (type == QuestionType.FILL_BLANK) {
            if (question.getOptions() == null || question.getOptions().isEmpty()) {
                throw new IllegalArgumentException(
                        "Fill-in-the-blank questions must have at least one accepted answer option.");
            }
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers — reflective-free field population
    // -------------------------------------------------------------------------

    /**
     * Sets the {@code categoryId} on the question if the concrete subclass exposes
     * a {@code setCategoryId(int)} method, otherwise silently ignores.
     */
    private void setCategoryId(Question question, int categoryId) {
        try {
            java.lang.reflect.Method m = question.getClass().getMethod("setCategoryId", int.class);
            m.invoke(question, categoryId);
        } catch (NoSuchMethodException ignored) {
            // Optional field; concrete class may not expose it
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Could not set categoryId on question.", e);
        }
    }

    /**
     * Sets the {@code createdBy} user id on the question if the concrete subclass
     * exposes a {@code setCreatedBy(int)} method.
     */
    private void setCreatedBy(Question question, int createdBy) {
        try {
            java.lang.reflect.Method m = question.getClass().getMethod("setCreatedBy", int.class);
            m.invoke(question, createdBy);
        } catch (NoSuchMethodException ignored) {
            // Optional field
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Could not set createdBy on question.", e);
        }
    }

    /**
     * Sets the {@code difficulty} on the question if the concrete subclass exposes
     * a {@code setDifficulty(Difficulty)} method.
     */
    private void setDifficulty(Question question, Difficulty difficulty) {
        try {
            java.lang.reflect.Method m = question.getClass()
                    .getMethod("setDifficulty", Difficulty.class);
            m.invoke(question, difficulty);
        } catch (NoSuchMethodException ignored) {
            // Optional field
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Could not set difficulty on question.", e);
        }
    }
}
