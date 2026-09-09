package com.quizapp.controller;

import com.quizapp.Main;
import com.quizapp.factory.QuestionFactory;
import com.quizapp.model.*;
import com.quizapp.repository.SQLiteCategoryRepository;
import com.quizapp.repository.SQLiteQuestionRepository;
import com.quizapp.service.CategoryService;
import com.quizapp.service.QuestionService;
import com.quizapp.util.AlertHelper;
import com.quizapp.util.SessionContext;
import javafx.beans.property.SimpleDoubleProperty;
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
 * Controller for managing the Question Bank.
 */
public class QuestionManagementController implements Initializable {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> typeFilter;
    @FXML private ComboBox<String> categoryFilter;
    @FXML private ComboBox<String> difficultyFilter;

    @FXML private TableView<Question> questionsTable;
    @FXML private TableColumn<Question, Number> idColumn;
    @FXML private TableColumn<Question, String> textColumn;
    @FXML private TableColumn<Question, String> typeColumn;
    @FXML private TableColumn<Question, Number> marksColumn;
    @FXML private TableColumn<Question, String> difficultyColumn;
    @FXML private TableColumn<Question, String> categoryColumn;

    private QuestionService questionService;
    private CategoryService categoryService;
    private Map<Integer, String> categoryNameMap = new HashMap<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        SQLiteQuestionRepository questionRepo = new SQLiteQuestionRepository();
        SQLiteCategoryRepository categoryRepo = new SQLiteCategoryRepository();

        questionService = new QuestionService(questionRepo, categoryRepo);
        categoryService = new CategoryService(categoryRepo);

        setupFilters();
        setupTableColumns();
        loadQuestions();
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

        List<String> types = new ArrayList<>();
        types.add("All Types");
        for (QuestionType qt : QuestionType.values()) {
            types.add(qt.name());
        }
        typeFilter.setItems(FXCollections.observableArrayList(types));
        typeFilter.getSelectionModel().selectFirst();

        List<String> diffs = new ArrayList<>(List.of("All Difficulties", "EASY", "MEDIUM", "HARD"));
        difficultyFilter.setItems(FXCollections.observableArrayList(diffs));
        difficultyFilter.getSelectionModel().selectFirst();
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getId()));
        textColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getQuestionText()));
        typeColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getQuestionType().name()));
        marksColumn.setCellValueFactory(cell -> new SimpleDoubleProperty(cell.getValue().getMarks()));
        difficultyColumn.setCellValueFactory(cell -> {
            Difficulty d = cell.getValue().getDifficulty();
            return new SimpleStringProperty(d != null ? d.name() : "N/A");
        });
        categoryColumn.setCellValueFactory(cell -> {
            String name = categoryNameMap.getOrDefault(cell.getValue().getCategoryId(), "General");
            return new SimpleStringProperty(name);
        });
    }

    private void loadQuestions() {
        List<Question> all = questionService.findAll();
        questionsTable.setItems(FXCollections.observableArrayList(all));
    }

    @FXML
    void handleSearch(ActionEvent event) {
        String keyword = searchField.getText() != null ? searchField.getText().trim() : null;
        if (keyword != null && keyword.isEmpty()) keyword = null;

        String selectedType = typeFilter.getValue();
        QuestionType type = null;
        if (selectedType != null && !selectedType.equals("All Types")) {
            try {
                type = QuestionType.valueOf(selectedType);
            } catch (Exception ignored) {}
        }

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

        List<Question> results = questionService.search(keyword, type, catId, diff);
        questionsTable.setItems(FXCollections.observableArrayList(results));
    }

    @FXML
    void handleResetFilter(ActionEvent event) {
        searchField.clear();
        typeFilter.getSelectionModel().selectFirst();
        categoryFilter.getSelectionModel().selectFirst();
        difficultyFilter.getSelectionModel().selectFirst();
        loadQuestions();
    }

    @FXML
    void handleCreateQuestion(ActionEvent event) {
        Dialog<Question> dialog = new Dialog<>();
        dialog.setTitle("Create New Question");
        dialog.setHeaderText("Add a question to the central question bank");

        ButtonType saveBtn = new ButtonType("Save Question", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        VBox root = new VBox(14);
        root.setPadding(new Insets(15));
        root.setPrefWidth(550);

        GridPane metaGrid = new GridPane();
        metaGrid.setHgap(10);
        metaGrid.setVgap(10);

        ComboBox<QuestionType> typeBox = new ComboBox<>(FXCollections.observableArrayList(QuestionType.values()));
        typeBox.setValue(QuestionType.MCQ);

        TextArea questionTextArea = new TextArea();
        questionTextArea.setPromptText("Enter the question statement here...");
        questionTextArea.setPrefRowCount(3);

        TextField marksField = new TextField("1.0");
        ComboBox<Difficulty> diffBox = new ComboBox<>(FXCollections.observableArrayList(Difficulty.values()));
        diffBox.setValue(Difficulty.MEDIUM);

        ComboBox<Category> catBox = new ComboBox<>(FXCollections.observableArrayList(categoryService.findAll()));
        if (!catBox.getItems().isEmpty()) catBox.getSelectionModel().selectFirst();

        metaGrid.add(new Label("Question Type:"), 0, 0);
        metaGrid.add(typeBox, 1, 0);
        metaGrid.add(new Label("Question Text:"), 0, 1);
        metaGrid.add(questionTextArea, 1, 1);
        metaGrid.add(new Label("Marks:"), 0, 2);
        metaGrid.add(marksField, 1, 2);
        metaGrid.add(new Label("Difficulty:"), 0, 3);
        metaGrid.add(diffBox, 1, 3);
        metaGrid.add(new Label("Category:"), 0, 4);
        metaGrid.add(catBox, 1, 4);

        VBox dynamicOptionsBox = new VBox(8);
        dynamicOptionsBox.setStyle("-fx-border-color: #dee2e6; -fx-padding: 10; -fx-background-color: #f8f9fa; -fx-background-radius: 6;");

        // Helper to rebuild options UI based on type
        Runnable updateOptionsUI = () -> {
            dynamicOptionsBox.getChildren().clear();
            QuestionType qt = typeBox.getValue();
            if (qt == QuestionType.MCQ) {
                dynamicOptionsBox.getChildren().add(new Label("MCQ Options (select the radio for the correct option):"));
                ToggleGroup tg = new ToggleGroup();
                for (int i = 1; i <= 4; i++) {
                    HBox row = new HBox(8);
                    RadioButton rb = new RadioButton();
                    rb.setToggleGroup(tg);
                    if (i == 1) rb.setSelected(true);
                    TextField tf = new TextField();
                    tf.setPromptText("Option " + i);
                    tf.setPrefWidth(350);
                    row.getChildren().addAll(rb, tf);
                    dynamicOptionsBox.getChildren().add(row);
                }
            } else if (qt == QuestionType.TRUE_FALSE) {
                dynamicOptionsBox.getChildren().add(new Label("Correct answer is:"));
                ToggleGroup tg = new ToggleGroup();
                RadioButton rTrue = new RadioButton("True");
                rTrue.setToggleGroup(tg);
                rTrue.setSelected(true);
                RadioButton rFalse = new RadioButton("False");
                rFalse.setToggleGroup(tg);
                dynamicOptionsBox.getChildren().addAll(rTrue, rFalse);
            } else if (qt == QuestionType.FILL_BLANK) {
                dynamicOptionsBox.getChildren().add(new Label("Exact correct text answer:"));
                TextField tf = new TextField();
                tf.setPromptText("e.g. Polymorphism or 42");
                dynamicOptionsBox.getChildren().add(tf);
            } else if (qt == QuestionType.MULTIPLE_ANSWER) {
                dynamicOptionsBox.getChildren().add(new Label("Options (check all options that are correct):"));
                for (int i = 1; i <= 4; i++) {
                    HBox row = new HBox(8);
                    CheckBox cb = new CheckBox();
                    TextField tf = new TextField();
                    tf.setPromptText("Option " + i);
                    tf.setPrefWidth(350);
                    row.getChildren().addAll(cb, tf);
                    dynamicOptionsBox.getChildren().add(row);
                }
            }
        };

        typeBox.setOnAction(e -> updateOptionsUI.run());
        updateOptionsUI.run();

        root.getChildren().addAll(metaGrid, dynamicOptionsBox);
        dialog.getDialogPane().setContent(root);

        dialog.setResultConverter(btn -> {
            if (btn == saveBtn) {
                String text = questionTextArea.getText().trim();
                if (text.isEmpty()) {
                    AlertHelper.showError("Validation Error", "Question text cannot be empty.");
                    return null;
                }
                double marks = 1.0;
                try {
                    marks = Double.parseDouble(marksField.getText().trim());
                } catch (Exception ignored) {}

                QuestionType qt = typeBox.getValue();
                Difficulty diff = diffBox.getValue();
                Category cat = catBox.getValue();
                int catId = cat != null ? cat.getId() : 1;
                User user = SessionContext.getCurrentUser();
                int teacherId = user != null ? user.getId() : 1;

                List<QuestionOption> options = new ArrayList<>();

                if (qt == QuestionType.MCQ) {
                    int order = 1;
                    for (int i = 1; i < dynamicOptionsBox.getChildren().size(); i++) {
                        HBox row = (HBox) dynamicOptionsBox.getChildren().get(i);
                        RadioButton rb = (RadioButton) row.getChildren().get(0);
                        TextField tf = (TextField) row.getChildren().get(1);
                        String optText = tf.getText().trim();
                        if (!optText.isEmpty()) {
                            options.add(new QuestionOption(0, 0, optText, rb.isSelected(), order++));
                        }
                    }
                } else if (qt == QuestionType.TRUE_FALSE) {
                    RadioButton rTrue = (RadioButton) dynamicOptionsBox.getChildren().get(1);
                    options.add(new QuestionOption(0, 0, "True", rTrue.isSelected(), 1));
                    options.add(new QuestionOption(0, 0, "False", !rTrue.isSelected(), 2));
                } else if (qt == QuestionType.FILL_BLANK) {
                    TextField tf = (TextField) dynamicOptionsBox.getChildren().get(1);
                    String val = tf.getText().trim();
                    options.add(new QuestionOption(0, 0, val, true, 1));
                } else if (qt == QuestionType.MULTIPLE_ANSWER) {
                    int order = 1;
                    for (int i = 1; i < dynamicOptionsBox.getChildren().size(); i++) {
                        HBox row = (HBox) dynamicOptionsBox.getChildren().get(i);
                        CheckBox cb = (CheckBox) row.getChildren().get(0);
                        TextField tf = (TextField) row.getChildren().get(1);
                        String optText = tf.getText().trim();
                        if (!optText.isEmpty()) {
                            options.add(new QuestionOption(0, 0, optText, cb.isSelected(), order++));
                        }
                    }
                }

                try {
                    return questionService.createQuestion(qt, text, marks, diff, catId, teacherId, options);
                } catch (Exception ex) {
                    AlertHelper.showError("Creation Failed", ex.getMessage());
                    return null;
                }
            }
            return null;
        });

        Optional<Question> created = dialog.showAndWait();
        created.ifPresent(q -> {
            loadQuestions();
            AlertHelper.showInfo("Success", "Question added to bank.");
        });
    }

    @FXML
    void handleViewDetails(ActionEvent event) {
        Question selected = questionsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showWarning("No Selection", "Please select a question to view details.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Question ID: ").append(selected.getId()).append("\n");
        sb.append("Type: ").append(selected.getQuestionType()).append("\n");
        sb.append("Marks: ").append(selected.getMarks()).append("\n\n");
        sb.append("Expected Correct Answer:\n").append(selected.getCorrectAnswerDisplay()).append("\n\n");
        sb.append("Configured Options:\n");

        if (selected.getOptions() != null) {
            for (QuestionOption opt : selected.getOptions()) {
                sb.append(" • ").append(opt.getOptionText());
                if (opt.isCorrect()) sb.append(" [CORRECT]");
                sb.append("\n");
            }
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Question Details");
        alert.setHeaderText(selected.getQuestionText());
        alert.setContentText(sb.toString());
        alert.showAndWait();
    }

    @FXML
    void handleDeleteQuestion(ActionEvent event) {
        Question selected = questionsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showWarning("No Selection", "Please select a question to delete.");
            return;
        }
        boolean confirmed = AlertHelper.showConfirmation("Delete Question",
                "Delete question: \"" + selected.getQuestionText() + "\"?");
        if (confirmed) {
            try {
                questionService.deleteQuestion(selected.getId());
                loadQuestions();
                AlertHelper.showInfo("Deleted", "Question was deleted from bank.");
            } catch (Exception ex) {
                AlertHelper.showError("Delete Failed", ex.getMessage());
            }
        }
    }

    @FXML
    void handleBack(ActionEvent event) {
        Main.showTeacherDashboard();
    }
}
