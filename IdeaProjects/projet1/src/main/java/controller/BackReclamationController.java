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

public class BackReclamationController {
    @FXML private TableView<Reclamation> reclamationTable;
    @FXML private TableColumn<Reclamation, Number> idColumn;
    @FXML private TableColumn<Reclamation, String> dateColumn;
    @FXML private TableColumn<Reclamation, String> statutColumn;
    @FXML private TableColumn<Reclamation, Void> actionsColumn;
    @FXML private Pagination pagination;

    private final ReclamationService reclamationService = new ReclamationService();
    private final ReponseService reponseService = new ReponseService();

    @FXML
    public void initialize() {
        configureTable();
        configurePagination();
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
        int itemsPerPage = 10;
        reclamationTable.getItems().setAll(
                reclamationService.getReclamations(pageIndex * itemsPerPage, itemsPerPage)
        );
    }
}