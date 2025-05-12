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
import service.HistoriqueConnexionService;
import service.UserService;
import utils.PasswordUtils;

import java.io.IOException;
import java.util.regex.Pattern;

public class SignupController {

    @FXML public Button btnGoogle;
    @FXML private TextField txtNom;
    @FXML private TextField txtPrenom;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private PasswordField txtConfirmPassword;
    @FXML private Button btnRegister;
    @FXML private ImageView logoImage;
    @FXML private ImageView googleIcon;
    @FXML private Hyperlink linkLogin;
    @FXML private ProgressBar passwordStrengthBar;
    @FXML private Label passwordStrengthLabel;

    private final UserService userService = new UserService();

    @FXML
    public void initialize() {
        logoImage.setImage(new Image(getClass().getResourceAsStream("/assets/logo1.png")));
        googleIcon.setImage(new Image(getClass().getResourceAsStream("/google.png")));

        btnRegister.setOnAction(event -> handleRegister());
        btnGoogle.setOnAction(event -> handleGoogleSignup());
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
        // 🔥 Ajout du listener de force du mot de passe
        //  change
        txtPassword.textProperty().addListener((obs, oldText, newText) -> {
            updatePasswordStrength(newText);
        });
    }

    //change
    private void updatePasswordStrength(String password) {
        if (password.isEmpty()) {
            passwordStrengthBar.setVisible(false);
            passwordStrengthLabel.setVisible(false);
            return;
        }

        passwordStrengthBar.setVisible(true);
        passwordStrengthLabel.setVisible(true);

        int strength = 0;

        if (password.length() >= 6) strength++;
        if (password.matches(".*[A-Z].*")) strength++;
        if (password.matches(".*[a-z].*")) strength++;
        if (password.matches(".*\\d.*")) strength++;

        double progress = strength / 4.0;
        passwordStrengthBar.setProgress(Math.min(progress, 1.0));

        if (strength <= 1) {
            passwordStrengthBar.setStyle("-fx-accent: red;");
            passwordStrengthLabel.setText("faible");
            passwordStrengthLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        } else if (strength == 2 || strength == 3) {
            passwordStrengthBar.setStyle("-fx-accent: orange;");
            passwordStrengthLabel.setText("moyen");
            passwordStrengthLabel.setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
        } else if (strength == 4) {
            passwordStrengthBar.setStyle("-fx-accent: green;");
            passwordStrengthLabel.setText("fort");
            passwordStrengthLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
        }
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
        // Récupérer l'utilisateur complet avec ID, puis enregistrer dans login_history
        User registeredUser = userService.getUserByEmail(email);
        new HistoriqueConnexionService().enregistrerConnexion(registeredUser);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/front.fxml"));
            Parent frontRoot = loader.load();

// ✅ Transmettre l'utilisateur connecté
            FrontClientController frontController = loader.getController();
            frontController.setConnectedUser(registeredUser);

// ✅ NE PAS appeler initialize() manuellement (JavaFX le fait)

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
    //change
    private boolean isValidPassword(String password) {
        return password.length() >= 6 &&
                password.matches(".*[A-Z].*") &&
                password.matches(".*[a-z].*") &&
                password.matches(".*\\d.*");
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
