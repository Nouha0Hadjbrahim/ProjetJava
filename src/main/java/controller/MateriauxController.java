package controller;

import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.transform.Rotate;
import model.Material;
import model.User;
import service.MateriauxService;
import service.WishlistService;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import utils.SessionManager;

public class MateriauxController {
    private static final int COLUMNS = 4;
    private static final int CARD_WIDTH = 250;
    private static final int CARD_HEIGHT = 300;
    private static final int IMAGE_SIZE = 150;

    @FXML private GridPane materialsGrid;
    @FXML private ComboBox<String> filterCombo;
    private User connectedUser;
    private MateriauxService materiauxService;
    private WishlistService wishlistService;

    @FXML
    public void initialize() {
        System.out.println("[MATERIAUX] Initialisation du contrôleur");
        try {
            materiauxService = new MateriauxService();
            wishlistService = new WishlistService();

            if (filterCombo == null || materialsGrid == null) {
                throw new IllegalStateException("Composants FXML non injectés correctement");
            }

            setupFilters();
            loadMaterials();

        } catch(Exception e) {
            handleError("Erreur d'initialisation", e);
        }
    }

    private void setupFilters() {
        System.out.println("[UI] Configuration des filtres");
        try {
            filterCombo.getItems().setAll("Tous", "Bois", "Métal", "Textile", "Céramique");
            filterCombo.setValue("Tous");

            filterCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                System.out.println("[FILTRE] Sélection: " + newVal);
                try {
                    if ("Tous".equals(newVal)) {
                        loadMaterials();
                    } else {
                        loadMaterialsByCategory(newVal);
                    }
                } catch (Exception e) {
                    handleError("Erreur de filtrage", e);
                }
            });
        } catch (Exception e) {
            handleError("Configuration des filtres", e);
        }
    }

    private void loadMaterialsByCategory(String category) throws SQLException {
        System.out.println("[DATA] Chargement matériaux par catégorie: " + category);
        materialsGrid.getChildren().clear();
        List<Material> materials = materiauxService.getByCategory(category);
        displayMaterials(materials);
    }

    private void displayMaterials(List<Material> materials) {
        System.out.println("[UI] Affichage de " + materials.size() + " matériaux");
        materialsGrid.getChildren().clear();

        int col = 0;
        int row = 0;

        for (Material material : materials) {
            StackPane flipCard = createFlipCard(material);
            materialsGrid.add(flipCard, col, row);

            col = (col + 1) % COLUMNS;
            if (col == 0) row++;
        }
    }

    private void loadMaterials() {
        System.out.println("[DATA] Chargement de tous les matériaux");
        try {
            List<Material> materials = materiauxService.getAll();
            displayMaterials(materials);
        } catch (Exception e) {
            handleError("Chargement des matériaux", e);
        }
    }

    private StackPane createFlipCard(Material material) {
        StackPane flipCard = new StackPane();
        flipCard.getStyleClass().add("flip-card");
        flipCard.setPrefSize(CARD_WIDTH, CARD_HEIGHT);

        VBox front = createFrontFace(material);
        VBox back = createBackFace(material, flipCard);

        flipCard.getChildren().addAll(front, back);
        back.setVisible(false);

        return flipCard;
    }

    private VBox createFrontFace(Material material) {
        VBox front = new VBox(10);
        front.getStyleClass().add("flip-card-front");
        front.setAlignment(Pos.CENTER);
        front.setPadding(new Insets(15));

        ImageView imageView = loadMaterialImage(material);

        Label nameLabel = new Label(material.getNomMateriel());
        nameLabel.getStyleClass().add("material-name");
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(CARD_WIDTH - 70);

        Label priceLabel = new Label(String.format("%.2f DT", material.getPrixUnitaire()));
        priceLabel.getStyleClass().add("material-price");

        Button detailsBtn = new Button("Détails");
        detailsBtn.getStyleClass().add("flip-btn");
        detailsBtn.setOnAction(e -> flipCard((StackPane) detailsBtn.getParent().getParent()));

        front.getChildren().addAll(imageView, nameLabel, priceLabel, detailsBtn);
        return front;
    }

    private VBox createBackFace(Material material, StackPane flipCard) {
        VBox back = new VBox(10);
        back.getStyleClass().add("flip-card-back");
        back.setAlignment(Pos.CENTER);
        back.setPadding(new Insets(15));

        Label descTitle = new Label("Description");
        descTitle.getStyleClass().add("back-title");

        TextArea description = new TextArea(material.getDescription());
        description.getStyleClass().add("back-description");
        description.setEditable(false);
        description.setWrapText(true);
        description.setPrefHeight(100);
        description.setMaxWidth(CARD_WIDTH - 50);

        HBox buttonsBox = createActionButtons(material, flipCard);

        back.getChildren().addAll(descTitle, description, buttonsBox);
        return back;
    }

    private HBox createActionButtons(Material material, StackPane flipCard) {
        HBox buttonsBox = new HBox(10);
        buttonsBox.setAlignment(Pos.CENTER);

        // Bouton "Ajouter au panier" avec icône
        ImageView cartIcon = new ImageView(new Image(getClass().getResourceAsStream("/assets/icons/panier.png")));
        cartIcon.setFitHeight(20);
        cartIcon.setFitWidth(20);
        Button addToCartBtn = new Button("", cartIcon);
        addToCartBtn.getStyleClass().add("action-btn");
        addToCartBtn.setOnAction(e -> handleAddToCart(material));

        // Bouton "Wishlist" avec icône
        ImageView wishlistIcon = new ImageView(new Image(getClass().getResourceAsStream("/assets/icons/heart.png")));
        wishlistIcon.setFitHeight(20);
        wishlistIcon.setFitWidth(20);
        Button wishlistBtn = new Button("", wishlistIcon);
        wishlistBtn.getStyleClass().add("action-btn");
        wishlistBtn.setOnAction(e -> handleWishlistAction(material));

        // Bouton "Retour" avec icône
        ImageView backIcon = new ImageView(new Image(getClass().getResourceAsStream("/assets/icons/retour.png")));
        backIcon.setFitHeight(20);
        backIcon.setFitWidth(20);
        Button backBtn = new Button("", backIcon);
        backBtn.getStyleClass().add("flip-btn");
        backBtn.setOnAction(e -> flipCard(flipCard));

        buttonsBox.getChildren().addAll(addToCartBtn, wishlistBtn, backBtn);
        return buttonsBox;
    }

    private void handleAddToCart(Material material) {
        System.out.println("[ACTION] Ajout au panier: " + material.getNomMateriel());
        // TODO: Implémenter la logique d'ajout au panier
        showAlert("Information", "Fonctionnalité d'ajout au panier à implémenter");
    }

    private void handleWishlistAction(Material material) {
        SessionManager sessionManager = SessionManager.getInstance();
        User currentUser = sessionManager.getCurrentUser();

        if (!sessionManager.isLoggedIn()) {
            showAlert("Connexion requise", "Vous devez être connecté pour utiliser la Wishlist");
            return;
        }

        try {
            wishlistService.addToWishlist(currentUser, material);
            System.out.println("[DATA] Matériau ajouté à la wishlist: " + material.getNomMateriel());



                openWishlistWindow();

        } catch (SQLException ex) {
            handleError("Ajout à la wishlist", ex);
        }
    }

    private void openWishlistWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fornt views/Wishlist.fxml"));
            Parent root = loader.load();

            /// Récupérer le contrôleur et charger les matériaux
            WishlistController controller = loader.getController();
            controller.loadWishlistMaterials();

            // Récupérer le stage actuel à partir de n'importe quel nœud de la scène
            Stage currentStage = (Stage) materialsGrid.getScene().getWindow();

            // Remplacer le contenu de la scène actuelle
            currentStage.getScene().setRoot(root);
            currentStage.setTitle("Ma Wishlist");
        } catch (IOException e) {
            handleError("Ouverture de la Wishlist", e);
        }
    }

    // Supprimez setConnectedUser() car inutile maintenant
    private ImageView loadMaterialImage(Material material) {
        ImageView imageView = new ImageView();
        try {
            String imagePath = "/assets/prod_mat/" + material.getPhoto();
            URL imageUrl = getClass().getResource(imagePath);
            Image image = imageUrl != null ?
                    new Image(imageUrl.toExternalForm()) :
                    new Image(getClass().getResourceAsStream("/assets/placeholder.png"));
            imageView.setImage(image);
        } catch (Exception e) {
            System.err.println("[ERREUR] Chargement image: " + e.getMessage());
            imageView.setImage(new Image(getClass().getResourceAsStream("/assets/default_material.png")));
        }

        imageView.setFitWidth(IMAGE_SIZE);
        imageView.setFitHeight(IMAGE_SIZE);
        imageView.setPreserveRatio(true);
        return imageView;
    }

    private void flipCard(StackPane flipCard) {
        System.out.println("[ANIMATION] Rotation de la carte");
        Node front = flipCard.getChildren().get(0);
        Node back = flipCard.getChildren().get(1);

        flipCard.setCache(true);
        flipCard.setCacheHint(CacheHint.ROTATE);
        flipCard.setRotationAxis(Rotate.Y_AXIS);

        RotateTransition rotateOut = new RotateTransition(Duration.millis(250), flipCard);
        rotateOut.setFromAngle(0);
        rotateOut.setToAngle(90);
        rotateOut.setInterpolator(Interpolator.EASE_OUT);

        RotateTransition rotateIn = new RotateTransition(Duration.millis(250), flipCard);
        rotateIn.setFromAngle(-90);
        rotateIn.setToAngle(0);
        rotateIn.setInterpolator(Interpolator.EASE_IN);

        rotateOut.setOnFinished(event -> {
            front.setVisible(!front.isVisible());
            back.setVisible(!back.isVisible());
            rotateIn.play();
        });

        rotateOut.play();
    }

    private void showAlert(String title, String message) {
        System.out.println("[ALERT] " + title + ": " + message);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private Optional<ButtonType> showConfirmation(String title, String message, String yesText, String noText) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        ButtonType yesButton = new ButtonType(yesText, ButtonBar.ButtonData.YES);
        ButtonType noButton = new ButtonType(noText, ButtonBar.ButtonData.NO);
        alert.getButtonTypes().setAll(yesButton, noButton);

        return alert.showAndWait();
    }

    private void handleError(String context, Exception e) {
        System.err.println("[ERREUR] " + context + ": " + e.getMessage());
        showAlert("Erreur", context + ": " + e.getMessage());
        e.printStackTrace();
    }

    public void setConnectedUser(User user) {
        System.out.println("[AUTH] Définition de l'utilisateur connecté: " + (user != null ? user.getId() : "null"));
        this.connectedUser = user;
        if (materialsGrid != null) {
            loadMaterials();
        }
    }
}