package com.quizapp.controller;

import com.quizapp.Main;
import com.quizapp.model.*;
import com.quizapp.repository.SQLiteCategoryRepository;
import com.quizapp.repository.SQLiteQuestionRepository;
import com.quizapp.repository.SQLiteQuizRepository;
import com.quizapp.service.CategoryService;
import com.quizapp.service.QuestionService;
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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.*;

/**
 * Controller for the Quiz Management screen.
 */
public class QuizManagementController implements Initializable {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> categoryFilter;
    @FXML private ComboBox<String> difficultyFilter;
    @FXML private ComboBox<String> statusFilter;

    @FXML private TableView<Quiz> quizzesTable;
    @FXML private TableColumn<Quiz, Number> idColumn;
    @FXML private TableColumn<Quiz, String> titleColumn;
    @FXML private TableColumn<Quiz, String> categoryColumn;
    @FXML private TableColumn<Quiz, String> difficultyColumn;
    @FXML private TableColumn<Quiz, String> timeLimitColumn;
    @FXML private TableColumn<Quiz, String> maxAttemptsColumn;
    @FXML private TableColumn<Quiz, String> strategyColumn;
    @FXML private TableColumn<Quiz, String> statusColumn;

    private QuizService quizService;
    private QuestionService questionService;
    private CategoryService categoryService;

    private Map<Integer, String> categoryNameMap = new HashMap<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        SQLiteQuizRepository quizRepo = new SQLiteQuizRepository();
        SQLiteQuestionRepository questionRepo = new SQLiteQuestionRepository();
        SQLiteCategoryRepository categoryRepo = new SQLiteCategoryRepository();

        quizService = new QuizService(quizRepo, questionRepo, categoryRepo);
        questionService = new QuestionService(questionRepo, categoryRepo);
        categoryService = new CategoryService(categoryRepo);

        setupFilters();
        setupTableColumns();
        loadQuizzes();
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

        List<String> statuses = new ArrayList<>(List.of("All Statuses", "DRAFT", "PUBLISHED", "ACTIVE", "COMPLETED", "ARCHIVED"));
        statusFilter.setItems(FXCollections.observableArrayList(statuses));
        statusFilter.getSelectionModel().selectFirst();
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
        maxAttemptsColumn.setCellValueFactory(cell -> {
            int max = cell.getValue().getMaxAttempts();
            return new SimpleStringProperty(max > 0 ? String.valueOf(max) : "Unlimited");
        });
        strategyColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getScoringStrategy()));
        statusColumn.setCellValueFactory(cell -> {
            QuizStatus s = cell.getValue().getStatus();
            return new SimpleStringProperty(s != null ? s.name() : "N/A");
        });
    }

    private void loadQuizzes() {
        List<Quiz> all = quizService.findAll();
        quizzesTable.setItems(FXCollections.observableArrayList(all));
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

        String statStr = statusFilter.getValue();
        QuizStatus status = null;
        if (statStr != null && !statStr.equals("All Statuses")) {
            try {
                status = QuizStatus.valueOf(statStr);
            } catch (Exception ignored) {}
        }

        List<Quiz> results = quizService.searchQuizzes(keyword, catId, diff, status);
        quizzesTable.setItems(FXCollections.observableArrayList(results));
    }

    @FXML
    void handleResetFilter(ActionEvent event) {
        searchField.clear();
        categoryFilter.getSelectionModel().selectFirst();
        difficultyFilter.getSelectionModel().selectFirst();
        statusFilter.getSelectionModel().selectFirst();
        loadQuizzes();
    }

    @FXML
    void handleCreateQuiz(ActionEvent event) {
        showQuizFormDialog(null);
    }

    @FXML
    void handleEditQuiz(ActionEvent event) {
        Quiz selected = quizzesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showWarning("No Selection", "Please select a quiz to edit.");
            return;
        }
        if (selected.getStatus() != QuizStatus.DRAFT) {
            AlertHelper.showWarning("Cannot Edit", "Only quizzes in DRAFT status can be modified.");
            return;
        }
        showQuizFormDialog(selected);
    }

    private void showQuizFormDialog(Quiz existing) {
        Dialog<Quiz> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Create New Quiz" : "Edit Quiz");
        dialog.setHeaderText(existing == null ? "Enter quiz configuration details" : "Update quiz properties");

        ButtonType saveBtnType = new ButtonType("Save Quiz", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtnType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 100, 10, 10));

        TextField titleField = new TextField(existing != null ? existing.getTitle() : "");
        titleField.setPromptText("Quiz Title");
        TextArea descField = new TextArea(existing != null ? existing.getDescription() : "");
        descField.setPromptText("Short description...");
        descField.setPrefRowCount(3);

        ComboBox<Category> catBox = new ComboBox<>();
        catBox.setItems(FXCollections.observableArrayList(categoryService.findAll()));
        if (existing != null) {
            for (Category c : catBox.getItems()) {
                if (c.getId() == existing.getCategoryId()) {
                    catBox.setValue(c);
                    break;
                }
            }
        }
        if (catBox.getValue() == null && !catBox.getItems().isEmpty()) {
            catBox.getSelectionModel().selectFirst();
        }

        ComboBox<Difficulty> diffBox = new ComboBox<>(FXCollections.observableArrayList(Difficulty.values()));
        diffBox.setValue(existing != null && existing.getDifficulty() != null ? existing.getDifficulty() : Difficulty.MEDIUM);

        TextField timeLimitField = new TextField(existing != null ? String.valueOf(existing.getTimeLimitMinutes()) : "30");
        TextField maxAttemptsField = new TextField(existing != null ? String.valueOf(existing.getMaxAttempts()) : "0");

        ComboBox<String> stratBox = new ComboBox<>(FXCollections.observableArrayList("STANDARD", "NEGATIVE_MARKING", "TIME_BASED"));
        stratBox.setValue(existing != null && existing.getScoringStrategy() != null ? existing.getScoringStrategy() : "STANDARD");

        CheckBox shuffleCheck = new CheckBox("Shuffle Questions");
        shuffleCheck.setSelected(existing != null && existing.isShuffleQuestions());

        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descField, 1, 1);
        grid.add(new Label("Category:"), 0, 2);
        grid.add(catBox, 1, 2);
        grid.add(new Label("Difficulty:"), 0, 3);
        grid.add(diffBox, 1, 3);
        grid.add(new Label("Time Limit (mins, 0=None):"), 0, 4);
        grid.add(timeLimitField, 1, 4);
        grid.add(new Label("Max Attempts (0=Unlimited):"), 0, 5);
        grid.add(maxAttemptsField, 1, 5);
        grid.add(new Label("Scoring Strategy:"), 0, 6);
        grid.add(stratBox, 1, 6);
        grid.add(shuffleCheck, 1, 7);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveBtnType) {
                String title = titleField.getText().trim();
                if (title.isEmpty()) {
                    AlertHelper.showError("Validation Error", "Quiz title is required.");
                    return null;
                }
                int timeLimit = 0;
                try {
                    timeLimit = Integer.parseInt(timeLimitField.getText().trim());
                } catch (Exception ignored) {}

                int maxAttempts = 0;
                try {
                    maxAttempts = Integer.parseInt(maxAttemptsField.getText().trim());
                } catch (Exception ignored) {}

                Category cat = catBox.getValue();
                int catId = cat != null ? cat.getId() : 1;
                User user = SessionContext.getCurrentUser();
                int userId = user != null ? user.getId() : 1;

                if (existing == null) {
                    Quiz created = quizService.createQuiz(
                            title,
                            descField.getText().trim(),
                            catId,
                            diffBox.getValue(),
                            timeLimit,
                            maxAttempts,
                            stratBox.getValue(),
                            userId
                    );
                    created.setShuffleQuestions(shuffleCheck.isSelected());
                    quizService.updateQuiz(created);
                    return created;
                } else {
                    existing.setTitle(title);
                    existing.setDescription(descField.getText().trim());
                    existing.setCategoryId(catId);
                    existing.setDifficulty(diffBox.getValue());
                    existing.setTimeLimitMinutes(timeLimit);
                    existing.setMaxAttempts(maxAttempts);
                    existing.setScoringStrategy(stratBox.getValue());
                    existing.setShuffleQuestions(shuffleCheck.isSelected());
                    quizService.updateQuiz(existing);
                    return existing;
                }
            }
            return null;
        });

        Optional<Quiz> result = dialog.showAndWait();
        result.ifPresent(quiz -> {
            loadQuizzes();
            AlertHelper.showInfo("Success", "Quiz '" + quiz.getTitle() + "' saved successfully.");
        });
    }

    @FXML
    void handleDeleteQuiz(ActionEvent event) {
        Quiz selected = quizzesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showWarning("No Selection", "Please select a quiz to delete.");
            return;
        }
        boolean confirmed = AlertHelper.showConfirmation("Confirm Delete",
                "Are you sure you want to delete quiz '" + selected.getTitle() + "'? This cannot be undone.");
        if (confirmed) {
            try {
                quizService.deleteQuiz(selected.getId());
                loadQuizzes();
                AlertHelper.showInfo("Deleted", "Quiz was successfully deleted.");
            } catch (Exception e) {
                AlertHelper.showError("Delete Failed", e.getMessage());
            }
        }
    }

    @FXML
    void handlePublishQuiz(ActionEvent event) {
        Quiz selected = quizzesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showWarning("No Selection", "Please select a quiz to publish.");
            return;
        }
        try {
            quizService.publishQuiz(selected.getId());
            loadQuizzes();
            AlertHelper.showInfo("Published",
                    "Quiz '" + selected.getTitle() + "' is now PUBLISHED and available for students to attempt!");
        } catch (Exception e) {
            AlertHelper.showError("Publish Failed", e.getMessage());
        }
    }

    @FXML
    void handleUnpublishQuiz(ActionEvent event) {
        Quiz selected = quizzesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showWarning("No Selection", "Please select a quiz to unpublish.");
            return;
        }
        try {
            quizService.unpublishQuiz(selected.getId());
            loadQuizzes();
            AlertHelper.showInfo("Unpublished",
                    "Quiz '" + selected.getTitle() + "' was reverted to DRAFT status.");
        } catch (Exception e) {
            AlertHelper.showError("Unpublish Failed", e.getMessage());
        }
    }

    @FXML
    void handleArchiveQuiz(ActionEvent event) {
        Quiz selected = quizzesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showWarning("No Selection", "Please select a quiz to archive.");
            return;
        }
        try {
            quizService.archiveQuiz(selected.getId());
            loadQuizzes();
            AlertHelper.showInfo("Archived",
                    "Quiz '" + selected.getTitle() + "' has been ARCHIVED. No further attempts can be made.");
        } catch (Exception e) {
            AlertHelper.showError("Archive Failed", e.getMessage());
        }
    }

    @FXML
    void handleManageQuizQuestions(ActionEvent event) {
        Quiz selected = quizzesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showWarning("No Selection", "Please select a quiz to manage its questions.");
            return;
        }

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Manage Questions — " + selected.getTitle());
        dialog.setHeaderText("Add or remove questions from this quiz");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        VBox content = new VBox(12);
        content.setPadding(new Insets(10));
        content.setPrefWidth(650);

        Label currentLabel = new Label("Current Questions in this Quiz:");
        currentLabel.setStyle("-fx-font-weight: bold;");

        TableView<Question> quizQuestionsTable = new TableView<>();
        TableColumn<Question, Number> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getId()));
        TableColumn<Question, String> textCol = new TableColumn<>("Question");
        textCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getQuestionText()));
        textCol.setPrefWidth(350);
        TableColumn<Question, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getQuestionType().name()));
        TableColumn<Question, Number> marksCol = new TableColumn<>("Marks");
        marksCol.setCellValueFactory(c -> new SimpleIntegerProperty((int) c.getValue().getMarks()));

        quizQuestionsTable.getColumns().addAll(idCol, textCol, typeCol, marksCol);
        quizQuestionsTable.setPrefHeight(180);

        Runnable refreshQuizQuestions = () -> {
            List<Question> qs = quizService.getQuestionsForQuiz(selected.getId());
            quizQuestionsTable.setItems(FXCollections.observableArrayList(qs));
        };
        refreshQuizQuestions.run();

        // Remove button
        Button removeBtn = new Button("Remove Selected Question");
        removeBtn.getStyleClass().add("button-danger");
        removeBtn.setOnAction(e -> {
            Question q = quizQuestionsTable.getSelectionModel().getSelectedItem();
            if (q != null) {
                try {
                    quizService.removeQuestionFromQuiz(selected.getId(), q.getId());
                    refreshQuizQuestions.run();
                } catch (Exception ex) {
                    AlertHelper.showError("Error", ex.getMessage());
                }
            }
        });

        Separator sep = new Separator();

        Label bankLabel = new Label("Available Questions in Bank (click to add):");
        bankLabel.setStyle("-fx-font-weight: bold;");

        TableView<Question> bankTable = new TableView<>();
        TableColumn<Question, Number> bIdCol = new TableColumn<>("ID");
        bIdCol.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getId()));
        TableColumn<Question, String> bTextCol = new TableColumn<>("Question");
        bTextCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getQuestionText()));
        bTextCol.setPrefWidth(350);
        TableColumn<Question, String> bTypeCol = new TableColumn<>("Type");
        bTypeCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getQuestionType().name()));

        bankTable.getColumns().addAll(bIdCol, bTextCol, bTypeCol);
        bankTable.setPrefHeight(180);
        bankTable.setItems(FXCollections.observableArrayList(questionService.findAll()));

        Button addBtn = new Button("+ Add Selected Question to Quiz");
        addBtn.getStyleClass().add("button-success");
        addBtn.setOnAction(e -> {
            Question q = bankTable.getSelectionModel().getSelectedItem();
            if (q != null) {
                try {
                    quizService.addQuestionToQuiz(selected.getId(), q.getId());
                    refreshQuizQuestions.run();
                } catch (Exception ex) {
                    AlertHelper.showError("Cannot Add", ex.getMessage());
                }
            }
        });

        content.getChildren().addAll(currentLabel, quizQuestionsTable, removeBtn, sep, bankLabel, bankTable, addBtn);
        dialog.getDialogPane().setContent(content);
        dialog.showAndWait();
        loadQuizzes();
    }

    @FXML
    void handleBack(ActionEvent event) {
        Main.showTeacherDashboard();
    }
}
