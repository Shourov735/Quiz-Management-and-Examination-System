package com.quizapp.controller;

import com.quizapp.Main;
import com.quizapp.model.Quiz;
import com.quizapp.model.QuizAttempt;
import com.quizapp.model.User;
import com.quizapp.model.UserRole;
import com.quizapp.repository.*;
import com.quizapp.service.QuizService;
import com.quizapp.service.ReportingService;
import com.quizapp.util.SessionContext;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Controller for the Analytics &amp; Reports screen.
 */
public class ReportsController implements Initializable {

    @FXML private TabPane reportsTabPane;
    @FXML private Tab studentTab;

    // Tab 1: Quiz Analytics
    @FXML private ComboBox<Quiz> quizSelector;
    @FXML private Label quizAttemptsCountLabel;
    @FXML private Label quizAvgScoreLabel;
    @FXML private Label quizHighestScoreLabel;
    @FXML private Label quizLowestScoreLabel;

    @FXML private TableView<QuizAttempt> leaderboardTable;
    @FXML private TableColumn<QuizAttempt, Number> rankCol;
    @FXML private TableColumn<QuizAttempt, Number> attemptIdCol;
    @FXML private TableColumn<QuizAttempt, String> studentCol;
    @FXML private TableColumn<QuizAttempt, String> scoreCol;
    @FXML private TableColumn<QuizAttempt, String> totalMarksCol;
    @FXML private TableColumn<QuizAttempt, String> percentageCol;
    @FXML private TableColumn<QuizAttempt, String> statusCol;
    @FXML private TableColumn<QuizAttempt, String> dateCol;

    // Tab 2: Student History
    @FXML private Label studentTotalAttemptsLabel;
    @FXML private Label studentCompletedCountLabel;
    @FXML private Label studentOverallAvgLabel;
    @FXML private Label studentOverallPctLabel;

    @FXML private TableView<QuizAttempt> studentHistoryTable;
    @FXML private TableColumn<QuizAttempt, Number> histAttemptIdCol;
    @FXML private TableColumn<QuizAttempt, String> histQuizCol;
    @FXML private TableColumn<QuizAttempt, String> histScoreCol;
    @FXML private TableColumn<QuizAttempt, String> histStatusCol;
    @FXML private TableColumn<QuizAttempt, String> histDateCol;

    private QuizService quizService;
    private ReportingService reportingService;
    private UserRepository userRepo;

    private Map<Integer, String> studentNameCache = new HashMap<>();
    private Map<Integer, String> quizTitleCache = new HashMap<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        SQLiteQuizRepository quizRepo = new SQLiteQuizRepository();
        SQLiteQuestionRepository questionRepo = new SQLiteQuestionRepository();
        SQLiteCategoryRepository categoryRepo = new SQLiteCategoryRepository();
        SQLiteQuizAttemptRepository attemptRepo = new SQLiteQuizAttemptRepository();
        SQLiteAnswerRepository answerRepo = new SQLiteAnswerRepository();
        userRepo = new SQLiteUserRepository();

        quizService = new QuizService(quizRepo, questionRepo, categoryRepo);
        reportingService = new ReportingService(attemptRepo, quizRepo, answerRepo);

        setupTableColumns();
        loadQuizzesSelector();
        loadStudentReport();
    }

    private void setupTableColumns() {
        // Tab 1
        rankCol.setCellValueFactory(cell -> {
            int rank = leaderboardTable.getItems().indexOf(cell.getValue()) + 1;
            return new SimpleIntegerProperty(rank);
        });
        attemptIdCol.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getId()));
        studentCol.setCellValueFactory(c -> {
            int sId = c.getValue().getStudentId();
            String name = studentNameCache.computeIfAbsent(sId, id ->
                    userRepo.findById(id).map(User::getName).orElse("Student #" + id));
            return new SimpleStringProperty(name);
        });
        scoreCol.setCellValueFactory(c -> new SimpleStringProperty(String.format("%.1f", c.getValue().getScore())));
        totalMarksCol.setCellValueFactory(c -> new SimpleStringProperty(String.format("%.1f", c.getValue().getTotalMarks())));
        percentageCol.setCellValueFactory(c -> {
            double total = c.getValue().getTotalMarks();
            double pct = total > 0 ? (c.getValue().getScore() / total) * 100.0 : 0.0;
            return new SimpleStringProperty(String.format("%.1f%%", pct));
        });
        statusCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus().name()));
        dateCol.setCellValueFactory(c -> {
            if (c.getValue().getEndTime() != null) {
                return new SimpleStringProperty(c.getValue().getEndTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            } else if (c.getValue().getStartTime() != null) {
                return new SimpleStringProperty(c.getValue().getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            }
            return new SimpleStringProperty("N/A");
        });

        // Tab 2
        histAttemptIdCol.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getId()));
        histQuizCol.setCellValueFactory(c -> {
            int qId = c.getValue().getQuizId();
            String title = quizTitleCache.computeIfAbsent(qId, id ->
                    quizService.findById(id).map(Quiz::getTitle).orElse("Quiz #" + id));
            return new SimpleStringProperty(title);
        });
        histScoreCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getScore() + " / " + c.getValue().getTotalMarks()));
        histStatusCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus().name()));
        histDateCol.setCellValueFactory(c -> {
            if (c.getValue().getStartTime() != null) {
                return new SimpleStringProperty(c.getValue().getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            }
            return new SimpleStringProperty("N/A");
        });
    }

    private void loadQuizzesSelector() {
        List<Quiz> quizzes = quizService.findAll();
        quizSelector.setItems(FXCollections.observableArrayList(quizzes));

        // Format selector display
        quizSelector.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Quiz item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getTitle() + " (" + item.getStatus() + ")");
            }
        });
        quizSelector.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Quiz item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getTitle() + " (" + item.getStatus() + ")");
            }
        });

        quizSelector.setOnAction(e -> handleRefreshQuizReport(null));

        if (!quizzes.isEmpty()) {
            quizSelector.getSelectionModel().selectFirst();
            handleRefreshQuizReport(null);
        }
    }

    @FXML
    void handleRefreshQuizReport(ActionEvent event) {
        Quiz selected = quizSelector.getValue();
        if (selected == null) return;

        Map<String, Object> stats = reportingService.getQuizReport(selected.getId());
        quizAttemptsCountLabel.setText(String.valueOf(stats.getOrDefault("attemptCount", 0)));
        quizAvgScoreLabel.setText(String.valueOf(stats.getOrDefault("averageScore", 0.0)));
        quizHighestScoreLabel.setText(String.valueOf(stats.getOrDefault("highestScore", 0.0)));
        quizLowestScoreLabel.setText(String.valueOf(stats.getOrDefault("lowestScore", 0.0)));

        List<QuizAttempt> leaderboard = reportingService.getLeaderboard(selected.getId());
        leaderboardTable.setItems(FXCollections.observableArrayList(leaderboard));
    }

    @SuppressWarnings("unchecked")
    private void loadStudentReport() {
        User user = SessionContext.getCurrentUser();
        int studentId = user != null ? user.getId() : 1;

        Map<String, Object> overall = reportingService.getStudentOverallReport(studentId);

        studentTotalAttemptsLabel.setText(String.valueOf(overall.getOrDefault("totalAttempts", 0)));
        studentCompletedCountLabel.setText(String.valueOf(overall.getOrDefault("completedAttempts", 0)));
        studentOverallAvgLabel.setText(String.valueOf(overall.getOrDefault("averageScore", 0.0)));
        studentOverallPctLabel.setText(overall.getOrDefault("overallPercentage", 0.0) + "%");

        Object attemptsObj = overall.get("attempts");
        if (attemptsObj instanceof List<?>) {
            studentHistoryTable.setItems(FXCollections.observableArrayList((List<QuizAttempt>) attemptsObj));
        }
    }

    @FXML
    void handleBack(ActionEvent event) {
        User user = SessionContext.getCurrentUser();
        if (user != null && user.getRole() == UserRole.TEACHER) {
            Main.showTeacherDashboard();
        } else {
            Main.showStudentDashboard();
        }
    }
}
