package com.quizapp.controller;

import com.quizapp.Main;
import com.quizapp.model.*;
import com.quizapp.observer.ProgressTracker;
import com.quizapp.observer.QuizEvent;
import com.quizapp.observer.QuizEventPublisher;
import com.quizapp.repository.*;
import com.quizapp.service.AttemptService;
import com.quizapp.service.QuizService;
import com.quizapp.util.AlertHelper;
import com.quizapp.util.SessionContext;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.net.URL;
import java.util.*;

/**
 * Controller for taking a timed/untimed quiz examination.
 */
public class QuizAttemptController implements Initializable {

    @FXML private Label quizTitleLabel;
    @FXML private Label progressLabel;
    @FXML private Label timerLabel;
    @FXML private FlowPane questionNavPane;

    @FXML private Label questionNumberBadge;
    @FXML private Label questionTypeBadge;
    @FXML private Label questionMarksLabel;
    @FXML private Label questionTextLabel;
    @FXML private VBox answerContainer;

    @FXML private Button prevButton;
    @FXML private Button nextButton;
    @FXML private Button submitButton;

    private QuizService quizService;
    private AttemptService attemptService;
    private QuizEventPublisher eventPublisher;
    private ProgressTracker progressTracker;

    private Quiz quiz;
    private QuizAttempt attempt;
    private List<Question> questions = new ArrayList<>();
    private int currentIndex = 0;

    // Cache of student responses: questionId -> answer string
    private final Map<Integer, String> answeredMap = new HashMap<>();

    // Timer
    private Timeline countdownTimeline;
    private int remainingSeconds;

    // References to dynamically created controls for current question
    private ToggleGroup singleChoiceGroup;
    private TextField fillBlankField;
    private List<CheckBox> multipleChoiceBoxes = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        SQLiteQuizRepository quizRepo = new SQLiteQuizRepository();
        SQLiteQuestionRepository questionRepo = new SQLiteQuestionRepository();
        SQLiteCategoryRepository categoryRepo = new SQLiteCategoryRepository();
        SQLiteQuizAttemptRepository attemptRepo = new SQLiteQuizAttemptRepository();
        SQLiteAnswerRepository answerRepo = new SQLiteAnswerRepository();

        eventPublisher = new QuizEventPublisher();
        quizService = new QuizService(quizRepo, questionRepo, categoryRepo);
        attemptService = new AttemptService(attemptRepo, quizRepo, questionRepo, answerRepo, eventPublisher);

        int quizId = SessionContext.getSelectedQuizId();
        User student = SessionContext.getCurrentUser();

        if (quizId <= 0 || student == null) {
            AlertHelper.showError("Session Error", "No quiz or student session found.");
            Main.showStudentDashboard();
            return;
        }

        try {
            quiz = quizService.findById(quizId).orElseThrow(() -> new IllegalStateException("Quiz not found"));
            quizTitleLabel.setText(quiz.getTitle());

            // Start attempt
            attempt = attemptService.startAttempt(student.getId(), quizId);
            SessionContext.setCurrentAttemptId(attempt.getId());

            // Load questions
            questions = quizService.getQuestionsForQuiz(quizId);
            if (quiz.isShuffleQuestions()) {
                Collections.shuffle(questions);
            }

            // Register ProgressTracker Observer
            progressTracker = new ProgressTracker(questions.size());
            eventPublisher.subscribe(QuizEvent.ANSWER_SUBMITTED, progressTracker);
            eventPublisher.subscribe(QuizEvent.ATTEMPT_STARTED, progressTracker);

            // Load any pre-existing answers
            List<Answer> existingAnswers = attemptService.getAnswersForAttempt(attempt.getId());
            for (Answer a : existingAnswers) {
                if (a.getAnswerValue() != null && !a.getAnswerValue().isBlank()) {
                    answeredMap.put(a.getQuestionId(), a.getAnswerValue());
                }
            }

            setupTimer();
            buildQuestionNavButtons();
            showQuestion(0);

        } catch (Exception e) {
            AlertHelper.showError("Failed to Start Quiz", e.getMessage());
            Main.showStudentDashboard();
        }
    }

    private void setupTimer() {
        if (quiz.getTimeLimitMinutes() > 0) {
            remainingSeconds = quiz.getTimeLimitMinutes() * 60;
            updateTimerDisplay();

            countdownTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
                remainingSeconds--;
                updateTimerDisplay();
                if (remainingSeconds <= 0) {
                    countdownTimeline.stop();
                    handleTimeExpired();
                }
            }));
            countdownTimeline.setCycleCount(Timeline.INDEFINITE);
            countdownTimeline.play();
        } else {
            timerLabel.setText("No Limit");
            timerLabel.setStyle("-fx-text-fill: #27ae60;");
        }
    }

    private void updateTimerDisplay() {
        int mins = remainingSeconds / 60;
        int secs = remainingSeconds % 60;
        timerLabel.setText(String.format("%02d:%02d", mins, secs));
        if (remainingSeconds < 120) {
            timerLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        }
    }

    private void handleTimeExpired() {
        saveCurrentAnswer();
        try {
            attemptService.timeoutAttempt(attempt.getId());
            AlertHelper.showWarning("Time Expired", "Examination time has ended. Your answers have been submitted.");
            Main.showResult();
        } catch (Exception ex) {
            AlertHelper.showError("Error", ex.getMessage());
            Main.showStudentDashboard();
        }
    }

    private void buildQuestionNavButtons() {
        questionNavPane.getChildren().clear();
        for (int i = 0; i < questions.size(); i++) {
            final int index = i;
            Button navBtn = new Button(String.valueOf(i + 1));
            navBtn.setPrefSize(34, 34);
            navBtn.setStyle("-fx-background-radius: 50%; -fx-font-size: 11px; -fx-font-weight: bold;");
            updateNavButtonStyle(navBtn, i);

            navBtn.setOnAction(e -> {
                saveCurrentAnswer();
                showQuestion(index);
            });
            questionNavPane.getChildren().add(navBtn);
        }
    }

    private void updateNavButtonStyle(Button btn, int index) {
        if (index == currentIndex) {
            btn.setStyle("-fx-background-radius: 50%; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-color: #3498db; -fx-text-fill: white;");
        } else if (answeredMap.containsKey(questions.get(index).getId())) {
            btn.setStyle("-fx-background-radius: 50%; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-color: #27ae60; -fx-text-fill: white;");
        } else {
            btn.setStyle("-fx-background-radius: 50%; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-color: white; -fx-border-color: #bdc3c7; -fx-border-radius: 50%; -fx-text-fill: #2c3e50;");
        }
    }

    private void showQuestion(int index) {
        if (index < 0 || index >= questions.size()) return;
        currentIndex = index;

        Question q = questions.get(index);
        questionNumberBadge.setText("Q" + (index + 1));
        questionTypeBadge.setText(q.getQuestionType().name().replace('_', ' '));
        questionMarksLabel.setText(q.getMarks() + (q.getMarks() == 1.0 ? " Mark" : " Marks"));
        questionTextLabel.setText(q.getQuestionText());
        progressLabel.setText("Question " + (index + 1) + " of " + questions.size());

        prevButton.setDisable(index == 0);
        nextButton.setDisable(index == questions.size() - 1);

        // Rebuild answer container
        answerContainer.getChildren().clear();
        answerContainer.getChildren().add(new Label("Select / Enter your answer:"));

        String savedAnswer = answeredMap.get(q.getId());

        singleChoiceGroup = null;
        fillBlankField = null;
        multipleChoiceBoxes.clear();

        if (q.getQuestionType() == QuestionType.MCQ || q.getQuestionType() == QuestionType.TRUE_FALSE) {
            singleChoiceGroup = new ToggleGroup();
            for (QuestionOption opt : q.getOptions()) {
                RadioButton rb = new RadioButton(opt.getOptionText());
                rb.setToggleGroup(singleChoiceGroup);
                rb.setStyle("-fx-font-size: 14px; -fx-padding: 6 10;");
                if (savedAnswer != null && (savedAnswer.equalsIgnoreCase(opt.getOptionText()) || savedAnswer.equals(String.valueOf(opt.getId())))) {
                    rb.setSelected(true);
                }
                answerContainer.getChildren().add(rb);
            }
        } else if (q.getQuestionType() == QuestionType.FILL_BLANK) {
            fillBlankField = new TextField(savedAnswer != null ? savedAnswer : "");
            fillBlankField.setPromptText("Type your exact answer here...");
            fillBlankField.setPrefHeight(38);
            fillBlankField.setStyle("-fx-font-size: 14px;");
            answerContainer.getChildren().add(fillBlankField);
        } else if (q.getQuestionType() == QuestionType.MULTIPLE_ANSWER) {
            List<String> selectedList = savedAnswer != null
                    ? Arrays.asList(savedAnswer.split(","))
                    : Collections.emptyList();

            for (QuestionOption opt : q.getOptions()) {
                CheckBox cb = new CheckBox(opt.getOptionText());
                cb.setStyle("-fx-font-size: 14px; -fx-padding: 6 10;");
                if (selectedList.contains(opt.getOptionText()) || selectedList.contains(String.valueOf(opt.getId()))) {
                    cb.setSelected(true);
                }
                multipleChoiceBoxes.add(cb);
                answerContainer.getChildren().add(cb);
            }
        }

        // Update navigator buttons colors
        for (int i = 0; i < questionNavPane.getChildren().size(); i++) {
            Button btn = (Button) questionNavPane.getChildren().get(i);
            updateNavButtonStyle(btn, i);
        }
    }

    private void saveCurrentAnswer() {
        if (questions.isEmpty() || currentIndex >= questions.size()) return;
        Question q = questions.get(currentIndex);
        String answerValue = "";

        if (q.getQuestionType() == QuestionType.MCQ || q.getQuestionType() == QuestionType.TRUE_FALSE) {
            if (singleChoiceGroup != null && singleChoiceGroup.getSelectedToggle() != null) {
                RadioButton rb = (RadioButton) singleChoiceGroup.getSelectedToggle();
                answerValue = rb.getText();
            }
        } else if (q.getQuestionType() == QuestionType.FILL_BLANK) {
            if (fillBlankField != null) {
                answerValue = fillBlankField.getText().trim();
            }
        } else if (q.getQuestionType() == QuestionType.MULTIPLE_ANSWER) {
            List<String> chosen = new ArrayList<>();
            for (CheckBox cb : multipleChoiceBoxes) {
                if (cb.isSelected()) {
                    chosen.add(cb.getText());
                }
            }
            answerValue = String.join(",", chosen);
        }

        if (!answerValue.isBlank()) {
            answeredMap.put(q.getId(), answerValue);
            try {
                attemptService.submitAnswer(attempt.getId(), q.getId(), answerValue);
            } catch (Exception ex) {
                System.err.println("Failed to autosave answer: " + ex.getMessage());
            }
        }
    }

    @FXML
    void handlePrevious(ActionEvent event) {
        saveCurrentAnswer();
        if (currentIndex > 0) {
            showQuestion(currentIndex - 1);
        }
    }

    @FXML
    void handleNext(ActionEvent event) {
        saveCurrentAnswer();
        if (currentIndex < questions.size() - 1) {
            showQuestion(currentIndex + 1);
        }
    }

    @FXML
    void handleSubmit(ActionEvent event) {
        saveCurrentAnswer();

        int answeredCount = answeredMap.size();
        int total = questions.size();
        int unanswered = total - answeredCount;

        String msg = "You have answered " + answeredCount + " of " + total + " questions.\n";
        if (unanswered > 0) {
            msg += "⚠ Warning: You have " + unanswered + " unanswered question(s).\n";
        }
        msg += "Are you sure you want to submit your examination?";

        boolean confirm = AlertHelper.showConfirmation("Submit Examination", msg);
        if (confirm) {
            if (countdownTimeline != null) {
                countdownTimeline.stop();
            }
            try {
                attemptService.submitAttempt(attempt.getId());
                Main.showResult();
            } catch (Exception ex) {
                AlertHelper.showError("Submission Failed", ex.getMessage());
            }
        }
    }
}
