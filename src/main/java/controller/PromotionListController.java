package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Promotion;
import model.User;
import service.PromotionService;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class PromotionListController {

    @FXML private GridPane promotionGrid;
    @FXML private Button addButton;
    @FXML private Label userNameLabel;
    @FXML private ImageView userPhoto;
    @FXML private ImageView logoImage;
    @FXML private Button btnDashboard;
    @FXML private Button btnProductTable;
    @FXML private Button btnTableUtilisateurs;
    @FXML private Button btnPromotionTable;
    @FXML private StackPane mainContent;

    private PromotionService promotionService;
    private User connectedUser;

    public void setConnectedUser(User user) {
        this.connectedUser = user;
        updateUserInfo();
    }

    @FXML
    public void initialize() {
        promotionService = new PromotionService();
        updateUserInfo();
        loadPromotions();
    }

    private void updateUserInfo() {
        if (userNameLabel != null && userPhoto != null && logoImage != null) {
            if (connectedUser != null) {
                userNameLabel.setText(connectedUser.getPrenom() + "." + connectedUser.getNom());
            } else {
                userNameLabel.setText("Utilisateur");
            }
            try {
                InputStream logoStream = getClass().getResourceAsStream("/assets/logo2.png");
                if (logoStream != null) {
                    logoImage.setImage(new Image(logoStream));
                } else {
                    System.err.println("Logo image not found: /assets/logo2.png");
                }
                InputStream photoStream = getClass().getResourceAsStream("/assets/userf.png");
                if (photoStream != null) {
                    userPhoto.setImage(new Image(photoStream));
                } else {
                    System.err.println("User photo not found: /assets/userf.png");
                }
            } catch (Exception e) {
                System.err.println("Error loading images: " + e.getMessage());
            }
        }
    }

    @FXML
    void loadPromotions() {
        List<Promotion> promotions = promotionService.getAll();
        promotionGrid.getChildren().clear();
        int column = 0;
        int row = 0;

        for (Promotion promotion : promotions) {
            Node card = createPromotionCard(promotion);
            promotionGrid.add(card, column, row);
            column++;
            if (column >= 4) {
                column = 0;
                row++;
            }
        }
    }

    private Node createPromotionCard(Promotion promotion) {
        VBox card = new VBox(10);
        card.getStyleClass().add("promotion-card");
        card.setPadding(new Insets(15));

        // Code Coupon
        Label codeLabel = new Label("🎟️ Code: " + promotion.getCodeCoupon());
        codeLabel.getStyleClass().add("promotion-code");

        // New Price
        Label priceLabel = new Label("💰 Prix Nouveau: " + String.format("%.2f DT", promotion.getPrixNouv()));
        priceLabel.getStyleClass().add("promotion-price");

        // Start Date
        Label startDateLabel = new Label("📅 Début: " + promotion.getStartDate().toString());
        startDateLabel.getStyleClass().add("promotion-date");

        // End Date
        Label endDateLabel = new Label("📅 Fin: " + promotion.getEndDate().toString());
        endDateLabel.getStyleClass().add("promotion-date");

        // Buttons
        Button editBtn = createIconButton("/assets/edit.png", "Modifier");
        editBtn.setOnAction(e -> openEditPage(promotion.getId()));

        Button deleteBtn = createIconButton("/assets/delete.png", "Supprimer");
        deleteBtn.setOnAction(e -> deletePromotion(promotion.getId()));

        HBox buttonBox = new HBox(10, editBtn, deleteBtn);
        buttonBox.setAlignment(Pos.CENTER);

        card.getChildren().addAll(codeLabel, priceLabel, startDateLabel, endDateLabel, buttonBox);
        return card;
    }

    private Button createIconButton(String iconPath, String tooltip) {
        Button button = new Button();
        button.getStyleClass().add("icon-button");
        try {
            InputStream iconStream = getClass().getResourceAsStream(iconPath);
            if (iconStream != null) {
                ImageView icon = new ImageView(new Image(iconStream));
                icon.setFitHeight(20);
                icon.setFitWidth(20);
                button.setGraphic(icon);
                button.setTooltip(new javafx.scene.control.Tooltip(tooltip));
            } else {
                button.setText(tooltip);
                System.err.println("Icon not found: " + iconPath);
            }
        } catch (Exception e) {
            System.err.println("Failed to load icon: " + iconPath + " - " + e.getMessage());
            button.setText(tooltip);
        }
        return button;
    }

    private void deletePromotion(int id) {
        try {
            promotionService.deletePromotion(id);
            System.out.println("✅ Promotion supprimée: ID " + id);
            loadPromotions();
        } catch (Exception e) {
            System.err.println("❌ Erreur suppression promotion: " + e.getMessage());
        }
    }

    private void openEditPage(int promotionId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Produit_Promotion/edit_promotion.fxml"));
            Parent root = loader.load();
            EditPromotionController controller = loader.getController();
            controller.setPromotionId(promotionId);
            Stage stage = new Stage();
            stage.setTitle("Modifier Promotion");
            stage.setScene(new Scene(root, 600, 500));
            stage.setOnHidden(e -> loadPromotions());
            stage.show();
        } catch (IOException e) {
            System.err.println("❌ Erreur chargement page édition: " + e.getMessage());
        }
    }

    @FXML
    private void openAddPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Produit_Promotion/add_promotion.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Ajouter Promotion");
            stage.setScene(new Scene(root, 600, 500));
            stage.setOnHidden(e -> loadPromotions());
            stage.show();
        } catch (IOException e) {
            System.err.println("❌ Erreur chargement page ajout promotion: " + e.getMessage());
        }
    }

    @FXML
    private void loadDashboardHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/DashboardHome.fxml"));
            Parent view = loader.load();
            // Pass connectedUser to the new controller if needed
            mainContent.getChildren().setAll(view);
        } catch (IOException e) {
            System.err.println("Error loading dashboard: " + e.getMessage());
        }
    }

    @FXML
    private void loadProductCatalog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Produit_Promotion/product_catalog.fxml"));
            Parent view = loader.load();
            // Pass connectedUser to ProductCatalog controller
            ProductCatalog controller = loader.getController();
            mainContent.getChildren().setAll(view);
        } catch (IOException e) {
            System.err.println("Error loading product catalog: " + e.getMessage());
        }
    }

    @FXML
    private void loadTableUtilisateurs() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/TableUtilisateurs.fxml"));
            Parent view = loader.load();
            // Pass connectedUser to TableUtilisateursController if needed
            mainContent.getChildren().setAll(view);
        } catch (IOException e) {
            System.err.println("Error loading utilisateurs: " + e.getMessage());
        }
    }
}