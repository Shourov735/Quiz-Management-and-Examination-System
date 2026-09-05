package com.quizapp.repository;

import com.quizapp.model.Answer;

import java.util.List;
import java.util.Optional;

/**
 * Repository abstraction for Answer persistence operations.
 */
public interface AnswerRepository {

    /** Persist a new answer and return generated id. */
    int save(Answer answer);

    /** Persist or update (upsert) an answer. */
    int saveOrUpdate(Answer answer);

    /** Find answer by id. */
    Optional<Answer> findById(int id);

    /** Find all answers for a given attempt. */
    List<Answer> findByAttemptId(int attemptId);

    /** Find a specific answer for a question within an attempt. */
    Optional<Answer> findByAttemptAndQuestion(int attemptId, int questionId);

    /** Update an existing answer. */
    boolean update(Answer answer);

    /** Delete all answers for an attempt. */
    boolean deleteByAttemptId(int attemptId);

    /** Count correct answers for an attempt. */
    int countCorrect(int attemptId);

    /** Count incorrect answers for an attempt. */
    int countIncorrect(int attemptId);
}
