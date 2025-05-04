package controller;

import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import model.Promotion;
import service.PromotionService;

import java.time.LocalDate;

public class AddPromotionController {

    @FXML
    private TextField codeField;
    @FXML
    private TextField prixNouvField;
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;

    @FXML
    private Label codeError;
    @FXML
    private Label prixNouvError;
    @FXML
    private Label startDateError;
    @FXML
    private Label endDateError;

    private PromotionService promotionService;

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

            // Close the window
            codeField.getScene().getWindow().hide();
        } catch (NumberFormatException e) {
            System.out.println("❌ Erreur format numérique: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("❌ Erreur ajout promotion: " + e.getMessage());
        }
    }

    @FXML
    private void cancel() {
        // Close the window without saving
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
}