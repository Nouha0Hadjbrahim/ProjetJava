package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Parent;

import java.net.URL;

public class Main extends Application {
    @Override
    public void start(Stage stage) {
        try {
            String fxmlPath = "/signup.fxml";
            System.out.println("Attempting to load FXML from: " + fxmlPath);
            URL fxmlUrl = getClass().getResource(fxmlPath);
            if (fxmlUrl == null) {
                System.out.println("FXML file not found at path: " + fxmlPath);
            } else {
                System.out.println("FXML file found, loading...");
            }

            Parent root = FXMLLoader.load(fxmlUrl);
            Scene scene = new Scene(root);
            stage.setTitle("Formulaire d'inscription");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error while loading FXML: " + e.getMessage());
        }
    }


    public static void main(String[] args) {
        System.out.println("Launching JavaFX...");
        launch(args);
    }
}

