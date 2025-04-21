package controller;

import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.Material;
import model.User;
import service.WishlistService;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import utils.SessionManager;

public class WishlistController {
    @FXML private GridPane materialsGrid;
    private WishlistService wishlistService;

    @FXML
    public void initialize() {
        System.out.println("[WISHLIST] Initialisation du contrôleur");
        try {
            wishlistService = new WishlistService();

        } catch (SQLException e) {
            handleError("Erreur d'initialisation", e);
        }
    }

    public void loadWishlistMaterials() {
        System.out.println("[WISHLIST] Chargement des matériaux");
        SessionManager sessionManager = SessionManager.getInstance();
        User currentUser = sessionManager.getCurrentUser();

        if (!sessionManager.isLoggedIn()) {
            showAlert("Connexion requise", "Vous devez être connecté pour voir votre Wishlist");
            return;
        }

        try {
            List<Material> materials = wishlistService.getUserWishlist(currentUser);
            materialsGrid.getChildren().clear();

            if (materials.isEmpty()) {
                System.out.println("[WISHLIST] Aucun matériau trouvé");
                Label emptyLabel = new Label("Votre Wishlist est vide");
                emptyLabel.getStyleClass().add("empty-wishlist-label");
                materialsGrid.add(emptyLabel, 0, 0, 4, 1);
                return;
            }

            System.out.println("[WISHLIST] Affichage de " + materials.size() + " matériaux");
            int col = 0;
            int row = 0;
            int maxColumns = 4;

            for (Material material : materials) {
                StackPane flipCard = createFlipCard(material);
                materialsGrid.add(flipCard, col, row);

                col++;
                if (col >= maxColumns) {
                    col = 0;
                    row++;
                }
            }
        } catch (Exception e) {
            handleError("Erreur de chargement de la Wishlist", e);
        }
    }

    private Button createRemoveButton(Material material) {
        Button removeBtn = new Button("Retirer");
        removeBtn.getStyleClass().add("wishlist-remove-btn");

        removeBtn.setOnAction(e -> {
            System.out.println("[ACTION] Bouton Retirer cliqué pour: " + material.getNomMateriel());
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Confirmation");
            confirmation.setHeaderText("Retirer ce matériau");
            confirmation.setContentText("Êtes-vous sûr de vouloir retirer " + material.getNomMateriel() + " de votre Wishlist ?");

            Optional<ButtonType> result = confirmation.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    User currentUser = SessionManager.getInstance().getCurrentUser();
                    wishlistService.removeFromWishlist(currentUser, material);
                    System.out.println("[DATA] Matériau retiré: " + material.getNomMateriel());
                    showAlert("Succès", material.getNomMateriel() + " a été retiré de votre Wishlist");
                    loadWishlistMaterials();
                } catch (SQLException ex) {
                    handleError("Erreur lors de la suppression", ex);
                }
            }
        });
        return removeBtn;
    }

    @FXML
    private void handleBackToMaterials() {
        try {
            URL fxmlLocation = getClass().getResource("/fornt views/materiaux.fxml");
            Parent root = FXMLLoader.load(fxmlLocation);
            Stage stage = (Stage) materialsGrid.getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setTitle("Matériaux");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleError(String context, Exception e) {
        System.err.println("[ERREUR] " + context + ": " + e.getMessage());
        showAlert("Erreur", context + ": " + e.getMessage());
        e.printStackTrace();
    }

    private void showAlert(String title, String message) {
        System.out.println("[ALERT] " + title + ": " + message);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    private StackPane createFlipCard(Material material) {
        StackPane flipCard = new StackPane();
        flipCard.getStyleClass().add("flip-card");
        flipCard.setPrefSize(250, 300);

        VBox front = createFrontFace(material);
        VBox back = createBackFace(material, flipCard);

        flipCard.getChildren().addAll(front, back);
        back.setVisible(false);

        flipCard.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                System.out.println("[ACTION] Double-clic sur la carte: " + material.getNomMateriel());
                flipCard(flipCard);
            }
        });

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
        nameLabel.setMaxWidth(180);

        Label priceLabel = new Label(String.format("%.2f DT", material.getPrixUnitaire()));
        priceLabel.getStyleClass().add("material-price");

        Button detailsBtn = new Button("Détails");
        detailsBtn.getStyleClass().add("flip-btn");
        detailsBtn.setOnAction(e -> {
            System.out.println("[ACTION] Bouton Détails cliqué pour: " + material.getNomMateriel());
            flipCard((StackPane) detailsBtn.getParent().getParent());
        });

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
        description.setMaxWidth(200);

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
        addToCartBtn.setOnAction(e -> {
            System.out.println("[ACTION] Ajout au panier: " + material.getNomMateriel());

        });

        // Bouton "Supprimer" (déjà défini via une méthode externe)
        Button removeBtn = createRemoveButton(material);

        // Bouton "Retour" avec icône
        ImageView backIcon = new ImageView(new Image(getClass().getResourceAsStream("/assets/icons/retour.png")));
        backIcon.setFitHeight(20);
        backIcon.setFitWidth(20);
        Button backBtn = new Button("", backIcon);
        backBtn.getStyleClass().add("flip-btn");
        backBtn.setOnAction(e -> {
            System.out.println("[ACTION] Bouton Retour cliqué");
            flipCard(flipCard); // Appel à la méthode de retournement de carte
        });

        buttonsBox.getChildren().addAll(addToCartBtn, removeBtn, backBtn);
        return buttonsBox;
    }
    ImageView loadMaterialImage(Material material) {
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

        imageView.setFitWidth(150);
        imageView.setFitHeight(150);
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

}