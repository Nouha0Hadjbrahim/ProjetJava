package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import model.User;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import utils.SessionManager;

public class FrontClientController {

    @FXML private ImageView logoImage;
    @FXML private ImageView userIcon;
    @FXML private ImageView cartIcon;
    @FXML private MenuButton userMenu;
    @FXML private MenuItem profilMenuItem;
    @FXML private MenuItem historiqueMenuItem;
    @FXML private MenuItem deconnexionMenuItem;
    @FXML private StackPane contentPane;
    @FXML private Hyperlink linkAccueil;
    @FXML private Hyperlink linkAteliers;
    @FXML private Hyperlink linkReclamation;
    @FXML private MenuButton boutiqueMenu;
    @FXML private Button btnPanier;
    @FXML private Hyperlink inscri;
    @FXML private MenuButton historiqueMenu;

    private User connectedUser;
    private List<Object> navLinks;

    private void loadPage(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent page = loader.load();

            // Passer l'utilisateur connecté uniquement à la page d'accueil
            if (fxmlPath.equals("/fornt views/accueil.fxml")) {
                System.out.println("Chargement de la page d'accueil...");
                System.out.println("Utilisateur connecté : " + (connectedUser != null ? connectedUser.getEmail() : "null"));
                
                AccueilController accueilController = loader.getController();
                accueilController.setConnectedUser(connectedUser);
                
                // Vérifier si l'utilisateur a bien été passé
                System.out.println("Utilisateur passé au contrôleur : " + (accueilController.getConnectedUser() != null ? accueilController.getConnectedUser().getEmail() : "null"));
            }

            contentPane.getChildren().setAll(page);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger la page: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void handleNavLinkClick(ActionEvent event) {
        Object source = event.getSource();

        if (source instanceof Hyperlink || source instanceof MenuButton || source instanceof MenuItem || source instanceof Button) {
            setActiveNav(source);

            if (source == linkAccueil) {
                loadPage("/fornt views/accueil.fxml");
            } else if (source == linkAteliers) {
                loadPage("/fornt views/ateliers.fxml");
            } else if (source == linkReclamation) {
                loadPage("/fornt views/reclamation.fxml");
            } else if (source == inscri) {
                loadPage("/fornt views/inscription_atelier.fxml");
            } else if (source instanceof Button) {
                Button btn = (Button) source;
                if ("btnPanier".equals(btn.getId())) {
                    loadPage("/commande_ligne/cart.fxml");
                }
            } else if (source instanceof MenuItem) {
                MenuItem item = (MenuItem) source;
                String text = item.getText();

                if ("Profil".equals(text)) {
                    loadPage("/fornt views/profil.fxml");
                } else if ("Mes inscriptions".equals(text)) {
                    loadPage("/fornt views/inscription_atelier.fxml"); // Ajout ici
                } else if ("Mes réclamations".equals(text)) {
                    loadPage("/fornt views/mes_reclamations.fxml"); // Exemple pour l'autre item
                }
            }
        }

        setActiveNav(source);
    }
    @FXML
    private void viewCart() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/commande_ligne/cart.fxml"));
            Parent page = loader.load();

            // Récupérer le contrôleur du Cart
            CartController cartController = loader.getController();

            // Passer l'utilisateur connecté
            cartController.setConnectedUser(connectedUser);

            // Afficher dans le contentPane
            contentPane.getChildren().setAll(page);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger la page du panier: " + e.getMessage());
        }
    }


    private void setActiveNav(Object activeLink) {
        for (Object link : navLinks) {
            if (link instanceof Hyperlink) {
                ((Hyperlink) link).getStyleClass().remove("active");
            } else if (link instanceof MenuButton) {
                ((MenuButton) link).getStyleClass().remove("active");
            }
        }

        if (activeLink instanceof Hyperlink) {
            ((Hyperlink) activeLink).getStyleClass().add("active");
        } else if (activeLink instanceof MenuButton) {
            ((MenuButton) activeLink).getStyleClass().add("active");
        }
    }

    @FXML
    public void initialize() {
        System.out.println("FrontClientController - initialize appelé");
        // Récupérer l'utilisateur de la session
        connectedUser = SessionManager.getInstance().getCurrentUser();
        System.out.println("Utilisateur de la session : " + (connectedUser != null ? connectedUser.getEmail() : "null"));

        logoImage.setImage(new Image(getClass().getResourceAsStream("/assets/logo2.png")));
        cartIcon.setImage(new Image(getClass().getResourceAsStream("/assets/chariot.png")));

        // Créer les menu items du menu Boutique
        MenuItem itemProduits = new MenuItem("Produits");
        MenuItem itemMateriaux = new MenuItem("Matériaux");

        itemProduits.setOnAction(e -> {
            setActiveNav(boutiqueMenu);
            loadPage("/Produit_Promotion/produit_catalogue_client.fxml");
        });

        itemMateriaux.setOnAction(e -> {
            setActiveNav(boutiqueMenu);
            loadPage("/fornt views/materiaux.fxml");
        });

        boutiqueMenu.getItems().setAll(itemProduits, itemMateriaux);
        
        // Initialiser la liste de navigation
        navLinks = new ArrayList<>();
        navLinks.add(linkAccueil);
        navLinks.add(boutiqueMenu);
        navLinks.add(linkAteliers);
        navLinks.add(linkReclamation);
        navLinks.add(inscri);


        setActiveNav(linkAccueil);
        loadPage("/fornt views/accueil.fxml");
    }

    public void setConnectedUser(User user) {
        System.out.println("FrontClientController - setConnectedUser appelé");
        System.out.println("Utilisateur reçu : " + (user != null ? user.getEmail() : "null"));
        this.connectedUser = user;
        SessionManager.getInstance().setCurrentUser(user);
        
        if (connectedUser != null) {
            // Chargement photo profil
            String photoPath = connectedUser.getPhoto();
            Image image;
            try {
                image = new Image(getClass().getResourceAsStream("/assets/users/" + photoPath));
                if (image.isError()) throw new Exception();
            } catch (Exception e) {
                image = new Image(getClass().getResourceAsStream("/assets/userf.png"));
            }
            userIcon.setImage(image);
            Circle clip = new Circle(16, 16, 16);
            userIcon.setClip(clip);
            
            // Recharger la page d'accueil pour mettre à jour l'utilisateur
            loadPage("/fornt views/accueil.fxml");
        }
    }

    @FXML
    public void handleDeconnexion(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Déconnexion");
        alert.setHeaderText("Confirmation de déconnexion");
        alert.setContentText("Êtes-vous sûr de vouloir vous déconnecter ?");

        ButtonType oui = new ButtonType("Oui");
        ButtonType non = new ButtonType("Non", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(oui, non);

        alert.showAndWait().ifPresent(response -> {
            if (response == oui) {
                try {
                    SessionManager.getInstance().clearSession();
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
                    Parent root = loader.load();
                    btnPanier.getScene().setRoot(root);
                    System.out.println("🔓 Déconnexion réussie");
                } catch (IOException e) {
                    e.printStackTrace();
                    System.err.println("❌ Échec du chargement de login.fxml");
                }
            }
        });
    }

    @FXML
    public void handleProfil(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fornt views/profil.fxml"));
            Parent profilView = loader.load();

            // Accès au contrôleur de profil
            ProfilController controller = loader.getController();
            controller.setUser(connectedUser);
            
            contentPane.getChildren().setAll(profilView);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger la page de profil");
        }
    }


    public void handleOrderHistoriqueItemClick(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/commande_ligne/orderHistory.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Historique des Commandes");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger l'historique.");
        }
    }
}
