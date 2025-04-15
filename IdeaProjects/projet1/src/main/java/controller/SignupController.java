package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import model.User;
import service.UserService;
import utils.PasswordUtils;

import java.io.IOException;
import java.util.regex.Pattern;

public class SignupController {

    @FXML private TextField txtNom;
    @FXML private TextField txtPrenom;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private PasswordField txtConfirmPassword;
    @FXML private Button btnRegister;
    @FXML private ImageView logoImage;
    @FXML private ImageView googleIcon;
    @FXML private Hyperlink linkLogin;

    private final UserService userService = new UserService();

    @FXML
    public void initialize() {
        logoImage.setImage(new Image(getClass().getResourceAsStream("/logo1.png")));
        googleIcon.setImage(new Image(getClass().getResourceAsStream("/google.png")));

        btnRegister.setOnAction(event -> handleRegister());
        linkLogin.setOnAction(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
                Parent loginRoot = loader.load();
                Stage stage = (Stage) linkLogin.getScene().getWindow();
                stage.setScene(new Scene(loginRoot));
                stage.show();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

    }

    private void handleRegister() {
        String nom = txtNom.getText().trim();
        String prenom = txtPrenom.getText().trim();
        String email = txtEmail.getText().trim();
        String password = txtPassword.getText();
        String confirmPassword = txtConfirmPassword.getText();

        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "⚠️ Tous les champs sont obligatoires !");
            return;
        }

        if (!isValidEmail(email)) {
            showAlert(Alert.AlertType.ERROR, "⚠️ L'email n'est pas valide !");
            return;
        }

        if (!isValidName(nom) || !isValidName(prenom)) {
            showAlert(Alert.AlertType.ERROR, "⚠️ Le nom et le prénom doivent contenir uniquement des lettres.");
            return;
        }

        if (userService.emailExists(email)) {
            showAlert(Alert.AlertType.ERROR, "⚠️ Cet email est déjà utilisé !");
            return;
        }

        if (!isValidPassword(password)) {
            showAlert(Alert.AlertType.ERROR, "⚠️ Le mot de passe doit contenir au moins 6 caractères, une lettre majuscule et un chiffre !");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showAlert(Alert.AlertType.ERROR, "⚠️ Les mots de passe ne correspondent pas !");
            return;
        }

        // Hachage et création de l'utilisateur
        String hashedPassword = PasswordUtils.hashPassword(password);
        User user = new User(nom, prenom, email, hashedPassword);

        userService.register(user);

        try {
            // Charger front.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/front.fxml"));
            Parent frontRoot = loader.load();

            // Passer l'utilisateur au frontController
            FrontClientController frontController = loader.getController();
            frontController.initialize(user); // ⚠️ Méthode à créer dans FrontController

            // Afficher la scène
            Stage stage = (Stage) btnRegister.getScene().getWindow();
            stage.setScene(new Scene(frontRoot));
            stage.setTitle("Edayetna - Bienvenue");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "❌ Erreur lors du chargement de l'accueil !");
        }
    }


    // ========== VALIDATEURS ==========

    private boolean isValidEmail(String email) {
        String regex = "^[\\w.-]+@[\\w.-]+\\.\\w{2,}$";
        return Pattern.matches(regex, email);
    }

    private boolean isValidPassword(String password) {
        return password.length() >= 6 &&
                password.matches(".*[A-Z].*") && // Majuscule
                password.matches(".*\\d.*");    // Chiffre
    }

    private boolean isValidName(String input) {
        return input.matches("^[\\p{L} '-]+$"); // Lettres, espaces, apostrophes, tirets
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }

}
