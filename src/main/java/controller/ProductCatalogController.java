package controller;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.Commande;
import model.LigneDeCommande;
import model.Produit;
import model.Promotion;
import model.User;
import service.CommandeService;
import service.LigneDeCommandeService;
import service.ProduitService;
import service.PromotionService;
import utils.SessionManager;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

public class ProductCatalogController {

    @FXML
    private GridPane productGrid;

    private ProduitService produitService;
    private PromotionService promotionService;
    private CommandeService commandeService;
    private LigneDeCommandeService ligneDeCommandeService;
    private User connectedUser;

    private Set<Integer> likedProducts;
    private Set<Integer> dislikedProducts;

    public void setConnectedUser(User user) {
        this.connectedUser = user;
    }

    @FXML
    public void initialize() {
        produitService = new ProduitService();
        promotionService = new PromotionService();
        commandeService = new CommandeService();
        ligneDeCommandeService = new LigneDeCommandeService();
        likedProducts = new HashSet<>();
        dislikedProducts = new HashSet<>();
        connectedUser = SessionManager.getInstance().getCurrentUser();
        System.out.println("[Produit] Utilisateur connecté: " + (connectedUser != null ? connectedUser.getEmail() : "null"));
        loadProducts();
    }

    private void loadProducts() {
        List<Produit> produits = produitService.getProduitsPage(1, 100);
        productGrid.getChildren().clear();

        int column = 0;
        int row = 0;

        for (Produit produit : produits) {
            Node card = createProductCard(produit);
            productGrid.add(card, column, row);

            column++;
            if (column >= 4) {
                column = 0;
                row++;
            }
        }
    }

    private Node createProductCard(Produit produit) {
        VBox card = new VBox(12);
        card.setPrefSize(300, 400);
        card.setPadding(new Insets(20));
        card.getStyleClass().add("product-card");
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color:  #fffdfb; -fx-border-radius: 10; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        ImageView imageView = loadImageView(produit.getImage());
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(250);
        imageView.setFitHeight(180);
        imageView.setSmooth(true);
        imageView.setCache(true);

        Label nameLabel = new Label("\uD83D\uDCE6 " + produit.getNomProduit());
        nameLabel.getStyleClass().add("product-name");
        nameLabel.setAlignment(Pos.CENTER);
        nameLabel.setMaxWidth(Double.MAX_VALUE);

        Label categoryLabel = new Label("\uD83D\uDCC2 Catégorie: " + produit.getCategorie());
        categoryLabel.getStyleClass().add("product-category");
        categoryLabel.setAlignment(Pos.CENTER);
        categoryLabel.setMaxWidth(Double.MAX_VALUE);

        Label priceLabel = createPriceLabel(produit);
        priceLabel.setAlignment(Pos.CENTER);
        priceLabel.setMaxWidth(Double.MAX_VALUE);

        Label stockLabel = new Label("\uD83D\uDCE6 Stock: " + produit.getStock());
        stockLabel.getStyleClass().add("product-stock");
        stockLabel.setAlignment(Pos.CENTER);
        stockLabel.setMaxWidth(Double.MAX_VALUE);

        Button addToCartButton = new Button("Add to Cart \uD83D\uDED2");
        addToCartButton.getStyleClass().add("add-to-cart-button");
        addToCartButton.setOnAction(e -> addToCart(produit));
        addToCartButton.setMaxWidth(Double.MAX_VALUE);

        Button likeButton = new Button("\uD83D\uDC4D");
        Button dislikeButton = new Button("\uD83D\uDC4E");
        likeButton.getStyleClass().add("like-dislike-button");
        dislikeButton.getStyleClass().add("like-dislike-button");

        Label countLabel = new Label(getLikeDislikeCountText(produit.getId()));
        countLabel.getStyleClass().add("like-dislike-count");
        countLabel.setAlignment(Pos.CENTER);

        updateButtonStyles(produit.getId(), likeButton, dislikeButton);

        likeButton.setOnAction(e -> handleLikeAction(produit.getId(), likeButton, dislikeButton, countLabel));
        dislikeButton.setOnAction(e -> handleDislikeAction(produit.getId(), likeButton, dislikeButton, countLabel));

        HBox likeDislikeContainer = new HBox(12, likeButton, countLabel, dislikeButton);
        likeDislikeContainer.setAlignment(Pos.CENTER);
        likeDislikeContainer.setMaxWidth(Double.MAX_VALUE);

        card.getChildren().addAll(imageView, nameLabel, categoryLabel, priceLabel, stockLabel, likeDislikeContainer, addToCartButton);

        return card;
    }

    private ImageView loadImageView(String imageName) {
        String path = "assets/prod_mat/" + imageName;
        InputStream imageStream = getClass().getClassLoader().getResourceAsStream(path);

        if (imageStream != null) {
            return new ImageView(new Image(imageStream, 260, 180, true, true));
        } else {
            System.err.println("❌ Image non trouvée : " + path);
            InputStream defaultImage = getClass().getClassLoader().getResourceAsStream("assets/prod_mat/prod1.jpeg");
            return new ImageView(new Image(defaultImage, 260, 180, true, true));
        }
    }

    private Label createPriceLabel(Produit produit) {
        Label priceLabel = new Label();

        if (produit.getIdPromotion() != null) {
            Promotion promo = promotionService.getPromotionById(produit.getIdPromotion());

            if (promo != null) {
                StringBuilder promoText = new StringBuilder();
                promoText.append(String.format("\uD83D\uDCB0 Prix original: ~~%.2f DT~~\n", produit.getPrix()));
                promoText.append(String.format("\uD83C\uDFF7️ Prix promo: %.2f DT\n", promo.getPrixNouv()));
                promoText.append("\uD83C\uDF9F️ Code: ").append(promo.getCodeCoupon()).append("\n");

                LocalDate endDate = promo.getEndDate();
                if (endDate != null) {
                    promoText.append("\uD83D\uDCC5 Fin de promo: ")
                            .append(new SimpleDateFormat("yyyy-MM-dd").format(Date.from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant())));
                } else {
                    promoText.append("\uD83D\uDCC5 Fin de promo: N/A");
                }

                priceLabel.setText(promoText.toString());
                priceLabel.getStyleClass().add("product-price-promo");
            } else {
                priceLabel.setText(String.format("\uD83D\uDCB0 Prix: %.2f DT", produit.getPrix()));
                priceLabel.getStyleClass().add("product-price");
            }
        } else {
            priceLabel.setText(String.format("\uD83D\uDCB0 Prix: %.2f DT", produit.getPrix()));
            priceLabel.getStyleClass().add("product-price");
        }

        return priceLabel;
    }

    private void handleLikeAction(int productId, Button likeButton, Button dislikeButton, Label countLabel) {
        if (likedProducts.contains(productId)) {
            likedProducts.remove(productId);
        } else {
            likedProducts.add(productId);
        }
        countLabel.setText(getLikeDislikeCountText(productId));
        updateButtonStyles(productId, likeButton, dislikeButton);
    }

    private void handleDislikeAction(int productId, Button likeButton, Button dislikeButton, Label countLabel) {
        if (dislikedProducts.contains(productId)) {
            dislikedProducts.remove(productId);
        } else {
            dislikedProducts.add(productId);
        }
        countLabel.setText(getLikeDislikeCountText(productId));
        updateButtonStyles(productId, likeButton, dislikeButton);
    }

    private void updateButtonStyles(int productId, Button likeButton, Button dislikeButton) {
        boolean isLiked = likedProducts.contains(productId);
        boolean isDisliked = dislikedProducts.contains(productId);

        likeButton.setStyle(isLiked ? "-fx-background-color: #4CAF50; -fx-text-fill: white;" : "-fx-background-color: transparent; -fx-text-fill: #666;");
        dislikeButton.setStyle(isDisliked ? "-fx-background-color: #F44336; -fx-text-fill: white;" : "-fx-background-color: transparent; -fx-text-fill: #666;");
    }

    private String getLikeDislikeCountText(int productId) {
        int likeCount = likedProducts.contains(productId) ? 1 : 0;
        int dislikeCount = dislikedProducts.contains(productId) ? 1 : 0;
        return String.format("Likes: %d | Dislikes: %d", likeCount, dislikeCount);
    }

    private void addToCart(Produit produit) {
        try {
            if (connectedUser == null) {
                showAlert("Erreur", "Utilisateur non connecté.");
                return;
            }

            List<Commande> userCommandes = commandeService.getCommandesByUserId(connectedUser.getId());
            Commande cart = userCommandes.stream().filter(c -> "Pending".equals(c.getStatut())).findFirst().orElse(null);

            if (cart == null) {
                cart = new Commande();
                cart.setIdUser(connectedUser.getId());
                cart.setDateCommande(new Date());
                cart.setMontantTotal(0.0);
                cart.setStatut("Pending");
                cart.setAdresseLivraison("");
                cart.setPaiement("");
                commandeService.addCommande(cart);
            }

            List<LigneDeCommande> items = ligneDeCommandeService.getLignesDeCommandeByCommandeId(cart.getId());

            LigneDeCommande existingItem = items.stream()
                    .filter(item -> item.getIdProduit() == produit.getId())
                    .findFirst().orElse(null);

            double currentTotal = items.stream()
                    .mapToDouble(item -> item.getPrixUnitaire() * item.getQuantite())
                    .sum();

            cart.setMontantTotal(currentTotal);

            if (existingItem != null) {
                existingItem.setQuantite(existingItem.getQuantite() + 1);
                ligneDeCommandeService.updateLigneDeCommande(existingItem);
                cart.setMontantTotal(cart.getMontantTotal() + produit.getPrix());
            } else {
                LigneDeCommande ligne = new LigneDeCommande();
                ligne.setIdCommande(cart.getId());
                ligne.setQuantite(1);
                ligne.setPrixUnitaire(produit.getPrix());
                ligne.setIdProduit(produit.getId());
                ligne.setIdMateriau(0);
                ligneDeCommandeService.addLigneDeCommande(ligne);
                cart.setMontantTotal(cart.getMontantTotal() + produit.getPrix());
            }

            commandeService.updateCommande(cart);
            showAlert("Succès", "Produit ajouté au panier : " + produit.getNomProduit());
        } catch (Exception e) {
            System.err.println("\u274C Erreur lors de l'ajout au panier: " + e.getMessage());
            showAlert("Erreur", "Impossible d'ajouter le produit au panier: " + e.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}