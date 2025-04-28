package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Reclamation;
import service.ReclamationService;
import service.ReponseService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BackReclamationController {
    @FXML private TableView<Reclamation> reclamationTable;
    @FXML private TableColumn<Reclamation, Number> idColumn;
    @FXML private TableColumn<Reclamation, String> dateColumn;
    @FXML private TableColumn<Reclamation, String> statutColumn;
    @FXML private TableColumn<Reclamation, Void> actionsColumn;
    @FXML private Pagination pagination;
    @FXML private ToggleButton toggleArchive;

    //statistique
    @FXML private Label pendingCount;
    @FXML private Label inProgressCount;
    @FXML private Label answeredCount;

    private boolean showArchived = false;

    private final ReclamationService reclamationService = new ReclamationService();
    private final ReponseService reponseService = new ReponseService();

    @FXML
    public void initialize() {
        configureTable();
        configurePagination();
        configureToggle();
        loadStatistics();
        reclamationTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void configureTable() {
        idColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty());
        dateColumn.setCellValueFactory(cellData -> cellData.getValue().dateReclamationProperty().asString());
        statutColumn.setCellValueFactory(cellData -> cellData.getValue().statutProperty());


        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("Consulter");

            {
                btn.getStyleClass().add("action-button");
                btn.setOnAction(event -> {
                    Reclamation rec = getTableView().getItems().get(getIndex());
                    consulterReclamation(rec);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btn);
                }
            }
        });

        reclamationTable.setFixedCellSize(35);
        reclamationTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

    }

    private void consulterReclamation(Reclamation reclamation) {
        try {
            System.out.println("Ouverture des détails pour la réclamation ID: " + reclamation.getId());

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/BackReclamationDetails.fxml"));
            Parent root = loader.load();

            BackReclamationDetailsController controller = loader.getController();
            controller.setReclamation(reclamation);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Détails Réclamation #" + reclamation.getId());
            stage.setScene(new Scene(root));

            // Empêche la fenêtre principale d'être utilisée pendant que la fenêtre de détail est ouverte
            stage.initOwner(reclamationTable.getScene().getWindow());

            stage.showAndWait(); // Utilisez showAndWait() pour une fenêtre modale

            // Rafraîchir les données après fermeture de la fenêtre de détail
            loadPage(pagination.getCurrentPageIndex());

        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de la vue de détail:");
            e.printStackTrace();

            // Afficher une alerte à l'utilisateur
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Impossible d'ouvrir les détails");
            alert.setContentText("Une erreur est survenue lors de l'ouverture des détails de la réclamation.\n\nDétails: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void configurePagination() {
        pagination.setPageFactory(pageIndex -> {
            loadPage(pageIndex);
            return new Label("Page " + (pageIndex + 1));
        });
    }

    private void loadPage(int pageIndex) {
        try {
            int itemsPerPage = 4;
            List<Reclamation> items;

            if (showArchived) {
                items = reclamationService.getArchivedReclamations(pageIndex * itemsPerPage, itemsPerPage);
                pagination.setPageCount(calculatePageCount(reclamationService.getTotalArchivedReclamations(), itemsPerPage));
            } else {
                items = reclamationService.getActiveReclamations(pageIndex * itemsPerPage, itemsPerPage);
                pagination.setPageCount(calculatePageCount(reclamationService.getTotalActiveReclamations(), itemsPerPage));
            }

            // Debug: Afficher le nombre d'items chargés
            System.out.println("Nombre de réclamations chargées: " + items.size());

            reclamationTable.getItems().setAll(items);
            adjustTableViewHeight(items.size());

        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des réclamations:");
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les réclamations");
        }
    }


    private int calculatePageCount(int totalItems, int itemsPerPage) {
        return (totalItems + itemsPerPage - 1) / itemsPerPage;
    }

    private void adjustTableViewHeight(int itemCount) {
        double rowHeight = 35; // Hauteur fixe d'une ligne
        double headerHeight = 30; // Hauteur de l'en-tête
        double borderWidth = 2; // Bordure

        double height = itemCount * rowHeight + headerHeight + borderWidth;
        reclamationTable.setPrefHeight(Math.max(height, headerHeight + borderWidth)); // Hauteur minimale = header + bordure
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void configureToggle() {
        if (toggleArchive != null && pagination != null) { // toujours vérifier
            toggleArchive.setText("Voir les archives");

            toggleArchive.selectedProperty().addListener((obs, oldVal, newVal) -> {
                showArchived = newVal;
                toggleArchive.setText(showArchived ? "Voir les réclamations" : "Voir les archives");

                pagination.setCurrentPageIndex(0); // Réinitialise à la première page
                loadPage(0); // Recharge les données
            });
        } else {
            System.out.println("toggleArchive ou pagination est null !");
        }
    }

    private void loadStatistics() {
        Map<String, Integer> stats = reclamationService.getReclamationStats();
        pendingCount.setText(String.valueOf(stats.getOrDefault("En attente", 0)));
        inProgressCount.setText(String.valueOf(stats.getOrDefault("En cours", 0)));
        answeredCount.setText(String.valueOf(stats.getOrDefault("Répondu", 0)));
    }

    private void refreshAll() {
        loadStatistics();
        loadPage(pagination.getCurrentPageIndex());
    }

}