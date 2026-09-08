package com.quizapp.service;

import com.quizapp.model.Difficulty;
import com.quizapp.model.Question;
import com.quizapp.model.Quiz;
import com.quizapp.model.QuizStatus;
import com.quizapp.repository.CategoryRepository;
import com.quizapp.repository.QuestionRepository;
import com.quizapp.repository.QuizRepository;
import com.quizapp.state.QuizStateManager;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for creating, managing, and querying {@link Quiz} entities.
 *
 * <p>Orchestrates the quiz lifecycle (DRAFT → PUBLISHED → ACTIVE → COMPLETED →
 * ARCHIVED) via the {@link QuizStateManager} State-pattern implementation.
 * Question membership and reporting queries are also provided here.</p>
 *
 * <p>Validation failures raise {@link IllegalArgumentException};
 * lifecycle / business-rule violations raise {@link IllegalStateException}.</p>
 */
public class QuizService {

    // -------------------------------------------------------------------------
    // Fields
    // -------------------------------------------------------------------------

    /** Persistence layer for Quiz entities. */
    private final QuizRepository quizRepo;

    /** Persistence layer for Question entities (used to attach questions to quizzes). */
    private final QuestionRepository questionRepo;

    /** Persistence layer for Category entities (used to validate category ids). */
    private final CategoryRepository categoryRepo;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * Creates a {@code QuizService} with the supplied repositories.
     *
     * @param quizRepo     quiz persistence; must not be {@code null}
     * @param questionRepo question persistence; must not be {@code null}
     * @param categoryRepo category persistence; must not be {@code null}
     */
    public QuizService(QuizRepository quizRepo,
                       QuestionRepository questionRepo,
                       CategoryRepository categoryRepo) {
        if (quizRepo == null) {
            throw new IllegalArgumentException("QuizRepository must not be null.");
        }
        if (questionRepo == null) {
            throw new IllegalArgumentException("QuestionRepository must not be null.");
        }
        if (categoryRepo == null) {
            throw new IllegalArgumentException("CategoryRepository must not be null.");
        }
        this.quizRepo = quizRepo;
        this.questionRepo = questionRepo;
        this.categoryRepo = categoryRepo;
    }

    // -------------------------------------------------------------------------
    // Create
    // -------------------------------------------------------------------------

    /**
     * Creates a new {@link Quiz} in {@link QuizStatus#DRAFT} status and persists it.
     *
     * @param title            non-blank quiz title
     * @param description      optional description
     * @param categoryId       owning category (0 to leave uncategorised)
     * @param difficulty       overall difficulty; must not be {@code null}
     * @param timeLimitMinutes time limit in minutes (0 for untimed)
     * @param maxAttempts      maximum attempts allowed per student (0 for unlimited)
     * @param scoringStrategy  scoring strategy name (e.g. {@code "STANDARD"})
     * @param createdBy        teacher user id
     * @return persisted {@link Quiz} with generated id
     * @throws IllegalArgumentException if title is blank
     */
    public Quiz createQuiz(String title, String description, int categoryId,
                           Difficulty difficulty, int timeLimitMinutes,
                           int maxAttempts, String scoringStrategy, int createdBy) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Quiz title must not be blank.");
        }
        if (difficulty == null) {
            throw new IllegalArgumentException("Difficulty must not be null.");
        }

        Quiz quiz = new Quiz();
        quiz.setTitle(title.trim());
        quiz.setDescription(description != null ? description.trim() : "");
        quiz.setCategoryId(categoryId);
        quiz.setDifficulty(difficulty);
        quiz.setTimeLimitSeconds(timeLimitMinutes * 60);
        quiz.setMaxAttempts(maxAttempts);
        quiz.setScoringStrategy(scoringStrategy != null ? scoringStrategy : "STANDARD");
        quiz.setCreatedBy(createdBy);
        quiz.setStatus(QuizStatus.DRAFT);
        quiz.setCreatedAt(LocalDateTime.now());
        quiz.setUpdatedAt(LocalDateTime.now());

        int generatedId = quizRepo.save(quiz);
        quiz.setId(generatedId);
        return quiz;
    }

    // -------------------------------------------------------------------------
    // Read
    // -------------------------------------------------------------------------

    /**
     * Finds a quiz by primary key.
     *
     * @param id the quiz identifier
     * @return an {@link Optional} containing the quiz, or empty if not found
     */
    public Optional<Quiz> findById(int id) {
        return quizRepo.findById(id);
    }

    /**
     * Returns all persisted quizzes.
     *
     * @return list of all quizzes; never {@code null}
     */
    public List<Quiz> findAll() {
        return quizRepo.findAll();
    }

    /**
     * Returns all quizzes with a given status.
     *
     * @param status the status to filter by; must not be {@code null}
     * @return matching quizzes; never {@code null}
     */
    public List<Quiz> findByStatus(QuizStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("QuizStatus must not be null.");
        }
        return quizRepo.findByStatus(status);
    }

    /**
     * Returns all quizzes created by a given teacher.
     *
     * @param teacherId teacher user id
     * @return quizzes created by that teacher; never {@code null}
     */
    public List<Quiz> findByTeacher(int teacherId) {
        return quizRepo.findByCreatedBy(teacherId);
    }

    /**
     * Searches quizzes using optional filters.
     *
     * @param keyword    title keyword (pass {@code null} or blank to skip)
     * @param categoryId category filter (pass {@code 0} to skip)
     * @param difficulty difficulty name (pass {@code null} or blank to skip)
     * @param status     status filter (pass {@code null} to skip)
     * @return matching quizzes; never {@code null}
     */
    public List<Quiz> searchQuizzes(String keyword, int categoryId,
                                    String difficulty, QuizStatus status) {
        return quizRepo.search(keyword, categoryId, difficulty, status);
    }

    // -------------------------------------------------------------------------
    // Update / Delete
    // -------------------------------------------------------------------------

    /**
     * Updates an existing quiz.
     *
     * <p>Only quizzes in {@link QuizStatus#DRAFT} state may be freely edited;
     * other states may be restricted by the repository or state manager.</p>
     *
     * @param quiz the quiz to update; must not be {@code null} and must have a valid id
     * @return the updated quiz
     * @throws IllegalArgumentException if validation fails
     * @throws IllegalStateException    if the quiz is not in an editable state or does not exist
     */
    public Quiz updateQuiz(Quiz quiz) {
        if (quiz == null) {
            throw new IllegalArgumentException("Quiz must not be null.");
        }
        if (quiz.getTitle() == null || quiz.getTitle().isBlank()) {
            throw new IllegalArgumentException("Quiz title must not be blank.");
        }

        Quiz existing = quizRepo.findById(quiz.getId())
                .orElseThrow(() -> new IllegalStateException(
                        "Quiz with id " + quiz.getId() + " does not exist."));

        // ACTIVE and COMPLETED quizzes cannot be edited
        if (existing.getStatus() == QuizStatus.ACTIVE
                || existing.getStatus() == QuizStatus.COMPLETED) {
            throw new IllegalStateException(
                    "Cannot edit a quiz in " + existing.getStatus() + " state.");
        }

        quiz.setUpdatedAt(LocalDateTime.now());
        boolean updated = quizRepo.update(quiz);
        if (!updated) {
            throw new IllegalStateException(
                    "Failed to update quiz with id " + quiz.getId() + ".");
        }
        return quiz;
    }

    /**
     * Deletes a quiz by its primary key.
     *
     * <p>Only quizzes in {@link QuizStatus#DRAFT} or {@link QuizStatus#ARCHIVED}
     * state may be deleted.</p>
     *
     * @param id the quiz identifier
     * @return {@code true} if deleted, {@code false} if not found
     * @throws IllegalStateException if the quiz is in an undeletable state
     */
    public boolean deleteQuiz(int id) {
        Quiz quiz = quizRepo.findById(id).orElse(null);
        if (quiz == null) {
            return false;
        }
        if (quiz.getStatus() != QuizStatus.DRAFT && quiz.getStatus() != QuizStatus.ARCHIVED) {
            throw new IllegalStateException(
                    "Only DRAFT or ARCHIVED quizzes can be deleted. Current status: "
                            + quiz.getStatus());
        }
        return quizRepo.delete(id);
    }

    // -------------------------------------------------------------------------
    // Lifecycle transitions (State pattern)
    // -------------------------------------------------------------------------

    /**
     * Publishes a quiz, transitioning it from DRAFT to PUBLISHED.
     *
     * <p>Validates that the quiz has a non-blank title and at least one question
     * before delegating to {@link QuizStateManager#publish(Quiz)}.</p>
     *
     * @param quizId the quiz to publish
     * @return the updated quiz
     * @throws IllegalArgumentException if validation fails
     * @throws IllegalStateException    if the quiz does not exist or the transition is illegal
     */
    public Quiz publishQuiz(int quizId) throws IllegalStateException {
        Quiz quiz = requireQuiz(quizId);
        validateForPublish(quiz);
        QuizStateManager.publish(quiz);
        quiz.setUpdatedAt(LocalDateTime.now());
        quizRepo.update(quiz);
        return quiz;
    }

    /**
     * Unpublishes a quiz, reverting it from PUBLISHED back to DRAFT.
     *
     * @param quizId the quiz to unpublish
     * @return the updated quiz
     * @throws IllegalStateException if the quiz does not exist or the transition is illegal
     */
    public Quiz unpublishQuiz(int quizId) throws IllegalStateException {
        Quiz quiz = requireQuiz(quizId);
        QuizStateManager.unpublish(quiz);
        quiz.setUpdatedAt(LocalDateTime.now());
        quizRepo.update(quiz);
        return quiz;
    }

    /**
     * Archives a quiz, transitioning it to {@link QuizStatus#ARCHIVED}.
     *
     * @param quizId the quiz to archive
     * @return the updated quiz
     * @throws IllegalStateException if the quiz does not exist or the transition is illegal
     */
    public Quiz archiveQuiz(int quizId) throws IllegalStateException {
        Quiz quiz = requireQuiz(quizId);
        QuizStateManager.archive(quiz);
        quiz.setUpdatedAt(LocalDateTime.now());
        quizRepo.update(quiz);
        return quiz;
    }

    // -------------------------------------------------------------------------
    // Question management
    // -------------------------------------------------------------------------

    /**
     * Adds a question to a quiz.
     *
     * <p>The quiz must be in {@link QuizStatus#DRAFT} or {@link QuizStatus#PUBLISHED}
     * state.  The next available display-order position is computed automatically.</p>
     *
     * @param quizId     the target quiz id
     * @param questionId the question to add
     * @return {@code true} on success
     * @throws IllegalStateException if the quiz does not exist or cannot accept new questions
     */
    public boolean addQuestionToQuiz(int quizId, int questionId) {
        Quiz quiz = requireQuiz(quizId);

        if (!canAddQuestions(quiz)) {
            throw new IllegalStateException(
                    "Cannot add questions to a quiz in " + quiz.getStatus() + " state.");
        }

        int nextOrder = questionRepo.getNextOrderForQuiz(quizId);
        return questionRepo.addToQuiz(quizId, questionId, nextOrder);
    }

    /**
     * Removes a question from a quiz.
     *
     * @param quizId     the target quiz id
     * @param questionId the question to remove
     * @return {@code true} if the question was removed
     */
    public boolean removeQuestionFromQuiz(int quizId, int questionId) {
        return questionRepo.removeFromQuiz(quizId, questionId);
    }

    /**
     * Returns all questions belonging to a quiz, in display order.
     *
     * @param quizId the quiz id
     * @return ordered list of questions; never {@code null}
     */
    public List<Question> getQuestionsForQuiz(int quizId) {
        return questionRepo.findByQuizId(quizId);
    }

    // -------------------------------------------------------------------------
    // Reporting
    // -------------------------------------------------------------------------

    /**
     * Returns aggregate statistics for a quiz.
     *
     * <p>The returned map contains the following keys:
     * <ul>
     *   <li>{@code "attemptCount"} – total attempts (Integer)</li>
     *   <li>{@code "averageScore"} – mean score across all attempts (Double)</li>
     *   <li>{@code "highestScore"} – best score recorded (Double)</li>
     *   <li>{@code "lowestScore"}  – lowest score recorded (Double)</li>
     *   <li>{@code "totalMarks"}   – maximum possible marks for the quiz (Double)</li>
     * </ul>
     * </p>
     *
     * @param quizId the quiz identifier
     * @return statistics map; never {@code null}
     * @throws IllegalStateException if the quiz does not exist
     */
    public Map<String, Object> getQuizStatistics(int quizId) {
        Quiz quiz = requireQuiz(quizId);

        Map<String, Object> stats = new HashMap<>();
        stats.put("attemptCount", quizRepo.countAttempts(quizId));
        stats.put("averageScore", quizRepo.getAverageScore(quizId));
        stats.put("highestScore", quizRepo.getHighestScore(quizId));
        stats.put("lowestScore", quizRepo.getLowestScore(quizId));
        stats.put("totalMarks", quiz.getTotalMarks());
        return stats;
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Retrieves a quiz by id, throwing {@link IllegalStateException} if not found.
     *
     * @param quizId the quiz id
     * @return the found quiz
     */
    private Quiz requireQuiz(int quizId) {
        return quizRepo.findById(quizId)
                .orElseThrow(() -> new IllegalStateException(
                        "Quiz with id " + quizId + " does not exist."));
    }

    /**
     * Returns {@code true} if the quiz's current status allows adding questions.
     *
     * @param quiz the quiz to check
     * @return {@code true} for DRAFT or PUBLISHED quizzes
     */
    private boolean canAddQuestions(Quiz quiz) {
        return quiz.getStatus() == QuizStatus.DRAFT
                || quiz.getStatus() == QuizStatus.PUBLISHED;
    }

    /**
     * Validates that a quiz satisfies all pre-publish requirements.
     *
     * @param quiz the quiz to validate
     * @throws IllegalArgumentException if any requirement is not met
     */
    private void validateForPublish(Quiz quiz) throws IllegalArgumentException {
        if (quiz.getTitle() == null || quiz.getTitle().isBlank()) {
            throw new IllegalArgumentException("Quiz title must not be blank before publishing.");
        }

        List<Question> questions = questionRepo.findByQuizId(quiz.getId());
        if (questions.isEmpty()) {
            throw new IllegalArgumentException(
                    "Quiz must have at least one question before it can be published.");
        }

        for (Question q : questions) {
            if (q.getMarks() <= 0) {
                throw new IllegalArgumentException(
                        "All questions must have positive marks. Question id "
                                + q.getId() + " has marks = " + q.getMarks() + ".");
            }
        }
    }
}
