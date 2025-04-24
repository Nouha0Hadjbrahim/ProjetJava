package controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import model.Ateliers;
import model.User;
import service.AteliersService;
import model.InscriptionAtelier;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.Separator;
import javafx.scene.control.ButtonType;
import javafx.geometry.Insets;
import javafx.stage.Modality;
import service.EpingleService;
import service.InscriptionAtelierService;
import utils.SessionManager;

public class FrontAtelierController implements Initializable {

    @FXML
    private GridPane cardContainer;

    @FXML
    private ComboBox<String> categoryFilter;
    @FXML
    private ComboBox<String> niveauFilter;
    @FXML
    private ComboBox<String> priceSortFilter;


    private final AteliersService atelierService = new AteliersService();
    private final EpingleService epingleService = new EpingleService(); // tout en haut de la classe
    private final int userId = SessionManager.getCurrentUser().getId();


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialiser les filtres
        categoryFilter.getItems().addAll("Toutes les catégories", "Poterie", "Peinture", "Broderie", "Tricot", "Couture", "Bijoux Faits Main");
        categoryFilter.setValue("Toutes les catégories");

        niveauFilter.getItems().addAll("Tous les niveaux", "Débutant", "Intermédiaire", "Avancé");
        niveauFilter.setValue("Tous les niveaux");

        priceSortFilter.getItems().addAll("Tri par prix", "Croissant", "Décroissant");
        priceSortFilter.setValue("Tri par prix");

        // Ajouter un listener pour le tri par prix
        priceSortFilter.valueProperty().addListener((obs, oldVal, newVal) -> {
            loadAllAteliers(); // Sans pagination
        });

        // Ajout des listeners sur les filtres
        categoryFilter.valueProperty().addListener((obs, oldVal, newVal) -> {
            loadAllAteliers(); // Sans pagination
        });

        niveauFilter.valueProperty().addListener((obs, oldVal, newVal) -> {
            loadAllAteliers(); // Sans pagination
        });

        // Chargement initial sans pagination
        loadAllAteliers();
    }



    public void loadAllAteliers() {
        // Récupération complète sans pagination
        List<Ateliers> allAteliers = atelierService.getAllAteliers();

        String selectedCategorie = categoryFilter.getValue();
        String selectedNiveau = niveauFilter.getValue();
        String selectedPriceSort = priceSortFilter.getValue();

        // Appliquer les filtres
        List<Ateliers> filteredAteliers = new ArrayList<>();
        for (Ateliers atelier : allAteliers) {
            boolean matchCategorie = selectedCategorie.equals("Toutes les catégories") ||
                    (atelier.getCategorie() != null && atelier.getCategorie().equalsIgnoreCase(selectedCategorie));

            boolean matchNiveau = selectedNiveau.equals("Tous les niveaux") ||
                    (atelier.getNiveau_diff() != null && atelier.getNiveau_diff().equalsIgnoreCase(selectedNiveau));

            if (matchCategorie && matchNiveau) {
                filteredAteliers.add(atelier);
            }
        }

        EpingleService epingleService = new EpingleService();
        Set<Integer> epingledIds = epingleService.getEpinglesForUser(userId);

        // 🔝 Trier : ateliers épinglés d'abord
        filteredAteliers.sort((a1, a2) -> {
            boolean e1 = epingledIds.contains(a1.getId());
            boolean e2 = epingledIds.contains(a2.getId());
            return Boolean.compare(!e1, !e2); // true < false donc e1 true → -1
        });

        // Tri
        if ("Croissant".equals(selectedPriceSort)) {
            filteredAteliers.sort((a1, a2) -> Double.compare(a1.getPrix(), a2.getPrix()));
        } else if ("Décroissant".equals(selectedPriceSort)) {
            filteredAteliers.sort((a1, a2) -> Double.compare(a2.getPrix(), a1.getPrix()));
        }

        // Affichage sans pagination
        displayAteliers(filteredAteliers);

    }



    private void displayAteliers(List<Ateliers> ateliers) {
        cardContainer.getChildren().clear();

        if (ateliers.isEmpty()) {
            Label noResults = new Label("Aucun atelier disponible pour le moment");
            noResults.getStyleClass().add("no-results-label");
            cardContainer.add(noResults, 0, 0);
            return;
        }

        int columns = 3;
        int row = 0;
        int col = 0;

        for (int i = 0; i < ateliers.size(); i++) {
            Ateliers atelier = ateliers.get(i);
            VBox atelierCard = createAtelierCard(atelier);

            cardContainer.add(atelierCard, col, row);

            col++;
            if (col == columns) {
                col = 0;
                row++;
            }
        }
    }

    private VBox createAtelierCard(Ateliers atelier) {
        VBox card = new VBox();
        card.getStyleClass().add("atelier-card");
        card.setSpacing(8);

        // Titre de l'atelier
        Label titleLabel = new Label(atelier.getTitre());
        titleLabel.getStyleClass().add("atelier-title");

        // Formateur
        String nomFormateur = atelierService.getNomFormateur(atelier.getUser());
        Label formateurLabel = new Label("Formateur : " + nomFormateur);
        formateurLabel.getStyleClass().add("atelier-stitle");

        // Prix
        Label priceLabel = new Label();
        TextFlow priceFlow = new TextFlow(
                new Text("DT "),
                new Text(String.valueOf((int)atelier.getPrix())),
                new Text(" / atelier")
        );
        priceFlow.setStyle("-fx-font-size: 14px;");
        priceFlow.getChildren().get(1).setStyle("-fx-font-weight: bold; -fx-fill: #3a4c68; -fx-font-size: 16px;");
        priceLabel.setGraphic(priceFlow);

        // Détails
        String description = (atelier.getDescription() != null)
                ? (atelier.getDescription().length() > 15
                ? atelier.getDescription().substring(0, 15) + "..."
                : atelier.getDescription())
                : "Aucune description"; // Valeur par défaut

        Label descLabel = new Label("✓ " + description);
        Label niveauLabel = new Label("✓ Niveau : " + atelier.getNiveau_diff());
        Label dateLabel = new Label("✓ Date : " + formatDate(atelier.getDatecours()));
        Label dureeLabel = new Label("✓ Durée : " + atelier.getDuree() + " min");

        // Styles communs
        for (Label label : new Label[]{descLabel, niveauLabel, dateLabel, dureeLabel}) {
            label.getStyleClass().add("atelier-label");
        }

        // Bouton d'épinglage
        Button epingleBtn = new Button();
        boolean isPinned = epingleService.isEpingle(userId, atelier.getId());
        updateEpingleButtonIcon(epingleBtn, isPinned);

        epingleBtn.setOnAction(e -> {
            if (epingleService.isEpingle(userId, atelier.getId())) {
                epingleService.deseingler(userId, atelier.getId());
            } else {
                epingleService.epingler(userId, atelier.getId());
            }
            loadAllAteliers(); // recharger pour réordonner
        });

        // Boutons - CORRECTION ICI
        HBox actionsBox = new HBox(15);
        actionsBox.getStyleClass().add("atelier-actions");

        Hyperlink voirPlusLink = new Hyperlink("Voir plus");
        voirPlusLink.getStyleClass().add("voir-plus-link"); // Nom de classe CSS corrigé

        voirPlusLink.setOnAction(e -> showAtelierDetails(atelier));


        Button inscrireBtn = new Button("S'inscrire");
        inscrireBtn.getStyleClass().add("inscrire-btn"); // Nom de classe CSS corrigé

        inscrireBtn.setOnAction(event -> {
            System.out.println("=== DEBUG INSCRIPTION ==="); // Log
            System.out.println("Bouton cliqué pour atelier: " + atelier.getId()); // Log

            User currentUser = SessionManager.getCurrentUser();
            System.out.println("Utilisateur récupéré: " + (currentUser != null ? currentUser.getId() : "NULL")); // Log

            if (currentUser == null) {
                System.err.println("ERREUR: Session utilisateur nulle malgré connexion"); // Log
                showAlert("Connexion requise", "Veuillez vous connecter pour vous inscrire");
                return;
            }

            // 2. Vérifier si déjà inscrit
            InscriptionAtelierService inscriptionService = new InscriptionAtelierService();
            if (inscriptionService.checkExistingInscription(currentUser.getId(), atelier.getId())) {
                showAlert("Information", "Vous êtes déjà inscrit à cet atelier");
                return;
            }

            LocalDateTime dateAtelierLdt = atelier.getDatecours();
            LocalDate dateAtelier = dateAtelierLdt.toLocalDate(); // On ne garde que la date
            LocalDate aujourdhui = LocalDate.now();

            String statut;
            if (dateAtelier.isAfter(aujourdhui)) {
                statut = "à venir";
            } else if (dateAtelier.isBefore(aujourdhui)) {
                statut = "passé";
            } else {
                statut = "aujourd'hui";
            }

            // 3. Créer l'inscription avec le statut approprié
            InscriptionAtelier nouvelleInscription = new InscriptionAtelier(
                    atelier.getId(),
                    currentUser.getId(),
                    LocalDate.now(),  // date d'inscription
                    statut
            );

            // 4. Sauvegarder en base
            if (inscriptionService.createInscription(nouvelleInscription)) {
                showAlert("Succès", "Inscription réussie à l'atelier " + atelier.getTitre());

                // 5. Mettre à jour l'affichage (optionnel)
                loadAllAteliers();
            } else {
                showAlert("Erreur", "Échec de l'inscription");
            }
        });

        actionsBox.getChildren().addAll(epingleBtn, voirPlusLink, inscrireBtn);


        // Organisation de la carte - CORRECTION ICI
        card.getChildren().addAll(
                titleLabel,
                formateurLabel,
                priceLabel,
                descLabel,
                niveauLabel,
                dateLabel,
                dureeLabel,
                actionsBox // Ajout du conteneur des boutons à la fin
        );

        return card;
    }



    // Méthode helper pour formater la date
    private String formatDate(LocalDateTime date) {
        if (date == null) return "Date non définie";
        return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    private void showAtelierDetails(Ateliers atelier) {
        // Création de la fenêtre modale
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Détails de l'atelier");
        dialog.initModality(Modality.APPLICATION_MODAL);

        // Conteneur principal
        VBox mainContainer = new VBox(10);
        mainContainer.setPadding(new Insets(20));
        mainContainer.setStyle("-fx-background-color: white;");

        // Titre
        Label titleLabel = new Label(atelier.getTitre());
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #8C4A1D;");

        // GridPane pour les détails organisés
        GridPane detailsGrid = new GridPane();
        detailsGrid.setHgap(10);
        detailsGrid.setVgap(10);
        detailsGrid.setPadding(new Insets(10));

        // Ajout des détails dans la grille
        detailsGrid.addRow(0, createDetailLabel("Formateur:"), createValueLabel(atelierService.getNomFormateur(atelier.getUser())));
        detailsGrid.addRow(1, createDetailLabel("Catégorie:"), createValueLabel(atelier.getCategorie()));
        detailsGrid.addRow(2, createDetailLabel("Niveau:"), createValueLabel(atelier.getNiveau_diff()));
        detailsGrid.addRow(3, createDetailLabel("Prix:"), createValueLabel(atelier.getPrix() + " DT"));
        detailsGrid.addRow(4, createDetailLabel("Date:"), createValueLabel(formatDate(atelier.getDatecours())));
        detailsGrid.addRow(5, createDetailLabel("Durée:"), createValueLabel(atelier.getDuree() + " min"));

        // Description
        Label descTitle = new Label("Description:");
        descTitle.setStyle("-fx-font-weight: bold; -fx-padding: 5 0 0 0;");

        TextArea descriptionArea = new TextArea(atelier.getDescription());
        descriptionArea.setEditable(false);
        descriptionArea.setWrapText(true);
        descriptionArea.setStyle("-fx-control-inner-background: #fff; -fx-border-color: #8C4A1D;");

        // Lien
        Label linkTitle = new Label("Lien de l'atelier:");
        linkTitle.setStyle("-fx-font-weight: bold; -fx-padding: 5 0 0 0;");

        Hyperlink link = new Hyperlink(atelier.getLien());
        link.setStyle("-fx-text-fill: #8C4A1D; -fx-border-color: transparent;");

        // Organisation du contenu
        mainContainer.getChildren().addAll(
                titleLabel,
                new Separator(),
                detailsGrid,
                descTitle,
                descriptionArea,
                linkTitle,
                link
        );

        // Configuration du dialog
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setContent(mainContainer);
        dialogPane.getButtonTypes().add(ButtonType.CLOSE);
        dialogPane.setStyle("-fx-background-color: white;");
        dialogPane.setPrefSize(500, 500);

        // Style des boutons
        Node closeButton = dialogPane.lookupButton(ButtonType.CLOSE);
        closeButton.setStyle("-fx-background-color: #8C4A1D; -fx-text-fill: white;");

        dialog.showAndWait();
    }

    // Méthodes helpers pour créer les labels
    private Label createDetailLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-weight: bold; -fx-text-fill: #5a3921;");
        return label;
    }

    private Label createValueLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: #333;");
        return label;
    }
    // Ajoutez cette méthode pour gérer l'inscription
    private void handleInscription(Ateliers atelier) {
        // 1. Vérifier si l'utilisateur est connecté
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            showAlert("Erreur", "Veuillez vous connecter pour vous inscrire");
            return;
        }

        // 2. Vérifier s'il est déjà inscrit
        InscriptionAtelierService inscriptionService = new InscriptionAtelierService();
        boolean dejaInscrit = inscriptionService.checkExistingInscription(
                currentUser.getId(),
                atelier.getId()
        );

        if (dejaInscrit) {
            showAlert("Information", "Vous êtes déjà inscrit à cet atelier");
            return;
        }

        // 3. Créer la nouvelle inscription
        InscriptionAtelier nouvelleInscription = new InscriptionAtelier(
                atelier.getId(),
                currentUser.getId(),
                LocalDate.now(),
                "À venir" // Statut initial
        );

        // 4. Enregistrer en base de données
        boolean success = inscriptionService.createInscription(nouvelleInscription);

        if (success) {
            showSuccessAlert("Inscription réussie", "Votre inscription à l'atelier a été enregistrée");
        } else {
            showAlert("Erreur", "Échec de l'inscription");
        }
    }

    // Méthode pour afficher une alerte de succès
    private void showSuccessAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Style personnalisé pour le succès
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #e8f5e9;");
        dialogPane.lookup(".content.label").setStyle("-fx-text-fill: #2e7d32;");

        alert.showAndWait();
    }
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Style optionnel pour correspondre à votre thème
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(
                getClass().getResource("/styles/atelier.css").toExternalForm()
        );
        dialogPane.getStyleClass().add("alert-dialog");

        alert.showAndWait();
    }

    private void updateEpingleButtonIcon(Button btn, boolean isPinned) {
        btn.setText("📌");
        btn.setStyle(
                "-fx-font-size: 18px;" +
                        "-fx-text-fill: " + (isPinned ? "#8B4513" : "#A9A9A9") + ";" +  // marron si épinglé, gris sinon
                        "-fx-background-color: transparent;" +
                        "-fx-cursor: hand;"
        );
    }




}