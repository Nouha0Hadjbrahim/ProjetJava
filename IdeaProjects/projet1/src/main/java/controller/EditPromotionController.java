package controller;

import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import model.Promotion;
import service.PromotionService;

import java.time.LocalDate;

public class EditPromotionController {

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

    private int promotionId;
    private PromotionService promotionService;
    private String originalCode;

    public void initialize() {
        promotionService = new PromotionService();
        clearErrors();
    }

    public void setPromotionId(int id) {
        this.promotionId = id;
        // Load existing promotion data
        Promotion promotion = promotionService.getPromotionById(id);
        if (promotion != null) {
            codeField.setText(promotion.getCodeCoupon());
            prixNouvField.setText(String.valueOf(promotion.getPrixNouv()));
            startDatePicker.setValue(promotion.getStartDate());
            endDatePicker.setValue(promotion.getEndDate());
            originalCode = promotion.getCodeCoupon(); // Store original code for validation
        } else {
            System.out.println("❌ Promotion non trouvée: ID " + id);
        }
    }

    @FXML
    private void savePromotion() {
        if (!validateFields()) {
            return;
        }

        try {
            // Create updated promotion object
            Promotion updatedPromotion = new Promotion();
            updatedPromotion.setId(promotionId);
            updatedPromotion.setCodeCoupon(codeField.getText());
            updatedPromotion.setPrixNouv(Double.parseDouble(prixNouvField.getText()));
            updatedPromotion.setStartDate(startDatePicker.getValue());
            updatedPromotion.setEndDate(endDatePicker.getValue());

            // Update in DB
            promotionService.updatePromotion(updatedPromotion);
            System.out.println("✅ Promotion mise à jour: ID " + promotionId);

            // Close the window
            codeField.getScene().getWindow().hide();
        } catch (NumberFormatException e) {
            System.out.println("❌ Erreur format numérique: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("❌ Erreur mise à jour promotion: " + e.getMessage());
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
        } else if (!code.equals(originalCode) && promotionService.codeCouponExists(code)) {
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