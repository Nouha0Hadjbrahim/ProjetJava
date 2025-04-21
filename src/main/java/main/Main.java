package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Parent;

public class Main extends Application {
    @Override
    public void start(Stage stage) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/signup.fxml"));
            Scene scene = new Scene(root);
            stage.setTitle("Formulaire d'inscription");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace(); // ✅ Affiche l'erreur dans la console
        }
    }


    public static void main(String[] args) {
        launch(args);

    }
}
