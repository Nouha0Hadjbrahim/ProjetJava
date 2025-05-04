package controller;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import model.Promotion;
import model.Produit;
import service.PromotionService;
import service.ProduitService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

public class AddProduitController {

    @FXML
    private TextField nomField;
    @FXML
    private ComboBox<String> categorieComboBox;
    @FXML
    private TextField prixField;
    @FXML
    private TextField stockField;
    @FXML
    private TextField imageField;
    @FXML
    private ComboBox<Promotion> promotionComboBox;

    @FXML
    private Label nomError;
    @FXML
    private Label categorieError;
    @FXML
    private Label prixError;
    @FXML
    private Label stockError;
    @FXML
    private Label imageError;
    @FXML
    private Label promotionError;

    private ProduitService produitService;
    private PromotionService promotionService;
    private final String[] categories = {"Électronique", "Vêtements", "Alimentation", "Maison", "Sport"}; // Predefined categories

    public void initialize() {
        produitService = new ProduitService();
        promotionService = new PromotionService();
        clearErrors();
        loadCategories();
        loadPromotions();
    }

    private void loadCategories() {
        categorieComboBox.getItems().addAll(categories);
    }

    private void loadPromotions() {
        // Load all promotions into the ComboBox
        promotionComboBox.getItems().addAll(promotionService.getAll());
        // Customize how promotions are displayed in the ComboBox
        promotionComboBox.setCellFactory(lv -> new javafx.scene.control.ListCell<Promotion>() {
            @Override
            protected void updateItem(Promotion promotion, boolean empty) {
                super.updateItem(promotion, empty);
                if (empty || promotion == null) {
                    setText(null);
                } else {
                    setText(promotion.getCodeCoupon() + " (Prix: " + promotion.getPrixNouv() + " DT)");
                }
            }
        });
        // Customize the display of the selected item
        promotionComboBox.setButtonCell(new javafx.scene.control.ListCell<Promotion>() {
            @Override
            protected void updateItem(Promotion promotion, boolean empty) {
                super.updateItem(promotion, empty);
                if (empty || promotion == null) {
                    setText(null);
                } else {
                    setText(promotion.getCodeCoupon() + " (Prix: " + promotion.getPrixNouv() + " DT)");
                }
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
                // Define the target directory (src/main/resources/resources/images/)
                String targetDir = "src/main/resources/resources/images/";
                File directory = new File(targetDir);
                if (!directory.exists()) {
                    directory.mkdirs(); // Create the directory if it doesn't exist
                }

                // Create a unique filename to avoid overwriting
                String originalFileName = selectedFile.getName();
                String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
                String newFileName = System.currentTimeMillis() + fileExtension; // Use timestamp to avoid conflicts
                Path targetPath = Paths.get(targetDir, newFileName);

                // Copy the file to the target directory
                Files.copy(selectedFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

                // Set the relative path in the imageField
                String relativePath = "resources/images/" + newFileName;
                imageField.setText(relativePath);
            } catch (IOException e) {
                imageError.setText("Erreur lors de l'upload de l'image : " + e.getMessage());
            }
        }
    }

    @FXML
    private void saveProduit() {
        if (!validateFields()) {
            return;
        }

        try {
            // Create new product object
            Produit newProduit = new Produit();
            newProduit.setNomProduit(nomField.getText());
            newProduit.setCategorie(categorieComboBox.getSelectionModel().getSelectedItem());
            newProduit.setPrix(Double.parseDouble(prixField.getText()));
            newProduit.setStock(Integer.parseInt(stockField.getText()));
            newProduit.setImage(imageField.getText());
            // Set the selected promotion ID (if any)
            Promotion selectedPromotion = promotionComboBox.getSelectionModel().getSelectedItem();
            newProduit.setIdPromotion(selectedPromotion != null ? selectedPromotion.getId() : null);
            // Remove hardcoded idUser to avoid unique constraint violation
            // newProduit.setIdUser(1); // Comment out; replace with actual user ID if available
            newProduit.setStatut("Available"); // Default status

            // Save to DB
            produitService.addProduit(newProduit);
            System.out.println("✅ Produit ajouté: " + newProduit.getNomProduit());

            // Close the window
            nomField.getScene().getWindow().hide();
        } catch (NumberFormatException e) {
            System.out.println("❌ Erreur format numérique: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("❌ Erreur ajout produit: " + e.getMessage());
        }
    }

    @FXML
    private void cancel() {
        // Close the window without saving
        nomField.getScene().getWindow().hide();
    }

    private boolean validateFields() {
        clearErrors();
        boolean isValid = true;

        // Validate Nom
        if (nomField.getText().trim().isEmpty()) {
            nomError.setText("Le nom est requis.");
            isValid = false;
        }

        // Validate Catégorie
        if (categorieComboBox.getSelectionModel().getSelectedItem() == null) {
            categorieError.setText("La catégorie est requise.");
            isValid = false;
        }

        // Validate Prix
        try {
            double prix = Double.parseDouble(prixField.getText().trim());
            if (prix <= 0) {
                prixError.setText("Le prix doit être positif.");
                isValid = false;
            }
        } catch (NumberFormatException e) {
            prixError.setText("Le prix doit être un nombre valide.");
            isValid = false;
        }

        // Validate Stock
        try {
            int stock = Integer.parseInt(stockField.getText().trim());
            if (stock < 0) {
                stockError.setText("Le stock ne peut pas être négatif.");
                isValid = false;
            }
        } catch (NumberFormatException e) {
            stockError.setText("Le stock doit être un entier valide.");
            isValid = false;
        }

        // Validate Image
        String imagePath = imageField.getText().trim();
        if (imagePath.isEmpty()) {
            imageError.setText("Le chemin de l'image est requis.");
            isValid = false;
        } else if (!(imagePath.toLowerCase().endsWith(".png") || imagePath.toLowerCase().endsWith(".jpg"))) {
            imageError.setText("L'image doit être un fichier .png ou .jpg.");
            isValid = false;
        }

        // Promotion validation (optional field, so no error if not selected)
        // No validation needed for now

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
}