package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import model.Fournisseur;
import service.FournisseurService;

import java.io.IOException;
import java.sql.SQLException;

public class AjoutFournisseurController {

    @FXML private TextField nomField;
    @FXML private TextField adresseField;
    @FXML private TextField contactField;
    @FXML private Button btnAjouter;
    @FXML private Button btnRetour;
    @FXML private Label errorLabel;

    private FournisseurService fournisseurService;

    @FXML
    public void initialize() {
        try {
            fournisseurService = new FournisseurService();
            setupFieldValidations();
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de se connecter à la base de données");
            e.printStackTrace();
        }
    }

    private void setupFieldValidations() {
        // Validation pour le nom (lettres et certains caractères spéciaux uniquement)
        nomField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("[a-zA-Z éèàêâîôûùç'-]*")) {
                nomField.setText(oldVal);
            }
        });

        // Validation pour l'adresse (pas de caractères spéciaux sauf ceux autorisés)
        adresseField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("[a-zA-Z0-9 éèàêâîôûùç'-,.]*")) {
                adresseField.setText(oldVal);
            }
        });

        // Validation pour le contact (exactement 8 chiffres)
        contactField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d{0,8}")) { // Permet jusqu'à 8 chiffres
                contactField.setText(oldVal);
            }
        });
    }

    @FXML
    private void handleAjouter() {
        resetErrorStyles();

        // Validation du nom
        if (nomField.getText().trim().isEmpty()) {
            showFieldError(nomField, "Le nom du fournisseur est obligatoire");
            return;
        } else if (!nomField.getText().matches("[a-zA-Z éèàêâîôûùç'-]+")) {
            showFieldError(nomField, "Le nom ne doit contenir que des lettres et certains caractères spéciaux");
            return;
        }
        if (adresseField.getText().trim().isEmpty()) {
            showFieldError(adresseField, "L'adresse est obligatoire");
            return;}
        // Validation de l'adresse
        else if (!adresseField.getText().matches("[a-zA-Z0-9 éèàêâîôûùç'-,.*£!$#@&]*")) {
            showFieldError(adresseField, "L'adresse contient des caractères non autorisés");
            return;
        }

        // Validation du contact
        if (contactField.getText().trim().isEmpty()) {
            showFieldError(contactField, "Le contact est obligatoire");
            return;
        } else if (!contactField.getText().matches("\\d{8}")) {
            showFieldError(contactField, "Le contact doit contenir exactement 8 chiffres");
            return;
        }

        try {
            Fournisseur nouveauFournisseur = new Fournisseur(
                    nomField.getText().trim(),
                    adresseField.getText().trim(),
                    contactField.getText().trim()
            );

            fournisseurService.create(nouveauFournisseur);
            showAlert("Succès", "Fournisseur ajouté avec succès!");
            clearForm();

        } catch (SQLException e) {
            showAlert("Erreur BD", "Erreur lors de l'ajout: " + e.getMessage());
        }
    }

    private void resetErrorStyles() {
        nomField.setStyle("");
        adresseField.setStyle("");
        contactField.setStyle("");
        errorLabel.setText("");
    }

    private void showFieldError(TextField field, String message) {
        field.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
        errorLabel.setText(message);
        field.requestFocus();
    }

    private void clearForm() {
        nomField.clear();
        adresseField.clear();
        contactField.clear();
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/TableFournisseur.fxml"));
            Parent tableFournisseur = loader.load();

            // Accéder au DashboardController parent
            Scene currentScene = btnRetour.getScene();
            Parent dashboardRoot = currentScene.getRoot();
            StackPane mainContent = (StackPane) dashboardRoot.lookup("#mainContent");

            if (mainContent != null) {
                mainContent.getChildren().setAll(tableFournisseur);
            } else {
                // Fallback si la structure est différente
                Stage stage = (Stage) btnRetour.getScene().getWindow();
                stage.setScene(new Scene(tableFournisseur));
            }
        } catch (IOException e) {
            showAlert("Erreur", "Impossible de revenir à la liste des fournisseurs");
            e.printStackTrace();
        }
    }
}