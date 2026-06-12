package com.petopia.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ArenaController {

    @FXML private Button navHome;
    @FXML private Button navMyPets;
    @FXML private Button navMarketplace;
    @FXML private Button backHomeBtn;

    @FXML private ImageView swordIconLeft;
    @FXML private ImageView swordIconRight;

    @FXML private ImageView fighter1Image;
    @FXML private ImageView fighter2Image;
    @FXML private ImageView fighter3Image;

    @FXML private ImageView swordStat1, swordStat2, swordStat3;
    @FXML private ImageView protectStat1, protectStat2, protectStat3;
    @FXML private ImageView loveStat1, loveStat2, loveStat3;

    @FXML private VBox card1;
    @FXML private VBox card2;
    @FXML private VBox card3;

    @FXML
    public void initialize() {
        // Title sword icons
        loadImage(swordIconLeft,  "/images/pedang.png");
        loadImage(swordIconRight, "/images/pedang.png");

        // Pet images
        loadImage(fighter1Image, "/images/pet1.png");
        loadImage(fighter2Image, "/images/pet2.png");
        loadImage(fighter3Image, "/images/pet4.png");

        // Stat icons — semua card pakai gambar yang sama
        String[] statPaths = {"/images/pedang.png", "/images/protect.png", "/images/love.png"};

        loadImage(swordStat1,   statPaths[0]);
        loadImage(protectStat1, statPaths[1]);
        loadImage(loveStat1,    statPaths[2]);

        loadImage(swordStat2,   statPaths[0]);
        loadImage(protectStat2, statPaths[1]);
        loadImage(loveStat2,    statPaths[2]);

        loadImage(swordStat3,   statPaths[0]);
        loadImage(protectStat3, statPaths[1]);
        loadImage(loveStat3,    statPaths[2]);

        // Navbar
        navHome.setOnAction(e -> navigateTo("/fxml/Home.fxml", navHome));
        navMyPets.setOnAction(e -> navigateTo("/fxml/MyPets.fxml", navMyPets));
        navMarketplace.setOnAction(e -> navigateTo("/fxml/Marketplace.fxml", navMarketplace));
    }

    @FXML private void selectCard1() { selectCard(card1); }
    @FXML private void selectCard2() { selectCard(card2); }
    @FXML private void selectCard3() { selectCard(card3); }

    private void selectCard(VBox card) {
        card1.getStyleClass().remove("fighter-card-selected");
        card2.getStyleClass().remove("fighter-card-selected");
        card3.getStyleClass().remove("fighter-card-selected");
        card.getStyleClass().add("fighter-card-selected");
    }

    @FXML
    private void goToHome() {
        navigateTo("/fxml/Home.fxml", backHomeBtn);
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
            scene.getStylesheets().add(
                    getClass().getResource("/css/style.css").toExternalForm()
            );
            stage.setScene(scene);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
