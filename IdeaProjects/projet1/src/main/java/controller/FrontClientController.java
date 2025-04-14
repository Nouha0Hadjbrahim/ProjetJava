package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import model.User;

public class FrontClientController {

    @FXML private ImageView logoImage;
    @FXML private ImageView userIcon;
    @FXML private ImageView cartIcon;
    @FXML private MenuButton userMenu;
    @FXML private MenuItem profilMenuItem;
    @FXML private MenuItem historiqueMenuItem;
    @FXML private MenuItem deconnexionMenuItem;
    @FXML private StackPane contentPane;
    @FXML private Hyperlink linkAccueil;
    @FXML private Hyperlink linkAteliers;
    @FXML private Hyperlink linkReclamation;
    @FXML private MenuButton boutiqueMenu;
    @FXML private Button btnPanier;

    private List<Object> navLinks;

    @FXML
    public void initialize() {
        logoImage.setImage(new Image(getClass().getResourceAsStream("/assets/logo2.png")));
        userIcon.setImage(new Image(getClass().getResourceAsStream("/assets/userf.png")));
        cartIcon.setImage(new Image(getClass().getResourceAsStream("/assets/chariot.png")));

        // Créer les menu items du menu Boutique (ici explicitement)
        MenuItem itemProduits = new MenuItem("Produits");
        MenuItem itemMateriaux = new MenuItem("Matériaux");

        itemProduits.setOnAction(e -> {
            setActiveNav(boutiqueMenu);
            loadPage("/fornt views/produits.fxml");
        });

        itemMateriaux.setOnAction(e -> {
            setActiveNav(boutiqueMenu);
            loadPage("/fornt views/materiaux.fxml");
        });

        boutiqueMenu.getItems().setAll(itemProduits, itemMateriaux);

        // Ajout dans la liste de navigation
        navLinks = new ArrayList<>();
        navLinks.add(linkAccueil);
        navLinks.add(boutiqueMenu);
        navLinks.add(linkAteliers);
        navLinks.add(linkReclamation);

        setActiveNav(linkAccueil);
        loadDashboardHome();
    }


    private void loadDashboardHome() {
        loadPage("/fornt views/accueil.fxml");
    }

    private void loadPage(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent page = loader.load();
            contentPane.getChildren().setAll(page);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleNavLinkClick(ActionEvent event) {
        Object source = event.getSource();

        if (source instanceof Hyperlink || source instanceof MenuButton || source instanceof MenuItem || source instanceof Button) {
            setActiveNav(source);

            if (source == linkAccueil) {
                loadPage("/fornt views/accueil.fxml");
            } else if (source == linkAteliers) {
                loadPage("/fornt views/ateliers.fxml");
            } else if (source == linkReclamation) {
                loadPage("/fornt views/reclamation.fxml");
            } else if (source instanceof Button) {
                Button btn = (Button) source;
                if ("btnPanier".equals(btn.getId())) {
                    loadPage("/fornt views/panier.fxml");
                }
            }
            else if (source instanceof MenuItem) {
                MenuItem item = (MenuItem) source;

                String text = item.getText();
                if ("Profil".equals(text)) {
                    loadPage("/fornt views/profil.fxml");
                }
            }

        }
    }



    private void setActiveNav(Object activeLink) {
        for (Object link : navLinks) {
            if (link instanceof Hyperlink) {
                ((Hyperlink) link).getStyleClass().remove("active");
            } else if (link instanceof MenuButton) {
                ((MenuButton) link).getStyleClass().remove("active");
            }
        }

        if (activeLink instanceof Hyperlink) {
            ((Hyperlink) activeLink).getStyleClass().add("active");
        } else if (activeLink instanceof MenuButton) {
            ((MenuButton) activeLink).getStyleClass().add("active");
        }
    }

    @FXML
    public void handleProfil(ActionEvent event) {
        System.out.println("👤 Profil cliqué");
    }

    @FXML
    public void handleHistorique(ActionEvent event) {
        System.out.println("🧾 Historique cliqué");
    }

    @FXML
    public void handleDeconnexion(ActionEvent event) {
        System.out.println("🚪 Déconnexion cliquée");
    }

    @FXML
    public void handleMouseEnterIcon(MouseEvent event) {
        userMenu.show();
    }

    @FXML
    public void handleMouseExitIcon(MouseEvent event) {
        userMenu.hide();
    }
}
