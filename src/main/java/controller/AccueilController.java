package controller;

import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import model.Message;
import service.MessageService;
import model.User;

public class AccueilController {

    private User connectedUser;

    @FXML
    private ImageView potImage;

    @FXML
    private HBox logosBox;
    @FXML private Button btnProduits;
    @FXML private Button btnMateriaux;
    @FXML private HBox imagesBox;
    @FXML private HBox cardContainer;
    @FXML private ImageView pinIcon;
    @FXML private ImageView phoneIcon;
    @FXML private ImageView mailIcon;
    @FXML private WebView mapWebView;
    @FXML
    private TextField objetField;

    @FXML
    private TextArea messageField;
    @FXML
    public void initialize() {
        loadPotImage();
        loadPartnerLogos();
        animateFloatingImage();
        animateLogosScroll();
        // Charger les matériaux par défaut
        showImages("materiaux");
        setActiveButton(btnMateriaux);
        // Chargement des icônes
        pinIcon.setImage(new Image(getClass().getResourceAsStream("/assets/icons/pin.png")));
        phoneIcon.setImage(new Image(getClass().getResourceAsStream("/assets/icons/phone.png")));
        mailIcon.setImage(new Image(getClass().getResourceAsStream("/assets/icons/mail.png")));
        //map
        WebEngine webEngine = mapWebView.getEngine();
        String url = getClass().getResource("/assets/mapbox_map.html").toExternalForm();
        webEngine.load(url);
        // 🎬 Animation d'apparition pour les cartes ateliers
        animateCardsIn();
    }
    @FXML
    private void handleEnvoyerMessage() {
        String objet = objetField.getText();
        String message = messageField.getText();

        if (objet.isEmpty() || message.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez remplir tous les champs !");
            alert.showAndWait();
            return;
        }

        System.out.println("Tentative d'envoi de message...");
        System.out.println("Utilisateur actuel : " + (connectedUser != null ? connectedUser.getEmail() : "null"));

        // Créer et ajouter le message avec l'email de l'utilisateur connecté
        String userEmail = (connectedUser != null) ? connectedUser.getEmail() : "visiteur@example.com";
        Message newMessage = new Message(objet, message, userEmail);
        MessageService.getInstance().addMessage(newMessage);

        // Afficher un message de succès
        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
        successAlert.setTitle("Succès");
        successAlert.setHeaderText(null);
        successAlert.setContentText("Votre message a été envoyé avec succès !");
        successAlert.showAndWait();

        // Vider les champs après envoi
        objetField.clear();
        messageField.clear();
    }
    private void animateCardsIn() {
        for (int i = 0; i < cardContainer.getChildren().size(); i++) {
            VBox card = (VBox) cardContainer.getChildren().get(i);
            card.setOpacity(0);

            FadeTransition fade = new FadeTransition(Duration.seconds(1), card);
            fade.setFromValue(0);
            fade.setToValue(1);
            fade.setDelay(Duration.millis(i * 200));

            TranslateTransition slide = new TranslateTransition(Duration.seconds(1), card);
            slide.setFromY(30);
            slide.setToY(0);
            slide.setDelay(Duration.millis(i * 200));

            fade.play();
            slide.play();
        }
    }

    private void loadPotImage() {
        try {
            Image pot = new Image(getClass().getResourceAsStream("/assets/hero.png"));
            potImage.setImage(pot);
        } catch (Exception e) {
            System.out.println("❌ Erreur chargement pot.png : " + e.getMessage());
        }
    }

    private void loadPartnerLogos() {
        String[] logos = {
                "1", "2", "3",
                "4", "5", "6", "7"
        };

        for (String fileName : logos) {
            try {
                Image logo = new Image(getClass().getResourceAsStream("/assets/clients/" + fileName+".png"));
                ImageView logoView = new ImageView(logo);
                logoView.setFitHeight(60);
                logoView.setPreserveRatio(true);
                logosBox.getChildren().add(logoView);
            } catch (Exception e) {
                System.out.println("⚠️ Logo non trouvé : " + fileName);
            }
        }
    }

    private void animateFloatingImage() {
        TranslateTransition floatAnim = new TranslateTransition(Duration.seconds(2), potImage);
        floatAnim.setFromY(0);
        floatAnim.setToY(-20);
        floatAnim.setAutoReverse(true);
        floatAnim.setCycleCount(TranslateTransition.INDEFINITE);
        floatAnim.play();
    }

    private void animateLogosScroll() {
        TranslateTransition scrollAnim = new TranslateTransition(Duration.seconds(6), logosBox);
        scrollAnim.setFromX(0);
        scrollAnim.setToX(-100); // ajuste la distance si besoin
        scrollAnim.setAutoReverse(true);
        scrollAnim.setCycleCount(TranslateTransition.INDEFINITE);
        scrollAnim.play();
    }
    private void showImages(String type) {
        imagesBox.getChildren().clear();

        String[] images;
        if (type.equals("materiaux")) {
            images = new String[] {"mat1.jpeg", "mat2.jpeg", "mat3.jpeg"};
        } else {
            images = new String[] {"prod1.jpeg", "prod2.jpeg", "prod3.jpeg"};
        }

        for (String imgFile : images) {
            try {
                ImageView imgView = new ImageView(new Image(getClass().getResourceAsStream("/assets/prod_mat/" + imgFile)));
                imgView.setFitHeight(200);
                imgView.setFitWidth(200);
                imgView.setPreserveRatio(true);
                imagesBox.getChildren().add(imgView);
            } catch (Exception e) {
                System.out.println("⚠️ Image non trouvée : " + imgFile);
            }
        }
    }
    @FXML
    private void handleBoutiqueProduits() {
        showImages("produits");
        setActiveButton(btnProduits);
    }

    @FXML
    private void handleBoutiqueMateriaux() {
        showImages("materiaux");
        setActiveButton(btnMateriaux);
    }
    private void setActiveButton(Button activeBtn) {
        btnProduits.getStyleClass().remove("active");
        btnMateriaux.getStyleClass().remove("active");
        if (!activeBtn.getStyleClass().contains("active")) {
            activeBtn.getStyleClass().add("active");
        }
    }

    public User getConnectedUser() {
        return connectedUser;
    }

    public void setConnectedUser(User user) {
        this.connectedUser = user;
        System.out.println("AccueilController - Utilisateur défini : " + (user != null ? user.getEmail() : "null"));
    }

}
