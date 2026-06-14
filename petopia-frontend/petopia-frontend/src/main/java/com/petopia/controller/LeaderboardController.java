package com.petopia.controller;

import com.petopia.api.ApiClient;
import com.petopia.model.LeaderboardEntry;
import com.petopia.model.Session;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class LeaderboardController {

    @FXML private Button navHome;
    @FXML private Button navMyPets;
    @FXML private Button navLeaderboard;
    @FXML private Button navLogin;
    @FXML private Button backHomeBtn;

    @FXML private TableView<LeaderboardEntry>           leaderboardTable;
    @FXML private TableColumn<LeaderboardEntry, Integer> rankColumn;
    @FXML private TableColumn<LeaderboardEntry, String>  usernameColumn;
    @FXML private TableColumn<LeaderboardEntry, Integer> winColumn;
    @FXML private TableColumn<LeaderboardEntry, Integer> loseColumn;
    @FXML private TableColumn<LeaderboardEntry, Integer> scoreColumn;
    @FXML private TableColumn<LeaderboardEntry, String>  lastEnemyColumn;

    @FXML
    public void initialize() {
        navHome.setOnAction(e        -> navigateTo("/fxml/Home.fxml",        navHome));
        navMyPets.setOnAction(e      -> navigateTo("/fxml/MyPets.fxml",      navMyPets));
        navLeaderboard.setOnAction(e -> navigateTo("/fxml/Leaderboard.fxml", navLeaderboard));
        backHomeBtn.setOnAction(e    -> navigateTo("/fxml/Home.fxml",        backHomeBtn));

        if (Session.isLoggedIn()) {
            String label = Session.getDisplayName() != null && !Session.getDisplayName().isBlank()
                    ? Session.getDisplayName().toUpperCase()
                    : Session.getUsername().toUpperCase();
            navLogin.setText(label);
            navLogin.setOnAction(e -> { Session.logout(); navigateTo("/fxml/Login.fxml", navLogin); });
        } else {
            navLogin.setText("LOGIN");
            navLogin.setOnAction(e -> navigateTo("/fxml/Login.fxml", navLogin));
        }

        rankColumn.setCellValueFactory(d ->
                new javafx.beans.property.SimpleIntegerProperty(d.getValue().getRank()).asObject());
        usernameColumn.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue().getUsername()));
        winColumn.setCellValueFactory(d ->
                new javafx.beans.property.SimpleIntegerProperty(d.getValue().getWin()).asObject());
        loseColumn.setCellValueFactory(d ->
                new javafx.beans.property.SimpleIntegerProperty(d.getValue().getLose()).asObject());
        scoreColumn.setCellValueFactory(d ->
                new javafx.beans.property.SimpleIntegerProperty(d.getValue().getScore()).asObject());
        lastEnemyColumn.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue().getLastEnemy()));

        loadLeaderboard();
    }

    private void loadLeaderboard() {
        Task<List<LeaderboardEntry>> task = new Task<>() {
            @Override
            protected List<LeaderboardEntry> call() throws Exception {
                HttpResponse<String> resp = ApiClient.get("/battles/leaderboard");
                if (resp.statusCode() != 200) return fallbackData();
                return parseLeaderboard(resp.body());
            }
        };

        task.setOnSucceeded(e ->
                leaderboardTable.setItems(FXCollections.observableArrayList(task.getValue())));
        task.setOnFailed(e ->
                leaderboardTable.setItems(FXCollections.observableArrayList(fallbackData())));

        new Thread(task, "leaderboard-thread").start();
    }

    /** Parse the JSON array returned by GET /api/battles/leaderboard */
    private List<LeaderboardEntry> parseLeaderboard(String json) {
        List<LeaderboardEntry> list = new ArrayList<>();
        // Simple array splitting — each entry is an object {...}
        int depth = 0, start = -1;
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '{') { if (depth++ == 0) start = i; }
            else if (c == '}') {
                if (--depth == 0 && start >= 0) {
                    String obj = json.substring(start, i + 1);
                    int    rank      = parseInt(obj, "rank");
                    String username  = ApiClient.extractString(obj, "username");
                    int    win       = parseInt(obj, "win");
                    int    lose      = parseInt(obj, "lose");
                    int    score     = parseInt(obj, "score");
                    String lastEnemy = ApiClient.extractString(obj, "lastEnemy");
                    list.add(new LeaderboardEntry(rank, username, win, lose, score, lastEnemy));
                    start = -1;
                }
            }
        }
        return list;
    }

    private int parseInt(String json, String key) {
        Long v = ApiClient.extractLong(json, key);
        return v == null ? 0 : v.intValue();
    }

    private List<LeaderboardEntry> fallbackData() {
        return List.of(
            new LeaderboardEntry(1, "No data yet", 0, 0, 0, "-")
        );
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
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            stage.setScene(scene);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
