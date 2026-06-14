package com.petopia.controller;

import com.petopia.api.ApiClient;
import com.petopia.model.Session;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.http.HttpResponse;

public class LoginController {

    @FXML private TextField     usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button        loginBtn;
    @FXML private Label         errorLabel;

    @FXML
    public void initialize() {
        loginBtn.setOnAction(e -> handleLogin());
        usernameField.setOnKeyPressed(e -> { if (e.getCode().toString().equals("ENTER")) handleLogin(); });
        passwordField.setOnKeyPressed(e -> { if (e.getCode().toString().equals("ENTER")) handleLogin(); });
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Username and password cannot be empty.");
            return;
        }

        loginBtn.setDisable(true);
        errorLabel.setText("Logging in...");

        // Call API on background thread — never block JavaFX thread
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                String json = "{\"username\":\"" + escape(username) +
                              "\",\"password\":\"" + escape(password) + "\"}";
                HttpResponse<String> resp = ApiClient.post("/auth/login", json);

                Platform.runLater(() -> {
                    if (resp.statusCode() == 200) {
                        // Parse response
                        String body = resp.body();
                        Long   userId      = ApiClient.extractLong(body,   "userId");
                        String uname       = ApiClient.extractString(body, "username");
                        String displayName = ApiClient.extractString(body, "displayName");
                        String token       = ApiClient.extractString(body, "token");

                        Session.login(userId, uname, displayName, token);
                        navigateToHome();
                    } else {
                        String errMsg = ApiClient.extractString(resp.body(), "error");
                        showError(errMsg != null ? errMsg : "Login failed.");
                        loginBtn.setDisable(false);
                    }
                });
                return null;
            }
        };

        task.setOnFailed(e -> Platform.runLater(() -> {
            showError("Cannot connect to server. Is the backend running?");
            loginBtn.setDisable(false);
        }));

        new Thread(task, "login-thread").start();
    }

    private void showError(String message) {
        errorLabel.setText(message);
    }

    private void navigateToHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Home.fxml"));
            Stage stage = (Stage) loginBtn.getScene().getWindow();
            Scene scene = new Scene(loader.load(), 1000, 700);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void goToRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Register.fxml"));
            Stage stage = (Stage) loginBtn.getScene().getWindow();
            Scene scene = new Scene(loader.load(), 1000, 700);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void goToHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Home.fxml"));
            Stage stage = (Stage) loginBtn.getScene().getWindow();
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
