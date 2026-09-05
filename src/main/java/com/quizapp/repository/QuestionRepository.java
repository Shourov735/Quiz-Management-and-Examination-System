package com.quizapp.repository;

import com.quizapp.model.Question;
import com.quizapp.model.QuestionType;

import java.util.List;
import java.util.Optional;

/**
 * Repository abstraction for Question persistence operations.
 */
public interface QuestionRepository {

    /** Persist a new question (with its options) and return generated id. */
    int save(Question question);

    /** Find question by id, including its options. */
    Optional<Question> findById(int id);

    /** Return all questions with their options. */
    List<Question> findAll();

    /** Find questions by type. */
    List<Question> findByType(QuestionType type);

    /** Find questions by category. */
    List<Question> findByCategoryId(int categoryId);

    /** Find questions created by a specific teacher. */
    List<Question> findByCreatedBy(int teacherId);

    /**
     * Search questions by text, type, category, difficulty (all optional).
     */
    List<Question> search(String textKeyword, QuestionType type, int categoryId, String difficulty);

    /** Find all questions belonging to a quiz (ordered). */
    List<Question> findByQuizId(int quizId);

    /** Update a question and its options. */
    boolean update(Question question);

    /** Delete a question by id (cascades to options). */
    boolean delete(int id);

    /** Add a question to a quiz with a given order. */
    boolean addToQuiz(int quizId, int questionId, int order);

    /** Remove a question from a quiz. */
    boolean removeFromQuiz(int quizId, int questionId);

    /** Get the next available order position for a quiz. */
    int getNextOrderForQuiz(int quizId);
}
