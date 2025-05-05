package controller;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import model.Produit;
import model.Promotion;
import service.ProduitService;
import service.PromotionService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class EditProduitController {
    private ProductCatalog productCatalogController;

    public void setProductCatalogController(ProductCatalog controller) {
        this.productCatalogController = controller;
    }


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

    private int produitId;
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

    public void setProduitId(int id) {
        this.produitId = id;
        // Load existing product data
        Produit produit = produitService.getProduitById(id);
        if (produit != null) {
            nomField.setText(produit.getNomProduit());
            categorieComboBox.getSelectionModel().select(produit.getCategorie());
            prixField.setText(String.valueOf(produit.getPrix()));
            stockField.setText(String.valueOf(produit.getStock()));
            imageField.setText(produit.getImage());
            // Pre-select the promotion if it exists
            if (produit.getIdPromotion() != null) {
                Promotion promotion = promotionService.getPromotionById(produit.getIdPromotion());
                if (promotion != null) {
                    promotionComboBox.getSelectionModel().select(promotion);
                }
            }
        } else {
            System.out.println("❌ Produit non trouvé: ID " + id);
        }
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
                // Define the target directory (relative to project root)
                String targetDir = "src/main/resources/resources/images/";
                File directory = new File(targetDir);
                if (!directory.exists()) {
                    boolean created = directory.mkdirs(); // Create the directory if it doesn't exist
                    System.out.println("📁 Directory created: " + created + " at " + directory.getAbsolutePath());
                } else {
                    System.out.println("📁 Directory already exists: " + directory.getAbsolutePath());
                }

                // Create a unique filename to avoid overwriting
                String originalFileName = selectedFile.getName();
                String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
                String newFileName = System.currentTimeMillis() + fileExtension; // Use timestamp to avoid conflicts
                Path targetPath = Paths.get(targetDir, newFileName);

                // Log the source and target paths
                System.out.println("📤 Source file: " + selectedFile.getAbsolutePath());
                System.out.println("📥 Target path: " + targetPath.toAbsolutePath());

                // Copy the file to the target directory
                Files.copy(selectedFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

                // Set the relative path in the imageField
                String relativePath = "resources/images/" + newFileName;
                imageField.setText(relativePath);
                System.out.println("✅ Image uploaded successfully: " + relativePath);
            } catch (IOException e) {
                e.printStackTrace(); // Print the full stack trace for debugging
                imageError.setText("Erreur lors de l'upload de l'image : " + e.getMessage());
                System.out.println("❌ Image upload failed: " + e.getMessage());
            }
        }
    }

    @FXML
    private void saveProduit() {
        if (!validateFields()) {
            return;
        }

        try {
            // Create updated product object
            Produit updatedProduit = new Produit();
            updatedProduit.setId(produitId);
            updatedProduit.setNomProduit(nomField.getText());
            updatedProduit.setCategorie(categorieComboBox.getSelectionModel().getSelectedItem());
            updatedProduit.setPrix(Double.parseDouble(prixField.getText()));
            updatedProduit.setStock(Integer.parseInt(stockField.getText()));
            updatedProduit.setImage(imageField.getText());

            // Set selected promotion ID
            Promotion selectedPromotion = promotionComboBox.getSelectionModel().getSelectedItem();
            updatedProduit.setIdPromotion(selectedPromotion != null ? selectedPromotion.getId() : null);

            // Keep existing fields that aren't in the form
            Produit existingProduit = produitService.getProduitById(produitId);
            if (existingProduit != null) {
                updatedProduit.setIdUser(existingProduit.getIdUser());
                updatedProduit.setStatut(existingProduit.getStatut());
            }

            // Update in DB
            produitService.updateProduit(updatedProduit);
            System.out.println("✅ Produit mis à jour: ID " + produitId);

            // ✅ Close current edit window
            imageField.getScene().getWindow().hide();

            // ✅ Open new ProductCatalog.fxml window
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/Produit_Promotion/product_catalog.fxml"));
            javafx.scene.Parent root = loader.load();

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Catalogue Produits");
            stage.setScene(new javafx.scene.Scene(root));
            stage.show();

        } catch (NumberFormatException e) {
            System.out.println("❌ Erreur format numérique: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("❌ Erreur mise à jour produit: " + e.getMessage());
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