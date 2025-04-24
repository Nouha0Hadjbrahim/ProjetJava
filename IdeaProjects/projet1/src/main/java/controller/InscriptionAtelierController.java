package controller;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;
import model.InscriptionAtelier;
import model.Ateliers;

import org.kordamp.ikonli.javafx.FontIcon;

import service.InscriptionAtelierService;
import service.AteliersService;
import service.SimpleWebService;
import utils.SessionManager;

import java.io.File;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Hashtable;
import java.util.List;



public class InscriptionAtelierController {

    @FXML
    private ComboBox<String> statutFilter;

    private final InscriptionAtelierService inscriptionService = new InscriptionAtelierService();
    private final AteliersService atelierService = new AteliersService();

    @FXML
    private GridPane historiqueContainer;


    public void initialize() {
        // Initialiser le filtre
        statutFilter.getItems().addAll("Tous", "À venir", "Aujourd'hui", "Terminé");
        statutFilter.setValue("Tous");


        statutFilter.setStyle("-fx-background-color: #f5f5f5; " +
                "-fx-background-radius: 15px; " +
                "-fx-border-radius: 15px; " +
                "-fx-border-color: #d3d3d3; " +
                "-fx-pref-width: 180px; " +
                "-fx-pref-height: 30px; " +
                "-fx-font-size: 12px; " +
                "-fx-padding: 0 0 0 10px;");

        // Charger les inscriptions
        loadUserInscriptions();

        // Ajouter le listener pour le filtre
        statutFilter.valueProperty().addListener((obs, oldVal, newVal) -> {
            loadUserInscriptions();
        });
    }

    private void loadUserInscriptions() {
        historiqueContainer.getChildren().clear();

        // Récupérer l'ID de l'utilisateur connecté
        int userId = SessionManager.getCurrentUser().getId();

        // Récupérer toutes les inscriptions de l'utilisateur
        List<InscriptionAtelier> inscriptions = inscriptionService.getInscriptionsByUser(userId);

        // Filtrer selon la sélection
        String filter = statutFilter.getValue();

        int col = 0;
        int row = 0;

        for (InscriptionAtelier inscription : inscriptions) {
            Ateliers atelier = atelierService.getAtelierById(inscription.getAtelierId());

            // Appliquer le filtre
            if (!filter.equals("Tous")) {
                if (filter.equals("À venir") && !inscription.getStatut().equalsIgnoreCase("à venir")) continue;
                if (filter.equals("Aujourd'hui") && !inscription.getStatut().equalsIgnoreCase("aujourd'hui")) continue;
                if (filter.equals("Terminé") && !inscription.getStatut().equalsIgnoreCase("passé")) continue;
            }

            // Créer la carte d'inscription
            VBox card = new VBox(8);
            card.setPadding(new Insets(20)); // marge intérieure plus grande
            card.setPrefWidth(300);
            card.setPrefHeight(200); // ou plus si tu veux
            card.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 2, 2);");

            Label titleLabel = new Label(atelier.getTitre());
            titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #843C0C;");

            Label formateurLabel = new Label("Formateur: " + atelierService.getNomFormateur(atelier.getUser()));
            formateurLabel.setStyle("-fx-text-fill: #555;");

            Label dateLabel = new Label("Date d'inscription: " +
                    inscription.getDateInscription().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            dateLabel.setStyle("-fx-text-fill: #555;");

            Label statutLabel = new Label("Statut: " + capitalizeFirstLetter(inscription.getStatut()));
            statutLabel.setStyle(getStatutStyle(inscription.getStatut()));

            // Actions (info et suppression)
            HBox actions = new HBox(10);
            actions.setAlignment(Pos.CENTER_LEFT);

            FontIcon infoIcon = new FontIcon("fas-info-circle");
            infoIcon.setIconSize(16);
            infoIcon.setIconColor(javafx.scene.paint.Color.web("#1565c0"));
            Button infoBtn = new Button("", infoIcon);
            infoBtn.setStyle("-fx-background-color: transparent;");
            infoBtn.setOnAction(e -> showInscriptionDetails(inscription, atelier));

            FontIcon deleteIcon = new FontIcon("fas-times-circle");
            deleteIcon.setIconSize(16);
            deleteIcon.setIconColor(javafx.scene.paint.Color.RED);
            Button deleteBtn = new Button("", deleteIcon);
            deleteBtn.setStyle("-fx-background-color: transparent;");
            deleteBtn.setUserData(inscription);
            deleteBtn.setOnAction(e -> {
                InscriptionAtelier selectedInscription = (InscriptionAtelier) deleteBtn.getUserData();
                if (selectedInscription != null) {
                    boolean success = inscriptionService.deleteInscription(selectedInscription.getId());
                    if (success) {
                        loadUserInscriptions(); // Rechargement de la liste
                        showAlert("Suppression", "Inscription supprimée !");
                    } else {
                        showAlert("Erreur", "Une erreur est survenue lors de la suppression.");
                    }
                } else {
                    showAlert("Erreur", "Aucune inscription sélectionnée !");
                }
            });

            actions.getChildren().addAll(infoBtn, deleteBtn);

            card.getChildren().addAll(titleLabel, formateurLabel, dateLabel, statutLabel, actions);

            // Ajout dans la grille : 3 colonnes max
            historiqueContainer.add(card, col, row);
            col++;
            if (col == 3) {
                col = 0;
                row++;
            }
        }
    }


    private String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    private String getStatutStyle(String statut) {
        if (statut == null) return "";

        // Style de base commun
        String baseStyle = "-fx-font-weight: bold; " +
                "-fx-padding: 2px 8px; " +
                "-fx-background-radius: 10px; ";

        switch (statut.toLowerCase()) {
            case "à venir":
                return baseStyle + "-fx-text-fill: #5D4037; " + // Marron foncé
                        "-fx-background-color: #D7CCC8; "; // Beige clair
            case "passé": // Correspond à "Terminé"
                return baseStyle + "-fx-text-fill: #4E342E; " + // Marron très foncé
                        "-fx-background-color: #BCAAA4; "; // Beige moyen
            case "aujourd'hui":
                return baseStyle + "-fx-text-fill: #5D4037; " + // Marron foncé
                        "-fx-background-color: #D7CCC8; "; // Beige clair
            default:
                return baseStyle;
        }
    }


    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(
                getClass().getResource("/styles/atelier.css").toExternalForm()
        );
        dialogPane.getStyleClass().add("alert-dialog");

        alert.showAndWait();

    }

    private void showInscriptionDetails(InscriptionAtelier inscription, Ateliers atelier) {
        try {
            // 1. Génère le fichier HTML avec les détails
            File htmlFile = generateHtmlFile(inscription, atelier);

            // 2. Copie du logo à côté du HTML
            File logoSrc = new File("src/main/resources/assets/logo2.png");
            File logoDest = new File(htmlFile.getParent(), "logo2.png");
            java.nio.file.Files.copy(logoSrc.toPath(), logoDest.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            // 3. Démarre un mini serveur HTTP sur un port libre
            int port = 8080;
            boolean serverStarted = false;
            while (!serverStarted && port < 8100) {
                try {
                    new SimpleWebService(port, htmlFile);
                    serverStarted = true;
                } catch (java.net.BindException e) {
                    port++; // essaie le port suivant
                }
            }

            if (!serverStarted) {
                showAlert("Erreur", "Aucun port disponible pour le serveur web.");
                return;
            }

            // 4. Génère l'URL et le QR code
            String ip = "192.168.1.14"; // ton IP locale Wi-Fi ici
            String url = "http://" + ip + ":" + port;

            File tempQR = File.createTempFile("qr-", ".png");
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(url, BarcodeFormat.QR_CODE, 300, 300);
            MatrixToImageWriter.writeToPath(matrix, "PNG", tempQR.toPath());

            // 5. Affiche une popup avec le QR code
            ImageView qrView = new ImageView(new javafx.scene.image.Image(tempQR.toURI().toString()));
            qrView.setFitWidth(250);
            qrView.setPreserveRatio(true);

            VBox box = new VBox(10, new Label("Scannez ce QR Code pour voir les détails"), qrView);
            box.setAlignment(Pos.CENTER);
            box.setPadding(new Insets(15));

            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("QR Code de l'inscription");
            dialog.getDialogPane().setContent(box);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            dialog.getDialogPane().setMinWidth(400);
            dialog.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Une erreur est survenue lors de la génération du QR code.");
        }
    }




    // Méthode utilitaire pour formater la date
    private String formatDate(TemporalAccessor date) {
        if (date instanceof LocalDateTime) {
            return ((LocalDateTime) date).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        } else if (date instanceof LocalDate) {
            return ((LocalDate) date).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }
        return date.toString();
    }

    private File generateHtmlFile(InscriptionAtelier inscription, Ateliers atelier) {
        try {
            File file = new File(System.getProperty("java.io.tmpdir"), "inscription-" + inscription.getId() + ".html");

            String html = "<!DOCTYPE html><html><head><meta charset='UTF-8'>" +
                    "<title>Détails de l'atelier</title>" +
                    "<style>" +
                    "body { font-family: Arial, sans-serif; background-color: #f9f9f9; margin: 0; padding: 0; }" +
                    ".container { max-width: 600px; margin: 30px auto; background-color: white; padding: 30px; border-radius: 12px; box-shadow: 0 0 10px rgba(0,0,0,0.1); }" +
                    "h2 { color: #843C0C; text-align: center; font-size: 26px; }" +
                    ".info-block { margin: 15px 0; font-size: 18px; }" +
                    ".label { font-weight: bold; color: #5D4037; display: inline-block; width: 160px; }" +
                    ".value { display: inline-block; color: #333; }" +
                    ".header { display: flex; align-items: center; justify-content: flex-start; margin-bottom: 20px; }" +
                    ".header img { height: 50px; margin-right: 15px; }" +
                    ".header-title { font-size: 20px; color: #843C0C; font-weight: bold; }" +
                    "</style></head><body>" +
                    "<div class='container'>" +
                    "<div class='header'>" +
                    "<img src='logo2.png' alt='Logo'>" +
                    "<div class='header-title'>Détails de l'inscription</div>" +
                    "</div>" +
                    "<h2>" + atelier.getTitre() + "</h2>" +
                    "<div class='info-block'><span class='label'>Catégorie :</span><span class='value'>" + atelier.getCategorie() + "</span></div>" +
                    "<div class='info-block'><span class='label'>Description :</span><span class='value'>" + atelier.getDescription() + "</span></div>" +
                    "<div class='info-block'><span class='label'>Niveau :</span><span class='value'>" + atelier.getNiveau_diff() + "</span></div>" +
                    "<div class='info-block'><span class='label'>Prix :</span><span class='value'>" + atelier.getPrix() + " DT</span></div>" +
                    "<div class='info-block'><span class='label'>Date :</span><span class='value'>" + formatDate(atelier.getDatecours()) + "</span></div>" +
                    "<div class='info-block'><span class='label'>Durée :</span><span class='value'>" + atelier.getDuree() + " minutes</span></div>" +
                    "<div class='info-block'><span class='label'>Lien :</span><span class='value'><a href='" + atelier.getLien() + "'>" + atelier.getLien() + "</a></span></div>" +
                    "</div></body></html>";

            java.nio.file.Files.writeString(file.toPath(), html);
            return file;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
