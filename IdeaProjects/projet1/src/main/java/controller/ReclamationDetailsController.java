package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import model.Reclamation;
import service.ReclamationService;
import service.ReponseService;

import java.sql.SQLException;
import java.text.Normalizer;
import java.util.Optional;

public class ReclamationDetailsController {
    @FXML private Label idLabel;
    @FXML private Label titreLabel;
    @FXML private TextArea descriptionArea;
    @FXML private Label statutLabel;
    @FXML private Label dateLabel;
    @FXML private TextArea reponseArea;
    @FXML private HBox actionButtonsBox;
    @FXML private Button modifierButton;
    @FXML private Button supprimerButton;
    @FXML private Button pdfButton;

    private Runnable refreshCallback;
    private Reclamation reclamation;
    private final ReclamationService reclamationService = new ReclamationService();

    public void initialize() {
        modifierButton.setOnAction(event -> handleModifier());
        supprimerButton.setOnAction(event -> handleSupprimer());
    }

    public void setReclamation(Reclamation reclamation) {
        this.reclamation = reclamation;
        updateUI();
    }

    public void setRefreshCallback(Runnable callback) {
        this.refreshCallback = callback;
    }

    private void updateUI() {
        if (reclamation == null) {
            System.out.println("Aucune réclamation à afficher.");
            return;
        }

        idLabel.setText(String.valueOf(reclamation.getId()));
        titreLabel.setText(reclamation.getTitre());
        descriptionArea.setText(reclamation.getDescription());
        statutLabel.setText(reclamation.getStatut());
        dateLabel.setText(reclamation.getDateReclamation().toString());

        // Masquer tous les boutons par défaut
        modifierButton.setVisible(false);
        supprimerButton.setVisible(false);
        pdfButton.setVisible(false);

        String statut = reclamation.getStatut().trim().toLowerCase();

        try {
            // Gestion des statuts
            if (statut.equals("en attente")) {
                modifierButton.setVisible(true);
                supprimerButton.setVisible(true);
                reponseArea.setText("Pas encore répondue");
            }
            else if (statut.equals("en cours") || statut.equals("encours")) {
                reponseArea.setText("Pas encore répondue");
            }
            else if (statut.equals("répondu") || statut.equals("repondu") ||
                    statut.equals("répondue") || statut.equals("repondue")) {
                pdfButton.setVisible(true);
                ReponseService reponseService = new ReponseService();
                String reponse = reponseService.getReponseByReclamationId(reclamation.getId());
                reponseArea.setText(reponse != null && !reponse.trim().isEmpty() ? reponse : "Pas encore répondue");
            }
            else {
                reponseArea.setText("Statut inconnu : " + reclamation.getStatut());
            }
        } catch (Exception e) {
            e.printStackTrace();
            reponseArea.setText("Erreur lors du chargement de la réponse");
        }
    }


    @FXML
    private void handleModifier() {
        titreLabel.setVisible(false);
        descriptionArea.setEditable(true);

        TextField titreField = new TextField(reclamation.getTitre());
        titreField.setStyle("-fx-font-size: 14px; -fx-padding: 5px;");
        GridPane.setConstraints(titreField, 1, 1);

        GridPane gridPane = (GridPane) titreLabel.getParent();
        gridPane.getChildren().remove(titreLabel);
        gridPane.getChildren().add(titreField);

        modifierButton.setText("Enregistrer");
        modifierButton.setOnAction(e -> handleSaveModifications(titreField));
        supprimerButton.setText("Annuler");
        supprimerButton.setOnAction(e -> cancelModification(gridPane));
    }

    private void handleSaveModifications(TextField titreField) {
        String nouveauTitre = titreField.getText().trim();
        String nouvelleDescription = descriptionArea.getText().trim();

        if (nouveauTitre.isEmpty() || nouvelleDescription.isEmpty()) {
            showAlert("Erreur", "Le titre et la description ne peuvent pas être vides");
            return;
        }

        try {
            boolean success = reclamationService.updateReclamation(
                    reclamation.getId(),
                    nouveauTitre,
                    nouvelleDescription
            );

            if (success) {
                reclamation.setTitre(nouveauTitre);
                reclamation.setDescription(nouvelleDescription);

                GridPane gridPane = (GridPane) titreField.getParent();
                gridPane.getChildren().remove(titreField);
                gridPane.getChildren().add(titreLabel);
                titreLabel.setText(nouveauTitre);
                descriptionArea.setText(nouvelleDescription);

                titreLabel.setVisible(true);
                descriptionArea.setEditable(false);

                modifierButton.setText("Modifier");
                modifierButton.setOnAction(e -> handleModifier());
                supprimerButton.setText("Supprimer");
                supprimerButton.setOnAction(e -> handleSupprimer());

                if (refreshCallback != null) {
                    refreshCallback.run();
                }

                showAlert("Succès", "Réclamation modifiée avec succès");
            } else {
                showAlert("Erreur", "Échec de la mise à jour");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Une erreur est survenue lors de la mise à jour : " + e.getMessage());
        }
    }

    private void cancelModification(GridPane gridPane) {
        resetToViewMode(gridPane);
        descriptionArea.setText(reclamation.getDescription());
    }

    private void resetToViewMode(GridPane gridPane) {
        TextField titreField = (TextField) gridPane.getChildren().stream()
                .filter(node -> {
                    Integer col = GridPane.getColumnIndex(node);
                    Integer row = GridPane.getRowIndex(node);
                    return (col != null && col == 1) && (row != null && row == 1) && node instanceof TextField;
                })
                .findFirst()
                .orElse(null);

        if (titreField != null) {
            gridPane.getChildren().remove(titreField);
        }

        gridPane.getChildren().add(titreLabel);
        titreLabel.setVisible(true);
        descriptionArea.setEditable(false);

        modifierButton.setText("Modifier");
        modifierButton.setOnAction(e -> handleModifier());
        supprimerButton.setText("Supprimer");
        supprimerButton.setOnAction(e -> handleSupprimer());
    }

    @FXML
    private void handleSupprimer() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer cette réclamation ?");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cette réclamation ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                boolean deleted = reclamationService.deleteReclamation(reclamation.getId());

                if (deleted) {
                    showAlert("Succès", "Réclamation supprimée avec succès");
                    if (refreshCallback != null) {
                        refreshCallback.run(); // Rafraîchir la liste
                    }
                    ((Stage) supprimerButton.getScene().getWindow()).close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Erreur", "Erreur lors de la suppression : " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleGeneratePDF() {
        System.out.println("Génération PDF pour la réclamation: " + reclamation.getId());
    }

    @FXML
    private void handleBack() {
        titreLabel.getScene().getWindow().hide();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(
                title.equals("Erreur") ? Alert.AlertType.ERROR :
                        title.equals("Succès") ? Alert.AlertType.INFORMATION : Alert.AlertType.INFORMATION
        );
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
