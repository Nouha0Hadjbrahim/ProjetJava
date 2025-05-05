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
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Produit;
import model.Promotion;
import service.ProduitService;
import service.PromotionService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ProductCatalog {

    @FXML private GridPane productGrid;
    @FXML private Button addButton;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> categorieFilter;
    @FXML private TextField minPrixFilter;
    @FXML private TextField maxPrixFilter;
    @FXML private TextField minStockFilter;
    @FXML private TextField maxStockFilter;
    @FXML private ComboBox<String> sortFilter;

    private ProduitService produitService;
    private PromotionService promotionService;
    private ObservableList<Produit> produitsList;
    private ObservableList<String> categories;
    private ObservableList<String> sortOptions;

    @FXML
    public void initialize() {
        produitService = new ProduitService();
        promotionService = new PromotionService();
        produitsList = FXCollections.observableArrayList();
        categories = FXCollections.observableArrayList();
        sortOptions = FXCollections.observableArrayList("A-Z", "Z-A");

        // Initialize filters
        loadFilters();
        categorieFilter.setItems(categories);
        sortFilter.setItems(sortOptions);
        categories.add(0, "Toutes");
        sortFilter.setValue("A-Z");

        loadProducts();
    }

    private void loadFilters() {
        categories.addAll(produitService.getProduitsPage(1, Integer.MAX_VALUE)
                .stream()
                .map(Produit::getCategorie)
                .distinct()
                .collect(Collectors.toList()));
    }

    void loadProducts() {
        try {
            List<Produit> produits = produitService.getProduitsPage(1, 100);
            produitsList.setAll(produits);
            loadProducts(productGrid, produits);
        } catch (Exception e) {
            System.err.println("Error loading products: " + e.getMessage());
        }
    }

    private void loadProducts(GridPane grid, List<Produit> produits) {
        grid.getChildren().clear();
        int column = 0;
        int row = 0;
        int maxColumns = 4;

        for (Produit produit : produits) {
            Node card = createProduitCard(produit);
            grid.add(card, column, row);
            column++;
            if (column >= maxColumns) {
                column = 0;
                row++;
            }
        }
    }

    @FXML
    private void filterProducts() {
        String searchText = searchField.getText() != null ? searchField.getText().trim().toLowerCase() : "";
        String selectedCategorie = categorieFilter.getValue() != null ? categorieFilter.getValue() : "Toutes";
        String selectedSort = sortFilter.getValue() != null ? sortFilter.getValue() : "A-Z";

        // Parse price filters
        double minPrix = parseDouble(minPrixFilter.getText(), 0.0);
        double maxPrix = parseDouble(maxPrixFilter.getText(), Double.MAX_VALUE);

        // Parse stock filters
        int minStock = parseInt(minStockFilter.getText(), 0);
        int maxStock = parseInt(maxStockFilter.getText(), Integer.MAX_VALUE);

        List<Produit> filteredList = produitService.getProduitsPage(1, Integer.MAX_VALUE)
                .stream()
                .filter(produit -> produit.getNomProduit().toLowerCase().contains(searchText))
                .filter(produit -> selectedCategorie.equals("Toutes") || produit.getCategorie().equals(selectedCategorie))
                .filter(produit -> produit.getPrix() >= minPrix && produit.getPrix() <= maxPrix)
                .filter(produit -> produit.getStock() >= minStock && produit.getStock() <= maxStock)
                .sorted(getComparator(selectedSort))
                .collect(Collectors.toList());

        produitsList.setAll(filteredList);
        loadProducts(productGrid, filteredList);
    }

    private double parseDouble(String text, double defaultValue) {
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private int parseInt(String text, int defaultValue) {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private Comparator<Produit> getComparator(String sortOption) {
        if ("Z-A".equals(sortOption)) {
            return (p1, p2) -> p2.getNomProduit().compareToIgnoreCase(p1.getNomProduit());
        }
        return (p1, p2) -> p1.getNomProduit().compareToIgnoreCase(p2.getNomProduit());
    }

    private Node createProduitCard(Produit produit) {
        VBox card = new VBox(10);
        card.getStyleClass().add("product-card");
        card.setPadding(new Insets(15));

        // Image
        ImageView imageView;
        try {
            InputStream imageStream = getClass().getClassLoader().getResourceAsStream(produit.getImage());
            if (imageStream == null) throw new IOException("Image not found: " + produit.getImage());
            Image image = new Image(imageStream, 200, 150, true, true);
            imageView = new ImageView(image);
            imageStream.close();
        } catch (Exception e) {
            System.err.println("Image load failed: " + e.getMessage());
            InputStream fallback = getClass().getClassLoader().getResourceAsStream("images/placeholder.png");
            imageView = (fallback != null) ? new ImageView(new Image(fallback, 200, 150, true, true)) : new ImageView();
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

        // Buttons
        Button editBtn = createIconButton("/assets/edit.png", "Modifier");
        editBtn.setOnAction(e -> openEditPage(produit.getId()));

        Button deleteBtn = createIconButton("/assets/delete.png", "Supprimer");
        deleteBtn.setOnAction(e -> deleteProduit(produit.getId()));

        HBox buttonBox = new HBox(10, editBtn, deleteBtn);
        buttonBox.setAlignment(Pos.CENTER);

        card.getChildren().addAll(imageView, nameLabel, categoryLabel, priceLabel, stockLabel, buttonBox);
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
            } else {
                button.setText(tooltip); // fallback text
            }
        } catch (Exception e) {
            System.err.println("Failed to load icon: " + iconPath + " - " + e.getMessage());
            button.setText(tooltip);
        }
        return button;
    }

    private void deleteProduit(int id) {
        try {
            produitService.deleteProduit(id);
            loadProducts();
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
            AddProduitController controller = loader.getController();
            controller.setProductCatalogController(this);
            Stage stage = new Stage();
            stage.setTitle("Ajouter Produit");
            stage.setScene(new Scene(root, 600, 400));
            stage.setOnHidden(e -> loadProducts());
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur chargement page ajout: " + e.getMessage());
        }
    }


}