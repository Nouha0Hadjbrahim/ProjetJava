package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Promotion;
import service.PromotionService;

import java.io.IOException;
import java.util.List;

public class PromotionListController {

    @FXML
    private ListView<Promotion> promotionListView;

    private PromotionService promotionService;

    public void initialize() {
        promotionService = new PromotionService();
        loadPromotions();
    }

    private void loadPromotions() {
        List<Promotion> promotions = promotionService.getAll();
        promotionListView.getItems().setAll(promotions);

        // Customize the ListView cells
        promotionListView.setCellFactory(listView -> new ListCell<Promotion>() {
            @Override
            protected void updateItem(Promotion promotion, boolean empty) {
                super.updateItem(promotion, empty);
                if (empty || promotion == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // Create a custom layout for each promotion item
                    VBox vbox = new VBox(5);
                    vbox.getStyleClass().add("promotion-item");

                    Label codeLabel = new Label("🎟️ Code: " + promotion.getCodeCoupon());
                    codeLabel.getStyleClass().add("promotion-code");

                    Label priceLabel = new Label("💰 Prix Nouveau: " + String.format("%.2f DT", promotion.getPrixNouv()));
                    priceLabel.getStyleClass().add("promotion-price");

                    Label startDateLabel = new Label("📅 Début: " + promotion.getStartDate().toString());
                    startDateLabel.getStyleClass().add("promotion-date");

                    Label endDateLabel = new Label("📅 Fin: " + promotion.getEndDate().toString());
                    endDateLabel.getStyleClass().add("promotion-date");

                    // Buttons for Edit and Delete
                    Button editBtn = new Button("✏️ Modifier");
                    editBtn.getStyleClass().add("edit-button");
                    editBtn.setOnAction(e -> openEditPage(promotion.getId()));

                    Button deleteBtn = new Button("🗑️ Supprimer");
                    deleteBtn.getStyleClass().add("delete-button");
                    deleteBtn.setOnAction(e -> deletePromotion(promotion.getId()));

                    HBox buttonBox = new HBox(10, editBtn, deleteBtn);
                    buttonBox.setAlignment(Pos.CENTER);

                    vbox.getChildren().addAll(codeLabel, priceLabel, startDateLabel, endDateLabel, buttonBox);
                    setGraphic(vbox);
                }
            }
        });
    }

    private void deletePromotion(int id) {
        try {
            promotionService.deletePromotion(id);
            System.out.println("✅ Promotion supprimée: ID " + id);
            // Refresh the list
            loadPromotions();
        } catch (Exception e) {
            System.out.println("❌ Erreur suppression promotion: " + e.getMessage());
        }
    }

    private void openEditPage(int promotionId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Produit_Promotion/edit_promotion.fxml"));
            Parent root = loader.load();

            // Pass promotion ID to edit controller
            EditPromotionController controller = loader.getController();
            controller.setPromotionId(promotionId);

            Stage stage = new Stage();
            stage.setTitle("Modifier Promotion");
            stage.setScene(new Scene(root, 600, 500));
            stage.setOnHidden(e -> loadPromotions()); // Refresh list after edit
            stage.show();
        } catch (IOException e) {
            System.out.println("❌ Erreur chargement page édition: " + e.getMessage());
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
            stage.setOnHidden(e -> loadPromotions()); // Refresh list after adding
            stage.show();
        } catch (IOException e) {
            System.out.println("❌ Erreur chargement page ajout promotion: " + e.getMessage());
        }
    }
}