package com.petopia.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class HomeController {

    @FXML
    private Button battleButton;

    @FXML
    public void initialize() {

        battleButton.setOnAction(event -> {
            try {

                FXMLLoader loader =
                        new FXMLLoader(getClass().getResource("/fxml/Arena.fxml"));

                Stage stage = (Stage) battleButton.getScene().getWindow();

                Scene scene = new Scene(loader.load());

                stage.setScene(scene);

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}