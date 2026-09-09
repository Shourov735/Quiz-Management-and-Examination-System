package com.quizapp.controller;

import com.quizapp.Main;
import com.quizapp.model.Category;
import com.quizapp.model.Difficulty;
import com.quizapp.model.Question;
import com.quizapp.model.Quiz;
import com.quizapp.model.QuizAttempt;
import com.quizapp.model.QuizStatus;
import com.quizapp.model.User;
import com.quizapp.observer.QuizEventPublisher;
import com.quizapp.repository.*;
import com.quizapp.service.AttemptService;
import com.quizapp.service.CategoryService;
import com.quizapp.service.QuizService;
import com.quizapp.util.AlertHelper;
import com.quizapp.util.SessionContext;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Controller for the Student Dashboard screen.
 */
public class StudentDashboardController implements Initializable {

    @FXML private Label welcomeLabel;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> categoryFilter;
    @FXML private ComboBox<String> difficultyFilter;

    @FXML private TableView<Quiz> quizzesTable;
    @FXML private TableColumn<Quiz, Number> idColumn;
    @FXML private TableColumn<Quiz, String> titleColumn;
    @FXML private TableColumn<Quiz, String> categoryColumn;
    @FXML private TableColumn<Quiz, String> difficultyColumn;
    @FXML private TableColumn<Quiz, String> timeLimitColumn;
    @FXML private TableColumn<Quiz, String> attemptsAllowedColumn;
    @FXML private TableColumn<Quiz, String> attemptsMadeColumn;
    @FXML private TableColumn<Quiz, String> strategyColumn;

    private QuizService quizService;
    private AttemptService attemptService;
    private CategoryService categoryService;

    private Map<Integer, String> categoryNameMap = new HashMap<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        SQLiteQuizRepository quizRepo = new SQLiteQuizRepository();
        SQLiteQuestionRepository questionRepo = new SQLiteQuestionRepository();
        SQLiteCategoryRepository categoryRepo = new SQLiteCategoryRepository();
        SQLiteQuizAttemptRepository attemptRepo = new SQLiteQuizAttemptRepository();
        SQLiteAnswerRepository answerRepo = new SQLiteAnswerRepository();

        quizService = new QuizService(quizRepo, questionRepo, categoryRepo);
        attemptService = new AttemptService(attemptRepo, quizRepo, questionRepo, answerRepo, new QuizEventPublisher());
        categoryService = new CategoryService(categoryRepo);

        User current = SessionContext.getCurrentUser();
        if (current != null) {
            welcomeLabel.setText("Welcome, " + current.getName());
        }

        setupFilters();
        setupTableColumns();
        loadAvailableQuizzes();
    }

    private void setupFilters() {
        categoryNameMap.clear();
        List<String> catNames = new ArrayList<>();
        catNames.add("All Categories");
        for (Category c : categoryService.findAll()) {
            catNames.add(c.getName());
            categoryNameMap.put(c.getId(), c.getName());
        }
        categoryFilter.setItems(FXCollections.observableArrayList(catNames));
        categoryFilter.getSelectionModel().selectFirst();

        List<String> diffs = new ArrayList<>(List.of("All Difficulties", "EASY", "MEDIUM", "HARD"));
        difficultyFilter.setItems(FXCollections.observableArrayList(diffs));
        difficultyFilter.getSelectionModel().selectFirst();
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getId()));
        titleColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTitle()));
        categoryColumn.setCellValueFactory(cell -> {
            String name = categoryNameMap.getOrDefault(cell.getValue().getCategoryId(), "General");
            return new SimpleStringProperty(name);
        });
        difficultyColumn.setCellValueFactory(cell -> {
            Difficulty d = cell.getValue().getDifficulty();
            return new SimpleStringProperty(d != null ? d.name() : "N/A");
        });
        timeLimitColumn.setCellValueFactory(cell -> {
            int mins = cell.getValue().getTimeLimitMinutes();
            return new SimpleStringProperty(mins > 0 ? mins + " mins" : "No Limit");
        });
        attemptsAllowedColumn.setCellValueFactory(cell -> {
            int max = cell.getValue().getMaxAttempts();
            return new SimpleStringProperty(max > 0 ? String.valueOf(max) : "Unlimited");
        });
        attemptsMadeColumn.setCellValueFactory(cell -> {
            User current = SessionContext.getCurrentUser();
            if (current != null) {
                int count = attemptService.findByStudentAndQuiz(current.getId(), cell.getValue().getId()).size();
                return new SimpleStringProperty(String.valueOf(count));
            }
            return new SimpleStringProperty("0");
        });
        strategyColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getScoringStrategy()));
    }

    private void loadAvailableQuizzes() {
        List<Quiz> published = quizService.findByStatus(QuizStatus.PUBLISHED);
        quizzesTable.setItems(FXCollections.observableArrayList(published));
    }

    @FXML
    void handleSearch(ActionEvent event) {
        String keyword = searchField.getText() != null ? searchField.getText().trim() : null;
        if (keyword != null && keyword.isEmpty()) keyword = null;

        String selectedCat = categoryFilter.getValue();
        int catId = 0;
        if (selectedCat != null && !selectedCat.equals("All Categories")) {
            for (Map.Entry<Integer, String> entry : categoryNameMap.entrySet()) {
                if (entry.getValue().equalsIgnoreCase(selectedCat)) {
                    catId = entry.getKey();
                    break;
                }
            }
        }

        String diff = difficultyFilter.getValue();
        if (diff != null && diff.equals("All Difficulties")) diff = null;

        List<Quiz> results = quizService.searchQuizzes(keyword, catId, diff, QuizStatus.PUBLISHED);
        quizzesTable.setItems(FXCollections.observableArrayList(results));
    }

    @FXML
    void handleResetFilter(ActionEvent event) {
        searchField.clear();
        categoryFilter.getSelectionModel().selectFirst();
        difficultyFilter.getSelectionModel().selectFirst();
        loadAvailableQuizzes();
    }

    @FXML
    void handleStartQuiz(ActionEvent event) {
        Quiz selected = quizzesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showWarning("No Quiz Selected", "Please choose an examination to start.");
            return;
        }

        User current = SessionContext.getCurrentUser();
        if (current == null) {
            AlertHelper.showError("Not Authenticated", "Please sign in again.");
            Main.showLoginScreen();
            return;
        }

        // Business Rule: Validate attempt eligibility
        if (!attemptService.canAttempt(current.getId(), selected.getId())) {
            AlertHelper.showWarning("Attempt Limit Exceeded",
                    "You have already reached the maximum allowed attempts ("
                            + selected.getMaxAttempts() + ") for this examination.");
            return;
        }

        // Verify quiz has questions
        List<Question> questions = quizService.getQuestionsForQuiz(selected.getId());
        if (questions.isEmpty()) {
            AlertHelper.showWarning("Unavailable", "This quiz does not currently have questions.");
            return;
        }

        boolean startConfirmed = AlertHelper.showConfirmation("Start Examination",
                "Ready to start '" + selected.getTitle() + "'?\n"
                        + "Time Limit: " + (selected.getTimeLimitMinutes() > 0 ? selected.getTimeLimitMinutes() + " minutes" : "No Limit") + "\n"
                        + "Total Questions: " + questions.size() + "\n\n"
                        + "Your timer will start immediately.");

        if (startConfirmed) {
            SessionContext.setSelectedQuizId(selected.getId());
            Main.showQuizAttempt(selected.getId());
        }
    }

    @FXML
    void handleViewAttempts(ActionEvent event) {
        User current = SessionContext.getCurrentUser();
        if (current == null) return;

        List<QuizAttempt> attempts = attemptService.findByStudent(current.getId());

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("My Examination History");
        dialog.setHeaderText("All past attempts and recorded scores");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        content.setPrefWidth(600);

        TableView<QuizAttempt> table = new TableView<>();
        TableColumn<QuizAttempt, Number> idCol = new TableColumn<>("Attempt #");
        idCol.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getId()));
        TableColumn<QuizAttempt, String> quizCol = new TableColumn<>("Quiz ID");
        quizCol.setCellValueFactory(c -> new SimpleStringProperty("Quiz #" + c.getValue().getQuizId()));
        TableColumn<QuizAttempt, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus().name()));
        TableColumn<QuizAttempt, String> scoreCol = new TableColumn<>("Score");
        scoreCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getScore() + " / " + c.getValue().getTotalMarks()));
        TableColumn<QuizAttempt, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(c -> {
            if (c.getValue().getStartTime() != null) {
                return new SimpleStringProperty(c.getValue().getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            }
            return new SimpleStringProperty("N/A");
        });

        table.getColumns().addAll(idCol, quizCol, statusCol, scoreCol, dateCol);
        table.setItems(FXCollections.observableArrayList(attempts));
        table.setPrefHeight(260);

        content.getChildren().add(table);
        dialog.getDialogPane().setContent(content);
        dialog.showAndWait();
    }

    @FXML
    void handleViewPerformance(ActionEvent event) {
        Main.showReports();
    }

    @FXML
    void handleLogout(ActionEvent event) {
        SessionContext.clearSession();
        Main.showLoginScreen();
    }
}
