package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import model.User;
import service.HistoriqueConnexionService;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import service.UserService;
import service.GoogleAuthService;
import utils.SessionManager;

public class LoginController {

    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private Button btnLogin;
    @FXML private ImageView logoImage;
    @FXML private ImageView googleIcon;
    @FXML private ImageView faceIcon;
    @FXML private Hyperlink linkSignup;
    @FXML private Button btnFaceID;
    @FXML private Button btnGoogle;


    @FXML
    public void initialize() {
        // Chargement des images
        logoImage.setImage(new Image(getClass().getResourceAsStream("/assets/logo1.png")));
        googleIcon.setImage(new Image(getClass().getResourceAsStream("/google.png")));
        faceIcon.setImage(new Image(getClass().getResourceAsStream("/face-id.png")));
        btnFaceID.setOnAction(e -> handleFaceIDLogin());
        btnGoogle.setOnAction(e -> handleGoogleLogin());

        btnLogin.setOnAction(e -> handleLogin());
        linkSignup.setOnAction(e -> openSignupPage());

    }



    @FXML
    private void handleFaceIDLogin() {
        try {
            // Chemin absolu vers le script face_id_check.py
            String scriptPath = new File("src/main/resources/face_id_check.py").getAbsolutePath();

            // Commande : python script
            ProcessBuilder pb = new ProcessBuilder("python", scriptPath);
            pb.redirectErrorStream(true); // Combine stdout et stderr

            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String result = reader.readLine(); // Lire la 1ère ligne

            int exitCode = process.waitFor(); // attendre fin exécution
            System.out.println("FaceID result: " + result + " | Exit code: " + exitCode);

            if ("MATCH".equals(result)) {
                String adminEmail = "nouhahbhbn@gmail.com";
                UserService userService = new UserService();
                User user = userService.getUserByEmail(adminEmail);

                if (user != null && user.getRoles().contains("ROLE_ADMIN")) {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/dashboard.fxml"));
                    Parent root = loader.load();
                    DashboardController controller = loader.getController();
                    controller.initialize(user);
                    Stage stage = (Stage) txtEmail.getScene().getWindow();
                    stage.setScene(new Scene(root));
                    stage.setTitle("Dashboard Admin (Face ID)");
                    stage.show();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Utilisateur admin introuvable.");
                }
            } else if ("NO_MATCH".equals(result)) {
                showAlert(Alert.AlertType.WARNING, "Visage non reconnu.");
            } else if ("NO_FACE_DETECTED".equals(result)) {
                showAlert(Alert.AlertType.WARNING, "Aucun visage détecté.");
            } else if ("CAMERA_ERROR".equals(result)) {
                showAlert(Alert.AlertType.ERROR, "Erreur lors de l'accès à la caméra.");
            } else if ("REFERENCE_ERROR".equals(result)) {
                showAlert(Alert.AlertType.ERROR, "Erreur : image de référence introuvable ou invalide.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur de traitement Face ID.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur lors de l'exécution du script Face ID.");
        }
    }

    @FXML
    private void handleGoogleLogin() {
        try {
            GoogleAuthService googleAuth = new GoogleAuthService();
            User user = googleAuth.authenticateWithGoogle();

            // 🔒 Vérification si l'utilisateur est null
            if (user == null) {
                showAlert(Alert.AlertType.ERROR, "❌ Échec de l'authentification Google.");
                return;
            }

            // 🔒 Vérification du statut
            if ("blocked".equalsIgnoreCase(user.getStatut())) {
                showAlert(Alert.AlertType.ERROR, "❌ Ce compte est bloqué. Veuillez contacter l'administration.");
                return;
            }
            // AJOUT: Enregistrement de l'utilisateur dans le SessionManager
            SessionManager.getInstance().setCurrentUser(user);
            // 🔐 Historique de connexion
            new HistoriqueConnexionService().enregistrerConnexion(user);

            // 🎯 Vérification des rôles
            String roles = user.getRoles();
            Stage stage = (Stage) btnGoogle.getScene().getWindow();
            FXMLLoader loader;
            Parent root;

            if (roles.contains("ROLE_ADMIN")) {
                loader = new FXMLLoader(getClass().getResource("/dashboard.fxml"));
                root = loader.load();
                DashboardController controller = loader.getController();
                controller.initialize(user);
                stage.setTitle("Dashboard Admin");

            } else if (roles.contains("ROLE_ARTISAN")) {
                loader = new FXMLLoader(getClass().getResource("/dashboardArtisan.fxml"));
                root = loader.load();
                DashboardArtisanController controller = loader.getController();
                controller.initialize(user);
                stage.setTitle("Espace Artisan");

            } else {
                loader = new FXMLLoader(getClass().getResource("/front.fxml"));
                root = loader.load();
                FrontClientController controller = loader.getController();
                controller.setConnectedUser(user);
                stage.setTitle("Espace Client");
            }

            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur lors de l'authentification Google.");
        }
    }


    private void openSignupPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/signup.fxml"));
            StackPane signupRoot = loader.load();
            Scene scene = new Scene(signupRoot);
            Stage stage = (Stage) linkSignup.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void handleLogin() {
        String email = txtEmail.getText().trim();
        String password = txtPassword.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Tous les champs sont requis !");
            return;
        }

        UserService userService = new UserService();
        User user = userService.login(email, password);

        if (user == null) {
            showAlert(Alert.AlertType.ERROR, "Email ou mot de passe incorrect.");
            return;
        }

        // 🔒 Vérification du statut
        if ("blocked".equalsIgnoreCase(user.getStatut())) {
            showAlert(Alert.AlertType.ERROR, "❌ Ce compte est bloqué. Veuillez contacter l'administration.");
            return; // Empêche l'accès
        }

        new HistoriqueConnexionService().enregistrerConnexion(user);
        SessionManager.setCurrentUser(user);
        System.out.println("Utilisateur récupéré: " + (user != null ? user.getId() : "NULL")); // Log

        // 🔑 Vérification du rôle
        String roles = user.getRoles();
        if (roles.contains("ROLE_ADMIN")) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/dashboard.fxml"));
                Parent root = loader.load();
                DashboardController controller = loader.getController();
                controller.initialize(user);
                Stage stage = (Stage) txtEmail.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Tableau de bord admin");
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else if (roles.contains("ROLE_CLIENT")) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/front.fxml"));
                Parent root = loader.load();
                FrontClientController controller = loader.getController();
                controller.setConnectedUser(user);
                Stage stage = (Stage) txtEmail.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Espace client");
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else if (roles.contains("ROLE_ARTISAN")) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/dashboardArtisan.fxml"));
                Parent root = loader.load();
                DashboardArtisanController controller = loader.getController();
                controller.initialize(user);
                Stage stage = (Stage) txtEmail.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Espace artisan");
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            showAlert(Alert.AlertType.ERROR, "Rôle inconnu.");
        }
    }


    @FXML
    private void handleForgotPassword() {
        try {
            URL fxmlUrl = getClass().getResource("/views/ForgotPassword.fxml");
            if (fxmlUrl == null) {
                throw new IOException("Cannot find FXML file: /views/ForgotPassword.fxml");
            }
            
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            
            Stage stage = (Stage) txtEmail.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de la page de récupération de mot de passe: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }
}
