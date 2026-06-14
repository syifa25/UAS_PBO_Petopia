package com.petopia.controller;

import com.petopia.model.Session;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Duration;

public class MyPetsController {

    // ── Navbar ──────────────────────────────────────────────
    @FXML private Button navHome;
    @FXML private Button navMyPets;
    @FXML private Button navLeaderboard;
    @FXML private Button navLogin;

    // ── Pet images ──────────────────────────────────────────
    @FXML private ImageView pet1Image;
    @FXML private ImageView pet2Image;
    @FXML private ImageView pet3Image;
    @FXML private ImageView pet4Image;
    @FXML private ImageView pet5Image;

    // ── Stat labels (live-updated) ───────────────────────────
    @FXML private Label p1Level; @FXML private Label p1Exp; @FXML private Label p1Stats;
    @FXML private Label p2Level; @FXML private Label p2Exp; @FXML private Label p2Stats;
    @FXML private Label p3Level; @FXML private Label p3Exp; @FXML private Label p3Stats;
    @FXML private Label p4Level; @FXML private Label p4Exp; @FXML private Label p4Stats;
    @FXML private Label p5Level; @FXML private Label p5Exp; @FXML private Label p5Stats;

    // ── Action buttons (for cooldown) ────────────────────────
    @FXML private Button feed1Btn; @FXML private Button play1Btn; @FXML private Button train1Btn;
    @FXML private Button feed2Btn; @FXML private Button play2Btn; @FXML private Button train2Btn;
    @FXML private Button feed3Btn; @FXML private Button play3Btn; @FXML private Button train3Btn;
    @FXML private Button feed4Btn; @FXML private Button play4Btn; @FXML private Button train4Btn;
    @FXML private Button feed5Btn; @FXML private Button play5Btn; @FXML private Button train5Btn;

    // ════════════════════════════════════════════════════════
    // PET STATE — in-memory, survives for this session
    // ════════════════════════════════════════════════════════
    /** Mutable state for one pet on the My Pets screen */
    private static class PetState {
        final String name;
        int level;
        int exp;
        int maxHp;
        int hp;
        int atk;
        int def;

        // EXP needed = 100 * level (grows with level)
        int expToNextLevel() { return 100 * level; }

        PetState(String name, int level, int maxHp, int atk, int def) {
            this.name  = name;
            this.level = level;
            this.exp   = 0;
            this.maxHp = maxHp;
            this.hp    = maxHp;
            this.atk   = atk;
            this.def   = def;
        }

        /** FEED: restore 20% of max HP. Returns message. */
        String feed() {
            if (hp >= maxHp) return name + " is already at full HP!";
            int heal = Math.max(1, maxHp / 5);
            hp = Math.min(maxHp, hp + heal);
            return name + " recovered " + heal + " HP!  (" + hp + "/" + maxHp + ")";
        }

        /** PLAY: gain 25 EXP, may level up. Returns message. */
        String play() {
            exp += 25;
            if (exp >= expToNextLevel()) return levelUp(" had fun and");
            return name + " had fun!  EXP: " + exp + "/" + expToNextLevel();
        }

        /** TRAIN: gain 40 EXP + +1 ATK or DEF, may level up. Returns message. */
        String train() {
            exp += 40;
            // Alternate: odd levels boost ATK, even boost DEF
            String statMsg;
            if (level % 2 != 0) { atk++; statMsg = "ATK is now " + atk; }
            else                 { def++; statMsg = "DEF is now " + def; }
            if (exp >= expToNextLevel()) return levelUp(" trained hard and") + "  " + statMsg;
            return name + " trained hard!  " + statMsg + "  EXP: " + exp + "/" + expToNextLevel();
        }

        private String levelUp(String verb) {
            exp -= expToNextLevel();
            level++;
            maxHp += 10;
            hp = maxHp; // full heal on level up
            atk += 2;
            def += 2;
            return name + verb + " leveled up!  Now LV." + level + "!  Full HP restored!";
        }
    }

    // Static so stats persist when navigating away and back
    private static final PetState[] PETS = {
        new PetState("DOG",        10, 110, 15,  5),
        new PetState("RISOL CAT",  15, 120, 18,  6),
        new PetState("CAT",        12, 115, 17,  7),
        new PetState("AQUA SLIME",  5,  80, 12,  4),
        new PetState("SWIFT BIRD",  8, 100, 16,  5),
    };

    // ════════════════════════════════════════════════════════
    // INIT
    // ════════════════════════════════════════════════════════
    @FXML
    public void initialize() {
        loadImage(pet1Image, "/images/pet1.png");
        loadImage(pet2Image, "/images/risol_mayo_cat.png");
        loadImage(pet3Image, "/images/pet2.png");
        loadImage(pet4Image, "/images/slime_ijo.png");
        loadImage(pet5Image, "/images/burung.png");

        navHome.setOnAction(e        -> navigateTo("/fxml/Home.fxml",        navHome));
        navMyPets.setOnAction(e      -> navigateTo("/fxml/MyPets.fxml",      navMyPets));
        navLeaderboard.setOnAction(e -> navigateTo("/fxml/Leaderboard.fxml", navLeaderboard));

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

        // Refresh all card labels from current state (handles back-navigation)
        refreshCard(0, p1Level, p1Exp, p1Stats);
        refreshCard(1, p2Level, p2Exp, p2Stats);
        refreshCard(2, p3Level, p3Exp, p3Stats);
        refreshCard(3, p4Level, p4Exp, p4Stats);
        refreshCard(4, p5Level, p5Exp, p5Stats);
    }

    // ════════════════════════════════════════════════════════
    // ACTION HANDLERS
    // ════════════════════════════════════════════════════════
    @FXML private void feedPet1()  { doAction(0, "FEED",  p1Level, p1Exp, p1Stats, feed1Btn,  play1Btn,  train1Btn);  }
    @FXML private void playPet1()  { doAction(0, "PLAY",  p1Level, p1Exp, p1Stats, feed1Btn,  play1Btn,  train1Btn);  }
    @FXML private void trainPet1() { doAction(0, "TRAIN", p1Level, p1Exp, p1Stats, feed1Btn,  play1Btn,  train1Btn);  }

    @FXML private void feedPet2()  { doAction(1, "FEED",  p2Level, p2Exp, p2Stats, feed2Btn,  play2Btn,  train2Btn);  }
    @FXML private void playPet2()  { doAction(1, "PLAY",  p2Level, p2Exp, p2Stats, feed2Btn,  play2Btn,  train2Btn);  }
    @FXML private void trainPet2() { doAction(1, "TRAIN", p2Level, p2Exp, p2Stats, feed2Btn,  play2Btn,  train2Btn);  }

    @FXML private void feedPet3()  { doAction(2, "FEED",  p3Level, p3Exp, p3Stats, feed3Btn,  play3Btn,  train3Btn);  }
    @FXML private void playPet3()  { doAction(2, "PLAY",  p3Level, p3Exp, p3Stats, feed3Btn,  play3Btn,  train3Btn);  }
    @FXML private void trainPet3() { doAction(2, "TRAIN", p3Level, p3Exp, p3Stats, feed3Btn,  play3Btn,  train3Btn);  }

    @FXML private void feedPet4()  { doAction(3, "FEED",  p4Level, p4Exp, p4Stats, feed4Btn,  play4Btn,  train4Btn);  }
    @FXML private void playPet4()  { doAction(3, "PLAY",  p4Level, p4Exp, p4Stats, feed4Btn,  play4Btn,  train4Btn);  }
    @FXML private void trainPet4() { doAction(3, "TRAIN", p4Level, p4Exp, p4Stats, feed4Btn,  play4Btn,  train4Btn);  }

    @FXML private void feedPet5()  { doAction(4, "FEED",  p5Level, p5Exp, p5Stats, feed5Btn,  play5Btn,  train5Btn);  }
    @FXML private void playPet5()  { doAction(4, "PLAY",  p5Level, p5Exp, p5Stats, feed5Btn,  play5Btn,  train5Btn);  }
    @FXML private void trainPet5() { doAction(4, "TRAIN", p5Level, p5Exp, p5Stats, feed5Btn,  play5Btn,  train5Btn);  }

    // ════════════════════════════════════════════════════════
    // CORE LOGIC
    // ════════════════════════════════════════════════════════
    private void doAction(int idx, String action,
                          Label levelLbl, Label expLbl, Label statsLbl,
                          Button feedBtn, Button playBtn, Button trainBtn) {
        PetState pet = PETS[idx];
        String msg;
        switch (action) {
            case "FEED"  -> msg = pet.feed();
            case "PLAY"  -> msg = pet.play();
            default      -> msg = pet.train(); // TRAIN
        }

        refreshCard(idx, levelLbl, expLbl, statsLbl);
        showInfo(action + "!", msg);

        // 3-second cooldown on all 3 buttons for this card
        cooldown(feedBtn, playBtn, trainBtn, 3000);
    }

    private void refreshCard(int idx, Label levelLbl, Label expLbl, Label statsLbl) {
        PetState p = PETS[idx];
        levelLbl.setText("LVL: " + p.level);
        expLbl.setText("EXP: " + p.exp + "/" + p.expToNextLevel());
        statsLbl.setText("ATK:" + p.atk + " DEF:" + p.def);
    }

    /** Disable all 3 card buttons for `ms` milliseconds then re-enable. */
    private void cooldown(Button a, Button b, Button c, int ms) {
        a.setDisable(true); b.setDisable(true); c.setDisable(true);
        a.setOpacity(0.5);  b.setOpacity(0.5);  c.setOpacity(0.5);
        PauseTransition pt = new PauseTransition(Duration.millis(ms));
        pt.setOnFinished(e -> {
            a.setDisable(false); b.setDisable(false); c.setDisable(false);
            a.setOpacity(1.0);   b.setOpacity(1.0);   c.setOpacity(1.0);
        });
        pt.play();
    }

    // ════════════════════════════════════════════════════════
    // HELPERS
    // ════════════════════════════════════════════════════════
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

    private void showInfo(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
