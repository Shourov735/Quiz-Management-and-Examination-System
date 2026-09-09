package com.quizapp;

import com.quizapp.database.DatabaseConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

/**
 * Main JavaFX Application entry point for the Quiz Management & Examination System.
 *
 * <p>Initializes the database connection, runs the schema/seeder if necessary,
 * then launches the Login screen as the primary stage.</p>
 */
public class Main extends Application {

    /** Application window title. */
    public static final String APP_TITLE = "Quiz Management & Examination System";

    /** Minimum window width. */
    public static final double MIN_WIDTH = 900;

    /** Minimum window height. */
    public static final double MIN_HEIGHT = 650;

    /** Shared primary stage reference (accessible to controllers for scene switching). */
    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;

        // Initialise database (creates tables and seeds data on first run)
        DatabaseConnection.getInstance().initializeDatabase();

        // Load the login screen
        showLoginScreen();

        stage.setTitle(APP_TITLE);
        stage.setMinWidth(MIN_WIDTH);
        stage.setMinHeight(MIN_HEIGHT);
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        // Close database connection gracefully
        DatabaseConnection.getInstance().closeConnection();
        super.stop();
    }

    // -------------------------------------------------------------------------
    // Scene navigation helpers
    // -------------------------------------------------------------------------

    /**
     * Navigate to the Login screen.
     */
    public static void showLoginScreen() {
        switchScene("/fxml/login.fxml", "Login");
    }

    /**
     * Navigate to the Teacher Dashboard.
     */
    public static void showTeacherDashboard() {
        switchScene("/fxml/teacher_dashboard.fxml", "Teacher Dashboard");
    }

    /**
     * Navigate to the Student Dashboard.
     */
    public static void showStudentDashboard() {
        switchScene("/fxml/student_dashboard.fxml", "Student Dashboard");
    }

    /**
     * Navigate to the Quiz Management screen.
     */
    public static void showQuizManagement() {
        switchScene("/fxml/quiz_management.fxml", "Quiz Management");
    }

    /**
     * Navigate to the Question Management screen.
     */
    public static void showQuestionManagement() {
        switchScene("/fxml/question_management.fxml", "Question Management");
    }

    /**
     * Navigate to the Quiz Attempt screen.
     *
     * @param quizId the quiz to attempt
     */
    public static void showQuizAttempt(int quizId) {
        switchScene("/fxml/quiz_attempt.fxml", "Quiz Attempt");
        // The controller will receive quizId via a static setter before scene switch if needed
    }

    /**
     * Navigate to the Result screen.
     */
    public static void showResult() {
        switchScene("/fxml/result.fxml", "Result");
    }

    /**
     * Navigate to the Reports screen.
     */
    public static void showReports() {
        switchScene("/fxml/reports.fxml", "Reports");
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    /**
     * Load an FXML scene and replace the current primary stage scene.
     *
     * @param fxmlPath  classpath-relative path to the FXML file
     * @param title     window sub-title appended after the app title
     */
    public static void switchScene(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    Main.class.getResource(fxmlPath));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                    Objects.requireNonNull(
                            Main.class.getResource("/css/style.css")).toExternalForm());
            primaryStage.setScene(scene);
            primaryStage.setTitle(APP_TITLE + " — " + title);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load FXML: " + fxmlPath, e);
        }
    }

    /**
     * Load an FXML with a specific loader so the caller can retrieve the controller.
     *
     * @param fxmlPath classpath-relative path to the FXML file
     * @return the FXMLLoader after loading (controller accessible via {@code loader.getController()})
     */
    public static FXMLLoader loadFXML(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource(fxmlPath));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                    Objects.requireNonNull(
                            Main.class.getResource("/css/style.css")).toExternalForm());
            primaryStage.setScene(scene);
            return loader;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load FXML: " + fxmlPath, e);
        }
    }

    /**
     * Returns the shared primary stage.
     */
    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    /**
     * Application entry point.
     */
    public static void main(String[] args) {
        launch(args);
    }
}
