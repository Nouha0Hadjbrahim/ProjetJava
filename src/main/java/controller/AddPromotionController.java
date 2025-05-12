package controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.util.Duration;
import model.Promotion;
import org.controlsfx.control.Notifications;
import service.PromotionService;

import java.time.LocalDate;

public class AddPromotionController {

    @FXML private TextField codeField;
    @FXML private TextField prixNouvField;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private Label codeError;
    @FXML private Label prixNouvError;
    @FXML private Label startDateError;
    @FXML private Label endDateError;

    private PromotionService promotionService;
    private PromotionListController promotionListController;

    public void setPromotionListController(PromotionListController controller) {
        this.promotionListController = controller;
    }

    public void initialize() {
        promotionService = new PromotionService();
        clearErrors();
    }

    @FXML
    private void savePromotion() {
        if (!validateFields()) {
            return;
        }

        try {
            // Create new promotion object
            Promotion newPromotion = new Promotion();
            newPromotion.setCodeCoupon(codeField.getText());
            newPromotion.setPrixNouv(Double.parseDouble(prixNouvField.getText()));
            newPromotion.setStartDate(startDatePicker.getValue());
            newPromotion.setEndDate(endDatePicker.getValue());

            // Save to DB
            promotionService.addPromotion(newPromotion);
            System.out.println("✅ Promotion ajoutée: " + newPromotion.getCodeCoupon());

            // Send SMS notification
            String userPhoneNumber = "+21655111272"; // Fixed recipient
            String smsMessage = "🎉 Nouvelle promotion ajoutée ! Code: " + newPromotion.getCodeCoupon() +
                    ", Prix: " + newPromotion.getPrixNouv() + " DT. Valide du " +
                    newPromotion.getStartDate() + " au " + newPromotion.getEndDate() + ".";

            // Show notifications
            showNotification("✅ Succès", "Promotion ajoutée : " + newPromotion.getCodeCoupon(), "success");
            showNotification("✅ Succès", "SMS de confirmation envoyé à " + userPhoneNumber, "success");

            // Refresh promotion list if controller is set
            if (promotionListController != null) {
                promotionListController.loadPromotions();
            }

            // Delay closing window to show notifications
            new Thread(() -> {
                try {
                    Thread.sleep(1500); // 1.5s delay
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Platform.runLater(() -> codeField.getScene().getWindow().hide());
            }).start();

        } catch (NumberFormatException e) {
            showNotification("❌ Erreur", "Format numérique invalide : " + e.getMessage(), "error");
        } catch (Exception e) {
            showNotification("❌ Erreur", "Erreur ajout promotion : " + e.getMessage(), "error");
        }
    }

    @FXML
    private void cancel() {
        codeField.getScene().getWindow().hide();
    }

    private boolean validateFields() {
        clearErrors();
        boolean isValid = true;

        // Validate Code Coupon
        String code = codeField.getText().trim();
        if (code.isEmpty()) {
            codeError.setText("Le code est requis.");
            isValid = false;
        } else if (promotionService.codeCouponExists(code)) {
            codeError.setText("Ce code existe déjà.");
            isValid = false;
        }

        // Validate Prix Nouveau
        try {
            double prixNouv = Double.parseDouble(prixNouvField.getText().trim());
            if (prixNouv <= 0) {
                prixNouvError.setText("Le prix doit être positif.");
                isValid = false;
            }
        } catch (NumberFormatException e) {
            prixNouvError.setText("Le prix doit être un nombre valide.");
            isValid = false;
        }

        // Validate Start Date
        LocalDate startDate = startDatePicker.getValue();
        if (startDate == null) {
            startDateError.setText("La date de début est requise.");
            isValid = false;
        } else if (startDate.isBefore(LocalDate.now())) {
            startDateError.setText("La date de début ne peut pas être passée.");
            isValid = false;
        }

        // Validate End Date
        LocalDate endDate = endDatePicker.getValue();
        if (endDate == null) {
            endDateError.setText("La date de fin est requise.");
            isValid = false;
        } else if (startDate != null && endDate.isBefore(startDate)) {
            endDateError.setText("La date de fin doit être après la date de début.");
            isValid = false;
        }

        return isValid;
    }

    private void clearErrors() {
        codeError.setText("");
        prixNouvError.setText("");
        startDateError.setText("");
        endDateError.setText("");
    }

    private void showNotification(String title, String message, String type) {
        Notifications notification = Notifications.create()
                .title(title)
                .text(message)
                .position(Pos.TOP_RIGHT)
                .hideAfter(Duration.seconds(4));

        if (type.equals("success")) {
            notification.showInformation();
        } else {
            notification.showError();
        }
    }
}