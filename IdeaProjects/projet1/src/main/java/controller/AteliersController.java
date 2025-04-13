package controller;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Ateliers;
import service.AteliersService;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class AteliersController {

    @FXML private TableView<Ateliers> atelierTable;
    @FXML private HBox paginationContainer;
    @FXML private VBox rootContainer;


    private final AteliersService atelierService = new AteliersService();
    private int currentPage = 1;
    private final int rowsPerPage = 5;

    @FXML
    public void initialize() {
        setupTableColumns();
        loadAteliersPage(currentPage);
    }

    private void setupTableColumns() {
        // Vos colonnes existantes
        TableColumn<Ateliers, String> titreCol = new TableColumn<>("Titre");
        titreCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitre()));

        TableColumn<Ateliers, String> categorieCol = new TableColumn<>("Catégorie");
        categorieCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCategorie()));

        TableColumn<Ateliers, String> descriptionCol = new TableColumn<>("Description");
        descriptionCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDescription()));

        TableColumn<Ateliers, String> niveauDiffCol = new TableColumn<>("Niveau Diff.");
        niveauDiffCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNiveau_diff()));

        TableColumn<Ateliers, Double> prixCol = new TableColumn<>("Prix");
        prixCol.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getPrix()).asObject());

        TableColumn<Ateliers, String> dateCoursCol = new TableColumn<>("Date Cours");
        dateCoursCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDatecours().toString()));

        TableColumn<Ateliers, Integer> dureeCol = new TableColumn<>("Durée");
        dureeCol.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getDuree()).asObject());

        TableColumn<Ateliers, String> lienCol = new TableColumn<>("Lien");
        lienCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLien()));

        TableColumn<Ateliers, Void> actionsColumn = new TableColumn<>("Actions");
        actionsColumn.setPrefWidth(120);

        // Configuration de la colonne Actions
        actionsColumn.setCellFactory(param -> new TableCell<Ateliers, Void>() {
            private final Button editButton = new Button();
            private final Button deleteButton = new Button();
            private final Button viewMoreButton = new Button();
            private final HBox pane = new HBox(5);

            {
                editButton.setText("\uD83D\uDD8A"); // Icône de crayon
                deleteButton.setText("\uD83D\uDDD1"); // Icône de corbeille

                editButton.setStyle("-fx-background-color: transparent;");
                deleteButton.setStyle("-fx-background-color: transparent;");

                editButton.setOnAction(event -> {
                    Ateliers atelier = getTableView().getItems().get(getIndex());

                    // Initialize FXMLLoader for the AjouterAtelier form
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/ajouterAtelier.fxml"));
                        Parent formView = loader.load(); // Load the form view

                        // Get the controller of the form
                        AjouterAtelierController controller = loader.getController();
                        controller.setAtelier(atelier); // Pass the atelier to be edited

                        // Display the form in the parent container
                        controller.setParentContainer(rootContainer);
                        rootContainer.getChildren().setAll(formView); // Display the form in the same container

                    } catch (IOException e) {
                        e.printStackTrace();
                        showAlert(Alert.AlertType.ERROR, "Erreur lors de l'ouverture du formulaire d'édition.");
                    }
                });

                deleteButton.setOnAction(event -> {
                    Ateliers atelier = getTableView().getItems().get(getIndex());

                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Confirmation");
                    alert.setHeaderText("Suppression d'un atelier");
                    alert.setContentText("Voulez-vous vraiment supprimer : " + atelier.getTitre() + " ?");

                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        atelierService.supprimerAtelier(atelier.getId()); // ✅ Appel correct
                        getTableView().getItems().remove(atelier); // Mise à jour de l'affichage
                    }
                });


                pane.getChildren().addAll(editButton, deleteButton);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(pane);
                }
            }
        });

        // Ajouter toutes les colonnes à la TableView
        atelierTable.getColumns().addAll(titreCol, categorieCol, descriptionCol, niveauDiffCol, prixCol, dateCoursCol, dureeCol, lienCol, actionsColumn);
    }



    private void loadAteliersPage(int page) {
        List<Ateliers> ateliers = atelierService.getAteliersPage(page, rowsPerPage);
        atelierTable.getItems().setAll(ateliers);
        generatePagination();
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

    @FXML
    private void ouvrirAjoutAtelier() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/ajouterAtelier.fxml"));
            Parent formView = loader.load();

            AjouterAtelierController formController = loader.getController();
            formController.setParentContainer(rootContainer);

            rootContainer.getChildren().setAll(formView); // affiche le formulaire dans le même conteneur

        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Impossible d’ouvrir la fenêtre d’ajout.");
            alert.show();
        }
    }
    private void showAlert(Alert.AlertType alertType, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle("Alert");
        alert.setHeaderText(null);  // Optional, if you don't want a header
        alert.setContentText(message);
        alert.showAndWait();
    }




}
