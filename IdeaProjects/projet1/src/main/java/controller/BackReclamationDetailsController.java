package controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.Reclamation;
import service.ReclamationService;
import service.ReponseService;
import service.TranslationService;

import java.sql.SQLException;

public class BackReclamationDetailsController {
    @FXML private Label idLabel;
    @FXML private Label titreLabel;
    @FXML private TextArea descriptionArea;
    @FXML private Label statutLabel;
    @FXML private Label dateLabel;
    @FXML private TextArea reponseArea;
    @FXML private CheckBox finaleCheckbox;
    @FXML private Button actionButton;
    @FXML private Button retourButton;

    @FXML private MenuButton translateMenu;
    private final TranslationService translationService = new TranslationService();

    private Reclamation reclamation;
    private final ReclamationService reclamationService = new ReclamationService();
    private final ReponseService reponseService = new ReponseService();

    public void setReclamation(Reclamation reclamation) {
        this.reclamation = reclamation;
        loadReponseExistante();
        updateUI();
    }

    private void loadReponseExistante() {
        try {
            String reponse = reponseService.getReponseByReclamationId(reclamation.getId());
            if (reponse != null) {
                reponseArea.setText(reponse);
                finaleCheckbox.setSelected(reponseService.isReponseFinale(reclamation.getId()));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateUI() {
        idLabel.setText(String.valueOf(reclamation.getId()));
        titreLabel.setText(reclamation.getTitre());
        descriptionArea.setText(reclamation.getDescription());
        statutLabel.setText(reclamation.getStatut());
        dateLabel.setText(reclamation.getDateReclamation().toString());

        configureTranslationMenu();

        switch (reclamation.getStatut()) {
            case "En attente":
                configureForEnAttente();
                break;
            case "En cours":
                configureForEnCours();
                break;
            case "Répondu":
                configureForRepondu();
                break;
        }
    }

    private void configureForEnAttente() {
        actionButton.setText("Répondre");
        actionButton.setOnAction(e -> handleReponse());
        actionButton.setVisible(true);
        reponseArea.setEditable(true);
        finaleCheckbox.setVisible(true);
    }

    private void configureForEnCours() {
        actionButton.setText("Modifier");
        actionButton.setOnAction(e -> handleReponse());
        actionButton.setVisible(true);
        reponseArea.setEditable(true);
        finaleCheckbox.setVisible(true);
    }

    private void configureForRepondu() {
        actionButton.setVisible(false); // Cache le bouton d'action
        reponseArea.setEditable(false);
        finaleCheckbox.setVisible(false);
    }

    private void handleReponse() {
        String texteReponse = reponseArea.getText();
        if (texteReponse.isEmpty()) {
            showAlert("Erreur", "Veuillez saisir une réponse");
            return;
        }

        try {
            boolean estFinale = finaleCheckbox.isSelected();
            boolean success = reponseService.enregistrerReponse(
                    reclamation.getId(),
                    texteReponse,
                    estFinale
            );

            if (success) {
                String nouveauStatut = estFinale ? "Répondu" : "En cours";
                reclamationService.updateStatut(reclamation.getId(), nouveauStatut);
                showAlert("Succès", "Réponse enregistrée avec succès");
                closeWindow();
            }
        } catch (Exception e) {
            showAlert("Erreur", "Erreur technique: " + e.getMessage());
        }
    }

    @FXML
    private void handleRetour() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) retourButton.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    //traduction

    private void configureTranslationMenu() {
        translateMenu.getItems().clear();

        // Langues disponibles avec leurs codes
        String[][] languages = {
                {"Français", "fr"},
                {"Anglais", "en"},
                {"Espagnol", "es"},
                {"Allemand", "de"},
                {"Arabe", "ar"},
                {"Italien", "it"},
                {"Portugais", "pt"}
        };

        for (String[] lang : languages) {
            MenuItem item = new MenuItem(lang[0]);
            String targetLanguage = lang[1];
            item.setOnAction(e -> {
                showLoadingAlert();
                new Thread(() -> {
                    String translatedText = translationService.translateText(
                            reclamation.getDescription(),
                            targetLanguage
                    );
                    // Met à jour l'UI sur le thread JavaFX
                    javafx.application.Platform.runLater(() -> {
                        descriptionArea.setText(translatedText);
                        closeLoadingAlert();
                    });
                }).start();
            });
            translateMenu.getItems().add(item);
        }
    }

    private Alert loadingAlert;

    private void showLoadingAlert() {
        javafx.application.Platform.runLater(() -> {
            loadingAlert = new Alert(Alert.AlertType.INFORMATION);
            loadingAlert.setTitle("Traduction en cours");
            loadingAlert.setHeaderText(null);
            loadingAlert.setContentText("Veuillez patienter pendant la traduction...");
            loadingAlert.show();
        });
    }

    private void closeLoadingAlert() {
        javafx.application.Platform.runLater(() -> {
            if (loadingAlert != null) {
                loadingAlert.close();
            }
        });
    }

}