package com.petopia.controller;
import com.petopia.model.Session;
import com.petopia.battle.BattleEvent;
import com.petopia.battle.BattleService;
import com.petopia.db.DatabaseUtil;
import com.petopia.model.Pet;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.Instant;

public class ArenaController {

    @FXML private Button navHome;
    @FXML private Button navMyPets;
    @FXML private Button navMarketplace;
    @FXML private Button navLogin;
    @FXML private Button backHomeBtn;

    @FXML private ImageView playerImage;
    @FXML private ImageView enemyImage;

    @FXML private Label playerName;
    @FXML private Label enemyName;

    @FXML private ProgressBar playerHpBar;
    @FXML private ProgressBar enemyHpBar;
    @FXML private Label playerHpLabel;
    @FXML private Label enemyHpLabel;

    @FXML private TextArea battleLog;
    @FXML private Button attackBtn;
    @FXML private Button potionBtn;
    @FXML private Button fleeBtn;

    private BattleService battleService;
    private Pet playerPet;
    private Pet enemyPet;

    // Pet data mapping (nama, image, level, maxHp, attack, defense)
    private static final Object[][] PET_DATA = {
            {"DOG",          "/images/pet1.png",          10, 110, 15, 5},
            {"RISOL CAT",    "/images/risol_mayo_cat.png", 15, 120, 18, 6},
            {"CAT",          "/images/pet2.png",           12, 115, 17, 7},
            {"AQUA SLIME",   "/images/slime_ijo.png",       5,  80, 12, 4},
            {"SWIFT BIRD",   "/images/burung.png",           8, 100, 16, 5},
    };

    private static final Object[][] ENEMY_DATA = {
            {"WILD BEAR", "/images/enemy.png", 10, 110, 16, 6},
    };

    @FXML
    public void initialize() {
        navHome.setOnAction(e -> navigateTo("/fxml/Home.fxml", navHome));
        navMyPets.setOnAction(e -> navigateTo("/fxml/MyPets.fxml", navMyPets));
        navMarketplace.setOnAction(e -> navigateTo("/fxml/Leaderboard.fxml", navMarketplace));

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
    }

    public void initBattle(int playerPetIndex) {
        // Create player pet dari data
        Object[] playerData = PET_DATA[Math.min(playerPetIndex, PET_DATA.length - 1)];
        playerPet = new Pet(
                (String) playerData[0],
                (String) playerData[1],
                (Integer) playerData[2],
                (Integer) playerData[3],
                (Integer) playerData[4],
                (Integer) playerData[5]
        );

        // Pilih enemy random
        int enemyIdx = (int) (Math.random() * ENEMY_DATA.length);
        Object[] enemyData = ENEMY_DATA[enemyIdx];
        enemyPet = new Pet(
                (String) enemyData[0],
                (String) enemyData[1],
                (Integer) enemyData[2],
                (Integer) enemyData[3],
                (Integer) enemyData[4],
                (Integer) enemyData[5]
        );

        // Setup UI
        loadImage(playerImage, playerPet.getImagePath());
        loadImage(enemyImage, enemyPet.getImagePath());
        playerName.setText(playerPet.getName());
        enemyName.setText(enemyPet.getName());

        // Initialize HP display
        playerHpLabel.setText(playerPet.getHp() + " / " + playerPet.getMaxHp());
        enemyHpLabel.setText(enemyPet.getHp() + " / " + enemyPet.getMaxHp());
        playerHpBar.setProgress(1.0);
        enemyHpBar.setProgress(1.0);

        battleLog.clear();

        battleService = new BattleService(playerPet, enemyPet, this::onBattleEvent);

        // Wire action buttons
        attackBtn.setOnAction(e -> {
            disableActions(true);
            battleService.playerAttack();
            disableActions(false);
        });
        potionBtn.setOnAction(e -> {
            disableActions(true);
            battleService.playerUsePotion();
            disableActions(false);
        });
        fleeBtn.setOnAction(e -> {
            disableActions(true);
            battleService.playerFlee();
            disableActions(false);
        });

        battleService.start();
    }

    private void onBattleEvent(BattleEvent ev) {
        Platform.runLater(() -> {
            switch (ev.getType()) {
                case LOG:
                    appendLog(ev.getMessage());
                    break;
                case HP_UPDATE:
                    if (ev.getActor() == 0) {
                        playerHpBar.setProgress((double) ev.getHp() / ev.getMaxHp());
                        playerHpLabel.setText(ev.getHp() + " / " + ev.getMaxHp());
                    } else {
                        enemyHpBar.setProgress((double) ev.getHp() / ev.getMaxHp());
                        enemyHpLabel.setText(ev.getHp() + " / " + ev.getMaxHp());
                    }
                    break;
                case BATTLE_END:
                    appendLog("=== " + ev.getMessage() + " ===");
                    disableActions(true);
                    saveBattleResultToDb(ev.getMessage());
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Battle Result");
                    alert.setHeaderText("Battle Finished");
                    alert.setContentText(ev.getMessage());
                    alert.showAndWait();
                    break;
            }
        });
    }

    private void appendLog(String text) {
        if (battleLog.getText().isEmpty()) {
            battleLog.setText(text);
        } else {
            battleLog.appendText("\n" + text);
        }
        // Auto-scroll ke bawah
        battleLog.setScrollTop(Double.MAX_VALUE);
    }

    private void disableActions(boolean disabled) {
        attackBtn.setDisable(disabled);
        potionBtn.setDisable(disabled);
        fleeBtn.setDisable(disabled);
    }

    private void loadImage(ImageView iv, String path) {
        try {
            var stream = getClass().getResourceAsStream(path);
            if (stream != null) {
                iv.setImage(new Image(stream));
            }
        } catch (Exception e) {
            System.out.println("Image not found: " + path);
        }
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
        }
    }

    public void goToHome() {
        navigateTo("/fxml/Home.fxml", backHomeBtn);
    }

    private void saveBattleResultToDb(String result) {
        try (Connection conn = DatabaseUtil.getConnection()) {
            String sql = "INSERT INTO battles (player_id, opponent_name, winner, duration_ms) VALUES (?, ?, ?, ?)";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                if (Session.isLoggedIn()) {
                    ps.setLong(1, Session.getUserId());
                } else {
                    ps.setObject(1, null);
                }

                ps.setString(2, enemyPet.getName());
                ps.setString(3, result);
                ps.setLong(4, 0L);
                ps.executeUpdate();
            }

        } catch (Exception e) {
            System.out.println("Warning: Failed to save battle result: " + e.getMessage());
        }
    }
}