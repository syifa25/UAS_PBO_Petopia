package com.petopia.controller;

import com.petopia.db.DatabaseUtil;
import com.petopia.model.LeaderboardEntry;
import com.petopia.model.Session;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LeaderboardController {

    @FXML private Button navHome;
    @FXML private Button navMyPets;
    @FXML private Button navLeaderboard;
    @FXML private Button navLogin;
    @FXML private Button backHomeBtn;

    @FXML private TableView<LeaderboardEntry> leaderboardTable;
    @FXML private TableColumn<LeaderboardEntry, Integer> rankColumn;
    @FXML private TableColumn<LeaderboardEntry, String> usernameColumn;
    @FXML private TableColumn<LeaderboardEntry, Integer> winColumn;
    @FXML private TableColumn<LeaderboardEntry, Integer> loseColumn;
    @FXML private TableColumn<LeaderboardEntry, Integer> scoreColumn;
    @FXML private TableColumn<LeaderboardEntry, String> lastEnemyColumn;

    @FXML
    public void initialize() {
        navHome.setOnAction(e -> navigateTo("/fxml/Home.fxml", navHome));
        navMyPets.setOnAction(e -> navigateTo("/fxml/MyPets.fxml", navMyPets));
        navLeaderboard.setOnAction(e -> navigateTo("/fxml/Leaderboard.fxml", navLeaderboard));
        backHomeBtn.setOnAction(e -> navigateTo("/fxml/Home.fxml", backHomeBtn));

        if (Session.isLoggedIn()) {
            navLogin.setText(Session.getDisplayName() != null && !Session.getDisplayName().isBlank()
                    ? Session.getDisplayName().toUpperCase()
                    : Session.getUsername().toUpperCase());

            navLogin.setOnAction(e -> {
                Session.logout();
                navigateTo("/fxml/Login.fxml", navLogin);
            });
        } else {
            navLogin.setText("LOGIN");
            navLogin.setOnAction(e -> navigateTo("/fxml/Login.fxml", navLogin));
        }

        rankColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleIntegerProperty(data.getValue().getRank()).asObject());
        usernameColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getUsername()));
        winColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleIntegerProperty(data.getValue().getWin()).asObject());
        loseColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleIntegerProperty(data.getValue().getLose()).asObject());
        scoreColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleIntegerProperty(data.getValue().getScore()).asObject());
        lastEnemyColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getLastEnemy()));

        loadLeaderboard();
    }

    private void loadLeaderboard() {
        ObservableList<LeaderboardEntry> data = FXCollections.observableArrayList();

        String sql = """
            SELECT 
                COALESCE(u.username, 'Guest') AS username,
                SUM(CASE WHEN LOWER(b.winner) LIKE '%defeated%' THEN 1 ELSE 0 END) AS win_count,
                SUM(CASE WHEN LOWER(b.winner) LIKE '%fled%' 
                          OR LOWER(b.winner) LIKE '%lost%' 
                          OR LOWER(b.winner) LIKE '%defeated by%' THEN 1 ELSE 0 END) AS lose_count,
                COUNT(*) AS total_battle,
                MAX(b.opponent_name) AS last_enemy
            FROM battles b
            LEFT JOIN users u ON b.player_id = u.id
            GROUP BY COALESCE(u.username, 'Guest')
            ORDER BY win_count DESC, total_battle DESC
            """;

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            int rank = 1;
            while (rs.next()) {
                int win = rs.getInt("win_count");
                int lose = rs.getInt("lose_count");
                int score = (win * 100) - (lose * 30);

                data.add(new LeaderboardEntry(
                        rank++,
                        rs.getString("username"),
                        win,
                        lose,
                        score,
                        rs.getString("last_enemy")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();

            data.add(new LeaderboardEntry(1, "umri", 5, 1, 470, "STRAY DOG"));
            data.add(new LeaderboardEntry(2, "trainer2", 3, 2, 240, "WILD SLIME"));
            data.add(new LeaderboardEntry(3, "guest", 1, 3, 10, "BURUNG"));
        }

        leaderboardTable.setItems(data);
    }

    @FXML
    public void goToHome() {
        navigateTo("/fxml/Home.fxml", backHomeBtn);
    }

    private void navigateTo(String fxmlPath, Node source) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Stage stage = (Stage) source.getScene().getWindow();
            Scene scene = new Scene(loader.load(), 1000, 700);
            scene.getStylesheets().add(
                    getClass().getResource("/css/style.css").toExternalForm()
            );
            stage.setScene(scene);
        } catch (Exception ex) {
            ex.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Load Error");
            alert.setHeaderText("Gagal memuat halaman");
            alert.setContentText(ex.getMessage());
            alert.showAndWait();
        }
    }
}