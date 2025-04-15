package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.User;
import service.UserService;
import utils.PasswordUtils;
import javafx.scene.shape.Circle;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class ProfilController {

    @FXML private TextField tfNom;
    @FXML private TextField tfPrenom;
    @FXML private TextField tfEmail;
    @FXML private PasswordField tfOldPassword;
    @FXML private PasswordField tfNewPassword;

    @FXML private ImageView photoProfil;
    @FXML private ImageView editIcon;
    private boolean photoModifiee = false;
    @FXML private Label labelNomPrenom;
    private User user;
    private final UserService userService = new UserService();

    public void setUser(User user) {
        this.user = user;
        labelNomPrenom.setText(user.getPrenom() + " " + user.getNom());
        tfNom.setText(user.getNom());
        tfPrenom.setText(user.getPrenom());
        tfEmail.setText(user.getEmail());

        try {
            String path = "/assets/users/" + user.getPhoto();
            photoProfil.setImage(new Image(getClass().getResourceAsStream(path)));
        } catch (Exception e) {
            photoProfil.setImage(new Image(getClass().getResourceAsStream("/assets/userf.png")));
        }
        Circle clip = new Circle(photoProfil.getFitWidth() / 2, photoProfil.getFitHeight() / 2, photoProfil.getFitWidth() / 2);
        photoProfil.setClip(clip);
        editIcon.setImage(new Image(getClass().getResourceAsStream("/assets/icons/modifier.png")));
        editIcon.setOnMouseClicked(event -> handleBrowsePhoto()); // il manquait ici

    }

    @FXML
    private void handleSave() {
        if (tfNom.getText().isEmpty() || tfPrenom.getText().isEmpty() || tfEmail.getText().isEmpty()) {
            showAlert("Champs manquants", "Veuillez remplir tous les champs obligatoires.");
            return;
        }

        boolean isModified = false;

        if (!user.getNom().equals(tfNom.getText())) {
            user.setNom(tfNom.getText());
            isModified = true;
        }

        if (!user.getPrenom().equals(tfPrenom.getText())) {
            user.setPrenom(tfPrenom.getText());
            isModified = true;
        }

        if (!user.getEmail().equals(tfEmail.getText())) {
            user.setEmail(tfEmail.getText());
            isModified = true;

        }

        if (photoModifiee) {
            isModified = true;
        }

        // Mot de passe
        String oldPass = tfOldPassword.getText();
        String newPass = tfNewPassword.getText();

        if (!newPass.isEmpty()) {
            if (oldPass.isEmpty()) {
                showAlert("Erreur", "Veuillez saisir l'ancien mot de passe.");
                return;
            }

            if (!PasswordUtils.checkPassword(oldPass, user.getPassword())) {
                showAlert("Erreur", "Mot de passe actuel incorrect.");
                return;
            }

            if (!isValidPassword(newPass)) {
                showAlert("Erreur", "Le nouveau mot de passe doit contenir au moins 6 caractères, une lettre majuscule et un chiffre.");
                return;
            }

            user.setPassword(PasswordUtils.hashPassword(newPass));
            isModified = true;

        }

        if (!isModified) {
            showAlert("ℹ️ Aucun changement", "Aucune modification détectée.");
            return;
        }

        // ✅ Confirmation de l'enregistrement
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Enregistrer les modifications ?");
        confirm.setContentText("Êtes-vous sûr de vouloir sauvegarder les modifications de votre profil ?");

        ButtonType oui = new ButtonType("Oui", ButtonBar.ButtonData.YES);
        ButtonType non = new ButtonType("Non", ButtonBar.ButtonData.NO);
        confirm.getButtonTypes().setAll(oui, non);

        confirm.showAndWait().ifPresent(response -> {
            if (response == oui) {
                System.out.println("🔍 Données de l'utilisateur à enregistrer :");
                System.out.println("ID        : " + user.getId());
                System.out.println("Nom       : " + user.getNom());
                System.out.println("Prénom    : " + user.getPrenom());
                System.out.println("Email     : " + user.getEmail());
                System.out.println("Photo     : " + user.getPhoto());
                System.out.println("Password  : " + user.getPassword());

                userService.updateUserWithPassword(user);
                showAlert("✅ Succès", "Profil mis à jour avec succès.");
            } else {
                System.out.println("❌ Modification annulée par l'utilisateur.");
            }
        });
    }

    @FXML
    private void handleBrowsePhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une nouvelle photo");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );

        File selectedFile = fileChooser.showOpenDialog(photoProfil.getScene().getWindow());
        if (selectedFile != null) {
            try {
                String fileName = selectedFile.getName();
                File destination = new File("src/main/resources/assets/users/" + fileName);
                destination.getParentFile().mkdirs();

                Files.copy(selectedFile.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);

                // Mise à jour dans l'objet user
                user.setPhoto(fileName);
                photoProfil.setImage(new Image(getClass().getResourceAsStream("/assets/users/" + fileName)));
                photoModifiee = true; // 👉 indique que la photo a été modifiée

                System.out.println("✅ Photo de profil mise à jour : " + fileName);
            } catch (Exception e) {
                System.out.println("❌ Erreur lors de la copie : " + e.getMessage());
                showAlert("Erreur", "Impossible de copier la photo.");
            }
        }
    }

    private void showAlert(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) tfNom.getScene().getWindow();
        stage.close();
    }
    private boolean isValidPassword(String password) {
        return password.length() >= 6 &&
                password.matches(".*[A-Z].*") &&    // au moins une majuscule
                password.matches(".*\\d.*");        // au moins un chiffre
    }

}
