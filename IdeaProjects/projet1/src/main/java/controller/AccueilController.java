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


public class AccueilController {

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
        WebEngine webEngine = mapWebView.getEngine();
        // map
        // Code HTML intégré avec carte Google Maps
        String html =
                "<html>" +
                        "<body style='margin:0'>" +
                        "<iframe " +
                        "src=\"https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d12763.067281693097!2d10.1763469!3d36.8649474!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x1302c93cb06f630d%3A0x7dd7b15cb48ab05e!2sLa%20Petite%20Ariana!5e0!3m2!1sfr!2stn!4v1712952342345\" " +
                        "width=\"100%\" height=\"100%\" style=\"border:0;\" allowfullscreen " +
                        "loading=\"lazy\" referrerpolicy=\"no-referrer-when-downgrade\">" +
                        "</iframe>" +
                        "</body>" +
                        "</html>";

        webEngine.loadContent(html);
        // 🎬 Animation d’apparition pour les cartes ateliers
        animateCardsIn();
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

}
