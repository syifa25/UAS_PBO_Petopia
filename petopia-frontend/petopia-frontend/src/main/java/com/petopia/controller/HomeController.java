package com.petopia.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class HomeController {

    @FXML private Button navHome;
    @FXML private Button navMyPets;
    @FXML private Button navMarketplace;
    @FXML private Button navLogin;

    @FXML private Button battleButton;
    @FXML private Button howToPlayBtn;
    @FXML private Button pet1Btn;
    @FXML private Button pet2Btn;
    @FXML private Button pet3Btn;
    @FXML private Button pet4Btn;

    @FXML private ImageView playerPetImage;
    @FXML private ImageView enemyPetImage;

    private final String[] petImages = {
            "/images/pet1.png",
            "/images/pet2.png",
            "/images/pet3.png",
            "/images/pet4.png"
    };

    private Button[] petButtons;
    private int selectedPetIndex = 0;

    @FXML
    public void initialize() {

        // ===== NAVBAR =====
        navHome.setOnAction(e -> navigateTo("/fxml/Home.fxml", navHome));
        navMyPets.setOnAction(e -> navigateTo("/fxml/MyPets.fxml", navMyPets));
        navMarketplace.setOnAction(e -> navigateTo("/fxml/Marketplace.fxml", navMarketplace));
        navLogin.setOnAction(e -> navigateTo("/fxml/Login.fxml", navLogin));

        // ===== PET SELECTOR =====
        petButtons = new Button[]{pet1Btn, pet2Btn, pet3Btn, pet4Btn};

        for (int i = 0; i < petButtons.length; i++) {
            final int idx = i;
            try {
                var stream = getClass().getResourceAsStream(petImages[i]);
                if (stream != null) {
                    ImageView iv = new ImageView(new Image(stream));
                    iv.setFitWidth(38);
                    iv.setFitHeight(38);
                    iv.setPreserveRatio(true);
                    petButtons[i].setGraphic(iv);
                }
            } catch (Exception e) {
                System.out.println("Gambar tidak ditemukan: " + petImages[i]);
            }
            petButtons[i].setOnAction(e -> selectPet(idx));
        }

        loadPlayerPet(selectedPetIndex);
        loadEnemyPet();

        // ===== BATTLE BUTTON =====
        battleButton.setOnAction(e -> navigateTo("/fxml/Arena.fxml", battleButton));

        // ===== HOW TO PLAY =====
        howToPlayBtn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("How To Play");
            alert.setHeaderText("PETOPIA - How To Play");
            alert.setContentText(
                    "1. Select your pet from the bottom left.\n" +
                            "2. Press BATTLE START! to fight the enemy.\n" +
                            "3. Use your skills in battle to defeat the opponent.\n" +
                            "4. Win battles to earn rewards!\n\n" +
                            "Good luck, Trainer!"
            );
            alert.showAndWait();
        });
    }

    // ===== NAVIGASI =====
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

    // ===== PET SELECTION =====
    private void selectPet(int index) {
        selectedPetIndex = index;
        for (Button btn : petButtons) {
            btn.getStyleClass().remove("pet-btn-selected");
        }
        petButtons[index].getStyleClass().add("pet-btn-selected");
        loadPlayerPet(index);
    }

    private void loadPlayerPet(int index) {
        try {
            var stream = getClass().getResourceAsStream(petImages[index]);
            if (stream != null) playerPetImage.setImage(new Image(stream));
        } catch (Exception e) {
            System.out.println("Player pet tidak ditemukan.");
        }
    }

    private void loadEnemyPet() {
        try {
            var stream = getClass().getResourceAsStream("/images/pet2.png");
            if (stream != null) enemyPetImage.setImage(new Image(stream));
        } catch (Exception e) {
            System.out.println("Enemy pet tidak ditemukan.");
        }
    }
}
