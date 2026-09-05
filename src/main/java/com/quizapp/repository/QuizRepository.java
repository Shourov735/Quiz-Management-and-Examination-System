package com.quizapp.repository;

import com.quizapp.model.Quiz;
import com.quizapp.model.QuizStatus;

import java.util.List;
import java.util.Optional;

/**
 * Repository abstraction for Quiz persistence operations.
 */
public interface QuizRepository {

    /** Persist a new quiz and return the generated id. */
    int save(Quiz quiz);

    /** Find quiz by primary key. */
    Optional<Quiz> findById(int id);

    /** Find all quizzes. */
    List<Quiz> findAll();

    /** Find quizzes by status. */
    List<Quiz> findByStatus(QuizStatus status);

    /** Find quizzes created by a specific teacher. */
    List<Quiz> findByCreatedBy(int teacherId);

    /** Find quizzes by category. */
    List<Quiz> findByCategoryId(int categoryId);

    /**
     * Search quizzes by title, category, or difficulty (all optional filters).
     * @param titleKeyword null or empty to skip
     * @param categoryId 0 to skip
     * @param difficulty null or empty to skip
     * @param status null to skip
     */
    List<Quiz> search(String titleKeyword, int categoryId, String difficulty, QuizStatus status);

    /** Update an existing quiz record. */
    boolean update(Quiz quiz);

    /** Delete a quiz by id. */
    boolean delete(int id);

    /** Count attempts for a given quiz (used for reporting). */
    int countAttempts(int quizId);

    /** Get average score for a quiz. */
    double getAverageScore(int quizId);

    /** Get highest score for a quiz. */
    double getHighestScore(int quizId);

    /** Get lowest score for a quiz. */
    double getLowestScore(int quizId);
}
