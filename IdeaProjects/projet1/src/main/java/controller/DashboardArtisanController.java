package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import model.User;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import java.io.IOException;

public class DashboardArtisanController {

    @FXML private Button btnDashboard;
    @FXML private StackPane mainContent;
    @FXML private HBox userBox;
    @FXML private Label userNameLabel;
    @FXML private ImageView userPhoto;
    @FXML private ImageView logoImage;
    @FXML private Button btnTableAteliersArtisan;
    @FXML private Button btnCalendrier;


    private final ContextMenu contextMenu = new ContextMenu();
    private User connectedUser;

    public void initialize(User user) {
        this.connectedUser = user;
        // Actions sur les boutons
        btnDashboard.setOnAction(e -> loadDashboardHome());
        // Chargement page par défaut
        loadDashboardHome();
        logoImage.setImage(new Image(getClass().getResourceAsStream("/assets/logo2.png")));

        if (connectedUser != null) {
            userNameLabel.setText(connectedUser.getPrenom() + " " + connectedUser.getNom());

            // Chargement photo profil
            String photoPath = connectedUser.getPhoto();
            Image image;
            try {
                image = new Image(getClass().getResourceAsStream("/assets/users/" + photoPath));
                if (image.isError()) throw new Exception();
            } catch (Exception e) {
                image = new Image(getClass().getResourceAsStream("/assets/userf.png"));
            }
            userPhoto.setImage(image);
            Circle clip = new Circle(16, 16, 16);
            userPhoto.setClip(clip);
        } else {
            userNameLabel.setText("Utilisateur");
        }
        MenuItem profilItem = new MenuItem("👤 Mon profil");
        profilItem.setOnAction(e -> handleMonProfil());

        MenuItem logoutItem = new MenuItem("🚪 Déconnexion");
        logoutItem.setOnAction(e -> handleDeconnexion());

        contextMenu.getItems().addAll(profilItem, logoutItem);
        // Charger une vue par défaut dans mainContent si souhaité
        // loadPage("/views/accueilArtisan.fxml");
    }

    @FXML
    public void initialize() {
        btnTableAteliersArtisan.setOnAction(event -> loadTableAteliers());
        btnCalendrier.setOnAction(event -> loadCalendrier());
    }


    @FXML
    private void showUserMenu() {
        contextMenu.show(userBox, Side.BOTTOM, 0, 5);
    }
    private void loadDashboardHome() {
        //
    }

    @FXML
    private void handleMonProfil() {
      //
    }
    @FXML
    private void handleDeconnexion() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de déconnexion");
        alert.setHeaderText("Voulez-vous vraiment vous déconnecter ?");
        alert.setContentText("Votre session sera terminée.");

        ButtonType boutonOui = new ButtonType("Oui", ButtonBar.ButtonData.YES);
        ButtonType boutonNon = new ButtonType("Non", ButtonBar.ButtonData.NO);

        alert.getButtonTypes().setAll(boutonOui, boutonNon);

        alert.showAndWait().ifPresent(response -> {
            if (response == boutonOui) {
                System.out.println("🚪 Déconnexion...");
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
                    Parent root = loader.load();
                    Stage stage = (Stage) userBox.getScene().getWindow();
                    stage.setScene(new Scene(root));
                    stage.show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("❌ Déconnexion annulée.");
            }
        });
    }
    private void loadTableAteliers() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/TableAteliers.fxml"));
            Parent view = loader.load();
            mainContent.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void loadCalendrier() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Calendrier.fxml"));
            Parent view = loader.load();
            mainContent.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
