package controller;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.Ateliers;
import service.AteliersService;
import service.InscriptionAtelierService;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AteliersAdminController {

    @FXML
    private VBox atelierListView;
    @FXML private HBox paginationContainer;
    @FXML private ComboBox<String> categoryFilter;

    private final AteliersService atelierService = new AteliersService();
    private final InscriptionAtelierService inscriptionService = new InscriptionAtelierService();
    private int currentPage = 1;
    private final int rowsPerPage = 4;

    @FXML
    public void initialize() {

        loadAteliersPage(currentPage);
        categoryFilter.getItems().addAll("Tous", "Poterie", "Peinture", "Broderie", "Tricot", "Couture", "Bijoux Faits Main");
        categoryFilter.setValue("Tous");

        // Ajouter le listener pour le filtre
        categoryFilter.valueProperty().addListener((obs, oldVal, newVal) -> {
            loadAteliersPage(currentPage); // Recharge la page avec la catégorie sélectionnée
        });
        // Chargement initial
        loadAteliersPage(currentPage);

    }

    private void loadAteliersPage(int page) {
        String selectedCategory = categoryFilter.getValue();

        // 1️⃣ Charger TOUS les ateliers
        List<Ateliers> allAteliers = atelierService.getAllAteliers();

        // 2️⃣ Filtrer selon la catégorie
        List<Ateliers> filteredAteliers = allAteliers.stream()
                .filter(a -> selectedCategory == null
                        || "Tous".equalsIgnoreCase(selectedCategory)
                        || selectedCategory.equalsIgnoreCase(a.getCategorie()))
                .collect(Collectors.toList());

        // 3️⃣ Déterminer les ateliers à afficher sur cette page
        int fromIndex = (page - 1) * rowsPerPage;
        int toIndex = Math.min(fromIndex + rowsPerPage, filteredAteliers.size());

        List<Ateliers> ateliersToShow = (fromIndex <= toIndex) ? filteredAteliers.subList(fromIndex, toIndex) : new ArrayList<>();

        // 🔥 Nettoyer l'affichage
        atelierListView.getChildren().clear();
        atelierListView.getChildren().add(createTableHeader());

        // 🔥 Ajouter les ateliers filtrés paginés
        for (Ateliers atelier : ateliersToShow) {
            HBox row = createAtelierRow(atelier);
            VBox.setMargin(row, new Insets(5, 0, 5, 0));
            atelierListView.getChildren().add(row);
        }

        // 4️⃣ Mettre à jour la pagination pour refléter les résultats filtrés
        generatePagination(filteredAteliers.size());
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

    private Label createBoldLabel(String text, int width) {
        Label label = new Label(text);
        label.setPrefWidth(width);
        label.setStyle("-fx-font-weight: bold; -fx-background-color: #ececec; -fx-padding: 5;");
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
                loadAteliersPage(pageIndex); // recharge page courante avec le filtre actif
            });

            pageBtn.getStyleClass().add("pagination-button");

            if (pageIndex == currentPage) {
                pageBtn.setStyle("-fx-background-color: #3a4c68; -fx-text-fill: white;");
            }

            paginationContainer.getChildren().add(pageBtn);
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
            loadAteliersPage(currentPage);
        }
    }



    private void showAlert(Alert.AlertType alertType, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle("Alerte");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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

        Label titreLabel = createHeaderLabel("Titre", 150);
        Label categorieLabel = createHeaderLabel("Catégorie", 120);
        Label descriptionLabel = createHeaderLabel("Description", 220);
        Label niveauLabel = createHeaderLabel("Niveau", 100);
        Label prixLabel = createHeaderLabel("Prix", 80);
        Label dateCoursLabel = createHeaderLabel("Date Cours", 150);
        Label dureeLabel = createHeaderLabel("Durée", 80);
        Label lienLabel = createHeaderLabel("Lien", 150);
        Label inscritsLabel = createHeaderLabel("Inscrits", 80);
        Label actionsLabel = createHeaderLabel("Actions", 80);

        header.getChildren().addAll(
                titreLabel, categorieLabel, descriptionLabel, niveauLabel,
                prixLabel, dateCoursLabel, dureeLabel, lienLabel, inscritsLabel, actionsLabel
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

        Label prix = createLabel(String.format("%.2f €", atelier.getPrix()), 80);
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

        // Actions bouton supprimer
        Button deleteBtn = new Button("🗑");
        deleteBtn.setOnAction(e -> supprimerAtelier(atelier));
        deleteBtn.getStyleClass().add("delete-button");

        HBox actions = new HBox(5, deleteBtn);
        actions.setAlignment(Pos.CENTER);
        actions.setPrefWidth(80);

        row.getChildren().addAll(
                titre, categorie, description, niveau, prix, date, duree, lien, inscritsBtn, actions
        );

        return row;
    }

    private Label createLabel(String text, double width) {
        Label label = new Label(text);
        label.setPrefWidth(width);
        label.setWrapText(true);
        label.getStyleClass().add("cell-text");
        return label;
    }




}