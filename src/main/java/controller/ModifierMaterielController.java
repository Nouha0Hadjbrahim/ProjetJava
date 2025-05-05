package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.util.StringConverter;
import model.Fournisseur;
import model.Material;
import service.FournisseurService;
import service.MateriauxService;
import javafx.stage.Stage;


import java.io.IOException;
import java.sql.SQLException;

public class ModifierMaterielController {

    @FXML private TextField nomField;
    @FXML private ComboBox<String> categorieCombo;
    @FXML private TextField quantiteField;
    @FXML private TextField seuilField;
    @FXML private TextField prixField;
    @FXML private TextArea descriptionArea;
    @FXML private TextField photoField;
    @FXML private ComboBox<Fournisseur> fournisseurCombo;
    @FXML private Button btnModifier;
    @FXML private Button btnAnnuler;
    @FXML private Button btnRetour;
    @FXML private Label errorLabel; // Label pour les messages d'erreur

    private MateriauxService materiauxService;
    private Material materialToModify;

    @FXML
    public void initialize() {
        try {
            materiauxService = new MateriauxService();
            FournisseurService fournisseurService = new FournisseurService();

            // Remplir les catégories
            categorieCombo.getItems().addAll(
                    "Peinture",
                    "Poterie",
                    "Tissu",
                    "Bois",
                    "Bijoux",
                    "Autre"
            );
            fournisseurCombo.setConverter(new StringConverter<Fournisseur>() {
                @Override
                public String toString(Fournisseur fournisseur) {
                    return fournisseur != null ? fournisseur.getNomFournisseur() : "";
                }


                public Fournisseur fromString(String string) {
                    return null;
                }
            });

            // Charger les fournisseurs
            fournisseurCombo.getItems().addAll(fournisseurService.getAll());

            // Configuration des validations
            setupFieldValidations();
        } catch (SQLException e) {
            showAlert("Erreur de connexion", "Impossible de se connecter à la base de données");
            e.printStackTrace();
        }

    }

    private void setupFieldValidations() {
        // Validation pour le nom (lettres uniquement)
        nomField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("[a-zA-Z éèàêâîôûùç'-]*")) {
                nomField.setText(oldVal);
            }
        });

        // Validation pour la quantité (nombres entiers positifs)
        quantiteField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                quantiteField.setText(oldVal);
            } else if (!newVal.isEmpty() && Integer.parseInt(newVal) <= 0) {
                quantiteField.setText("1");
            }
        });

        // Validation pour le seuil (minimum 10)
        seuilField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                seuilField.setText(oldVal);
            } else if (!newVal.isEmpty() && Integer.parseInt(newVal) < 10) {
                seuilField.setText("10");
            }
        });

        // Validation pour le prix (nombre décimal positif)
        prixField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*(\\.\\d*)?")) {
                prixField.setText(oldVal);
            } else if (!newVal.isEmpty() && Double.parseDouble(newVal) <= 0) {
                prixField.setText("0.1");
            }
        });

        // Validation pour la description (minimum 10 caractères si remplie)
        descriptionArea.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.isEmpty() && newVal.length() < 10) {
                descriptionArea.setStyle("-fx-border-color: orange;");
                errorLabel.setText("La description doit contenir au moins 10 caractères");
            } else {
                descriptionArea.setStyle("");
                errorLabel.setText("");
            }
        });
    }

    @FXML
    private void handleModifier() {
        resetErrorStyles();

        // Validation du nom (non vide et lettres uniquement)
        if (nomField.getText().trim().isEmpty()) {
            showFieldError(nomField, "Le nom du matériel est obligatoire");
            return;
        } else if (!nomField.getText().matches("[a-zA-Z éèàêâîôûùç'-]+")) {
            showFieldError(nomField, "Le nom ne doit contenir que des lettres");
            return;
        }

        // Validation de la catégorie (non vide)
        if (categorieCombo.getValue() == null) {
            showComboError(categorieCombo, "Veuillez sélectionner une catégorie");
            return;
        }

        // Validation quantité (nombre entier positif)
        if (quantiteField.getText().trim().isEmpty()) {
            showFieldError(quantiteField, "La quantité est obligatoire");
            return;
        } else if (Integer.parseInt(quantiteField.getText()) <= 0) {
            showFieldError(quantiteField, "La quantité doit être positive > 0");
            return;
        }

        // Validation seuil (minimum 10)
        if (seuilField.getText().trim().isEmpty()) {
            showFieldError(seuilField, "Le seuil minimum est obligatoire");
            return;
        } else if (Integer.parseInt(seuilField.getText()) < 10) {
            showFieldError(seuilField, "Le seuil minimum doit être ≥ 10");
            return;
        }

        // Validation prix (nombre décimal positif)
        if (prixField.getText().trim().isEmpty()) {
            showFieldError(prixField, "Le prix unitaire est obligatoire");
            return;
        } else if (Double.parseDouble(prixField.getText()) <= 0) {
            showFieldError(prixField, "Le prix doit être positif");
            return;
        }

        // Validation description (minimum 10 caractères si remplie)
        if (!descriptionArea.getText().isEmpty() && descriptionArea.getText().length() < 10) {
            showTextAreaError(descriptionArea, "La description doit contenir au moins 10 caractères");
            return;
        }

        // Validation supplémentaire : seuil <= quantité
        try {
            int quantite = Integer.parseInt(quantiteField.getText());
            int seuil = Integer.parseInt(seuilField.getText());

            if (seuil > quantite) {
                showFieldError(seuilField, "Le seuil ne peut pas être supérieur à la quantité");
                return;
            }



            // Mise à jour de l'objet Material avec les nouvelles données
            materialToModify.setNomMateriel(nomField.getText());
            materialToModify.setQuantiteStock(Integer.parseInt(quantiteField.getText()));
            materialToModify.setSeuilMin(Integer.parseInt(seuilField.getText()));
            materialToModify.setPrixUnitaire(Double.parseDouble(prixField.getText()));
            materialToModify.setCategorie(categorieCombo.getValue());
            materialToModify.setDescription(descriptionArea.getText());
            materialToModify.setPhoto(photoField.getText());
            materialToModify.setFournisseur(fournisseurCombo.getValue());

            // Sauvegarde des modifications
            materiauxService.modifier(materialToModify);
            showAlert("Succès", "Matériel modifié avec succès!");
            handleRetour(); // Appel de la méthode pour revenir à la liste des matériaux

            // Effacer le formulaire après modification
            clearForm();

        } catch (NumberFormatException e) {
            showAlert("Erreur", "Valeurs numériques invalides");
        } catch (SQLException e) {
            showAlert("Erreur BD", "Erreur lors de la modification: " + e.getMessage());
        }
    }

    private void resetErrorStyles() {
        nomField.setStyle("");
        quantiteField.setStyle("");
        seuilField.setStyle("");
        prixField.setStyle("");
        descriptionArea.setStyle("");
        categorieCombo.setStyle("");
        errorLabel.setText("");
    }

    private void showFieldError(TextField field, String message) {
        field.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
        errorLabel.setText(message);
        field.requestFocus();
    }

    private void showComboError(ComboBox<?> combo, String message) {
        combo.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
        errorLabel.setText(message);
        combo.requestFocus();
    }

    private void showTextAreaError(TextArea area, String message) {
        area.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
        errorLabel.setText(message);
        area.requestFocus();
    }

    private void clearForm() {
        nomField.clear();
        categorieCombo.getSelectionModel().clearSelection();
        quantiteField.clear();
        seuilField.clear();
        prixField.clear();
        descriptionArea.clear();
        photoField.clear();
        fournisseurCombo.getSelectionModel().clearSelection();
        resetErrorStyles();
    }

    private void showAlert(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleRetour() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/TableMateriaux.fxml"));
            Parent tableMateriaux = loader.load();

            // Accéder au DashboardController parent
            Scene currentScene = btnRetour.getScene();
            Parent dashboardRoot = currentScene.getRoot();
            StackPane mainContent = (StackPane) dashboardRoot.lookup("#mainContent");

            if (mainContent != null) {
                mainContent.getChildren().setAll(tableMateriaux);
            } else {
                // Fallback si la structure est différente
                Stage stage = (Stage) btnRetour.getScene().getWindow();
                stage.setScene(new Scene(tableMateriaux));
            }
        } catch (IOException e) {
            showAlert("Erreur", "Impossible de revenir à la liste des matériaux");
            e.printStackTrace();
        }
    }

    // Méthode pour initialiser avec le matériel à modifier
    public void initData(Material material) {
        System.out.println("Initialisation avec material: " + material);
        System.out.println("ID: " + material.getId());
        materialToModify = material;
        nomField.setText(material.getNomMateriel());
        categorieCombo.setValue(material.getCategorie());
        quantiteField.setText(String.valueOf(material.getQuantiteStock()));
        seuilField.setText(String.valueOf(material.getSeuilMin()));
        prixField.setText(String.valueOf(material.getPrixUnitaire()));
        descriptionArea.setText(material.getDescription());
        photoField.setText(material.getPhoto());
        if (material.getFournisseur() != null) {
            fournisseurCombo.getSelectionModel().select(material.getFournisseur());
        }
    }
}
