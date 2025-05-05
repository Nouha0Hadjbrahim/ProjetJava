package controller;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import model.Ateliers;
import service.AteliersService;
import service.InscriptionAtelierService;
import utils.SessionManager;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AteliersController {

    @FXML private VBox atelierListView;
    @FXML private HBox paginationContainer;
    @FXML private VBox bottomContainer;
    @FXML
    private TextField searchField;
    @FXML
    private VBox topContainer; // qui contient Titre + Recherche
    @FXML
    private VBox mainContent;  // centre de la fenêtre
    @FXML private HBox searchBox;

    private final AteliersService atelierService = new AteliersService();
    private final InscriptionAtelierService inscriptionService = new InscriptionAtelierService();
    private int currentPage = 1;
    private final int rowsPerPage = 4;
    int userId ;

    @FXML
    public void initialize() {
        if (SessionManager.getCurrentUser() != null) {
            userId = SessionManager.getCurrentUser().getId();

        } else {
            System.out.println("Erreur : Aucun utilisateur connecté !");
            // Tu peux soit afficher un message d'erreur, soit rediriger l'utilisateur.
        }
        loadAteliersPage(currentPage,userId);
        // Listener sur la barre de recherche
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            loadFilteredAteliers(newValue);
        });
        loadAteliersPage(currentPage,userId);
        animateSearchBar();
    }

    public void loadAteliersPage(int page, int artisanId) {
        List<Ateliers> ateliers = atelierService.getAteliersPageByArtisan(page, rowsPerPage, artisanId);

        try {
            ScrollPane scrollPane = new ScrollPane();
            scrollPane.setFitToWidth(true);
            VBox atelierVBox = new VBox(10);
            atelierVBox.setAlignment(Pos.CENTER);

            atelierVBox.getChildren().add(createTableHeader());

            for (Ateliers atelier : ateliers) {
                HBox row = createAtelierRow(atelier);
                VBox.setMargin(row, new Insets(5, 0, 5, 0));
                atelierVBox.getChildren().add(row);
            }

            scrollPane.setContent(atelierVBox);
            mainContent.getChildren().setAll(scrollPane); // ici ! Remplacer la vue centrale

        } catch (Exception e) {
            e.printStackTrace();
        }
        int totalAteliers = atelierService.getTotalAteliersByArtisan(artisanId);
        generatePagination(totalAteliers);

    }




    // Nouvelle méthode pour afficher la liste des inscrits
    private void showInscrits(Ateliers atelier) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Liste des inscrits - " + atelier.getTitre());

        ListView<String> listView = new ListView<>();
        List<String> inscrits = inscriptionService.getNomsInscrits(atelier.getId());

        if (inscrits.isEmpty()) {
            listView.getItems().add("Aucun inscrit pour cet atelier");
        } else {
            listView.getItems().addAll(inscrits);
        }

        dialog.getDialogPane().setContent(listView);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    // Les méthodes existantes restent inchangées
    private Label createLabel(String text, int width) {
        Label label = new Label(text);
        label.setPrefWidth(width);
        label.setWrapText(true);
        label.setStyle("-fx-border-color: lightgray; -fx-padding: 5;");
        return label;
    }


    private void generatePagination(int totalItems) {
        paginationContainer.getChildren().clear();
        int totalPages = (int) Math.ceil((double) totalItems / rowsPerPage);

        for (int i = 1; i <= totalPages; i++) {
            int pageIndex = i;
            Button pageBtn = new Button(String.valueOf(pageIndex));
            pageBtn.setOnAction(e -> {
                currentPage = pageIndex;
                loadAteliersPage(pageIndex,userId); // recharge page courante avec le filtre actif
            });

            pageBtn.getStyleClass().add("pagination-button");

            if (pageIndex == currentPage) {
                pageBtn.setStyle("-fx-background-color: #3a4c68; -fx-text-fill: white;");
            }

            paginationContainer.getChildren().add(pageBtn);
        }
    }

    private void ouvrirEditionAtelier(Ateliers atelier) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/ajouterAtelier.fxml"));
            Parent formView = loader.load();

            AjouterAtelierController controller = loader.getController();
            controller.setAtelier(atelier);
            controller.setAteliersController(this); // 🔥 Injecter le controleur principal
            controller.setRoot((BorderPane) mainContent.getParent()); // 🔥 Injecter aussi le BorderPane principal

            // 🔥 Cacher titre + recherche
            topContainer.setVisible(false);
            topContainer.setManaged(false);

            // 🔥 Cacher pagination
            bottomContainer.setVisible(false);
            bottomContainer.setManaged(false);

            // 🔥 Remplacer correctement le center du BorderPane
            BorderPane root = (BorderPane) mainContent.getParent();
            root.setCenter(formView);

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur lors de l'ouverture du formulaire d'édition.");
        }
    }




    private void supprimerAtelier(Ateliers atelier) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Suppression d'un atelier");
        alert.setContentText("Voulez-vous vraiment supprimer : " + atelier.getTitre() + " ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            atelierService.supprimerAtelier(atelier.getId());
            loadAteliersPage(currentPage,userId);
        }
    }

    @FXML
    private void ouvrirAjoutAtelier() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/ajouterAtelier.fxml"));
            Parent formView = loader.load();

            AjouterAtelierController formController = loader.getController();
            formController.setAteliersController(this);  // 🔥 donner le controleur principal
            formController.setRoot((BorderPane) mainContent.getParent()); // 🔥 donner le BorderPane principal

            // 🔥 Cacher titre + recherche
            topContainer.setVisible(false);
            topContainer.setManaged(false);

            // 🔥 Cacher pagination
            bottomContainer.setVisible(false);
            bottomContainer.setManaged(false);

            // 🔥 Remplacer uniquement le contenu center
            BorderPane root = (BorderPane) mainContent.getParent();
            root.setCenter(formView);

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Impossible d'ouvrir la fenêtre d'ajout.");
        }
    }

    private void showAlert(Alert.AlertType alertType, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle("Alerte");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void loadFilteredAteliers(String filter) {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);

        VBox atelierVBox = new VBox(10);
        atelierVBox.setAlignment(Pos.CENTER);

        atelierVBox.getChildren().add(createTableHeader());

        List<Ateliers> ateliers;
        if (filter == null || filter.trim().isEmpty()) {
            ateliers = atelierService.getAteliersPageByArtisan(currentPage, rowsPerPage, userId);
        } else {
            ateliers = atelierService.searchAteliersByTitre(filter, userId);
        }

        for (Ateliers atelier : ateliers) {
            HBox row = createAtelierRow(atelier);
            VBox.setMargin(row, new Insets(5, 0, 5, 0));
            atelierVBox.getChildren().add(row);
        }

        scrollPane.setContent(atelierVBox);
        mainContent.getChildren().setAll(scrollPane);  // 🔥 Remplacement sans animation
    }


    private Label createHeaderLabel(String text, double width) {
        Label label = new Label(text);
        label.setPrefWidth(width);
        label.getStyleClass().add("header-label");
        return label;
    }


    private HBox createTableHeader() {
        HBox header = new HBox(10);
        header.getStyleClass().add("table-header");
        header.setAlignment(Pos.CENTER_LEFT);
        header.setMaxWidth(1300);
        header.setMinWidth(1300);

        header.getChildren().addAll(
                createHeaderLabel("Titre", 150),
                createHeaderLabel("Catégorie", 120),
                createHeaderLabel("Description", 220),
                createHeaderLabel("Niveau", 100),
                createHeaderLabel("Prix", 80),
                createHeaderLabel("Date", 150),
                createHeaderLabel("Durée", 80),
                createHeaderLabel("Lien", 150),
                createHeaderLabel("Inscrits", 80),
                createHeaderLabel("Actions", 80)
        );

        return header;
    }

    private HBox createAtelierRow(Ateliers atelier) {
        HBox row = new HBox(10);
        row.getStyleClass().add("table-row");
        row.setAlignment(Pos.CENTER_LEFT);
        row.setMaxWidth(1300);
        row.setMinWidth(1300);

        String dateCours = (atelier.getDatecours() != null)
                ? atelier.getDatecours().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                : "";

        Label titre = createLabel(atelier.getTitre(), 150);
        titre.getStyleClass().add("atelier-titre");

        Label categorie = createLabel(atelier.getCategorie(), 120);
        categorie.getStyleClass().add("atelier-categorie");

        Label description = createLabel(atelier.getDescription(), 220);
        description.getStyleClass().add("atelier-description");

        Label niveau = createLabel(atelier.getNiveau_diff(), 100);
        niveau.getStyleClass().add("atelier-niveau");

        Label prix = createLabel(String.format("%.2f DT", atelier.getPrix()), 80);
        prix.getStyleClass().add("atelier-prix");

        Label date = createLabel(dateCours, 150);
        date.getStyleClass().add("atelier-date");

        Label duree = createLabel(atelier.getDuree() + " min", 80);
        duree.getStyleClass().add("atelier-duree");

        Label lien = createLabel(atelier.getLien(), 150);
        lien.getStyleClass().add("atelier-lien");

        // Inscrits bouton
        int nbInscrits = inscriptionService.getNombreInscriptions(atelier.getId());
        Button inscritsBtn = new Button(nbInscrits + " inscrits");
        inscritsBtn.setOnAction(e -> showInscrits(atelier));
        inscritsBtn.getStyleClass().add("inscrits-button");

        // Modifier bouton
        Button editButton = new Button("🖊");
        editButton.getStyleClass().add("edit-button"); // même classe que delete-button, ou une similaire
        editButton.setOnAction(e -> ouvrirEditionAtelier(atelier));


        // Supprimer bouton
        Button deleteButton = new Button("🗑");
        deleteButton.setOnAction(e -> supprimerAtelier(atelier));
        deleteButton.getStyleClass().add("delete-button");

        HBox actions = new HBox(5, editButton, deleteButton);
        actions.setAlignment(Pos.CENTER);
        actions.setPrefWidth(80);

        row.getChildren().addAll(
                titre, categorie, description, niveau, prix, date, duree, lien, inscritsBtn, actions
        );

        return row;
    }
    public VBox getTopContainer() {
        return topContainer;
    }

    public VBox getMainContent() {
        return mainContent;
    }

    public VBox getBottomContainer() {
        return bottomContainer;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getUserId() {
        return userId;
    }
    private void animateSearchBar() {
        FadeTransition fade = new FadeTransition(Duration.seconds(1), searchBox);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }
}