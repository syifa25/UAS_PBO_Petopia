package com.petopia;

import com.petopia.db.DatabaseUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        // Initialize DB schema early so any SQL errors are visible right away
        try {
            DatabaseUtil.initH2();
            // Optional: DatabaseUtil.printAllUsers(); // uncomment for debugging
        } catch (Exception e) {
            System.err.println("Database initialization failed in Main.start():");
            e.printStackTrace();
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Home.fxml"));
        Scene scene = new Scene(loader.load(), 1000, 700);

        scene.getStylesheets().add(
                getClass().getResource("/css/style.css").toExternalForm()
        );

        stage.setTitle("Petopia");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}