package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Commande;
import model.LigneDeCommande;
import model.Produit;
import model.Promotion;
import service.CommandeService;
import service.LigneDeCommandeService;
import service.ProduitService;
import service.PromotionService;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

public class ProductCatalogController {

    @FXML
    private GridPane productGrid;
    @FXML private ImageView logoImage;

    @FXML
    private Button viewCartButton;

    @FXML private ImageView userIcon;

    @FXML
    private Button viewOrderHistoryButton;

    private ProduitService produitService;
    private PromotionService promotionService;
    private CommandeService commandeService;
    private LigneDeCommandeService ligneDeCommandeService;

    // Assume a logged-in user (replace with actual user authentication)
    private final int currentUserId = 1; // For demo purposes

    public void initialize() {
        logoImage.setImage(new Image(getClass().getResourceAsStream("/assets/logo2.png")));
        produitService = new ProduitService();
        promotionService = new PromotionService();
        commandeService = new CommandeService();
        ligneDeCommandeService = new LigneDeCommandeService();
        loadProducts();
    }

    private void loadProducts() {
        List<Produit> produits = produitService.getProduitsPage(1, 100); // Load first 100 products
        productGrid.getChildren().clear();
        int column = 0;
        int row = 0;

        for (Produit produit : produits) {
            Node card = createProductCard(produit);
            productGrid.add(card, column, row);

            column++;
            if (column >= 4) { // 4 cards per row
                column = 0;
                row++;
            }
        }
    }

    private Node createProductCard(Produit produit) {
        VBox card = new VBox(10);
        card.getStyleClass().add("product-card");
        card.setPadding(new Insets(15));

        // Image
        ImageView imageView;
        try {
            System.out.println("📸 Loading image: " + produit.getImage());
            String imagePath = produit.getImage();
            InputStream imageStream = getClass().getClassLoader().getResourceAsStream(imagePath);
            if (imageStream == null) {
                throw new IOException("Resource not found: " + imagePath);
            }
            Image image = new Image(imageStream, 200, 150, true, true);
            imageView = new ImageView(image);
            imageStream.close();
            System.out.println("✅ Image loaded successfully: " + imagePath);
        } catch (Exception e) {
            System.out.println("❌ Failed to load image: " + produit.getImage() + " - Error: " + e.getMessage());
            imageView = new ImageView(new Image("file:resources/images/placeholder.png"));
        }
        imageView.getStyleClass().add("product-image");

        // Name
        Label nameLabel = new Label("📦 " + produit.getNomProduit());
        nameLabel.getStyleClass().add("product-name");

        // Category
        Label categoryLabel = new Label("📂 Catégorie: " + produit.getCategorie());
        categoryLabel.getStyleClass().add("product-category");

        // Price + Promotion
        Label priceLabel = new Label();
        if (produit.getIdPromotion() != null) {
            Promotion promo = promotionService.getPromotionById(produit.getIdPromotion());
            if (promo != null) {
                String promoText = String.format("💰 Prix: ~~%.2f DT~~ Promo: %.2f DT (%s)",
                        produit.getPrix(), promo.getPrixNouv(), promo.getCodeCoupon());
                // Handle the end date
                LocalDate endDate = promo.getEndDate();
                if (endDate != null) {
                    try {
                        // Convert LocalDate to Date for formatting
                        Date endDateAsDate = Date.from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        String endDateStr = dateFormat.format(endDateAsDate);
                        promoText += String.format("\nPromotion ends on: %s", endDateStr);
                    } catch (Exception e) {
                        System.out.println("❌ Failed to format promotion end date: " + e.getMessage());
                        promoText += "\nPromotion end date: N/A";
                    }
                } else {
                    promoText += "\nPromotion end date: N/A";
                }
                priceLabel.setText(promoText);
                priceLabel.getStyleClass().add("product-price-promo");
            } else {
                priceLabel.setText(String.format("💰 Prix: %.2f DT", produit.getPrix()));
                priceLabel.getStyleClass().add("product-price");
            }
        } else {
            priceLabel.setText(String.format("💰 Prix: %.2f DT", produit.getPrix()));
            priceLabel.getStyleClass().add("product-price");
        }

        // Stock
        Label stockLabel = new Label("📦 Stock: " + produit.getStock());
        stockLabel.getStyleClass().add("product-stock");

        // Add to Cart Button
        Button addToCartButton = new Button("Add to Cart 🛒");
        addToCartButton.getStyleClass().add("add-to-cart-button");
        addToCartButton.setOnAction(e -> addToCart(produit));

        card.getChildren().addAll(imageView, nameLabel, categoryLabel, priceLabel, stockLabel, addToCartButton);
        return card;
    }

    private void addToCart(Produit produit) {
        try {
            // Check if the user has an active "cart" (a Commande with statut "Pending")
            List<Commande> userCommandes = commandeService.getCommandesByUserId(currentUserId);
            Commande cart = null;
            for (Commande commande : userCommandes) {
                if ("Pending".equals(commande.getStatut())) {
                    cart = commande;
                    break;
                }
            }

            // If no cart exists, create a new one
            if (cart == null) {
                cart = new Commande();
                cart.setIdUser(currentUserId);
                cart.setDateCommande(new Date());
                cart.setMontantTotal(0.0); // Will be updated after adding items
                cart.setStatut("Pending");
                cart.setAdresseLivraison(""); // Will be set during checkout
                cart.setPaiement(""); // Will be set during checkout
                commandeService.addCommande(cart);
            }

            // Load all existing lignes de commande for the cart
            List<LigneDeCommande> items = ligneDeCommandeService.getLignesDeCommandeByCommandeId(cart.getId());

            // Check if the product is already in the cart
            LigneDeCommande existingItem = null;
            for (LigneDeCommande item : items) {
                if (item.getIdProduit() == produit.getId()) {
                    existingItem = item;
                    break;
                }
            }

            // Calculate the current total
            double currentTotal = 0.0;
            for (LigneDeCommande item : items) {
                currentTotal += item.getPrixUnitaire() * item.getQuantite();
            }
            cart.setMontantTotal(currentTotal);

            if (existingItem != null) {
                // Product already in cart, increase quantity
                int newQuantity = existingItem.getQuantite() + 1;
                existingItem.setQuantite(newQuantity);
                ligneDeCommandeService.updateLigneDeCommande(existingItem);

                // Update cart total
                cart.setMontantTotal(cart.getMontantTotal() + produit.getPrix());
            } else {
                // Product not in cart, add a new LigneDeCommande
                LigneDeCommande ligne = new LigneDeCommande();
                ligne.setIdCommande(cart.getId());
                ligne.setQuantite(1); // Default quantity of 1
                ligne.setPrixUnitaire(produit.getPrix());
                ligne.setIdProduit(produit.getId());
                ligneDeCommandeService.addLigneDeCommande(ligne);

                // Update the cart's total amount
                cart.setMontantTotal(cart.getMontantTotal() + produit.getPrix());
            }

            // Update the cart in the database
            commandeService.updateCommande(cart);

            System.out.println("✅ Added to cart: " + produit.getNomProduit());
        } catch (Exception e) {
            System.out.println("❌ Erreur lors de l'ajout au panier: " + e.getMessage());
        }
    }

    @FXML
    private void viewCart() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/commande_ligne/cart.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Votre Panier");
            stage.setScene(new Scene(root, 600, 400));
            stage.show();
        } catch (IOException e) {
            System.out.println("❌ Erreur chargement page panier: " + e.getMessage());
        }
    }

    @FXML
    private void viewOrderHistory() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/commande_ligne/orderHistory.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Historique des Commandes");
            stage.setScene(new Scene(root, 600, 400));
            stage.show();
        } catch (IOException e) {
            System.out.println("❌ Erreur chargement page historique: " + e.getMessage());
        }
    }

    public void handleNavLinkClick(ActionEvent actionEvent) {
    }
}