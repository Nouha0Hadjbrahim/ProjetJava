package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.Message;
import service.MessageService;
import service.EmailService;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class MessagesViewController {
    @FXML
    private ScrollPane messagesScrollPane;
    @FXML
    private VBox messagesContainer;

    private DashboardController dashboardController;
    private final EmailService emailService = new EmailService();

    public void setDashboardController(DashboardController controller) {
        this.dashboardController = controller;
        loadMessages();
    }

    private void loadMessages() {
        messagesContainer.getChildren().clear();
        List<Message> messages = MessageService.getInstance().getMessages();
        
        if (messages.isEmpty()) {
            Label noMessagesLabel = new Label("Aucun message disponible");
            noMessagesLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 16px;");
            messagesContainer.getChildren().add(noMessagesLabel);
            return;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        
        for (Message message : messages) {
            VBox messageBox = new VBox(10);
            messageBox.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
            
            // En-tête du message avec date
            HBox headerBox = new HBox(10);
            Label subjectLabel = new Label(message.getObjet());
            subjectLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #1d2135;");
            
            Label dateLabel = new Label(message.getDateEnvoi().format(formatter));
            dateLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");
            
            Label emailLabel = new Label("De: " + message.getEmail());
            emailLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");
            
            headerBox.getChildren().addAll(subjectLabel, dateLabel, emailLabel);
            
            // Contenu du message
            Label contentLabel = new Label(message.getContenu());
            contentLabel.setStyle("-fx-text-fill: #333; -fx-font-size: 14px;");
            contentLabel.setWrapText(true);
            
            // Statut du message
            Label statusLabel = new Label(message.isLu() ? "✓ Lu" : "Nouveau");
            statusLabel.setStyle(message.isLu() ? 
                "-fx-text-fill: #666; -fx-font-size: 12px;" : 
                "-fx-text-fill: #2196F3; -fx-font-size: 12px; -fx-font-weight: bold;");

            // Bouton Répondre
            Button replyButton = new Button("Répondre");
            replyButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-cursor: hand;");
            replyButton.setOnAction(e -> handleReply(message));

            HBox footerBox = new HBox(10);
            footerBox.getChildren().addAll(statusLabel, replyButton);
            
            messageBox.getChildren().addAll(headerBox, contentLabel, footerBox);
            messagesContainer.getChildren().add(messageBox);
        }
    }

    private void handleReply(Message message) {
        // Créer une boîte de dialogue pour la réponse
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Répondre au message");
        dialog.setHeaderText("Réponse à: " + message.getEmail());

        // Créer les champs de la boîte de dialogue
        TextArea responseArea = new TextArea();
        responseArea.setPromptText("Votre réponse...");
        responseArea.setPrefRowCount(5);
        responseArea.setWrapText(true);

        dialog.getDialogPane().setContent(responseArea);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Afficher la boîte de dialogue et attendre la réponse
        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK && !responseArea.getText().isEmpty()) {
                try {
                    // Envoyer l'email de réponse
                    String subject = "Re: " + message.getObjet();
                    emailService.sendEmail2(message.getEmail(), subject, responseArea.getText());
                    
                    // Afficher un message de succès
                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Succès");
                    successAlert.setHeaderText(null);
                    successAlert.setContentText("Votre réponse a été envoyée avec succès !");
                    successAlert.showAndWait();
                } catch (Exception e) {
                    // Afficher un message d'erreur
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Erreur");
                    errorAlert.setHeaderText(null);
                    errorAlert.setContentText("Erreur lors de l'envoi de la réponse: " + e.getMessage());
                    errorAlert.showAndWait();
                }
            }
        });
    }
} 