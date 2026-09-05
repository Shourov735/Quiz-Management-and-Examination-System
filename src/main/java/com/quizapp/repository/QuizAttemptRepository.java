package com.quizapp.repository;

import com.quizapp.model.QuizAttempt;
import com.quizapp.model.AttemptStatus;

import java.util.List;
import java.util.Optional;

/**
 * Repository abstraction for QuizAttempt persistence operations.
 */
public interface QuizAttemptRepository {

    /** Persist a new attempt and return generated id. */
    int save(QuizAttempt attempt);

    /** Find attempt by id. */
    Optional<QuizAttempt> findById(int id);

    /** Find all attempts for a given quiz. */
    List<QuizAttempt> findByQuizId(int quizId);

    /** Find all attempts by a given student. */
    List<QuizAttempt> findByStudentId(int studentId);

    /** Find attempts for a student on a specific quiz. */
    List<QuizAttempt> findByStudentAndQuiz(int studentId, int quizId);

    /** Count completed attempts for a student on a specific quiz. */
    int countCompletedAttempts(int studentId, int quizId);

    /** Get the best score for a student on a specific quiz. */
    double getBestScore(int studentId, int quizId);

    /** Get average score for a student on a specific quiz. */
    double getAverageScore(int studentId, int quizId);

    /** Update an existing attempt (status, score, endTime). */
    boolean update(QuizAttempt attempt);

    /** Find in-progress attempt for a student on a quiz (should be at most one). */
    Optional<QuizAttempt> findInProgress(int studentId, int quizId);
}
