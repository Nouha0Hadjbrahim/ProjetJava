package controller;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.Ateliers;
import service.AteliersService;
import service.InscriptionAtelierService;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AteliersAdminController {

    @FXML
    private ListView<Ateliers> atelierListView;
    @FXML private HBox paginationContainer;
    @FXML private ComboBox<String> categoryFilter;

    private final AteliersService atelierService = new AteliersService();
    private final InscriptionAtelierService inscriptionService = new InscriptionAtelierService();
    private int currentPage = 1;
    private final int rowsPerPage = 5;

    @FXML
    public void initialize() {

        loadAteliersPage(currentPage);
        categoryFilter.getItems().addAll("Tous", "Poterie", "Peinture", "Broderie", "Tricot", "Couture", "Bijoux Faits Main");
        categoryFilter.setValue("Tous");

        // Ajouter le listener pour le filtre
        categoryFilter.valueProperty().addListener((obs, oldVal, newVal) -> {
            loadAteliersPage(currentPage); // Recharge la page avec la catégorie sélectionnée
        });
    }

    private void loadAteliersPage(int page) {
        List<Ateliers> ateliers = atelierService.getAteliersPage(page, rowsPerPage);

        // Filtrage par catégorie
        String selectedCategory = categoryFilter.getValue();
        if (selectedCategory != null && !selectedCategory.equals("Tous")) {
            ateliers = ateliers.stream()
                    .filter(a -> selectedCategory.equals(a.getCategorie()))
                    .collect(Collectors.toList());
        }

        atelierListView.getItems().clear();

        // Ajouter une ligne d'en-tête
        atelierListView.getItems().add(new Ateliers()); // Dummy pour l'en-tête

        atelierListView.getItems().addAll(ateliers);

        atelierListView.setCellFactory(listView -> new ListCell<Ateliers>() {
            @Override
            protected void updateItem(Ateliers atelier, boolean empty) {
                super.updateItem(atelier, empty);
                if (empty || atelier == null) {
                    setGraphic(null);
                    return;
                }

                HBox row = new HBox();
                row.setSpacing(10);
                row.setStyle("-fx-padding: 10; -fx-border-color: lightgray; -fx-border-width: 0 0 1 0;");

                // Formatage de la date
                String dateCours = (atelier.getDatecours() != null)
                        ? atelier.getDatecours().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                        : "";

                // Vérifie si c'est la première ligne = en-tête
                if (atelier.getPrix() == 0.0 && atelier.getDatecours() == null && atelier.getDuree() == 0) {
                    row.getChildren().addAll(
                            createBoldLabel("Titre", 150),
                            createBoldLabel("Catégorie", 150),
                            createBoldLabel("Description", 200),
                            createBoldLabel("Niveau", 100),
                            createBoldLabel("Prix", 80),
                            createBoldLabel("Date", 160),
                            createBoldLabel("Durée", 80),
                            createBoldLabel("Lien", 150),
                            createBoldLabel("Inscrits", 80), // Nouvelle colonne
                            createBoldLabel("Actions", 80)
                    );
                } else {
                    // Affichage des autres champs sans id et user_id
                    Label titre = createLabel(atelier.getTitre(), 150);
                    Label categorie = createLabel(atelier.getCategorie(), 150);
                    Label description = createLabel(atelier.getDescription(), 200);
                    Label niveau = createLabel(atelier.getNiveau_diff(), 100);
                    Label prix = createLabel(String.valueOf(atelier.getPrix()), 80);
                    Label date = createLabel(dateCours, 160);
                    Label duree = createLabel(atelier.getDuree() + " min", 80);
                    Label lien = createLabel(atelier.getLien(), 150);

                    // Ajout du nombre d'inscrits avec bouton
                    int nbInscrits = inscriptionService.getNombreInscriptions(atelier.getId());
                    Button inscritsBtn = new Button(nbInscrits + " inscrits");
                    inscritsBtn.setStyle("-fx-background-color: #e3f2fd;");
                    inscritsBtn.setOnAction(e -> showInscrits(atelier));

                    Button deleteButton = new Button("\uD83D\uDDD1");
                    deleteButton.setStyle("-fx-background-color: transparent;");
                    deleteButton.setOnAction(event -> supprimerAtelier(atelier));

                    HBox actions = new HBox(5, deleteButton);
                    actions.setPrefWidth(120);
                    actions.setAlignment(Pos.CENTER);

                    row.getChildren().addAll(titre, categorie, description, niveau, prix, date, duree, lien, inscritsBtn, actions);
                }

                setGraphic(row);
            }
        });

        generatePagination();
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

    private void generatePagination() {
        paginationContainer.getChildren().clear();
        int total = atelierService.countAteliers();
        int totalPages = (int) Math.ceil((double) total / rowsPerPage);

        for (int i = 1; i <= totalPages; i++) {
            int pageIndex = i;
            Button pageBtn = new Button(String.valueOf(pageIndex));
            pageBtn.setOnAction(e -> {
                currentPage = pageIndex;
                loadAteliersPage(pageIndex);
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
}