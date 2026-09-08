package com.quizapp.service;

import com.quizapp.model.Quiz;
import com.quizapp.model.QuizAttempt;
import com.quizapp.repository.AnswerRepository;
import com.quizapp.repository.QuizAttemptRepository;
import com.quizapp.repository.QuizRepository;

import java.util.*;

/**
 * Service providing statistical, analytical, and reporting operations
 * for students and teachers.
 */
public class ReportingService {

    private final QuizAttemptRepository attemptRepo;
    private final QuizRepository quizRepo;
    private final AnswerRepository answerRepo;

    public ReportingService(QuizAttemptRepository attemptRepo,
                            QuizRepository quizRepo,
                            AnswerRepository answerRepo) {
        this.attemptRepo = Objects.requireNonNull(attemptRepo, "attemptRepo cannot be null");
        this.quizRepo = Objects.requireNonNull(quizRepo, "quizRepo cannot be null");
        this.answerRepo = Objects.requireNonNull(answerRepo, "answerRepo cannot be null");
    }

    /**
     * Generates an overall performance report for a specific student across all quizzes.
     */
    public Map<String, Object> getStudentOverallReport(int studentId) {
        List<QuizAttempt> attempts = attemptRepo.findByStudentId(studentId);
        Map<String, Object> report = new HashMap<>();

        report.put("totalAttempts", attempts.size());

        long completedCount = attempts.stream()
                .filter(a -> a.getStatus() != null && a.getStatus().isTerminal())
                .count();
        report.put("completedAttempts", completedCount);

        double totalScore = 0;
        double totalPossible = 0;
        Set<Integer> distinctQuizzes = new HashSet<>();

        for (QuizAttempt a : attempts) {
            if (a.getStatus() != null && a.getStatus().isTerminal()) {
                totalScore += a.getScore();
                totalPossible += a.getTotalMarks();
                distinctQuizzes.add(a.getQuizId());
            }
        }

        report.put("distinctQuizzesAttempted", distinctQuizzes.size());
        double avgScore = completedCount > 0 ? (totalScore / completedCount) : 0.0;
        double overallPercentage = totalPossible > 0 ? ((totalScore / totalPossible) * 100.0) : 0.0;

        report.put("averageScore", Math.round(avgScore * 100.0) / 100.0);
        report.put("overallPercentage", Math.round(overallPercentage * 100.0) / 100.0);
        report.put("attempts", attempts);

        return report;
    }

    /**
     * Generates a performance report for a specific student on a specific quiz.
     */
    public Map<String, Object> getStudentQuizReport(int studentId, int quizId) {
        List<QuizAttempt> attempts = attemptRepo.findByStudentAndQuiz(studentId, quizId);
        int completedAttempts = attemptRepo.countCompletedAttempts(studentId, quizId);
        double bestScore = attemptRepo.getBestScore(studentId, quizId);
        double avgScore = attemptRepo.getAverageScore(studentId, quizId);

        Optional<Quiz> quizOpt = quizRepo.findById(quizId);
        String quizTitle = quizOpt.map(Quiz::getTitle).orElse("Unknown Quiz");

        Map<String, Object> report = new HashMap<>();
        report.put("quizId", quizId);
        report.put("quizTitle", quizTitle);
        report.put("totalAttempts", attempts.size());
        report.put("completedAttempts", completedAttempts);
        report.put("bestScore", bestScore);
        report.put("averageScore", Math.round(avgScore * 100.0) / 100.0);
        report.put("attempts", attempts);

        return report;
    }

    /**
     * Generates an analytical report for an entire quiz (for teachers).
     */
    public Map<String, Object> getQuizReport(int quizId) {
        Optional<Quiz> quizOpt = quizRepo.findById(quizId);
        Map<String, Object> report = new HashMap<>();

        if (quizOpt.isEmpty()) {
            report.put("error", "Quiz not found");
            return report;
        }

        Quiz quiz = quizOpt.get();
        int attemptsCount = quizRepo.countAttempts(quizId);
        double avgScore = quizRepo.getAverageScore(quizId);
        double highestScore = quizRepo.getHighestScore(quizId);
        double lowestScore = quizRepo.getLowestScore(quizId);
        List<QuizAttempt> allAttempts = attemptRepo.findByQuizId(quizId);

        report.put("quizId", quiz.getId());
        report.put("quizTitle", quiz.getTitle());
        report.put("difficulty", quiz.getDifficulty() != null ? quiz.getDifficulty().name() : "N/A");
        report.put("status", quiz.getStatus() != null ? quiz.getStatus().name() : "N/A");
        report.put("attemptCount", attemptsCount);
        report.put("averageScore", Math.round(avgScore * 100.0) / 100.0);
        report.put("highestScore", highestScore);
        report.put("lowestScore", lowestScore);
        report.put("attempts", allAttempts);

        return report;
    }

    /**
     * Returns a leaderboard for a quiz, sorted by score in descending order.
     */
    public List<QuizAttempt> getLeaderboard(int quizId) {
        List<QuizAttempt> attempts = attemptRepo.findByQuizId(quizId);
        attempts.removeIf(a -> a.getStatus() == null || !a.getStatus().isTerminal());
        attempts.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));
        return attempts;
    }
}
