package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.User;
import service.UserService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class EditUserController {

    @FXML private TextField tfNom;
    @FXML private TextField tfPrenom;
    @FXML private TextField tfEmail;
    @FXML private ComboBox<String> comboRole;
    @FXML private ComboBox<String> comboStatut;
    @FXML private TextField tfPhoto;

    private User user;
    private final UserService userService = new UserService();

    // Initialisation appelée automatiquement après chargement FXML
    @FXML
    private void initialize() {
        comboRole.getItems().addAll("admin", "artisan", "client");
        comboStatut.getItems().addAll("active", "blocked");
    }

    // Reçoit l'utilisateur sélectionné depuis le controller parent
    public void setUser(User user) {
        this.user = user;

        tfNom.setText(user.getNom());
        tfPrenom.setText(user.getPrenom());
        tfEmail.setText(user.getEmail());
        tfPhoto.setText(user.getPhoto());

        String simpleRole;
        if (user.getRoles().contains("ADMIN")) {
            simpleRole = "admin";
        } else if (user.getRoles().contains("ARTISAN")) {
            simpleRole = "artisan";
        } else {
            simpleRole = "client";
        }
        comboRole.setValue(simpleRole);
        comboStatut.setValue(user.getStatut());
    }

    // Méthode appelée au clic sur "Enregistrer"
    @FXML
    private void handleSave() {
        if (tfNom.getText().isEmpty() || tfPrenom.getText().isEmpty() || tfEmail.getText().isEmpty()
                || comboRole.getValue() == null || comboStatut.getValue() == null) {
            showAlert("Champs manquants", "Veuillez remplir tous les champs.");
            return;
        }

        user.setNom(tfNom.getText());
        user.setPrenom(tfPrenom.getText());
        user.setEmail(tfEmail.getText());
        user.setStatut(comboStatut.getValue());
        user.setPhoto(tfPhoto.getText());

        // Formater rôle selon format BD
        String formattedRole = "[\"ROLE_" + comboRole.getValue().toUpperCase() + "\"]";

        System.out.println("Rôle final envoyé : " + formattedRole);

        user.setRoles(formattedRole);

        userService.updateUser(user);

        showAlert("✅ Succès", "L'utilisateur a été modifié avec succès.");
        System.out.println("Rôle final envoyé : " + formattedRole);

        // Fermer la fenêtre
        Stage stage = (Stage) tfNom.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleBrowsePhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );

        File selectedFile = fileChooser.showOpenDialog(tfPhoto.getScene().getWindow());

        if (selectedFile != null) {
            try {
                String fileName = selectedFile.getName();
                tfPhoto.setText(fileName); // affiche juste le nom

                // Destination : dossier assets/users dans le projet
                File destination = new File("src/main/resources/assets/users/" + fileName);

                // Créer le dossier s’il n’existe pas
                destination.getParentFile().mkdirs();

                // Copier l'image sélectionnée dans le dossier projet
                java.nio.file.Files.copy(
                        selectedFile.toPath(),
                        destination.toPath(),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING
                );

                System.out.println("✅ Image copiée dans /assets/users/" + fileName);
            } catch (Exception e) {
                System.out.println("❌ Erreur copie image : " + e.getMessage());
                showAlert("Erreur", "Impossible de copier l'image.");
            }
        }
    }

}
