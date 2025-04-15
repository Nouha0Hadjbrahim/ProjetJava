package controller;

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
import model.Produit;
import model.Promotion;
import model.User;
import service.ProduitService;
import service.PromotionService;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class ProductCatalog {

    @FXML private GridPane productGrid;
    @FXML private Button addButton;
    @FXML private Label userNameLabel;
    @FXML private ImageView userPhoto;
    @FXML private ImageView logoImage;
    @FXML private Button btnProductTable;
    @FXML private Button btnTableUtilisateurs;
    @FXML private Button btnDashboard;
    @FXML private StackPane mainContent;

    private ProduitService produitService;
    private PromotionService promotionService;
    private User connectedUser;

    public void setConnectedUser(User user) {
        this.connectedUser = user;
        updateUserInfo();
    }

    @FXML
    public void initialize() {
        // Initialize services
        produitService = new ProduitService();
        promotionService = new PromotionService();

        // Set button actions
        btnDashboard.setOnAction(e -> loadDashboardHome());
        btnProductTable.setOnAction(e -> loadProductCatalog());
        btnTableUtilisateurs.setOnAction(e -> loadTableUtilisateurs());

        // Load user info
        updateUserInfo();

        // Load product catalog by default
        loadProductCatalog();
    }

    private void updateUserInfo() {
        if (userNameLabel != null && userPhoto != null && logoImage != null) {
            if (connectedUser != null) {
                userNameLabel.setText(connectedUser.getPrenom() + "." + connectedUser.getNom());
            } else {
                userNameLabel.setText("Utilisateur");
            }
            try {
                logoImage.setImage(new Image(getClass().getResourceAsStream("/assets/logo2.png")));
                userPhoto.setImage(new Image(getClass().getResourceAsStream("/assets/userf.png")));
            } catch (Exception e) {
                System.err.println("Error loading images: " + e.getMessage());
            }
        }
    }

    private void loadProductCatalog() {
        try {
            System.out.println("Loading product catalog...");
            // Create the product catalog content
            VBox catalogContent = new VBox(20);
            catalogContent.setAlignment(Pos.CENTER);
            catalogContent.getStyleClass().add("anchor-pane");

            Button addButton = new Button("➕ Ajouter Produit");
            addButton.getStyleClass().add("add-button");
            addButton.setOnAction(e -> openAddPage());

            GridPane grid = new GridPane();
            grid.setHgap(20);
            grid.setVgap(20);
            grid.setAlignment(Pos.CENTER);
            grid.getStyleClass().add("grid-pane");
            grid.setPadding(new Insets(15));

            // Load products into grid
            List<Produit> produits = produitService.getProduitsPage(1, 100);
            loadProducts(grid, produits);

            catalogContent.getChildren().addAll(addButton, grid);
            mainContent.getChildren().setAll(catalogContent);
            System.out.println("Product catalog loaded successfully.");
        } catch (Exception e) {
            System.err.println("Error loading product catalog: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadProducts(GridPane grid, List<Produit> produits) {
        grid.getChildren().clear();
        int column = 0;
        int row = 0;

        for (Produit produit : produits) {
            Node card = createProduitCard(produit);
            grid.add(card, column, row);
            column++;
            if (column >= 4) {
                column = 0;
                row++;
            }
        }
    }

    private Node createProduitCard(Produit produit) {
        VBox card = new VBox(10);
        card.getStyleClass().add("product-card");
        card.setPadding(new Insets(15));

        ImageView imageView;
        try {
            String imagePath = produit.getImage();
            InputStream imageStream = getClass().getClassLoader().getResourceAsStream(imagePath);
            if (imageStream == null) {
                throw new IOException("Resource not found: " + imagePath);
            }
            Image image = new Image(imageStream, 200, 150, true, true);
            imageView = new ImageView(image);
            imageStream.close();
        } catch (Exception e) {
            System.err.println("Failed to load image: " + produit.getImage() + " - Error: " + e.getMessage());
            imageView = new ImageView(new Image("file:resources/images/placeholder.png"));
        }
        imageView.getStyleClass().add("product-image");

        Label nameLabel = new Label(produit.getNomProduit());
        nameLabel.getStyleClass().add("product-name");

        Label categoryLabel = new Label("📂 Catégorie: " + produit.getCategorie());
        categoryLabel.getStyleClass().add("product-category");

        Label priceLabel = new Label();
        if (produit.getIdPromotion() != null) {
            Promotion promo = promotionService.getPromotionById(produit.getIdPromotion());
            if (promo != null) {
                priceLabel.setText(String.format("💰 Prix: ~~%.2f DT~~ Promo: %.2f DT (%s)",
                        produit.getPrix(), promo.getPrixNouv(), promo.getCodeCoupon()));
                priceLabel.getStyleClass().add("product-price-promo");
            } else {
                priceLabel.setText(String.format("💰 Prix: %.2f DT", produit.getPrix()));
                priceLabel.getStyleClass().add("product-price");
            }
        } else {
            priceLabel.setText(String.format("💰 Prix: %.2f DT", produit.getPrix()));
            priceLabel.getStyleClass().add("product-price");
        }

        Label stockLabel = new Label("📦 Stock: " + produit.getStock());
        stockLabel.getStyleClass().add("product-stock");

        Button editBtn = new Button("✏️ Modifier");
        editBtn.getStyleClass().add("edit-button");
        editBtn.setOnAction(e -> openEditPage(produit.getId()));

        Button deleteBtn = new Button("🗑️ Supprimer");
        deleteBtn.getStyleClass().add("delete-button");
        deleteBtn.setOnAction(e -> deleteProduit(produit.getId()));

        HBox buttonBox = new HBox(10, editBtn, deleteBtn);
        buttonBox.setAlignment(Pos.CENTER);

        card.getChildren().addAll(imageView, nameLabel, categoryLabel, priceLabel, stockLabel, buttonBox);
        return card;
    }

    private void deleteProduit(int id) {
        try {
            produitService.deleteProduit(id);
            System.out.println("Produit supprimé: ID " + id);
            loadProductCatalog(); // Refresh
        } catch (Exception e) {
            System.err.println("Erreur suppression produit: " + e.getMessage());
        }
    }

    private void openEditPage(int produitId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Produit_Promotion/edit_produit.fxml"));
            Parent root = loader.load();
            EditProduitController controller = loader.getController();
            controller.setProduitId(produitId);
            Stage stage = new Stage();
            stage.setTitle("Modifier Produit");
            stage.setScene(new Scene(root, 600, 400));
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur chargement page édition: " + e.getMessage());
        }
    }

    @FXML
    private void openAddPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Produit_Promotion/add_produit.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Ajouter Produit");
            stage.setScene(new Scene(root, 600, 400));
            stage.setOnHidden(e -> loadProductCatalog()); // Refresh after add
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur chargement page ajout: " + e.getMessage());
        }
    }

    private void loadTableUtilisateurs() {
        try {
            System.out.println("Loading table utilisateurs...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/TableUtilisateurs.fxml"));
            Parent view = loader.load();
            mainContent.getChildren().setAll(view);
        } catch (IOException e) {
            System.err.println("Error loading table utilisateurs: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadDashboardHome() {
        try {
            System.out.println("Loading dashboard home...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/DashboardHome.fxml"));
            Parent view = loader.load();
            mainContent.getChildren().setAll(view);
        } catch (IOException e) {
            System.err.println("Error loading dashboard home: " + e.getMessage());
            e.printStackTrace();
        }
    }
}