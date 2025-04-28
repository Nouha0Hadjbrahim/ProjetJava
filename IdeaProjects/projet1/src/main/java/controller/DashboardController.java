package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane ;

import model.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import model.User;
import service.UserService;

import java.io.IOException;
import java.util.regex.Pattern;

public class DashboardController {

    @FXML
    private Label userNameLabel;

    @FXML
    private ImageView userPhoto;

    @FXML
    private ImageView logoImage; // facultatif si tu veux changer le logo dynamiquement


    @FXML
    private Button btnTableUtilisateurs;

    @FXML private Button btnDashboard;
    @FXML
    private StackPane mainContent;
    // Méthode appelée lors du chargement du dashboard avec l'utilisateur connecté
    public void initialize(User connectedUser) {
        btnDashboard.setOnAction(e -> loadDashboardHome());

// Charger par défaut au démarrage
        loadDashboardHome();

        if (connectedUser != null) {
            userNameLabel.setText(connectedUser.getPrenom() + "." + connectedUser.getNom());
        } else {
            userNameLabel.setText("Utilisateur");
        }

        // Charger une photo par défaut (tu peux la remplacer dynamiquement plus tard)
        logoImage.setImage(new Image(getClass().getResourceAsStream("/assets/logo2.png")));
        Image userImg = new Image(getClass().getResourceAsStream("/assets/userf.png"));
        userPhoto.setImage(userImg);
    }
    @FXML
    public void initialize() {
        btnTableUtilisateurs.setOnAction(event -> loadTableUtilisateurs());
    }

    // Getters si besoin (pour injection dynamique)
    public StackPane  getMainContent() {
        return mainContent;
    }


    private void loadTableUtilisateurs() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/TableUtilisateurs.fxml"));
            Parent view = loader.load();
            mainContent.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void loadDashboardHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/DashboardHome.fxml"));
            AnchorPane dashboardView = loader.load();
            mainContent.getChildren().setAll(dashboardView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleListeReclamation() {
        try {
            // Créer manuellement un FXMLLoader
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/BackReclamations.fxml"));
            Parent view = loader.load();

            BackReclamationController controller = loader.getController();
            // controller.doSomethingIfNeeded();

            // Remplacer le contenu du mainContent
            mainContent.getChildren().setAll(view);

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement de la vue des réclamations");
        }
    }


}
