package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import service.UserService;
import utils.PasswordUtils;

import java.io.IOException;

public class ResetPasswordController {
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label errorLabel;

    private String email;
    private final UserService userService = new UserService();

    public void setEmail(String email) {
        this.email = email;
    }

    @FXML
    private void handleResetPassword() {
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (password.isEmpty() || confirmPassword.isEmpty()) {
            showError("Veuillez remplir tous les champs");
            return;
        }

        if (!isPasswordValid(password)) {
            showError("Le mot de passe doit contenir au moins 6 caractères, une majuscule et un chiffre");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Les mots de passe ne correspondent pas");
            return;
        }

        try {
            String hashedPassword = PasswordUtils.hashPassword(password);
            userService.updatePasswordByEmail(email, hashedPassword);
            returnToLogin();
        } catch (Exception e) {
            showError("Erreur lors de la mise à jour du mot de passe");
        }
    }

    private boolean isPasswordValid(String password) {
        return password.length() >= 6 &&
                password.matches(".*[A-Z].*") &&
                password.matches(".*\\d.*");
    }

    @FXML
    private void handleBack() {
        returnToLogin();
    }

    private void returnToLogin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/login.fxml"));
            Stage stage = (Stage) passwordField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showError("Erreur lors du retour à la page de connexion");
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
} 