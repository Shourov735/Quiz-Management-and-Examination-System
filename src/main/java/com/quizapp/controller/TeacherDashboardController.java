package com.quizapp.controller;

import com.quizapp.Main;
import com.quizapp.model.Difficulty;
import com.quizapp.model.Quiz;
import com.quizapp.model.QuizStatus;
import com.quizapp.model.User;
import com.quizapp.repository.SQLiteCategoryRepository;
import com.quizapp.repository.SQLiteQuestionRepository;
import com.quizapp.repository.SQLiteQuizAttemptRepository;
import com.quizapp.repository.SQLiteQuizRepository;
import com.quizapp.service.QuestionService;
import com.quizapp.service.QuizService;
import com.quizapp.util.SessionContext;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for the Teacher Dashboard screen.
 */
public class TeacherDashboardController implements Initializable {

    @FXML private Label welcomeLabel;
    @FXML private Label totalQuizzesLabel;
    @FXML private Label publishedQuizzesLabel;
    @FXML private Label totalQuestionsLabel;
    @FXML private Label totalAttemptsLabel;

    @FXML private TableView<Quiz> recentQuizzesTable;
    @FXML private TableColumn<Quiz, Number> idColumn;
    @FXML private TableColumn<Quiz, String> titleColumn;
    @FXML private TableColumn<Quiz, String> difficultyColumn;
    @FXML private TableColumn<Quiz, String> statusColumn;
    @FXML private TableColumn<Quiz, String> strategyColumn;
    @FXML private TableColumn<Quiz, Number> timeLimitColumn;

    private QuizService quizService;
    private QuestionService questionService;
    private SQLiteQuizAttemptRepository attemptRepo;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        SQLiteQuizRepository quizRepo = new SQLiteQuizRepository();
        SQLiteQuestionRepository questionRepo = new SQLiteQuestionRepository();
        SQLiteCategoryRepository categoryRepo = new SQLiteCategoryRepository();
        attemptRepo = new SQLiteQuizAttemptRepository();

        quizService = new QuizService(quizRepo, questionRepo, categoryRepo);
        questionService = new QuestionService(questionRepo, categoryRepo);

        setupTableColumns();
        loadDashboardData();
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getId()));
        titleColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTitle()));
        difficultyColumn.setCellValueFactory(cell -> {
            Difficulty d = cell.getValue().getDifficulty();
            return new SimpleStringProperty(d != null ? d.name() : "N/A");
        });
        statusColumn.setCellValueFactory(cell -> {
            QuizStatus s = cell.getValue().getStatus();
            return new SimpleStringProperty(s != null ? s.name() : "N/A");
        });
        strategyColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getScoringStrategy()));
        timeLimitColumn.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getTimeLimitMinutes()));
    }

    private void loadDashboardData() {
        User current = SessionContext.getCurrentUser();
        if (current != null) {
            welcomeLabel.setText("Welcome, " + current.getName());
        }

        List<Quiz> teacherQuizzes = current != null
                ? quizService.findByTeacher(current.getId())
                : quizService.findAll();

        if (teacherQuizzes.isEmpty() && current != null) {
            // fallback to all quizzes for demo
            teacherQuizzes = quizService.findAll();
        }

        long publishedCount = teacherQuizzes.stream()
                .filter(q -> q.getStatus() == QuizStatus.PUBLISHED || q.getStatus() == QuizStatus.ACTIVE)
                .count();

        int questionsCount = questionService.findAll().size();

        int attemptsCount = 0;
        for (Quiz q : teacherQuizzes) {
            attemptsCount += attemptRepo.findByQuizId(q.getId()).size();
        }

        totalQuizzesLabel.setText(String.valueOf(teacherQuizzes.size()));
        publishedQuizzesLabel.setText(String.valueOf(publishedCount));
        totalQuestionsLabel.setText(String.valueOf(questionsCount));
        totalAttemptsLabel.setText(String.valueOf(attemptsCount));

        recentQuizzesTable.setItems(FXCollections.observableArrayList(teacherQuizzes));
    }

    @FXML
    void showQuizManagement() {
        Main.showQuizManagement();
    }

    @FXML
    void showQuestionManagement() {
        Main.showQuestionManagement();
    }

    @FXML
    void showReports() {
        Main.showReports();
    }

    @FXML
    void handleLogout() {
        SessionContext.clearSession();
        Main.showLoginScreen();
    }
}
