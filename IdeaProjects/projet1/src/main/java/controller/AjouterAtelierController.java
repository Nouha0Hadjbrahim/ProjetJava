package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import model.Ateliers;
import model.User;
import service.AteliersService;
import utils.SessionManager;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AjouterAtelierController {

    User currentUser = SessionManager.getCurrentUser();
    private Ateliers atelierEnEdition = null;

    @FXML private TextField titreField;
    @FXML private ComboBox<String> categorieField;
    @FXML private TextArea descriptionField;
    @FXML private ComboBox<String> niveauDiffField;
    @FXML private TextField prixField;
    @FXML private DatePicker datePicker;
    @FXML private TextField heureField;
    @FXML private TextField dureeField;
    @FXML private TextField lienField;

    private final AteliersService ateliersService = new AteliersService();
    private Runnable refreshCallback;
    private javafx.scene.layout.Pane parentContainer;

    public void setRefreshCallback(Runnable callback) {
        this.refreshCallback = callback;
    }

    public void setParentContainer(javafx.scene.layout.Pane container) {
        this.parentContainer = container;
    }

    @FXML
    private void initialize() {
        categorieField.getItems().addAll("Poterie", "Peinture", "Broderie", "Tricot", "Couture", "Bijoux", "Faits Main");
        niveauDiffField.getItems().addAll("Débutant", "Intermédiaire", "Avancé");
    }

    @FXML
    private void handleAjouterAtelier() {
        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Utilisateur non connecté.");
            return;
        }

        try {
            String titre = titreField.getText();
            String categorie = categorieField.getValue();
            String description = descriptionField.getText();
            String niveau = niveauDiffField.getValue();
            String lien = lienField.getText();

            if (titre.isEmpty() || categorie == null || description.isEmpty() || niveau == null || prixField.getText().isEmpty()
                    || datePicker.getValue() == null || heureField.getText().isEmpty() || dureeField.getText().isEmpty() || lien.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Veuillez remplir tous les champs.");
                return;
            }

            double prix = Double.parseDouble(prixField.getText());
            int duree = Integer.parseInt(dureeField.getText());

            String dateStr = datePicker.getValue().toString();
            String heureStr = heureField.getText(); // format "HH:mm"
            String dateHeureStr = dateStr + " " + heureStr;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            LocalDateTime dateHeure = LocalDateTime.parse(dateHeureStr, formatter);

            // Si l'atelier à modifier existe (mode modification)
            if (atelierEnEdition != null) {
                // Mise à jour des informations de l'atelier
                atelierEnEdition.setTitre(titre);
                atelierEnEdition.setCategorie(categorie);
                atelierEnEdition.setDescription(description);
                atelierEnEdition.setNiveau_diff(niveau);
                atelierEnEdition.setPrix(prix);
                atelierEnEdition.setDatecours(dateHeure);
                atelierEnEdition.setDuree(duree);
                atelierEnEdition.setLien(lien);

                ateliersService.modifierAtelier(atelierEnEdition); // Appel pour mettre à jour l'atelier dans la base de données
                showAlert(Alert.AlertType.INFORMATION, "Atelier modifié avec succès !");
            } else {
                // Création d'un nouvel atelier
                Ateliers atelier = new Ateliers();
                ateliersService.ajouterAtelier(atelier); // Appel pour ajouter un nouvel atelier dans la base de données
                showAlert(Alert.AlertType.INFORMATION, "Atelier ajouté avec succès !");
            }

            if (refreshCallback != null) {
                refreshCallback.run(); // Actualisation de la liste des ateliers
            }

            handleRetourListe(); // Retourner à la liste des ateliers

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Veuillez entrer des valeurs numériques valides pour le prix et la durée.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur lors de l'ajout ou de la modification de l'atelier : " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    @FXML
    private void handleRetourListe() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/TableAteliers.fxml"));
            Parent tableView = loader.load();

            if (parentContainer != null) {
                parentContainer.getChildren().setAll(tableView); // remplace le contenu
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur: conteneur parent introuvable.");
            }

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur lors du retour à la liste des ateliers.");
        }
    }

    public void setAtelier(Ateliers atelier) {
        this.atelierEnEdition = atelier;
        titreField.setText(atelier.getTitre());
        categorieField.setValue(atelier.getCategorie());
        descriptionField.setText(atelier.getDescription());
        niveauDiffField.setValue(atelier.getNiveau_diff());
        prixField.setText(String.valueOf(atelier.getPrix()));
        datePicker.setValue(atelier.getDatecours().toLocalDate());

        // Formatage de l'heure pour le champ heureField
        LocalDateTime dateHeure = atelier.getDatecours(); // Assurez-vous que l'atelier contient un LocalDateTime
        String heureStr = dateHeure.format(DateTimeFormatter.ofPattern("HH:mm")); // Format "HH:mm"
        heureField.setText(heureStr);

        dureeField.setText(String.valueOf(atelier.getDuree()));
        lienField.setText(atelier.getLien());
    }


}
