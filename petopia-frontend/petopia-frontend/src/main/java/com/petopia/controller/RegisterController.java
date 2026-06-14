package com.petopia.controller;

import com.petopia.db.DatabaseUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class RegisterController {

    @FXML private TextField usernameField;
    @FXML private TextField displayNameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button registerBtn;
    @FXML private Label errorLabel;

    @FXML
    public void initialize() {
        registerBtn.setOnAction(e -> handleRegister());
    }

    private void handleRegister() {
        String username = usernameField.getText().trim();
        String displayName = displayNameField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // trim + length limits
        username = username.length() > 30 ? username.substring(0, 30) : username;
        displayName = displayName.length() > 100 ? displayName.substring(0,100) : displayName;
        if (password.length() > 128) {
            showError("Password terlalu panjang (max 128 chars).");
            return;
        }

        // username format
        if (!username.matches("^[A-Za-z0-9_]{3,30}$")) {
            showError("Username harus 3-30 chars: huruf, angka atau underscore saja.");
            return;
        }

        // confirm password
        if (!password.equals(confirmPassword)) {
            showError("Password dan konfirmasi tidak cocok.");
            return;
        }

        // password strength: cek secara terpisah (lebih aman & mudah dibaca)
        if (password.length() < 8
                || !password.matches(".*[a-z].*")
                || !password.matches(".*[A-Z].*")
                || !password.matches(".*\\d.*")
                || !password.matches(".*[^A-Za-z0-9].*")) {
            showError("Password harus minimal 8 karakter, mengandung huruf besar, huruf kecil, angka, dan simbol.");
            return;
        }

        // Check if username exists first
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement check = conn.prepareStatement("SELECT COUNT(*) AS cnt FROM users WHERE username = ?")) {
            check.setString(1, username);
            try (ResultSet rs = check.executeQuery()) {
                if (rs.next() && rs.getInt("cnt") > 0) {
                    showError("Username already exists. Choose another username.");
                    return;
                }
            }
        }

        catch (Exception ex) {
            ex.printStackTrace();
            showError("DB check error: " + ex.getClass().getSimpleName() + " - " + ex.getMessage());
            return;
        }

        // If not exists, insert
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO users (username, display_name, password_hash) VALUES (?, ?, ?)")) {

            String passwordHash = DatabaseUtil.hashPassword(password);
            ps.setString(1, username);
            ps.setString(2, displayName);
            ps.setString(3, passwordHash);
            ps.executeUpdate();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText("Account Created!");
            alert.setContentText("Your account has been created. Please login.");
            alert.showAndWait();
            goToLogin();
        } catch (Exception e) {
            String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            if (msg.contains("unique") || msg.contains("duplicate") || msg.contains("constraint")) {
                showError("Username already exists (concurrent). Try a different username.");
            } else {
                showError("Failed to create account: " + e.getMessage());
            }
            e.printStackTrace();
        }
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
}