package com.petopia.controller;

import com.petopia.api.ApiClient;
import com.petopia.battle.BattleEvent;
import com.petopia.battle.BattleService;
import com.petopia.model.Pet;
import com.petopia.model.PlayerPet;
import com.petopia.model.EnemyPet;
import com.petopia.model.Session;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class ArenaController {

    // ── Navbar ──────────────────────────────────────────────
    @FXML private Button navHome;
    @FXML private Button navMyPets;
    @FXML private Button navMarketplace;
    @FXML private Button navLogin;
    @FXML private Button backHomeBtn;

    // ── Battle field ────────────────────────────────────────
    @FXML private ImageView playerImage;
    @FXML private ImageView enemyImage;
    @FXML private Label     playerName;
    @FXML private Label     enemyName;
    @FXML private Label     playerLevel;
    @FXML private Label     enemyLevel;
    @FXML private ProgressBar playerHpBar;
    @FXML private ProgressBar enemyHpBar;
    @FXML private Label     playerHpLabel;
    @FXML private Label     enemyHpLabel;

    // ── Log & actions ───────────────────────────────────────
    @FXML private Label  battleLog;
    @FXML private HBox   actionBox;
    @FXML private Button attackBtn;
    @FXML private Button potionBtn;
    @FXML private Label  potionLabel;
    @FXML private Button fleeBtn;

    // ── Result overlay ──────────────────────────────────────
    @FXML private StackPane resultOverlay;
    @FXML private VBox      resultPanel;
    @FXML private Label     resultIcon;
    @FXML private Label     resultTitle;
    @FXML private Label     resultMsg;
    @FXML private Button    playAgainBtn;

    // ── State ───────────────────────────────────────────────
    private BattleService battleService;
    private PlayerPet playerPet;
    private EnemyPet enemyPet;
    private int savedPlayerPetIndex = 0;

    // ── Message queue — shows each log line for 1.5s before the next ──
    private final java.util.Queue<String> logQueue = new java.util.LinkedList<>();
    private boolean logBusy = false;

    // ── Pet / Enemy data ────────────────────────────────────
    private static final Object[][] PET_DATA = {
            {"DOG",        "/images/pet1.png",          10, 110, 15, 5},
            {"RISOL CAT",  "/images/risol_mayo_cat.png", 15, 120, 18, 6},
            {"CAT",        "/images/pet2.png",           12, 115, 17, 7},
            {"AQUA SLIME", "/images/slime_ijo.png",       5,  80, 12, 4},
            {"SWIFT BIRD", "/images/burung.png",           8, 100, 16, 5},
    };

    private static final Object[][] ENEMY_DATA = {
            {"WILD BEAR", "/images/enemy.png", 10, 110, 16, 6},
    };

    // ════════════════════════════════════════════════════════
    // FXML init
    // ════════════════════════════════════════════════════════
    @FXML
    public void initialize() {
        navHome.setOnAction(e -> navigateTo("/fxml/Home.fxml", navHome));
        navMyPets.setOnAction(e -> navigateTo("/fxml/MyPets.fxml", navMyPets));
        navMarketplace.setOnAction(e -> navigateTo("/fxml/Leaderboard.fxml", navMarketplace));

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
    }

    // ════════════════════════════════════════════════════════
    // BATTLE INIT (called from HomeController)
    // ════════════════════════════════════════════════════════
    public void initBattle(int playerPetIndex) {
        savedPlayerPetIndex = playerPetIndex;

        // Build pets
        Object[] pd = PET_DATA[Math.min(playerPetIndex, PET_DATA.length - 1)];
        playerPet = new PlayerPet((String)pd[0], (String)pd[1], (Integer)pd[2],
                (Integer)pd[3], (Integer)pd[4], (Integer)pd[5]);

        Object[] ed = ENEMY_DATA[0];
        enemyPet  = new EnemyPet((String)ed[0], (String)ed[1], (Integer)ed[2],
                (Integer)ed[3], (Integer)ed[4], (Integer)ed[5], 0.20); // 20% crit

        // Populate UI
        loadImage(playerImage, playerPet.getImagePath());
        loadImage(enemyImage,  enemyPet.getImagePath());
        playerName.setText(playerPet.getName());
        enemyName.setText(enemyPet.getName());
        playerLevel.setText("LV." + playerPet.getLevel());
        enemyLevel.setText("LV."  + enemyPet.getLevel());

        setHpInstant(playerHpBar, playerHpLabel, playerPet.getHp(), playerPet.getMaxHp());
        setHpInstant(enemyHpBar,  enemyHpLabel,  enemyPet.getHp(),  enemyPet.getMaxHp());

        potionLabel.setText("POTION (1)");
        resultOverlay.setVisible(false);
        logQueue.clear();
        logBusy = false;

        // Wire buttons
        attackBtn.setOnAction(e -> onPlayerAttack());
        potionBtn.setOnAction(e -> onPlayerPotion());
        fleeBtn.setOnAction(e   -> onPlayerFlee());

        battleService = new BattleService(playerPet, enemyPet, this::onBattleEvent);

        // ── #14 Battle intro: lock buttons, show start message, then unlock ──
        disableActions(true);
        setLog("⚔  BATTLE START!  " + playerPet.getName() + "  vs  " + enemyPet.getName() + "!");

        PauseTransition intro = new PauseTransition(Duration.millis(1800));
        intro.setOnFinished(e -> {
            battleService.start();
            disableActions(false);
        });
        intro.play();
    }

    // ════════════════════════════════════════════════════════
    // PLAYER ACTIONS
    // ════════════════════════════════════════════════════════
    private void onPlayerAttack() {
        disableActions(true);
        // ── #4 Player lunge animation ──
        animateLunge(playerImage, true, () -> {
            battleService.playerAttack();
            // enemy counter fires inside BattleService after a pause (see onBattleEvent)
        });
    }

    private void onPlayerPotion() {
        disableActions(true);
        battleService.playerUsePotion();
    }

    private void onPlayerFlee() {
        disableActions(true);
        battleService.playerFlee();
    }

    // ════════════════════════════════════════════════════════
    // BATTLE EVENTS (called from BattleService via Consumer)
    // ════════════════════════════════════════════════════════
    private void onBattleEvent(BattleEvent ev) {
        Platform.runLater(() -> {
            switch (ev.getType()) {

                case LOG:
                    setLog(ev.getMessage());
                    break;

                case ENEMY_ATTACK:
                    // Enemy lunge starts after 600ms so player can read the log
                    PauseTransition enemyDelay = new PauseTransition(Duration.millis(600));
                    enemyDelay.setOnFinished(e -> animateLunge(enemyImage, false, null));
                    enemyDelay.play();
                    break;

                case HP_UPDATE:
                    if (ev.getActor() == 0) {
                        // Player HP drops AFTER enemy lunge lands:
                        // 600ms log delay + 180ms lunge travel = ~800ms, +100ms buffer
                        PauseTransition playerHpDelay = new PauseTransition(Duration.millis(900));
                        playerHpDelay.setOnFinished(e -> {
                            flashSprite(playerImage);
                            animateHpBar(playerHpBar, playerHpLabel,
                                    ev.getHp(), ev.getMaxHp());
                        });
                        playerHpDelay.play();
                    } else {
                        // Enemy HP drops on player hit (player lunge already in flight)
                        flashSprite(enemyImage);
                        animateHpBar(enemyHpBar, enemyHpLabel,
                                ev.getHp(), ev.getMaxHp());
                    }
                    break;
                case BATTLE_END:
                    setLog(ev.getMessage());
                    disableActions(true);
                    saveBattleResultToDb(ev.getMessage());

                    // Short pause so the final log line is readable before overlay appears
                    PauseTransition endDelay = new PauseTransition(Duration.millis(900));
                    endDelay.setOnFinished(e -> showResultOverlay(ev.getMessage()));
                    endDelay.play();
                    break;
            }
        });
    }

    // ════════════════════════════════════════════════════════
    // RESULT OVERLAY (#2 & #3)
    // ════════════════════════════════════════════════════════
    private void showResultOverlay(String message) {
        boolean victory = message.toLowerCase().contains("wins") &&
                          message.toLowerCase().startsWith(playerPet.getName().toLowerCase());
        boolean fled    = message.toLowerCase().contains("fled");

        if (victory) {
            resultIcon.setText("VICTORY");
            resultTitle.setText("YOU WIN!");
            resultTitle.getStyleClass().remove("result-title-defeat");
            resultMsg.setText(playerPet.getName() + " defeated " + enemyPet.getName() + "!");
        } else if (fled) {
            resultIcon.setText("ESCAPED");
            resultTitle.setText("FLED!");
            resultTitle.getStyleClass().remove("result-title-defeat");
            resultMsg.setText("You escaped safely.");
        } else {
            resultIcon.setText("DEFEAT");
            resultTitle.setText("YOU LOSE!");
            if (!resultTitle.getStyleClass().contains("result-title-defeat"))
                resultTitle.getStyleClass().add("result-title-defeat");
            resultMsg.setText(playerPet.getName() + " was defeated by " + enemyPet.getName() + "...");
        }

        // Fade the overlay in
        resultOverlay.setVisible(true);
        resultOverlay.setOpacity(0);
        FadeTransition ft = new FadeTransition(Duration.millis(400), resultOverlay);
        ft.setToValue(1.0);
        ft.play();
    }

    @FXML
    public void playAgain() {
        resultOverlay.setVisible(false);
        initBattle(savedPlayerPetIndex);
    }

    // ════════════════════════════════════════════════════════
    // ANIMATIONS
    // ════════════════════════════════════════════════════════

    /** #4 — Lunge attacker toward opponent then snap back */
    private void animateLunge(ImageView sprite, boolean isPlayer, Runnable onImpact) {
        double lungeX = isPlayer ? 220 : -220;
        double lungeY = -20;

        TranslateTransition lunge = new TranslateTransition(Duration.millis(180), sprite);
        lunge.setByX(lungeX);
        lunge.setByY(lungeY);

        TranslateTransition recoil = new TranslateTransition(Duration.millis(220), sprite);
        recoil.setToX(0);
        recoil.setToY(0);

        lunge.setOnFinished(e -> {
            if (onImpact != null) onImpact.run();
            recoil.play();
        });
        lunge.play();
    }

    /** Flash the hit sprite white briefly */
    private void flashSprite(ImageView sprite) {
        Timeline flash = new Timeline(
            new KeyFrame(Duration.millis(0),   e -> sprite.setStyle("-fx-opacity: 0.2;")),
            new KeyFrame(Duration.millis(80),  e -> sprite.setStyle("-fx-opacity: 1.0;")),
            new KeyFrame(Duration.millis(160), e -> sprite.setStyle("-fx-opacity: 0.2;")),
            new KeyFrame(Duration.millis(240), e -> sprite.setStyle("-fx-opacity: 1.0;"))
        );
        flash.play();
    }

    /** #5 — Smoothly animate HP bar from current to target value */
    private void animateHpBar(ProgressBar bar, Label label, int newHp, int maxHp) {
        double startProgress = bar.getProgress();
        double endProgress   = (double) newHp / maxHp;

        Timeline tl = new Timeline();
        int frames = 20;
        for (int i = 0; i <= frames; i++) {
            double t      = (double) i / frames;
            double value  = startProgress + (endProgress - startProgress) * t;
            int    dispHp = (int) Math.round(newHp + (bar.getProgress() * maxHp - newHp) * (1 - t));
            final double fv = value;
            final int    fh = (int) Math.round(startProgress * maxHp + (endProgress - startProgress) * maxHp * t);
            tl.getKeyFrames().add(new KeyFrame(Duration.millis(i * 20), e -> {
                bar.setProgress(fv);
                label.setText(fh + " / " + maxHp);
            }));
        }
        // Ensure final value is exact
        tl.getKeyFrames().add(new KeyFrame(Duration.millis(frames * 20 + 1), e -> {
            bar.setProgress(endProgress);
            label.setText(newHp + " / " + maxHp);
            updateHpBarColor(bar, endProgress);
            // Re-enable actions only after HP anim finishes (unless battle is over)
            if (resultOverlay == null || !resultOverlay.isVisible()) {
                disableActions(false);
            }
        }));
        tl.play();
    }

    /** Green → yellow → red based on HP % */
    private void updateHpBarColor(ProgressBar bar, double progress) {
        bar.getStyleClass().removeAll("hp-bar-yellow", "hp-bar-red");
        if (progress <= 0.25) {
            bar.getStyleClass().add("hp-bar-red");
        } else if (progress <= 0.50) {
            bar.getStyleClass().add("hp-bar-yellow");
        }
    }

    /** Set HP bar and label instantly (used on init) */
    private void setHpInstant(ProgressBar bar, Label label, int hp, int maxHp) {
        bar.setProgress((double) hp / maxHp);
        label.setText(hp + " / " + maxHp);
        updateHpBarColor(bar, (double) hp / maxHp);
    }

    // ════════════════════════════════════════════════════════
    // HELPERS
    // ════════════════════════════════════════════════════════

    /** Queue a log message — each shows for 1.5s before the next appears */
    private void setLog(String text) {
        logQueue.add(text);
        if (!logBusy) drainLogQueue();
    }

    private void drainLogQueue() {
        if (logQueue.isEmpty()) { logBusy = false; return; }
        logBusy = true;
        String next = logQueue.poll();
        battleLog.setText(next);
        PauseTransition hold = new PauseTransition(Duration.millis(1500));
        hold.setOnFinished(e -> drainLogQueue());
        hold.play();
    }

    private void disableActions(boolean disabled) {
        actionBox.setDisable(disabled);
        actionBox.setOpacity(disabled ? 0.5 : 1.0);
    }

    private void loadImage(ImageView iv, String path) {
        try {
            var stream = getClass().getResourceAsStream(path);
            if (stream != null) iv.setImage(new Image(stream));
        } catch (Exception e) {
            System.out.println("Image not found: " + path);
        }
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

    @FXML
    public void goToHome() {
        navigateTo("/fxml/Home.fxml", backHomeBtn);
    }

    private void saveBattleResultToDb(String result) {
        // Determine outcome cleanly
        String outcome;
        if (result.toLowerCase().contains("fled")) {
            outcome = "FLED";
        } else if (result.toLowerCase().startsWith(playerPet.getName().toLowerCase())) {
            outcome = "WIN";
        } else {
            outcome = "LOSS";
        }

        // Fire-and-forget on background thread — don't block the UI
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                String json = "{" +
                        (Session.isLoggedIn() ? "\"playerId\":" + Session.getUserId() + "," : "") +
                        "\"opponentName\":\"" + enemyPet.getName() + "\"," +
                        "\"winner\":\"" + outcome + "\"," +
                        "\"durationMs\":0}";

                String token = Session.getToken();
                if (token != null) {
                    ApiClient.post("/battles/result", json, token);
                } else {
                    ApiClient.post("/battles/result", json);
                }
                return null;
            }
        };
        task.setOnFailed(e ->
            System.out.println("Warning: Failed to save battle result via API: " +
                               task.getException().getMessage()));
        new Thread(task, "battle-save-thread").start();
    }
}
