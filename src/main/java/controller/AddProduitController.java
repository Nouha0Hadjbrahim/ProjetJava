package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import model.Promotion;
import model.Produit;
import service.PromotionService;
import service.ProduitService;
import model.User;
import service.UserService;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import javafx.scene.control.Alert;

public class AddProduitController {

    private ProductCatalog productCatalogController;

    public void setProductCatalogController(ProductCatalog controller) {
        this.productCatalogController = controller;
    }

    @FXML private TextField nomField;
    @FXML private ComboBox<String> categorieComboBox;
    @FXML private TextField prixField;
    @FXML private TextField stockField;
    @FXML private TextField imageField;
    @FXML private ComboBox<Promotion> promotionComboBox;

    @FXML private Label nomError;
    @FXML private Label categorieError;
    @FXML private Label prixError;
    @FXML private Label stockError;
    @FXML private Label imageError;
    @FXML private Label promotionError;
    private User connectedUser;

    private ProduitService produitService;
    private PromotionService promotionService;
    private final String[] categories = {"Électronique", "Vêtements", "Alimentation", "Maison", "Sport"};

    public void initialize() {
        produitService = new ProduitService();
        promotionService = new PromotionService();
        clearErrors();
        loadCategories();
        loadPromotions();
    }
    public void setConnectedUser(User user) {
        this.connectedUser = user;
    }

    private void loadCategories() {
        categorieComboBox.getItems().addAll(categories);
    }

    private void loadPromotions() {
        promotionComboBox.getItems().addAll(promotionService.getAll());
        promotionComboBox.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Promotion promotion, boolean empty) {
                super.updateItem(promotion, empty);
                setText((empty || promotion == null) ? null : promotion.getCodeCoupon() + " (Prix: " + promotion.getPrixNouv() + " DT)");
            }
        });
        promotionComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Promotion promotion, boolean empty) {
                super.updateItem(promotion, empty);
                setText((empty || promotion == null) ? null : promotion.getCodeCoupon() + " (Prix: " + promotion.getPrixNouv() + " DT)");
            }
        });
    }

    @FXML
    private void uploadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg")
        );
        File selectedFile = fileChooser.showOpenDialog(imageField.getScene().getWindow());
        if (selectedFile != null) {
            try {
                // Save to src/main/resources/images/
                String targetDir = "src/main/resources/images/";
                File directory = new File(targetDir);
                if (!directory.exists()) directory.mkdirs();

                String extension = selectedFile.getName().substring(selectedFile.getName().lastIndexOf("."));
                String newFileName = System.currentTimeMillis() + extension;
                Path targetPath = Paths.get(targetDir, newFileName);

                Files.copy(selectedFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                // Store the path as images/<filename>
                imageField.setText("images/" + newFileName);
            } catch (IOException e) {
                imageError.setText("Erreur lors de l'upload de l'image : " + e.getMessage());
            }
        }
    }
    @FXML
    private void saveProduit() {
        if (!validateFields()) return;

        try {
            Produit newProduit = new Produit();
            newProduit.setNomProduit(nomField.getText());
            newProduit.setCategorie(categorieComboBox.getValue());
            newProduit.setPrix(Double.parseDouble(prixField.getText()));
            newProduit.setStock(Integer.parseInt(stockField.getText()));
            newProduit.setImage(imageField.getText());
            Promotion selectedPromo = promotionComboBox.getValue();
            newProduit.setIdPromotion(selectedPromo != null ? selectedPromo.getId() : null);
            newProduit.setStatut("Available");

            if (connectedUser != null) {
                newProduit.setIdUser(connectedUser.getId());
            } else {
                System.err.println("❌ Utilisateur non connecté pour l'ajout de produit !");
                showAlert("Erreur", "Impossible d'ajouter un produit sans utilisateur connecté.");
                return;
            }

            produitService.addProduit(newProduit);
            System.out.println("✅ Produit ajouté: " + newProduit.getNomProduit());

            if (productCatalogController != null) {
                productCatalogController.loadProducts();
            }

            nomField.getScene().getWindow().hide();
        } catch (Exception e) {
            System.out.println("❌ Erreur ajout produit: " + e.getMessage());
        }
    }


    @FXML
    private void cancel() {
        nomField.getScene().getWindow().hide();
    }

    private boolean validateFields() {
        clearErrors();
        boolean isValid = true;

        if (nomField.getText().trim().isEmpty()) {
            nomError.setText("Le nom est requis.");
            isValid = false;
        }
        if (categorieComboBox.getValue() == null) {
            categorieError.setText("La catégorie est requise.");
            isValid = false;
        }
        try {
            double prix = Double.parseDouble(prixField.getText().trim());
            if (prix <= 0) prixError.setText("Le prix doit être positif.");
        } catch (NumberFormatException e) {
            prixError.setText("Le prix doit être un nombre valide.");
            isValid = false;
        }
        try {
            int stock = Integer.parseInt(stockField.getText().trim());
            if (stock < 0) stockError.setText("Le stock ne peut pas être négatif.");
        } catch (NumberFormatException e) {
            stockError.setText("Le stock doit être un entier valide.");
            isValid = false;
        }
        String image = imageField.getText().trim();
        if (image.isEmpty()) {
            imageError.setText("Le chemin de l'image est requis.");
            isValid = false;
        } else if (!(image.endsWith(".png") || image.endsWith(".jpg"))) {
            imageError.setText("L'image doit être un fichier .png ou .jpg.");
            isValid = false;
        }

        return isValid;
    }

    private void clearErrors() {
        nomError.setText("");
        categorieError.setText("");
        prixError.setText("");
        stockError.setText("");
        imageError.setText("");
        promotionError.setText("");
    }
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}