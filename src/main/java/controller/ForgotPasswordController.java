package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import service.EmailService;
import service.UserService;

import java.io.IOException;
import java.net.URL;
import java.util.Random;

public class ForgotPasswordController {
    @FXML private TextField emailField;
    @FXML private Label errorLabel;
    
    private final UserService userService = new UserService();
    private final EmailService emailService = new EmailService();
    private String generatedCode;
    
    @FXML
    private void handleSendCode() {
        String email = emailField.getText().trim();
        
        if (email.isEmpty()) {
            showError("Veuillez entrer votre email");
            return;
        }
        
        if (!userService.emailExists(email)) {
            showError("Cet email n'est pas enregistré");
            return;
        }
        
        // Generate random 6-digit code
        generatedCode = generateRandomCode();
        
        // Send email with code
        String subject = "Code de réinitialisation de mot de passe";
        String content = "Votre code de réinitialisation est: " + generatedCode + 
                        "\nCe code expirera dans 1 minute.";
        
        try {
            emailService.sendEmail(email, subject, content);
            openVerifyCodeScreen(email, generatedCode);
        } catch (Exception e) {
            showError("Erreur lors de l'envoi de l'email: " + e.getMessage());
        }
    }
    
    private String generateRandomCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }
    
    private void openVerifyCodeScreen(String email, String code) {
        try {
            URL fxmlUrl = getClass().getResource("/views/VerifyCode.fxml");
            if (fxmlUrl == null) {
                throw new IOException("Cannot find FXML file: /views/VerifyCode.fxml");
            }
            
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            
            VerifyCodeController controller = loader.getController();
            controller.setEmailAndCode(email, code);
            
            Stage stage = (Stage) emailField.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de la page de vérification: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleBack() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/login.fxml"));
            Stage stage = (Stage) emailField.getScene().getWindow();
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