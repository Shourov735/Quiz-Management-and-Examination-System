package com.quizapp.controller;

import com.quizapp.Main;
import com.quizapp.model.*;
import com.quizapp.observer.QuizEventPublisher;
import com.quizapp.repository.*;
import com.quizapp.service.AttemptService;
import com.quizapp.service.QuizService;
import com.quizapp.util.AlertHelper;
import com.quizapp.util.SessionContext;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Controller for the Examination Result &amp; Review screen.
 */
public class ResultController implements Initializable {

    @FXML private VBox scoreCardBox;
    @FXML private Label quizTitleLabel;
    @FXML private Label scoreNumberLabel;
    @FXML private Label percentageLabel;
    @FXML private Label statusBadge;

    @FXML private Label correctCountLabel;
    @FXML private Label wrongCountLabel;
    @FXML private Label timeSpentLabel;

    @FXML private TableView<AnswerReviewItem> reviewTable;
    @FXML private TableColumn<AnswerReviewItem, Number> qNumberCol;
    @FXML private TableColumn<AnswerReviewItem, String> questionCol;
    @FXML private TableColumn<AnswerReviewItem, String> studentAnswerCol;
    @FXML private TableColumn<AnswerReviewItem, String> correctAnswerCol;
    @FXML private TableColumn<AnswerReviewItem, String> marksCol;

    private AttemptService attemptService;
    private QuizService quizService;

    public static class AnswerReviewItem {
        private final int number;
        private final String questionText;
        private final String studentAnswer;
        private final String correctAnswer;
        private final double marksAwarded;
        private final double totalMarks;

        public AnswerReviewItem(int number, String questionText, String studentAnswer,
                                String correctAnswer, double marksAwarded, double totalMarks) {
            this.number = number;
            this.questionText = questionText;
            this.studentAnswer = studentAnswer;
            this.correctAnswer = correctAnswer;
            this.marksAwarded = marksAwarded;
            this.totalMarks = totalMarks;
        }

        public int getNumber() { return number; }
        public String getQuestionText() { return questionText; }
        public String getStudentAnswer() { return studentAnswer; }
        public String getCorrectAnswer() { return correctAnswer; }
        public double getMarksAwarded() { return marksAwarded; }
        public double getTotalMarks() { return totalMarks; }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        SQLiteQuizRepository quizRepo = new SQLiteQuizRepository();
        SQLiteQuestionRepository questionRepo = new SQLiteQuestionRepository();
        SQLiteCategoryRepository categoryRepo = new SQLiteCategoryRepository();
        SQLiteQuizAttemptRepository attemptRepo = new SQLiteQuizAttemptRepository();
        SQLiteAnswerRepository answerRepo = new SQLiteAnswerRepository();

        quizService = new QuizService(quizRepo, questionRepo, categoryRepo);
        attemptService = new AttemptService(attemptRepo, quizRepo, questionRepo, answerRepo, new QuizEventPublisher());

        setupReviewTable();
        loadResultData();
    }

    private void setupReviewTable() {
        qNumberCol.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getNumber()));
        questionCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getQuestionText()));
        studentAnswerCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStudentAnswer()));
        correctAnswerCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCorrectAnswer()));
        marksCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getMarksAwarded() + " / " + c.getValue().getTotalMarks()));
    }

    private void loadResultData() {
        int attemptId = SessionContext.getCurrentAttemptId();
        if (attemptId <= 0) {
            AlertHelper.showError("Result Error", "No examination attempt found in session.");
            Main.showStudentDashboard();
            return;
        }

        Optional<QuizAttempt> attemptOpt = attemptService.findById(attemptId);
        if (attemptOpt.isEmpty()) {
            AlertHelper.showError("Result Error", "Could not load examination attempt details.");
            Main.showStudentDashboard();
            return;
        }

        QuizAttempt attempt = attemptOpt.get();
        Optional<Quiz> quizOpt = quizService.findById(attempt.getQuizId());
        String title = quizOpt.map(Quiz::getTitle).orElse("Quiz");

        quizTitleLabel.setText(title);
        scoreNumberLabel.setText(String.format("%.1f / %.1f", attempt.getScore(), attempt.getTotalMarks()));

        double pct = attempt.getTotalMarks() > 0 ? (attempt.getScore() / attempt.getTotalMarks()) * 100.0 : 0.0;
        percentageLabel.setText(String.format("%.1f%% Score", pct));

        statusBadge.setText(attempt.getStatus() != null ? attempt.getStatus().name() : "COMPLETED");

        if (pct >= 50.0) {
            scoreCardBox.setStyle("-fx-background-color: #27ae60; -fx-background-radius: 12; -fx-padding: 30;");
        } else {
            scoreCardBox.setStyle("-fx-background-color: #e74c3c; -fx-background-radius: 12; -fx-padding: 30;");
        }

        // Calculate time taken
        long secondsTaken = 0;
        if (attempt.getStartTime() != null && attempt.getEndTime() != null) {
            secondsTaken = ChronoUnit.SECONDS.between(attempt.getStartTime(), attempt.getEndTime());
        }
        long mins = secondsTaken / 60;
        long secs = secondsTaken % 60;
        timeSpentLabel.setText(String.format("%02d:%02d", mins, secs));

        // Load answers and questions
        List<Answer> answers = attemptService.getAnswersForAttempt(attemptId);
        List<Question> questions = quizService.getQuestionsForQuiz(attempt.getQuizId());

        Map<Integer, Answer> answerByQId = new HashMap<>();
        for (Answer a : answers) {
            answerByQId.put(a.getQuestionId(), a);
        }

        int correctCount = 0;
        int wrongCount = 0;
        List<AnswerReviewItem> reviewItems = new ArrayList<>();

        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            Answer a = answerByQId.get(q.getId());

            String studentAns = (a != null && a.getAnswerValue() != null && !a.getAnswerValue().isBlank())
                    ? a.getAnswerValue() : "(Not Answered)";
            double marks = a != null ? a.getMarksAwarded() : 0.0;
            boolean isCorr = a != null && a.isCorrect();

            if (isCorr) {
                correctCount++;
            } else {
                wrongCount++;
            }

            reviewItems.add(new AnswerReviewItem(
                    i + 1,
                    q.getQuestionText(),
                    studentAns,
                    q.getCorrectAnswerDisplay(),
                    marks,
                    q.getMarks()
            ));
        }

        correctCountLabel.setText(String.valueOf(correctCount));
        wrongCountLabel.setText(String.valueOf(wrongCount));
        reviewTable.setItems(FXCollections.observableArrayList(reviewItems));
    }

    @FXML
    void handleBackToDashboard(ActionEvent event) {
        User user = SessionContext.getCurrentUser();
        if (user != null && user.getRole() == UserRole.TEACHER) {
            Main.showTeacherDashboard();
        } else {
            Main.showStudentDashboard();
        }
    }

    @FXML
    void handleViewReports(ActionEvent event) {
        Main.showReports();
    }
}
