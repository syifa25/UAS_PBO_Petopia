package com.petopia.controller;

import com.petopia.api.ApiClient;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.http.HttpResponse;

public class RegisterController {

    @FXML private TextField     usernameField;
    @FXML private TextField     displayNameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button        registerBtn;
    @FXML private Label         errorLabel;

    @FXML
    public void initialize() {
        registerBtn.setOnAction(e -> handleRegister());
    }

    private void handleRegister() {
        String username     = usernameField.getText().trim();
        String displayName  = displayNameField.getText().trim();
        String password     = passwordField.getText();
        String confirmPass  = confirmPasswordField.getText();

        // Client-side pre-validation (mirrors server rules, gives instant feedback)
        if (username.isEmpty() || password.isEmpty()) {
            showError("Username and password are required.");
            return;
        }
        if (!username.matches("^[A-Za-z0-9_]{3,30}$")) {
            showError("Username: 3-30 chars, letters/digits/underscore only.");
            return;
        }
        if (!password.equals(confirmPass)) {
            showError("Passwords do not match.");
            return;
        }
        if (password.length() < 8
                || !password.matches(".*[a-z].*")
                || !password.matches(".*[A-Z].*")
                || !password.matches(".*\\d.*")
                || !password.matches(".*[^A-Za-z0-9].*")) {
            showError("Password needs 8+ chars with upper, lower, digit and symbol.");
            return;
        }

        registerBtn.setDisable(true);
        errorLabel.setText("Creating account...");

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                String json = "{\"username\":\"" + escape(username) + "\"," +
                              "\"displayName\":\"" + escape(displayName) + "\"," +
                              "\"password\":\"" + escape(password) + "\"}";
                HttpResponse<String> resp = ApiClient.post("/auth/register", json);

                Platform.runLater(() -> {
                    if (resp.statusCode() == 201) {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Success");
                        alert.setHeaderText("Account Created");
                        alert.setContentText("Your account has been created. Please login.");
                        alert.showAndWait();
                        goToLogin();
                    } else {
                        String errMsg = ApiClient.extractString(resp.body(), "error");
                        showError(errMsg != null ? errMsg : "Registration failed.");
                        registerBtn.setDisable(false);
                    }
                });
                return null;
            }
        };

        task.setOnFailed(e -> Platform.runLater(() -> {
            showError("Cannot connect to server. Is the backend running?");
            registerBtn.setDisable(false);
        }));

        new Thread(task, "register-thread").start();
    }

    private void showError(String message) {
        errorLabel.setText(message);
    }

    @FXML
    public void goToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            Stage stage = (Stage) registerBtn.getScene().getWindow();
            Scene scene = new Scene(loader.load(), 1000, 700);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
