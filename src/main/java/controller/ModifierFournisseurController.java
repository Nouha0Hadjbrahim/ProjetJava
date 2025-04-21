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

public class ModifierFournisseurController {

    @FXML private TextField idField;
    @FXML private TextField nomField;
    @FXML private TextField adresseField;
    @FXML private TextField contactField;
    @FXML private Button btnEnregistrer;
    @FXML private Button btnAnnuler;
    @FXML private Label errorLabel;

    private FournisseurService fournisseurService;
    private Fournisseur fournisseurAModifier;

    public void setFournisseur(Fournisseur fournisseur) {
        this.fournisseurAModifier = fournisseur;
        idField.setText(String.valueOf(fournisseur.getId()));
        nomField.setText(fournisseur.getNomFournisseur());
        adresseField.setText(fournisseur.getAdresse());
        contactField.setText(fournisseur.getContact());
    }

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
        // Validation pour le nom
        nomField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("[a-zA-Z éèàêâîôûùç'-]*")) {
                nomField.setText(oldVal);
            }
        });

        // Validation pour l'adresse
        adresseField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("[a-zA-Z0-9 éèàêâîôûùç'-,.]*")) {
                adresseField.setText(oldVal);
            }
        });

        // Validation pour le contact
        contactField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d{0,8}")) {
                contactField.setText(oldVal);
            }
        });
    }

    @FXML
    private void handleEnregistrer() {
        resetErrorStyles();

        // Validation du nom
        if (nomField.getText().trim().isEmpty()) {
            showFieldError(nomField, "Le nom est obligatoire");
            return;
        } else if (!nomField.getText().matches("[a-zA-Z éèàêâîôûùç'-]+")) {
            showFieldError(nomField, "Caractères non autorisés dans le nom");
            return;
        }
        // Validation du nom
        if (adresseField.getText().trim().isEmpty()) {
            showFieldError(adresseField, "L'adresse est obligatoire");
            return;
        } else if (!adresseField.getText().matches("[a-zA-Z0-9 éèàêâîôûùç'.,-]*")) {
            showFieldError(adresseField, "Caractères non autorisés dans l'adresse");
            return;
        }

        // Validation du contact
        if (contactField.getText().trim().isEmpty()) {
            showFieldError(contactField, "Le contact est obligatoire");
            return;
        } else if (!contactField.getText().matches("\\d{8}")) {
            showFieldError(contactField, "Le contact doit avoir 8 chiffres");
            return;
        }

        try {
            // Mise à jour des informations
            fournisseurAModifier.setNomFournisseur(nomField.getText().trim());
            fournisseurAModifier.setAdresse(adresseField.getText().trim());
            fournisseurAModifier.setContact(contactField.getText().trim());

            fournisseurService.update(fournisseurAModifier);
            showAlert("Succès", "Fournisseur modifié avec succès!");
            handleAnnuler();

        } catch (SQLException e) {
            showAlert("Erreur BD", "Erreur lors de la modification: " + e.getMessage());
        }
    }

    @FXML
    private void handleAnnuler() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/TableFournisseur.fxml"));
            Parent tableFournisseur = loader.load();

            // Accéder au DashboardController parent
            Scene currentScene = btnAnnuler.getScene();
            Parent dashboardRoot = currentScene.getRoot();
            StackPane mainContent = (StackPane) dashboardRoot.lookup("#mainContent");

            if (mainContent != null) {
                mainContent.getChildren().setAll(tableFournisseur);
            } else {
                // Fallback si la structure est différente
                Stage stage = (Stage) btnAnnuler.getScene().getWindow();
                stage.setScene(new Scene(tableFournisseur));
            }
        } catch (IOException e) {
            showAlert("Erreur", "Impossible de revenir à la liste des fournisseurs");
            e.printStackTrace();
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

    private void showAlert(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}