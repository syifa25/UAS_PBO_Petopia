package com.petopia.controller;

import com.petopia.db.DatabaseUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import com.petopia.model.Session;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginBtn;
    @FXML private Label errorLabel;

    @FXML
    public void initialize() {
        loginBtn.setOnAction(e -> handleLogin());

        // Allow Enter key to login
        usernameField.setOnKeyPressed(e -> {
            if (e.getCode().toString().equals("ENTER")) handleLogin();
        });
        passwordField.setOnKeyPressed(e -> {
            if (e.getCode().toString().equals("ENTER")) handleLogin();
        });
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Username and password cannot be empty!");
            return;
        }

        if (authenticateUser(username, password)) {
            navigateToHome();
        } else {
            showError("Invalid username or password!");
            passwordField.clear();
        }
    }

    private boolean authenticateUser(String username, String password) {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT id, password_hash, display_name FROM users WHERE username = ?")) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password_hash");
                    String displayName = rs.getString("display_name");

                    if (DatabaseUtil.verifyPassword(password, storedHash)) {
                        Long userId = rs.getLong("id");
                        Session.login(userId, username, displayName);
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
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
}