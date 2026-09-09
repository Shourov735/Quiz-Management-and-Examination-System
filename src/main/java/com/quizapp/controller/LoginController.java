package com.quizapp.controller;

import com.quizapp.Main;
import com.quizapp.model.User;
import com.quizapp.model.UserRole;
import com.quizapp.repository.SQLiteUserRepository;
import com.quizapp.service.AuthenticationService;
import com.quizapp.util.AlertHelper;
import com.quizapp.util.SessionContext;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller for the Login screen.
 */
public class LoginController implements Initializable {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;

    private AuthenticationService authService;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        authService = new AuthenticationService(new SQLiteUserRepository());
        errorLabel.setText("");
    }

    @FXML
    void handleLogin(ActionEvent event) {
        String email = emailField.getText() != null ? emailField.getText().trim() : "";
        String password = passwordField.getText() != null ? passwordField.getText().trim() : "";

        if (email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please enter both email and password.");
            return;
        }

        try {
            Optional<User> userOpt = authService.login(email, password);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                SessionContext.setCurrentUser(user);
                errorLabel.setText("");

                if (user.getRole() == UserRole.TEACHER) {
                    Main.showTeacherDashboard();
                } else {
                    Main.showStudentDashboard();
                }
            } else {
                errorLabel.setText("Invalid email or password. Please try again.");
            }
        } catch (Exception e) {
            errorLabel.setText("Authentication error: " + e.getMessage());
        }
    }

    @FXML
    void handleRegister(ActionEvent event) {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Register New Student");
        dialog.setHeaderText("Create a new student examination account");

        ButtonType registerButtonType = new ButtonType("Register", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(registerButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText("Full Name");
        TextField newEmailField = new TextField();
        newEmailField.setPromptText("Email Address");
        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("Password");

        grid.add(new Label("Full Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Email:"), 0, 1);
        grid.add(newEmailField, 1, 1);
        grid.add(new Label("Password:"), 0, 2);
        grid.add(newPasswordField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == registerButtonType) {
                String name = nameField.getText().trim();
                String email = newEmailField.getText().trim();
                String pass = newPasswordField.getText().trim();
                if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                    AlertHelper.showError("Validation Error", "All fields are required.");
                    return null;
                }
                try {
                    return authService.register(name, email, pass, UserRole.STUDENT);
                } catch (Exception ex) {
                    AlertHelper.showError("Registration Failed", ex.getMessage());
                    return null;
                }
            }
            return null;
        });

        Optional<User> registered = dialog.showAndWait();
        registered.ifPresent(user -> {
            AlertHelper.showInfo("Registration Successful",
                    "Account registered for " + user.getName() + " (" + user.getEmail() + "). You can now log in.");
            emailField.setText(user.getEmail());
            passwordField.setText("");
        });
    }

    @FXML
    void fillTeacher(ActionEvent event) {
        emailField.setText("teacher@quiz.com");
        passwordField.setText("teacher123");
        errorLabel.setText("");
    }

    @FXML
    void fillStudent(ActionEvent event) {
        emailField.setText("student1@quiz.com");
        passwordField.setText("student123");
        errorLabel.setText("");
    }
}
