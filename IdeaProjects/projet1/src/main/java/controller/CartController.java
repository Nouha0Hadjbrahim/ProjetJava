package controller;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.Commande;
import model.LigneDeCommande;
import model.Produit;
import service.CommandeService;
import service.LigneDeCommandeService;
import service.ProduitService;

import java.io.InputStream;
import java.util.List;

public class CartController {

    @FXML
    private VBox cartItemsVBox;

    @FXML
    private Label totalLabel;

    @FXML
    private TextField addressField;

    @FXML
    private ChoiceBox<String> paymentChoice;

    @FXML
    private Button checkoutButton;

    private CommandeService commandeService;
    private LigneDeCommandeService ligneDeCommandeService;
    private ProduitService produitService;

    // Assume a logged-in user (replace with actual user authentication)
    private final int currentUserId = 1; // For demo purposes

    private Commande cart;

    public void initialize() {
        commandeService = new CommandeService();
        ligneDeCommandeService = new LigneDeCommandeService();
        produitService = new ProduitService();

        loadCart();
    }

    private void loadCart() {
        try {
            // Find the user's "Pending" cart
            List<Commande> userCommandes = commandeService.getCommandesByUserId(currentUserId);
            cart = null;
            for (Commande commande : userCommandes) {
                if ("Pending".equals(commande.getStatut())) {
                    cart = commande;
                    break;
                }
            }

            if (cart == null) {
                // If no cart exists, show an empty message
                cartItemsVBox.getChildren().add(new Label("Votre panier est vide."));
                totalLabel.setText("Total: 0.0 DT");
                checkoutButton.setDisable(true);
                return;
            }

            // Load cart items
            cartItemsVBox.getChildren().clear();
            List<LigneDeCommande> items = ligneDeCommandeService.getLignesDeCommandeByCommandeId(cart.getId());
            for (LigneDeCommande item : items) {
                cartItemsVBox.getChildren().add(createCartItemNode(item));
            }

            // Update total
            totalLabel.setText(String.format("Total: %.2f DT", cart.getMontantTotal()));
            checkoutButton.setDisable(items.isEmpty());
        } catch (Exception e) {
            System.out.println("❌ Erreur lors du chargement du panier: " + e.getMessage());
        }
    }

    private Node createCartItemNode(LigneDeCommande item) {
        HBox itemBox = new HBox(10);
        itemBox.getStyleClass().add("cart-item");

        // Get product details
        Produit produit = produitService.getProduitById(item.getIdProduit());
        if (produit == null) {
            System.out.println("❌ Produit non trouvé pour ID: " + item.getIdProduit());
            return new Label("Produit non trouvé (ID: " + item.getIdProduit() + ")");
        }

        // Product Image
        ImageView imageView;
        try {
            String imagePath = produit.getImage();
            InputStream imageStream = getClass().getClassLoader().getResourceAsStream(imagePath);
            if (imageStream == null) {
                throw new Exception("Resource not found: " + imagePath);
            }
            Image image = new Image(imageStream, 50, 50, true, true);
            imageView = new ImageView(image);
            imageStream.close();
        } catch (Exception e) {
            System.out.println("❌ Failed to load image: " + produit.getImage() + " - Error: " + e.getMessage());
            imageView = new ImageView(new Image("file:resources/images/placeholder.png"));
        }
        imageView.getStyleClass().add("cart-item-image");

        // Product Name
        Label nameLabel = new Label(produit.getNomProduit().toUpperCase());
        nameLabel.getStyleClass().add("cart-item-label");
        nameLabel.setPrefWidth(150); // Fixed width for alignment

        // Price
        Label priceLabel = new Label(String.format("%.2f DT", item.getPrixUnitaire()));
        priceLabel.getStyleClass().add("cart-item-label");
        priceLabel.setPrefWidth(80);

        // Quantity Controls
        HBox quantityBox = new HBox(5);
        Button minusButton = new Button("-");
        minusButton.getStyleClass().add("quantity-button");
        minusButton.setOnAction(e -> {
            System.out.println("🔽 Decreasing quantity for item: " + item.getId());
            updateQuantity(item, item.getQuantite() - 1);
        });

        Label quantityLabel = new Label(String.valueOf(item.getQuantite()));
        quantityLabel.getStyleClass().add("cart-item-label");
        quantityLabel.setPrefWidth(30);
        quantityLabel.setStyle("-fx-alignment: center;");

        Button plusButton = new Button("+");
        plusButton.getStyleClass().add("quantity-button");
        plusButton.setOnAction(e -> {
            System.out.println("🔼 Increasing quantity for item: " + item.getId());
            updateQuantity(item, item.getQuantite() + 1);
        });

        quantityBox.getChildren().addAll(minusButton, quantityLabel, plusButton);

        // Subtotal (Price × Quantity)
        Label subtotalLabel = new Label(String.format("Sous-total: %.2f DT", item.getPrixUnitaire() * item.getQuantite()));
        subtotalLabel.getStyleClass().add("subtotal-label");

        itemBox.getChildren().addAll(imageView, nameLabel, priceLabel, quantityBox, subtotalLabel);
        return itemBox;
    }

    private void updateQuantity(LigneDeCommande item, int newQuantity) {
        try {
            if (newQuantity <= 0) {
                removeItem(item);
                return;
            }

            // Check stock availability
            Produit produit = produitService.getProduitById(item.getIdProduit());
            if (produit == null) {
                System.out.println("❌ Produit non trouvé lors de la mise à jour: " + item.getIdProduit());
                return;
            }
            if (newQuantity > produit.getStock()) {
                System.out.println("❌ Quantité demandée (" + newQuantity + ") dépasse le stock disponible (" + produit.getStock() + ") pour " + produit.getNomProduit());
                return;
            }

            // Update quantity and total
            double oldTotal = item.getPrixUnitaire() * item.getQuantite();
            item.setQuantite(newQuantity);
            double newTotal = item.getPrixUnitaire() * newQuantity;
            ligneDeCommandeService.updateLigneDeCommande(item);

            // Update cart total
            cart.setMontantTotal(cart.getMontantTotal() - oldTotal + newTotal);
            commandeService.updateCommande(cart);

            // Reload cart
            loadCart();

            System.out.println("✅ Quantité mise à jour pour l'item: " + item.getId() + " (Nouvelle quantité: " + newQuantity + ")");
        } catch (Exception e) {
            System.out.println("❌ Erreur lors de la mise à jour de la quantité: " + e.getMessage());
        }
    }

    private void removeItem(LigneDeCommande item) {
        try {
            // Update cart total
            double itemTotal = item.getPrixUnitaire() * item.getQuantite();
            cart.setMontantTotal(cart.getMontantTotal() - itemTotal);
            commandeService.updateCommande(cart);

            // Remove the item
            ligneDeCommandeService.deleteLigneDeCommande(item.getId());

            // Reload cart
            loadCart();

            System.out.println("✅ Item supprimé: " + item.getId());
        } catch (Exception e) {
            System.out.println("❌ Erreur lors de la suppression de l'item: " + e.getMessage());
        }
    }

    @FXML
    private void checkout() {
        String address = addressField.getText();
        String payment = paymentChoice.getValue();

        if (address == null || address.trim().isEmpty()) {
            System.out.println("❌ Veuillez entrer une adresse de livraison.");
            return;
        }

        if (payment == null || payment.trim().isEmpty()) {
            System.out.println("❌ Veuillez sélectionner un mode de paiement.");
            return;
        }

        try {
            // Decrease stock for each product in the order
            List<LigneDeCommande> items = ligneDeCommandeService.getLignesDeCommandeByCommandeId(cart.getId());
            for (LigneDeCommande item : items) {
                Produit produit = produitService.getProduitById(item.getIdProduit());
                if (produit != null) {
                    int newStock = produit.getStock() - item.getQuantite();
                    if (newStock < 0) {
                        System.out.println("❌ Stock insuffisant pour " + produit.getNomProduit());
                        return;
                    }
                    produit.setStock(newStock);
                    produitService.updateProduit(produit);
                    System.out.println("✅ Stock mis à jour pour " + produit.getNomProduit() + ": " + newStock);
                }
            }

            // Finalize the order
            cart.setStatut("Confirmé");
            cart.setAdresseLivraison(address);
            cart.setPaiement(payment);
            commandeService.updateCommande(cart);

            // Clear the form
            addressField.clear();
            paymentChoice.setValue(null);

            // Reload cart (should show empty since the cart is now "Confirmé")
            loadCart();

            System.out.println("✅ Commande confirmée ! Cart ID: " + cart.getId());
        } catch (Exception e) {
            System.out.println("❌ Erreur lors de la confirmation de la commande: " + e.getMessage());
        }
    }
}