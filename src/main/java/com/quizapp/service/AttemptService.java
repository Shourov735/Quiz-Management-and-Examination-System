package com.quizapp.service;

import com.quizapp.model.Answer;
import com.quizapp.model.AttemptStatus;
import com.quizapp.model.Question;
import com.quizapp.model.Quiz;
import com.quizapp.model.QuizAttempt;
import com.quizapp.model.QuizStatus;
import com.quizapp.observer.QuizEvent;
import com.quizapp.observer.QuizEventPublisher;
import com.quizapp.repository.AnswerRepository;
import com.quizapp.repository.QuizAttemptRepository;
import com.quizapp.repository.QuizRepository;
import com.quizapp.repository.QuestionRepository;
import com.quizapp.scoring.ScoringStrategy;
import com.quizapp.scoring.ScoringStrategyFactory;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

/**
 * Service managing the full quiz-attempt lifecycle for students.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Starting a new attempt ({@link #startAttempt})</li>
 *   <li>Recording individual answers ({@link #submitAnswer})</li>
 *   <li>Evaluating and finalising an attempt ({@link #submitAttempt}, {@link #timeoutAttempt})</li>
 *   <li>Querying attempt history and results</li>
 * </ul>
 * </p>
 *
 * <p>Events are published via {@link QuizEventPublisher} after key state changes.
 * Validation failures throw {@link IllegalArgumentException};
 * business-rule violations throw {@link IllegalStateException}.</p>
 */
public class AttemptService {

    // -------------------------------------------------------------------------
    // Fields
    // -------------------------------------------------------------------------

    /** Persistence layer for QuizAttempt entities. */
    private final QuizAttemptRepository attemptRepo;

    /** Persistence layer for Quiz entities. */
    private final QuizRepository quizRepo;

    /** Persistence layer for Question entities. */
    private final QuestionRepository questionRepo;

    /** Persistence layer for Answer entities. */
    private final AnswerRepository answerRepo;

    /** Event bus for publishing quiz lifecycle events. */
    private final QuizEventPublisher eventPublisher;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * Creates an {@code AttemptService} with all required collaborators.
     *
     * @param attemptRepo    attempt repository; must not be {@code null}
     * @param quizRepo       quiz repository; must not be {@code null}
     * @param questionRepo   question repository; must not be {@code null}
     * @param answerRepo     answer repository; must not be {@code null}
     * @param eventPublisher event publisher; must not be {@code null}
     */
    public AttemptService(QuizAttemptRepository attemptRepo,
                          QuizRepository quizRepo,
                          QuestionRepository questionRepo,
                          AnswerRepository answerRepo,
                          QuizEventPublisher eventPublisher) {
        if (attemptRepo == null)    throw new IllegalArgumentException("AttemptRepository must not be null.");
        if (quizRepo == null)       throw new IllegalArgumentException("QuizRepository must not be null.");
        if (questionRepo == null)   throw new IllegalArgumentException("QuestionRepository must not be null.");
        if (answerRepo == null)     throw new IllegalArgumentException("AnswerRepository must not be null.");
        if (eventPublisher == null) throw new IllegalArgumentException("QuizEventPublisher must not be null.");

        this.attemptRepo    = attemptRepo;
        this.quizRepo       = quizRepo;
        this.questionRepo   = questionRepo;
        this.answerRepo     = answerRepo;
        this.eventPublisher = eventPublisher;
    }

    // -------------------------------------------------------------------------
    // Start
    // -------------------------------------------------------------------------

    /**
     * Starts a new quiz attempt for a student.
     *
     * <p>Pre-conditions checked before creating the attempt:
     * <ol>
     *   <li>The quiz must exist.</li>
     *   <li>The quiz status must be {@link QuizStatus#ACTIVE} or {@link QuizStatus#PUBLISHED}
     *       (i.e. {@code quiz.canBeAttempted()} returns {@code true}).</li>
     *   <li>The student must not have exceeded the quiz's {@code maxAttempts} limit.</li>
     *   <li>The student must not already have an in-progress attempt for this quiz.</li>
     * </ol>
     * </p>
     *
     * @param studentId the student user id
     * @param quizId    the quiz to attempt
     * @return the newly created, in-progress {@link QuizAttempt}
     * @throws IllegalStateException if any pre-condition is violated
     */
    public QuizAttempt startAttempt(int studentId, int quizId) throws IllegalStateException {
        Quiz quiz = requireQuiz(quizId);

        if (!quiz.canBeAttempted()) {
            throw new IllegalStateException(
                    "Quiz '" + quiz.getTitle() + "' is not currently open for attempts. "
                            + "Status: " + quiz.getStatus());
        }

        if (!canAttempt(studentId, quizId)) {
            throw new IllegalStateException(
                    "Student has reached the maximum number of allowed attempts for this quiz.");
        }

        if (attemptRepo.findInProgress(studentId, quizId).isPresent()) {
            throw new IllegalStateException(
                    "Student already has an in-progress attempt for this quiz.");
        }

        QuizAttempt attempt = new QuizAttempt();
        attempt.setStudentId(studentId);
        attempt.setQuizId(quizId);
        attempt.setStatus(AttemptStatus.IN_PROGRESS);
        attempt.setStartTime(LocalDateTime.now());
        attempt.setTotalMarks(quiz.getTotalMarks());

        int generatedId = attemptRepo.save(attempt);
        attempt.setId(generatedId);

        eventPublisher.publish(QuizEvent.ATTEMPT_STARTED, attempt);

        return attempt;
    }

    // -------------------------------------------------------------------------
    // Answer submission
    // -------------------------------------------------------------------------

    /**
     * Records or updates a student's answer to a single question within an attempt.
     *
     * <p>The answer is validated immediately against the question's correct answer(s).
     * The result ({@code isCorrect}) and provisional {@code marksAwarded} are stored.
     * These may be recomputed when the attempt is finally evaluated.</p>
     *
     * @param attemptId   the attempt the answer belongs to
     * @param questionId  the question being answered
     * @param answerValue the raw student input
     * @return the persisted {@link Answer}
     * @throws IllegalStateException    if the attempt is not in progress
     * @throws IllegalArgumentException if the attempt or question cannot be found
     */
    public Answer submitAnswer(int attemptId, int questionId, String answerValue) {
        QuizAttempt attempt = requireAttempt(attemptId);
        if (attempt.getStatus() != AttemptStatus.IN_PROGRESS) {
            throw new IllegalStateException(
                    "Cannot submit an answer to an attempt that is not in progress. "
                            + "Status: " + attempt.getStatus());
        }

        Question question = questionRepo.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Question with id " + questionId + " does not exist."));

        boolean correct = question.validateAnswer(answerValue);
        double marksAwarded = correct ? question.getMarks() : 0.0;

        // Upsert: update existing answer for this question if already submitted
        Answer answer = answerRepo.findByAttemptAndQuestion(attemptId, questionId)
                .orElse(new Answer());
        answer.setAttemptId(attemptId);
        answer.setQuestionId(questionId);
        answer.setSubmittedAnswer(answerValue);
        answer.setCorrect(correct);
        answer.setMarksAwarded(marksAwarded);

        int savedId = answerRepo.saveOrUpdate(answer);
        answer.setId(savedId);

        eventPublisher.publish(QuizEvent.ANSWER_SUBMITTED, answer);

        return answer;
    }

    // -------------------------------------------------------------------------
    // Finalise
    // -------------------------------------------------------------------------

    /**
     * Submits and finalises a quiz attempt voluntarily by the student.
     *
     * <p>Sets {@code endTime} to now, updates status to {@link AttemptStatus#SUBMITTED},
     * triggers scoring evaluation, then publishes a {@code QUIZ_COMPLETED} event.</p>
     *
     * @param attemptId the attempt to submit
     * @return the finalised {@link QuizAttempt}
     * @throws IllegalStateException if the attempt is not in progress
     */
    public QuizAttempt submitAttempt(int attemptId) throws IllegalStateException {
        QuizAttempt attempt = requireAttempt(attemptId);
        if (attempt.getStatus() != AttemptStatus.IN_PROGRESS) {
            throw new IllegalStateException(
                    "Cannot submit an attempt that is not in progress. "
                            + "Status: " + attempt.getStatus());
        }

        attempt.setEndTime(LocalDateTime.now());
        attempt.setStatus(AttemptStatus.SUBMITTED);

        Quiz quiz = requireQuiz(attempt.getQuizId());
        double score = evaluateAttempt(attempt, quiz);
        attempt.setScore(score);

        attemptRepo.update(attempt);

        eventPublisher.publish(QuizEvent.QUIZ_COMPLETED, attempt);

        return attempt;
    }

    /**
     * Times out an in-progress attempt when the allowed time has elapsed.
     *
     * <p>Sets status to {@link AttemptStatus#TIMED_OUT}, records end time,
     * evaluates scoring, and publishes a {@code TIME_EXPIRED} event.</p>
     *
     * @param attemptId the attempt that has timed out
     * @return the finalised {@link QuizAttempt}
     */
    public QuizAttempt timeoutAttempt(int attemptId) {
        QuizAttempt attempt = requireAttempt(attemptId);
        if (attempt.getStatus() != AttemptStatus.IN_PROGRESS) {
            // Already finalised — return as-is
            return attempt;
        }

        attempt.setEndTime(LocalDateTime.now());
        attempt.setStatus(AttemptStatus.TIMED_OUT);

        Quiz quiz = requireQuiz(attempt.getQuizId());
        double score = evaluateAttempt(attempt, quiz);
        attempt.setScore(score);

        attemptRepo.update(attempt);

        eventPublisher.publish(QuizEvent.TIME_EXPIRED, attempt);

        return attempt;
    }

    // -------------------------------------------------------------------------
    // Query
    // -------------------------------------------------------------------------

    /**
     * Finds an attempt by primary key.
     *
     * @param id the attempt identifier
     * @return an {@link Optional} containing the attempt, or empty if not found
     */
    public Optional<QuizAttempt> findById(int id) {
        return attemptRepo.findById(id);
    }

    /**
     * Returns all attempts made by a student.
     *
     * @param studentId the student user id
     * @return list of attempts; never {@code null}
     */
    public List<QuizAttempt> findByStudent(int studentId) {
        return attemptRepo.findByStudentId(studentId);
    }

    /**
     * Returns all attempts for a given quiz.
     *
     * @param quizId the quiz id
     * @return list of attempts; never {@code null}
     */
    public List<QuizAttempt> findByQuiz(int quizId) {
        return attemptRepo.findByQuizId(quizId);
    }

    /**
     * Returns all attempts by a student on a specific quiz.
     *
     * @param studentId the student user id
     * @param quizId    the quiz id
     * @return list of attempts; never {@code null}
     */
    public List<QuizAttempt> findByStudentAndQuiz(int studentId, int quizId) {
        return attemptRepo.findByStudentAndQuiz(studentId, quizId);
    }

    /**
     * Returns all answers submitted within a given attempt.
     *
     * @param attemptId the attempt id
     * @return list of answers; never {@code null}
     */
    public List<Answer> getAnswersForAttempt(int attemptId) {
        return answerRepo.findByAttemptId(attemptId);
    }

    // -------------------------------------------------------------------------
    // Eligibility
    // -------------------------------------------------------------------------

    /**
     * Checks whether a student is eligible to start a new attempt for a quiz.
     *
     * <p>Eligibility requires:
     * <ul>
     *   <li>The quiz is in an attemptable state ({@code quiz.canBeAttempted()}).</li>
     *   <li>The quiz allows unlimited attempts ({@code maxAttempts == 0}) OR
     *       the student's completed attempt count is below the limit.</li>
     * </ul>
     * </p>
     *
     * @param studentId the student user id
     * @param quizId    the quiz id
     * @return {@code true} if the student may start a new attempt
     */
    public boolean canAttempt(int studentId, int quizId) {
        Quiz quiz = quizRepo.findById(quizId).orElse(null);
        if (quiz == null || !quiz.canBeAttempted()) {
            return false;
        }
        if (quiz.getMaxAttempts() == 0) {
            return true;
        }
        int completed = attemptRepo.countCompletedAttempts(studentId, quizId);
        return completed < quiz.getMaxAttempts();
    }

    /**
     * Returns the number of remaining attempts a student has for a quiz.
     *
     * <p>Returns {@code Integer.MAX_VALUE} if the quiz allows unlimited attempts.</p>
     *
     * @param studentId the student user id
     * @param quizId    the quiz id
     * @return remaining attempts, or {@code Integer.MAX_VALUE} for unlimited
     */
    public int getRemainingAttempts(int studentId, int quizId) {
        Quiz quiz = quizRepo.findById(quizId).orElse(null);
        if (quiz == null) {
            return 0;
        }
        if (quiz.getMaxAttempts() == 0) {
            return Integer.MAX_VALUE;
        }
        int completed = attemptRepo.countCompletedAttempts(studentId, quizId);
        return Math.max(0, quiz.getMaxAttempts() - completed);
    }

    // -------------------------------------------------------------------------
    // Private evaluation
    // -------------------------------------------------------------------------

    /**
     * Evaluates all answers for a completed attempt and computes the final score.
     *
     * <p>Steps:
     * <ol>
     *   <li>Load all answers for the attempt.</li>
     *   <li>For each answer, re-validate the submitted value against the question
     *       to ensure correctness flags are up to date.</li>
     *   <li>Compute time spent in seconds (from {@code startTime} to {@code endTime}).</li>
     *   <li>Delegate score computation to the quiz's {@link ScoringStrategy}.</li>
     *   <li>Persist updated marks on each answer record.</li>
     * </ol>
     * </p>
     *
     * @param attempt the attempt being evaluated (endTime must already be set)
     * @param quiz    the associated quiz (provides totalMarks, timeLimitSeconds, strategy)
     * @return the computed score (never negative)
     */
    private double evaluateAttempt(QuizAttempt attempt, Quiz quiz) {
        List<Answer> answers = answerRepo.findByAttemptId(attempt.getId());
        List<Question> questions = questionRepo.findByQuizId(quiz.getId());

        // Build a quick lookup map by question id
        java.util.Map<Integer, Question> questionMap = new java.util.HashMap<>();
        for (Question q : questions) {
            questionMap.put(q.getId(), q);
        }

        // Re-validate each answer and update marks
        for (Answer answer : answers) {
            Question question = questionMap.get(answer.getQuestionId());
            if (question != null) {
                boolean correct = question.validateAnswer(answer.getSubmittedAnswer());
                double marks = correct ? question.getMarks() : 0.0;
                answer.setCorrect(correct);
                answer.setMarksAwarded(marks);
                answerRepo.update(answer);
            }
        }

        // Calculate time spent in seconds
        int timeSpentSeconds = 0;
        if (attempt.getStartTime() != null && attempt.getEndTime() != null) {
            timeSpentSeconds = (int) ChronoUnit.SECONDS.between(
                    attempt.getStartTime(), attempt.getEndTime());
        }

        // Resolve the scoring strategy and calculate final score
        ScoringStrategy strategy = ScoringStrategyFactory.getStrategy(quiz.getScoringStrategy());
        double score = strategy.calculateScore(answers, quiz.getTotalMarks(),
                quiz.getTimeLimitSeconds(), timeSpentSeconds);

        attempt.setTotalMarks(quiz.getTotalMarks());
        return score;
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Loads a quiz by id, throwing {@link IllegalStateException} if not found.
     */
    private Quiz requireQuiz(int quizId) {
        return quizRepo.findById(quizId)
                .orElseThrow(() -> new IllegalStateException(
                        "Quiz with id " + quizId + " does not exist."));
    }

    /**
     * Loads an attempt by id, throwing {@link IllegalArgumentException} if not found.
     */
    private QuizAttempt requireAttempt(int attemptId) {
        return attemptRepo.findById(attemptId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "QuizAttempt with id " + attemptId + " does not exist."));
    }
}
