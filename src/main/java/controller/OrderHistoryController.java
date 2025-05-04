package controller;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.Commande;
import model.LigneDeCommande;
import model.Produit;
import service.CommandeService;
import service.LigneDeCommandeService;
import service.ProduitService;

import java.text.SimpleDateFormat;
import java.util.List;

public class OrderHistoryController {

    @FXML
    private VBox ordersVBox;

    private CommandeService commandeService;
    private LigneDeCommandeService ligneDeCommandeService;
    private ProduitService produitService;

    // Assume a logged-in user (replace with actual user authentication)
    private final int currentUserId = 1; // For demo purposes

    public void initialize() {
        commandeService = new CommandeService();
        ligneDeCommandeService = new LigneDeCommandeService();
        produitService = new ProduitService();

        loadOrderHistory();
    }

    private void loadOrderHistory() {
        try {
            // Load all confirmed orders for the user
            List<Commande> userCommandes = commandeService.getCommandesByUserId(currentUserId);
            ordersVBox.getChildren().clear();

            boolean hasOrders = false;
            for (Commande commande : userCommandes) {
                if ("Confirmé".equals(commande.getStatut())) {
                    ordersVBox.getChildren().add(createOrderCard(commande));
                    hasOrders = true;
                }
            }

            if (!hasOrders) {
                Label noOrdersLabel = new Label("Aucune commande confirmée.");
                noOrdersLabel.getStyleClass().add("no-orders-label");
                ordersVBox.getChildren().add(noOrdersLabel);
            }
        } catch (Exception e) {
            System.out.println("❌ Erreur lors du chargement de l'historique: " + e.getMessage());
        }
    }

    private Node createOrderCard(Commande commande) {
        VBox orderCard = new VBox(10);
        orderCard.getStyleClass().add("order-card");

        // Order ID and Date
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Label orderInfo = new Label(String.format("Commande ID: %d | Date: %s", commande.getId(), dateFormat.format(commande.getDateCommande())));
        orderInfo.getStyleClass().add("order-label");

        // Total Amount
        Label totalLabel = new Label(String.format("Total: %.2f DT", commande.getMontantTotal()));
        totalLabel.getStyleClass().add("order-label");

        // Items in the Order
        VBox itemsBox = new VBox(5);
        List<LigneDeCommande> items = ligneDeCommandeService.getLignesDeCommandeByCommandeId(commande.getId());
        for (LigneDeCommande item : items) {
            Produit produit = produitService.getProduitById(item.getIdProduit());
            if (produit != null) {
                Label itemLabel = new Label(String.format("%s | Quantité: %d | Prix Unitaire: %.2f DT",
                        produit.getNomProduit(), item.getQuantite(), item.getPrixUnitaire()));
                itemLabel.getStyleClass().add("order-label");
                itemsBox.getChildren().add(itemLabel);
            }
        }

        // Address (Editable)
        HBox addressBox = new HBox(10);
        Label addressLabel = new Label("Adresse: ");
        addressLabel.getStyleClass().add("order-label");
        TextField addressField = new TextField(commande.getAdresseLivraison());
        addressField.setPromptText("Adresse de livraison");
        addressField.getStyleClass().add("address-field");
        addressBox.getChildren().addAll(addressLabel, addressField);

        // Payment Method (Editable)
        HBox paymentBox = new HBox(10);
        Label paymentLabel = new Label("Paiement: ");
        paymentLabel.getStyleClass().add("order-label");
        ChoiceBox<String> paymentChoice = new ChoiceBox<>();
        paymentChoice.getItems().addAll("Carte Bancaire", "PayPal", "Espèces");
        paymentChoice.setValue(commande.getPaiement() != null ? commande.getPaiement() : "Carte Bancaire");
        paymentChoice.getStyleClass().add("payment-choice");
        paymentBox.getChildren().addAll(paymentLabel, paymentChoice);

        // Button Container (to hold Save and Delete buttons)
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        // Save Button
        Button saveButton = new Button("Enregistrer 📝");
        saveButton.getStyleClass().add("save-button");
        saveButton.setOnAction(e -> {
            commande.setAdresseLivraison(addressField.getText());
            commande.setPaiement(paymentChoice.getValue());
            commandeService.updateCommande(commande);
            System.out.println("✅ Commande mise à jour ! ID: " + commande.getId());
            loadOrderHistory(); // Refresh the UI
        });

        // Delete Button
        Button deleteButton = new Button("Supprimer");
        deleteButton.getStyleClass().add("delete-button");
        deleteButton.setOnAction(e -> deleteOrder(commande.getId()));

        // Add buttons to the button container
        buttonBox.getChildren().addAll(saveButton, deleteButton);

        // Add all elements to the card
        orderCard.getChildren().addAll(orderInfo, totalLabel, itemsBox, addressBox, paymentBox, buttonBox);
        return orderCard;
    }

    private void deleteOrder(int commandeId) {
        try {
            // Delete associated LigneDeCommande entries first
            List<LigneDeCommande> items = ligneDeCommandeService.getLignesDeCommandeByCommandeId(commandeId);
            for (LigneDeCommande item : items) {
                ligneDeCommandeService.deleteLigneDeCommande(item.getId());
            }

            // Delete the Commande
            commandeService.deleteCommande(commandeId);
            System.out.println("✅ Commande supprimée : " + commandeId);

            // Reload the orders to refresh the UI
            loadOrderHistory();
        } catch (Exception e) {
            System.out.println("❌ Erreur lors de la suppression de la commande : " + e.getMessage());
        }
    }
}