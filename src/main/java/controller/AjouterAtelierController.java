package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
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
    private javafx.scene.layout.Pane parentContainer;
    private AteliersController ateliersController;
    private DashboardArtisanController dashboardController;// Référence vers le Dashboard
    private BorderPane root;


    public void setDashboardController(DashboardArtisanController dashboardController) {
        this.dashboardController = dashboardController;
    }


    public void setRoot(BorderPane root) {
        this.root = root;
    }

    public void setAteliersController(AteliersController controller) {
        this.ateliersController = controller;
    }
// Référence au contrôleur principal




    public void setParentContainer(javafx.scene.layout.Pane container) {
        this.parentContainer = container;
    }

    @FXML
    private void initialize() {
        categorieField.getItems().addAll("Poterie", "Peinture", "Broderie", "Tricot", "Couture", "Bijoux Faits Main");
        niveauDiffField.getItems().addAll("Débutant", "Intermédiaire", "Avancé");
    }

    @FXML
    private void handleAjouterAtelier() {
        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Utilisateur non connecté.");
            return;
        }

        try {
            // Récupération des valeurs
            String titre = titreField.getText().trim();
            String categorie = categorieField.getValue();
            String description = descriptionField.getText().trim();
            String niveau = niveauDiffField.getValue();
            String lien = lienField.getText().trim();

            // Validation des champs obligatoires
            if (titre.isEmpty() || categorie == null || description.isEmpty() || niveau == null
                    || prixField.getText().isEmpty() || datePicker.getValue() == null
                    || heureField.getText().isEmpty() || dureeField.getText().isEmpty() || lien.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Veuillez remplir tous les champs.");
                return;
            }

            // Conversion des valeurs numériques
            double prix = Double.parseDouble(prixField.getText());
            int duree = Integer.parseInt(dureeField.getText());

            // Construction de la date/heure
            String dateStr = datePicker.getValue().toString();
            String heureStr = heureField.getText(); // format "HH:mm"
            String dateHeureStr = dateStr + " " + heureStr;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            LocalDateTime dateHeure = LocalDateTime.parse(dateHeureStr, formatter);

            // VALIDATIONS
            if (!validateDescription(description)) return;
            if (!validateDate(dateHeure)) return;
            if (!validateDuree(duree)) return;
            if (!validateLien(lien)) return;

            // Vérification de l'unicité du titre
            if (atelierEnEdition == null) {
                // Cas de création
                if (ateliersService.titreExists(titre)) {
                    showAlert(Alert.AlertType.ERROR, "Ce titre d'atelier existe déjà. Veuillez en choisir un autre.");
                    return;
                }
            } else {
                // Cas de modification : on vérifie uniquement si le titre a changé
                if (!titre.equals(atelierEnEdition.getTitre()) && ateliersService.titreExists(titre)) {
                    showAlert(Alert.AlertType.ERROR, "Ce titre d'atelier existe déjà. Veuillez en choisir un autre.");
                    return;
                }
            }


            // Création ou modification de l'atelier
            if (atelierEnEdition != null) {
                // Mode modification
                atelierEnEdition.setTitre(titre);
                atelierEnEdition.setCategorie(categorie);
                atelierEnEdition.setDescription(description);
                atelierEnEdition.setNiveau_diff(niveau);
                atelierEnEdition.setPrix(prix);
                atelierEnEdition.setDatecours(dateHeure);
                atelierEnEdition.setDuree(duree);
                atelierEnEdition.setLien(lien);

                ateliersService.modifierAtelier(atelierEnEdition);
                showAlert(Alert.AlertType.INFORMATION, "Atelier modifié avec succès !");
            } else {
                // Mode création
                Ateliers atelier = new Ateliers();
                atelier.setUser(currentUser.getId());
                atelier.setTitre(titre);
                atelier.setCategorie(categorie);
                atelier.setDescription(description);
                atelier.setNiveau_diff(niveau);
                atelier.setPrix(prix);
                atelier.setDatecours(dateHeure);
                atelier.setDuree(duree);
                atelier.setLien(lien);

                ateliersService.ajouterAtelier(atelier);
                showAlert(Alert.AlertType.INFORMATION, "Atelier ajouté avec succès !");
            }

            handleRetourListe();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Veuillez entrer des valeurs numériques valides pour le prix et la durée.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur : " + e.getMessage());
            e.printStackTrace();
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
        if (root != null && ateliersController != null) {
            // 🔥 Remettre la vue des ateliers
            root.setCenter(ateliersController.getMainContent());

            // 🔥 Réafficher le top (titre + recherche)
            ateliersController.getTopContainer().setVisible(true);
            ateliersController.getTopContainer().setManaged(true);

            // 🔥 Réafficher la pagination
            ateliersController.getBottomContainer().setVisible(true);
            ateliersController.getBottomContainer().setManaged(true);

            // 🔥 Recharger la page courante
            ateliersController.loadAteliersPage(ateliersController.getCurrentPage(), ateliersController.getUserId());

        } else {
            showAlert(Alert.AlertType.ERROR, "Erreur: Impossible de revenir à la liste.");
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

    private boolean validateDescription(String description) {
        if (description.length() < 10) {
            showAlert(Alert.AlertType.ERROR, "La description doit contenir au moins 10 caractères.");
            return false;
        }
        return true;
    }

    private boolean validateDate(LocalDateTime dateHeure) {
        if (dateHeure.isBefore(LocalDateTime.now())) {
            showAlert(Alert.AlertType.ERROR, "La date du cours doit être ultérieure à aujourd'hui.");
            return false;
        }
        return true;
    }

    private boolean validateDuree(int duree) {
        if (duree < 30) {
            showAlert(Alert.AlertType.ERROR, "La durée doit être un nombre positif en minutes.");
            return false;
        }
        return true;
    }

    private boolean validateLien(String lien) {
        if (!lien.matches("^https://meet\\.google\\.com/[a-zA-Z0-9-]{3,}$")) {
            showAlert(Alert.AlertType.ERROR, "Le lien doit être sous la forme: https://meet.google.com/xxx");
            return false;
        }
        return true;
    }

}
