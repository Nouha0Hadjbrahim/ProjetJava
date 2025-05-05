package controller;

import com.stripe.exception.StripeException;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Commande;
import model.LigneDeCommande;
import model.Produit;
import model.User;
import model.Material;
import service.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;


public class CartController {

    @FXML private VBox cartItemsVBox;
    @FXML private Label totalLabel;
    @FXML private TextField addressField;
    @FXML private ChoiceBox<String> paymentChoice;
    @FXML private Button checkoutButton;

    private CommandeService commandeService;
    private LigneDeCommandeService ligneDeCommandeService;
    private ProduitService produitService;
    private MateriauxService materiauxService;
    private User connectedUser;
    private Commande cart;

    public void setConnectedUser(User user) {
        this.connectedUser = user;
        System.out.println("CartController setConnectedUser: user = " + (user != null ? user.getEmail() : "null"));
        loadCart(); // ✅ C'est ici
    }


    @FXML
    public void initialize() {
        System.out.println("CartController initialize: cartItemsVBox = " + cartItemsVBox);
        commandeService = new CommandeService();
        ligneDeCommandeService = new LigneDeCommandeService();
        produitService = new ProduitService();
        try {
            materiauxService = new MateriauxService();
        } catch (Exception e) {
            System.err.println("Erreur d'initialisation MateriauxService: " + e.getMessage());
        }
        // ❌ Ne PAS appeler loadCart() ici
        // Parce que connectedUser est encore null au moment du initialize
        if (paymentChoice != null) {
            paymentChoice.setItems(FXCollections.observableArrayList("Carte Bancaire", "PayPal", "Espèces"));
            paymentChoice.setValue("Carte Bancaire");
        }
    }


    @FXML
    private void loadCart() {
        try {
            if (cartItemsVBox == null) {
                showAlert("Erreur", "Erreur interne: conteneur des articles du panier non initialisé.");
                return;
            }

            if (connectedUser == null) {
                cartItemsVBox.getChildren().setAll(createEmptyCartNode());
                totalLabel.setText("Total: 0.0 DT");
                checkoutButton.setDisable(true);
                showAlert("Erreur", "Utilisateur non connecté.");
                return;
            }

            List<Commande> userCommandes = commandeService.getCommandesByUserId(connectedUser.getId());
            cart = null;
            for (Commande commande : userCommandes) {
                if ("Pending".equals(commande.getStatut())) {
                    cart = commande;
                    break;
                }
            }

            if (cart == null) {
                cartItemsVBox.getChildren().setAll(createEmptyCartNode());
                totalLabel.setText("Total: 0.0 DT");
                checkoutButton.setDisable(true);
                return;
            }

            cartItemsVBox.getChildren().clear();
            System.out.println(cart.getId());
            List<LigneDeCommande> items = ligneDeCommandeService.getLignesDeCommandeByCommandeId(cart.getId());
            System.out.println("🎯 Nombre de lignes récupérées : " + items.size());

            System.out.println("Cart: Loading " + items.size() + " cart items");
            for (LigneDeCommande item : items) {
                Node itemNode = null;
                if (item.getIdProduit() != 0) {
                    Produit produit = produitService.getProduitById(item.getIdProduit());
                    if (produit == null) {
                        System.err.println("❌ Produit NON TROUVÉ pour idProduit = " + item.getIdProduit());
                        Label errorLabel = new Label("Produit non trouvé (ID: " + item.getIdProduit() + ")");
                        errorLabel.getStyleClass().add("cart-item-label");
                        itemNode = new VBox(errorLabel);
                    } else {
                        itemNode = createCartItemNode(item);
                    }
                } else if (item.getIdMateriau() != 0) {
                    try {
                        Material material = materiauxService.getById(item.getIdMateriau());
                        if (material == null) {
                            System.err.println("❌ Matériau NON TROUVÉ pour idMateriau = " + item.getIdMateriau());
                            Label errorLabel = new Label("Matériau non trouvé (ID: " + item.getIdMateriau() + ")");
                            errorLabel.getStyleClass().add("cart-item-label");
                            itemNode = new VBox(errorLabel);
                        } else {
                            itemNode = createCartMaterialNode(item, material);
                        }
                    } catch (Exception e) {
                        System.err.println("Erreur chargement matériau: " + e.getMessage());
                        Label errorLabel = new Label("Erreur chargement matériau (ID: " + item.getIdMateriau() + ")");
                        errorLabel.getStyleClass().add("cart-item-label");
                        itemNode = new VBox(errorLabel);
                    }
                }
                if (itemNode != null) {
                    cartItemsVBox.getChildren().add(itemNode);
                }
            }

            totalLabel.setText(String.format("Total: %.2f DT", cart.getMontantTotal()));
            checkoutButton.setDisable(items.isEmpty());
        } catch (Exception e) {
            System.err.println("❌ Cart: Erreur lors du chargement du panier: " + e.getMessage());
            showAlert("Erreur", "Impossible de charger le panier: " + e.getMessage());
        }
    }

    private Node createEmptyCartNode() {
        VBox emptyNode = new VBox(10);
        emptyNode.getStyleClass().add("empty-cart");
        emptyNode.setAlignment(Pos.CENTER);
        emptyNode.setPadding(new Insets(20));
        Label message = new Label("Votre panier est vide.");
        message.getStyleClass().add("empty-cart-label");
        Button shopButton = new Button("Continuer vos achats 🛍️");
        shopButton.getStyleClass().add("shop-button");
        shopButton.setOnAction(e -> loadProductCatalog());
        emptyNode.getChildren().addAll(message, shopButton);
        return emptyNode;
    }

    private Node createCartItemNode(LigneDeCommande item) {
        VBox card = new VBox(10);
        card.getStyleClass().add("cart-item-card");
        card.setPadding(new Insets(15));
        card.setPrefWidth(600);
        card.setStyle("-fx-background-color: #fff; -fx-border-radius: 10; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        Produit produit = produitService.getProduitById(item.getIdProduit());
        if (produit == null) {
            System.err.println("❌ Cart: Produit non trouvé pour ID: " + item.getIdProduit());
            Label errorLabel = new Label("Produit non trouvé (ID: " + item.getIdProduit() + ")");
            errorLabel.getStyleClass().add("cart-item-label");
            card.getChildren().add(errorLabel);
            return card;
        }

        ImageView imageView;
        try {
            String imagePath = produit.getImage();
            if (imagePath != null && !imagePath.startsWith("/")) {
                imagePath = "/assets/prod_mat/" + imagePath;
            }
            System.out.println("Cart: Attempting to load image: " + imagePath);
            InputStream imageStream = getClass().getResourceAsStream(imagePath != null && !imagePath.trim().isEmpty() ? imagePath : "/assets/prod_mat/prod1.jpeg");
            if (imageStream == null) {
                System.err.println("❌ Cart: Image not found: " + (imagePath != null ? imagePath : "null") + ", falling back to placeholder");
                imageStream = getClass().getResourceAsStream("/assets/prod_mat/prod1.jpeg");
                if (imageStream == null) {
                    System.err.println("❌ Cart: Placeholder image not found: /assets/prod_mat/prod1.jpeg");
                    imageView = new ImageView();
                    imageView.setFitWidth(100);
                    imageView.setFitHeight(100);
                } else {
                    imageView = new ImageView(new Image(imageStream, 100, 100, true, true));
                    imageStream.close();
                }
            } else {
                imageView = new ImageView(new Image(imageStream, 100, 100, true, true));
                imageStream.close();
            }
        } catch (Exception e) {
            System.err.println("❌ Cart: Failed to load image for product " + produit.getNomProduit() + ": " + e.getMessage());
            imageView = new ImageView();
            imageView.setFitWidth(100);
            imageView.setFitHeight(100);
        }
        imageView.getStyleClass().add("cart-item-image");

        HBox contentBox = new HBox(20);
        contentBox.setAlignment(Pos.CENTER_LEFT);

        VBox infoBox = new VBox(5);
        Label nameLabel = new Label(produit.getNomProduit().toUpperCase());
        nameLabel.getStyleClass().add("cart-item-label");
        nameLabel.setWrapText(true);
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        Label priceLabel = new Label(String.format("Prix unitaire: %.2f DT", item.getPrixUnitaire()));
        priceLabel.getStyleClass().add("cart-item-price");
        infoBox.getChildren().addAll(nameLabel, priceLabel);
        infoBox.setPrefWidth(200);

        HBox quantityBox = new HBox(10);
        quantityBox.setAlignment(Pos.CENTER);
        Button minusButton = new Button("−");
        minusButton.getStyleClass().add("quantity-button");
        minusButton.setPrefWidth(40);
        minusButton.setOnAction(e -> updateQuantity(item, item.getQuantite() - 1));

        Label quantityLabel = new Label("🛒 " + item.getQuantite());
        quantityLabel.getStyleClass().add("quantity-label");
        quantityLabel.setStyle("-fx-alignment: center;-fx-text-fill: black; -fx-min-width: 60px; -fx-font-weight: bold;");

        Button plusButton = new Button("+");
        plusButton.getStyleClass().add("quantity-button");
        plusButton.setPrefWidth(40);
        plusButton.setOnAction(e -> updateQuantity(item, item.getQuantite() + 1));

        // Sous-total toujours à jour
        Label subtotalLabel = new Label(String.format("Sous-total: %.2f DT", item.getPrixUnitaire() * item.getQuantite()));
        subtotalLabel.getStyleClass().add("subtotal-label");

        quantityBox.getChildren().addAll(minusButton, quantityLabel, plusButton, subtotalLabel);

        Button deleteButton = new Button();
        deleteButton.getStyleClass().add("icon-button");
        try {
            InputStream iconStream = getClass().getResourceAsStream("/assets/icons/poubelle.png");
            if (iconStream != null) {
                ImageView icon = new ImageView(new Image(iconStream));
                icon.setFitHeight(20);
                icon.setFitWidth(20);
                deleteButton.setGraphic(icon);
                deleteButton.setTooltip(new Tooltip("Supprimer"));
                iconStream.close();
            } else {
                deleteButton.setText("X");
            }
        } catch (Exception e) {
            deleteButton.setText("X");
        }
        deleteButton.setOnAction(e -> removeItem(item));

        contentBox.getChildren().addAll(infoBox, quantityBox, deleteButton);
        card.getChildren().addAll(imageView, contentBox);

        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #fff; -fx-border-radius: 10; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 3); -fx-scale-x: 1.02; -fx-scale-y: 1.02;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: #fff; -fx-border-radius: 10; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2); -fx-scale-x: 1.0; -fx-scale-y: 1.0;"));

        return card;
    }

    private Node createCartMaterialNode(LigneDeCommande item, Material material) {
        VBox card = new VBox(10);
        card.getStyleClass().add("cart-item-card");
        card.setPadding(new Insets(15));
        card.setPrefWidth(600);
        card.setStyle("-fx-background-color: #fff; -fx-border-radius: 10; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        ImageView imageView;
        try {
            String imagePath = material.getPhoto();
            if (imagePath != null && !imagePath.startsWith("/")) {
                imagePath = "/assets/prod_mat/" + imagePath;
            }
            InputStream imageStream = getClass().getResourceAsStream(imagePath != null && !imagePath.trim().isEmpty() ? imagePath : "/assets/prod_mat/mat1.jpeg");
            if (imageStream == null) {
                imageStream = getClass().getResourceAsStream("/assets/prod_mat/mat1.jpeg");
                imageView = new ImageView(new Image(imageStream, 100, 100, true, true));
            } else {
                imageView = new ImageView(new Image(imageStream, 100, 100, true, true));
            }
            if (imageStream != null) imageStream.close();
        } catch (Exception e) {
            imageView = new ImageView();
            imageView.setFitWidth(100);
            imageView.setFitHeight(100);
        }
        imageView.getStyleClass().add("cart-item-image");

        HBox contentBox = new HBox(20);
        contentBox.setAlignment(Pos.CENTER_LEFT);

        VBox infoBox = new VBox(5);
        Label nameLabel = new Label(material.getNomMateriel().toUpperCase());
        nameLabel.getStyleClass().add("cart-item-label");
        nameLabel.setWrapText(true);
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        Label priceLabel = new Label(String.format("Prix unitaire: %.2f DT", item.getPrixUnitaire()));
        priceLabel.getStyleClass().add("cart-item-price");
        infoBox.getChildren().addAll(nameLabel, priceLabel);
        infoBox.setPrefWidth(200);

        HBox quantityBox = new HBox(10);
        quantityBox.setAlignment(Pos.CENTER);
        Button minusButton = new Button("−");
        minusButton.getStyleClass().add("quantity-button");
        minusButton.setPrefWidth(40);
        minusButton.setOnAction(e -> updateQuantity(item, item.getQuantite() - 1));

        Label quantityLabel = new Label("🛒 " + item.getQuantite());
        quantityLabel.getStyleClass().add("quantity-label");
        quantityLabel.setStyle("-fx-alignment: center;-fx-text-fill: black; -fx-min-width: 60px; -fx-font-weight: bold;");

        Button plusButton = new Button("+");
        plusButton.getStyleClass().add("quantity-button");
        plusButton.setPrefWidth(40);
        plusButton.setOnAction(e -> updateQuantity(item, item.getQuantite() + 1));

        // Sous-total toujours à jour
        Label subtotalLabel = new Label(String.format("Sous-total: %.2f DT", item.getPrixUnitaire() * item.getQuantite()));
        subtotalLabel.getStyleClass().add("subtotal-label");

        quantityBox.getChildren().addAll(minusButton, quantityLabel, plusButton, subtotalLabel);

        Button deleteButton = new Button();
        deleteButton.getStyleClass().add("icon-button");
        try {
            InputStream iconStream = getClass().getResourceAsStream("/assets/icons/poubelle.png");
            if (iconStream != null) {
                ImageView icon = new ImageView(new Image(iconStream));
                icon.setFitHeight(20);
                icon.setFitWidth(20);
                deleteButton.setGraphic(icon);
                deleteButton.setTooltip(new Tooltip("Supprimer"));
                iconStream.close();
            } else {
                deleteButton.setText("X");
            }
        } catch (Exception e) {
            deleteButton.setText("X");
        }
        deleteButton.setOnAction(e -> removeItem(item));

        contentBox.getChildren().addAll(infoBox, quantityBox, deleteButton);
        card.getChildren().addAll(imageView, contentBox);

        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #fff; -fx-border-radius: 10; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 3); -fx-scale-x: 1.02; -fx-scale-y: 1.02;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: #fff; -fx-border-radius: 10; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2); -fx-scale-x: 1.0; -fx-scale-y: 1.0;"));

        return card;
    }

    private void updateQuantity(LigneDeCommande item, int newQuantity) {
        try {
            if (item.getIdProduit() != 0) {
                // Cas produit
                Produit produit = produitService.getProduitById(item.getIdProduit());
                if (produit == null) {
                    showAlert("Erreur", "Produit non trouvé.");
                    return;
                }
                if (newQuantity > produit.getStock()) {
                    showAlert("Stock insuffisant", "Quantité demandée (" + newQuantity + ") dépasse le stock disponible (" + produit.getStock() + ") pour " + produit.getNomProduit());
                    return;
                }
            } else if (item.getIdMateriau() != 0) {
                // Cas matériau
                Material material = materiauxService.getById(item.getIdMateriau());
                if (material == null) {
                    showAlert("Erreur", "Matériau non trouvé.");
                    return;
                }
                if (newQuantity > material.getQuantiteStock()) {
                    showAlert("Stock insuffisant", "Quantité demandée (" + newQuantity + ") dépasse le stock disponible (" + material.getQuantiteStock() + ") pour " + material.getNomMateriel());
                    return;
                }
            }

            double oldTotal = item.getPrixUnitaire() * item.getQuantite();
            int oldQuantity = item.getQuantite();
            item.setQuantite(newQuantity);
            double newTotal = item.getPrixUnitaire() * newQuantity;
            ligneDeCommandeService.updateLigneDeCommande(item);

            // Mise à jour du stock pour les matériaux
            if (item.getIdMateriau() != 0) {
                Material material = materiauxService.getById(item.getIdMateriau());
                if (material != null) {
                    int diff = newQuantity - oldQuantity;
                    material.setQuantiteStock(material.getQuantiteStock() - diff);
                    materiauxService.modifier(material);
                }
            }

            cart.setMontantTotal(cart.getMontantTotal() - oldTotal + newTotal);
            commandeService.updateCommande(cart);

            loadCart();
        } catch (Exception e) {
            showAlert("Erreur", "Impossible de mettre à jour la quantité: " + e.getMessage());
        }
    }

    private void removeItem(LigneDeCommande item) {
        try {
            System.out.println("Cart: Removing item: Product ID = " + item.getIdProduit() + ", LigneDeCommande ID = " + item.getId());
            ligneDeCommandeService.deleteLigneDeCommande(item.getId());

            // Recalculer le total après suppression
            List<LigneDeCommande> items = ligneDeCommandeService.getLignesDeCommandeByCommandeId(cart.getId());
            double newTotal = items.stream()
                .mapToDouble(i -> i.getPrixUnitaire() * i.getQuantite())
                .sum();
            cart.setMontantTotal(newTotal);
            commandeService.updateCommande(cart);

            loadCart();
        } catch (Exception e) {
            System.err.println("❌ Cart: Erreur lors de la suppression de l'item: " + e.getMessage());
            showAlert("Erreur", "Impossible de supprimer l'item: " + e.getMessage());
        }
    }

    @FXML
    private void checkout() {
        String address = addressField.getText();
        String payment = paymentChoice.getValue();

        if (address == null || address.trim().isEmpty()) {
            showAlert("Adresse manquante", "Veuillez entrer une adresse de livraison.");
            return;
        }

        if (payment == null || payment.trim().isEmpty()) {
            showAlert("Paiement manquant", "Veuillez sélectionner un mode de paiement.");
            return;
        }

        try {
            List<LigneDeCommande> items = ligneDeCommandeService.getLignesDeCommandeByCommandeId(cart.getId());
            if (items.isEmpty()) {
                showAlert("Erreur", "Le panier est vide. Veuillez ajouter des produits.");
                return;
            }

            // Validate stock for all items
            for (LigneDeCommande item : items) {
                if (item.getIdProduit() != 0) {
                    Produit produit = produitService.getProduitById(item.getIdProduit());
                    if (produit == null) {
                        showAlert("Erreur", "Produit non trouvé pour ID: " + item.getIdProduit());
                        return;
                    }
                    int newStock = produit.getStock() - item.getQuantite();
                    if (newStock < 0) {
                        showAlert("Stock insuffisant", "Stock insuffisant pour " + produit.getNomProduit());
                        return;
                    }
                } else if (item.getIdMateriau() != 0) {
                    Material material = materiauxService.getById(item.getIdMateriau());
                    if (material == null) {
                        showAlert("Erreur", "Matériau non trouvé pour ID: " + item.getIdMateriau());
                        return;
                    }
                    int newStock = material.getQuantiteStock() - item.getQuantite();
                    if (newStock < 0) {
                        showAlert("Stock insuffisant", "Stock insuffisant pour " + material.getNomMateriel());
                        return;
                    }
                }
            }

            // Update cart details
            cart.setAdresseLivraison(address);
            cart.setPaiement(payment);

            // Get user email and name
            String userEmail = connectedUser.getEmail(); // Assumes User has getEmail()
            String userName = connectedUser.getNom() != null ? connectedUser.getPrenom() : "Client"; // Fallback to "Client"

            if ("Carte Bancaire".equals(payment)) {
                // Create Stripe checkout session
                String checkoutUrl = PaymentService.createCheckoutSession(cart.getMontantTotal(), items);

                // Update cart status to "En attente" (pending payment)
                cart.setStatut("En attente");
                commandeService.updateCommande(cart);

                // Send order confirmation email
                EmailcService.sendOrderConfirmation(userEmail, userName, cart, items);

                // Open Stripe checkout URL in the user's default browser
                try {
                    java.awt.Desktop.getDesktop().browse(new URI(checkoutUrl));
                } catch (Exception e) {
                    System.err.println("❌ Cart: Erreur lors de l'ouverture du navigateur: " + e.getMessage());
                    showAlert("Erreur", "Impossible d'ouvrir la page de paiement. Veuillez utiliser ce lien: " + checkoutUrl);
                }

                // Clear UI and reload cart
                addressField.clear();
                paymentChoice.setValue(null);
                loadCart();

                showAlert("Redirection", "Vous serez redirigé vers la page de paiement Stripe. Une confirmation de commande a été envoyée à votre email.");
            } else {
                // Handle other payment methods (PayPal, Espèces)
                // Update stock for all items
                for (LigneDeCommande item : items) {
                    Produit produit = produitService.getProduitById(item.getIdProduit());
                    if (produit != null) {
                        int newStock = produit.getStock() - item.getQuantite();
                        produit.setStock(newStock);
                        produitService.updateProduit(produit);
                    }
                }

                // Update cart status to "Confirmé"
                cart.setStatut("Confirmé");
                commandeService.updateCommande(cart);

                // Send order confirmation email
                EmailcService.sendOrderConfirmation(userEmail, userName, cart, items);

                // Clear UI and reload cart
                addressField.clear();
                paymentChoice.setValue(null);
                loadCart();

                showAlert("Succès", "Commande confirmée avec succès ! Une confirmation a été envoyée à votre email.");
            }
        } catch (StripeException e) {
            System.err.println("❌ Cart: Erreur lors de la création de la session Stripe: " + e.getMessage());
            showAlert("Erreur", "Échec de la création de la session de paiement: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ Cart: Erreur lors de la confirmation de la commande: " + e.getMessage());
            showAlert("Erreur", "Impossible de confirmer la commande: " + e.getMessage());
        }
    }

    @FXML
    private void loadProductCatalog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Produit_Promotion/produit_catalogue_client.fxml"));
            Parent root = loader.load();
            ProductCatalogController controller = loader.getController();
            controller.setConnectedUser(connectedUser);
            Stage stage = (Stage) cartItemsVBox.getScene().getWindow();
            stage.setTitle("Catalogue des Produits");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("❌ Cart: Erreur chargement catalogue produits: " + e.getMessage());
            showAlert("Erreur", "Impossible de charger le catalogue: " + e.getMessage());
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