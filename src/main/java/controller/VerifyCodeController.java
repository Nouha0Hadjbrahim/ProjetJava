package controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

public class VerifyCodeController {
    @FXML private TextField codeField;
    @FXML private Label errorLabel;
    @FXML private Label timerLabel;
    
    private String email;
    private String correctCode;
    private Timeline timer;
    private int timeLeft = 60;
    
    public void setEmailAndCode(String email, String code) {
        this.email = email;
        this.correctCode = code;
        startTimer();
    }
    
    private void startTimer() {
        timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            timeLeft--;
            timerLabel.setText("Temps restant: " + timeLeft + "s");
            
            if (timeLeft <= 0) {
                timer.stop();
                returnToLogin();
            }
        }));
        timer.setCycleCount(60);
        timer.play();
    }
    
    @FXML
    private void handleVerifyCode() {
        String enteredCode = codeField.getText().trim();
        
        if (enteredCode.isEmpty()) {
            showError("Veuillez entrer le code");
            return;
        }
        
        if (!enteredCode.equals(correctCode)) {
            showError("Code incorrect");
            return;
        }
        
        timer.stop();
        openResetPasswordScreen();
    }
    
    private void openResetPasswordScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/ResetPassword.fxml"));
            Parent root = loader.load();
            
            ResetPasswordController controller = loader.getController();
            controller.setEmail(email);
            
            Stage stage = (Stage) codeField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showError("Erreur lors du chargement de la page de réinitialisation");
        }
    }
    
    @FXML
    private void handleBack() {
        timer.stop();
        returnToLogin();
    }
    
    private void returnToLogin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/login.fxml"));
            Stage stage = (Stage) codeField.getScene().getWindow();
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