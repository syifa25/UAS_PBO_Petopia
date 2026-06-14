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

public class MyPetsController {

    @FXML private Button navHome;
    @FXML private Button navMyPets;
    @FXML private Button navLeaderboard;
    @FXML private Button navLogin;
    @FXML private Button goToArenaBtn;

    @FXML private ImageView pet1Image;
    @FXML private ImageView pet2Image;
    @FXML private ImageView pet3Image;
    @FXML private ImageView pet4Image;
    @FXML private ImageView pet5Image;

    @FXML
    public void initialize() {
        loadImage(pet1Image, "/images/pet1.png");
        loadImage(pet2Image, "/images/risol_mayo_cat.png");
        loadImage(pet3Image, "/images/pet2.png");
        loadImage(pet4Image, "/images/slime_ijo.png");
        loadImage(pet5Image, "/images/burung.png");

        navHome.setOnAction(e -> navigateTo("/fxml/Home.fxml", navHome));
        navMyPets.setOnAction(e -> navigateTo("/fxml/MyPets.fxml", navMyPets));
        navLeaderboard.setOnAction(e -> navigateTo("/fxml/Leaderboard.fxml", navLeaderboard));

        if (com.petopia.model.Session.isLoggedIn()) {
            navLogin.setText(com.petopia.model.Session.getDisplayName() != null && !com.petopia.model.Session.getDisplayName().isBlank()
                    ? com.petopia.model.Session.getDisplayName().toUpperCase()
                    : com.petopia.model.Session.getUsername().toUpperCase());
            navLogin.setOnAction(e -> {
                com.petopia.model.Session.logout();
                navigateTo("/fxml/Login.fxml", navLogin);
            });
        } else {
            navLogin.setText("LOGIN");
            navLogin.setOnAction(e -> navigateTo("/fxml/Login.fxml", navLogin));
        }
    }

    @FXML
    private void goToArena() {
        navigateTo("/fxml/Arena.fxml", goToArenaBtn);
    }

    @FXML private void feedPet1()  { showInfo("Fed!",   "Dog enjoyed the meal! 🍖"); }
    @FXML private void playPet1()  { showInfo("Play!",  "Dog is having fun! ▶"); }
    @FXML private void trainPet1() { showInfo("Train!", "Dog is getting stronger! 💪"); }

    @FXML private void feedPet2()  { showInfo("Fed!",   "Risol Cat enjoyed the meal! 🍖"); }
    @FXML private void playPet2()  { showInfo("Play!",  "Risol Cat is having fun! ▶"); }
    @FXML private void trainPet2() { showInfo("Train!", "Risol Cat is getting stronger! 💪"); }

    @FXML private void feedPet3()  { showInfo("Fed!",   "Cat enjoyed the meal! 🍖"); }
    @FXML private void playPet3()  { showInfo("Play!",  "Cat is having fun! ▶"); }
    @FXML private void trainPet3() { showInfo("Train!", "Cat is getting stronger! 💪"); }

    @FXML private void feedPet4()  { showInfo("Fed!",   "Aqua Slime enjoyed the meal! 🍖"); }
    @FXML private void playPet4()  { showInfo("Play!",  "Aqua Slime is having fun! ▶"); }
    @FXML private void trainPet4() { showInfo("Train!", "Aqua Slime is getting stronger! 💪"); }

    @FXML private void feedPet5()  { showInfo("Fed!",   "Swift Bird enjoyed the meal! 🍖"); }
    @FXML private void playPet5()  { showInfo("Play!",  "Swift Bird is having fun! ▶"); }
    @FXML private void trainPet5() { showInfo("Train!", "Swift Bird is getting stronger! 💪"); }

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

    private void showInfo(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
