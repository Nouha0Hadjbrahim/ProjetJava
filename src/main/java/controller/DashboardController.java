package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import model.User;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.control.Button;
import java.io.IOException;
import java.net.URL;
import model.Message;
import service.MessageService;

public class DashboardController {

    public Button btnCommande;
    @FXML private Button btnDashboard;
    @FXML private Button btnTableUtilisateurs;
    @FXML
    private Button btnTableFournisseur;
    @FXML
    private Button btnTableMateriaux;
    @FXML
    private Button btnTableCommande;
    @FXML
    private Button btnTableProduits;
    @FXML private StackPane mainContent;
    @FXML private StackPane alertStackPane;
    @FXML private ImageView alertIcon;
    @FXML private Label alertCounter;

    @FXML private HBox userBox;
    @FXML private Label userNameLabel;
    @FXML private ImageView userPhoto;
    @FXML private ImageView logoImage;

    @FXML
    private Button btnTableAteliers;


    private final ContextMenu contextMenu = new ContextMenu();
    private User connectedUser;
    private int messageCount = 0;

    public void initialize(User user) {
        this.connectedUser = user;
        initializeButtons();
        initializeAlertIcon();

        // Actions sur les boutons
        btnDashboard.setOnAction(e -> loadDashboardHome());
        btnTableUtilisateurs.setOnAction(e -> loadTableUtilisateurs());
        btnTableAteliers.setOnAction(event -> loadTableAteliers());
        btnTableCommande.setOnAction(event -> loadTableCommande());
        btnTableProduits.setOnAction(event -> loadTableProduits());

        // Chargement page par défaut
        loadDashboardHome();

        // Logo
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

        // Préparer menu contextuel
        MenuItem profilItem = new MenuItem("👤 Mon profil");
        profilItem.setOnAction(e -> handleMonProfil());

        MenuItem logoutItem = new MenuItem("🚪 Déconnexion");
        logoutItem.setOnAction(e -> handleDeconnexion());

        contextMenu.getItems().addAll(profilItem, logoutItem);
    }

    private void loadTableProduits() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Produit_Promotion/ProductsTableController.fxml"));
            Parent view = loader.load();

            // Récupérer le controller et passer l'utilisateur connecté
            ProductsTableController controller = loader.getController();
            controller.setConnectedUser(connectedUser); // 🔥 Important ici !

            mainContent.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void loadTableCommande() {try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/commande_ligne/TableCommande.fxml"));
        Parent view = loader.load();
        mainContent.getChildren().setAll(view);
    } catch (IOException e) {
        e.printStackTrace();
    }
    }

    private void initializeAlertIcon() {
        try {
            Image alertImage = new Image(getClass().getResourceAsStream("/assets/icons/alert.png"));
            if (alertImage.isError()) {
                throw new Exception("Image non trouvée");
            }
            alertIcon.setImage(alertImage);
        } catch (Exception e) {
            System.out.println("Icône d'alerte non trouvée, utilisation d'une icône par défaut");
            alertIcon.setImage(new Image(getClass().getResourceAsStream("/assets/icons/alert.png")));
        }
        updateAlertCounter();
    }

    public void updateAlertCounter() {
        int unreadCount = MessageService.getInstance().getUnreadCount();
        alertCounter.setText(String.valueOf(unreadCount));
        alertCounter.setVisible(unreadCount > 0);
    }

    @FXML
    private void handleAlertClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/MessagesView.fxml"));
            Parent messagesView = loader.load();
            MessagesViewController controller = loader.getController();
            controller.setDashboardController(this);
            mainContent.getChildren().setAll(messagesView);
            
            // Marquer tous les messages comme lus
            for (Message message : MessageService.getInstance().getMessages()) {
                message.setLu(true);
            }
            updateAlertCounter();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Erreur", "Impossible de charger la vue des messages");
        }
    }

    @FXML
    private void showUserMenu() {
        contextMenu.show(userBox, Side.BOTTOM, 0, 5);
    }

    private void loadDashboardHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/DashboardHome.fxml"));
            Parent dashboardView = loader.load();
            DashboardHomeController controller = loader.getController();
            controller.setUser(connectedUser);  // Passer l'utilisateur connecté
            mainContent.getChildren().setAll(dashboardView);
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    @FXML
    private void handleMonProfil() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/ProfilBack.fxml"));
            AnchorPane profilView = loader.load();
            ProfilController controller = loader.getController();
            controller.setUser(connectedUser); // ✅ plus d'erreur maintenant
            mainContent.getChildren().setAll(profilView);

        } catch (IOException e) {
            e.printStackTrace();
        }
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

    private void loadTableMateriaux() {
        System.out.println("Chargement de TableMateriaux.fxml...");
        URL url = getClass().getResource("/views/TableMateriaux.fxml");
        if (url == null) {
            System.err.println("Fichier TableMateriaux.fxml introuvable !");
            // Afficher une alerte à l'utilisateur
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Fichier introuvable");
            alert.setContentText("Le fichier TableMateriaux.fxml n'a pas été trouvé dans le dossier /views/");
            alert.showAndWait();
            return;
        } else {
            System.out.println("Fichier trouvé: " + url);
        }

        try {
            FXMLLoader loader = new FXMLLoader(url);
            Parent view = loader.load();
            TableMateriauxController controller = loader.getController();
            controller.setConnectedUser(connectedUser);
            mainContent.getChildren().setAll(view);
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement du FXML:");
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Erreur de chargement");
            alert.setContentText("Erreur lors du chargement de l'interface: " + e.getMessage());
            alert.showAndWait();
        }
    }
    private void loadTableFournisseur() {
        System.out.println("Tentative de chargement de l'interface fournisseur...");

        try {
            // 1. Vérification de l'existence du fichier
            URL fxmlUrl = getClass().getResource("/views/TableFournisseur.fxml");
            if (fxmlUrl == null) {
                throw new IOException("Le fichier TableFournisseur.fxml est introuvable dans le dossier views");
            }
            System.out.println("Fichier FXML trouvé : " + fxmlUrl);

            // 2. Chargement avec FXMLLoader
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            // 3. Accès au contrôleur si nécessaire
            TableFournisseurController controller = loader.getController();
            if (controller != null) {
                // Initialisation supplémentaire du contrôleur si besoin
            }

            // 4. Affichage dans la zone de contenu principale
            mainContent.getChildren().setAll(root);

            System.out.println("Interface fournisseur chargée avec succès !");

        } catch (IOException e) {
            System.err.println("Échec du chargement de l'interface fournisseur :");
            e.printStackTrace();

            // Message d'erreur détaillé
            String errorMessage = "Erreur lors du chargement de l'interface fournisseur :\n\n";

            if (e.getCause() != null) {
                errorMessage += "Cause : " + e.getCause().getMessage();

                // Détection des problèmes courants
                if (e.getCause().getMessage().contains("fx:id")) {
                    errorMessage += "\n\nProblème probable :\n"
                            + "- Un élément FXML a un fx:id qui ne correspond pas au contrôleur\n"
                            + "- Vérifiez les correspondances entre le FXML et le contrôleur";
                }
            } else {
                errorMessage += e.getMessage();
            }

            showErrorAlert("Erreur de chargement", errorMessage);
        } catch (Exception e) {
            System.err.println("Erreur inattendue :");
            e.printStackTrace();
            showErrorAlert("Erreur inattendue", e.toString());
        }
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Ajout du style pour un meilleur affichage
        alert.getDialogPane().setStyle("-fx-font-family: 'Arial'; -fx-font-size: 14px;");
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);

        alert.showAndWait();
    }
    public StackPane getMainContent() {
        return mainContent;
    }
    private void initializeButtons() {
        btnTableMateriaux.setOnAction(e -> loadTableMateriaux());
        btnTableFournisseur.setOnAction(e -> loadTableFournisseur());
    }
    private void loadTableAteliers() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/TableAteliersAdmin.fxml"));
            Parent view = loader.load();
            mainContent.getChildren().setAll(view);
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
