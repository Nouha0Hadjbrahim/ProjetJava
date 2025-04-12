package controller;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class FooterController {
    @FXML private ImageView iconX;
    @FXML private ImageView iconFacebook;
    @FXML private ImageView iconInstagram;
    @FXML private ImageView iconLinkedin;

    @FXML
    public void initialize() {
        iconX.setImage(new Image(getClass().getResourceAsStream("/assets/icons/x.png")));
        iconFacebook.setImage(new Image(getClass().getResourceAsStream("/assets/icons/facebook.png")));
        iconInstagram.setImage(new Image(getClass().getResourceAsStream("/assets/icons/instagram.png")));
        iconLinkedin.setImage(new Image(getClass().getResourceAsStream("/assets/icons/linkedin.png")));
    }
}
