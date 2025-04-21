package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import model.Fournisseur;
import service.FournisseurService;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class TableFournisseurController {

    @FXML private TextField searchField;
    @FXML private ListView<Fournisseur> listViewFournisseurs;
    @FXML private HBox paginationContainer;
    @FXML private Button btnAjouter;

    private FournisseurService fournisseurService;
    private int currentPage = 1;
    private final int rowsPerPage = 5;

    @FXML
    public void initialize() {
        try {
            fournisseurService = new FournisseurService();
            configureListView();
            loadFournisseursPage(currentPage);
        } catch (SQLException e) {
            showErrorAlert("Erreur d'initialisation", "Impossible de se connecter à la base de données");
            e.printStackTrace();
        }
    }

    private void configureListView() {
        listViewFournisseurs.setCellFactory(param -> new ListCell<Fournisseur>() {
            @Override
            protected void updateItem(Fournisseur fournisseur, boolean empty) {
                super.updateItem(fournisseur, empty);
                if (empty || fournisseur == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    HBox row = new HBox(10);
                    row.setStyle("-fx-padding: 5;");

                    Label nameLabel = new Label(fournisseur.getNomFournisseur());
                    nameLabel.setPrefWidth(200);

                    Label addressLabel = new Label(fournisseur.getAdresse());
                    addressLabel.setPrefWidth(250);

                    Label contactLabel = new Label(fournisseur.getContact());
                    contactLabel.setPrefWidth(150);

                    Button modifyButton = new Button();
                    ImageView editIcon = new ImageView(new Image(getClass().getResourceAsStream("/assets/icons/modifier.png")));
                    editIcon.setFitHeight(23);
                    editIcon.setFitWidth(23);
                    modifyButton.setGraphic(editIcon);
                    modifyButton.setStyle("-fx-background-color: transparent;");
                    modifyButton.setOnAction(event -> handleModifier(fournisseur));

                    Button deleteButton = new Button();
                    ImageView deleteIcon = new ImageView(new Image(getClass().getResourceAsStream("/assets/icons/supprimer.png")));
                    deleteIcon.setFitHeight(23);
                    deleteIcon.setFitWidth(23);
                    deleteButton.setGraphic(deleteIcon);
                    deleteButton.setStyle("-fx-background-color: transparent;");
                    deleteButton.setOnAction(event -> handleSupprimer(fournisseur));

                    HBox actionsBox = new HBox(5, modifyButton, deleteButton);
                    actionsBox.setPrefWidth(160);

                    row.getChildren().addAll(nameLabel, addressLabel, contactLabel, actionsBox);
                    setGraphic(row);
                }
            }
        });
    }

    private void loadFournisseursPage(int page) throws SQLException {
        List<Fournisseur> fournisseurs = fournisseurService.getFournisseursPage(page, rowsPerPage);
        listViewFournisseurs.getItems().setAll(fournisseurs);
        currentPage = page;
        generatePagination();
    }

    private void generatePagination() throws SQLException {
        paginationContainer.getChildren().clear();
        int total = fournisseurService.countFournisseurs();
        int totalPages = (int) Math.ceil((double) total / rowsPerPage);

        for (int i = 1; i <= totalPages; i++) {
            Button pageBtn = new Button(String.valueOf(i));
            final int pageIndex = i;

            pageBtn.setOnAction(e -> {
                try {
                    loadFournisseursPage(pageIndex);
                } catch (SQLException ex) {
                    showErrorAlert("Erreur", "Impossible de charger la page " + pageIndex);
                }
            });

            pageBtn.getStyleClass().add("pagination-button");
            if (i == currentPage) {
                pageBtn.setStyle("-fx-background-color: #3a4c68; -fx-text-fill: white;");
            }
            paginationContainer.getChildren().add(pageBtn);
        }
    }

    @FXML
    private void handleAjouter() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/AjouterFournissseur.fxml"));
            Parent form = loader.load();

            // Accéder au DashboardController parent
            Scene currentScene = btnAjouter.getScene();
            Parent dashboardRoot = currentScene.getRoot();
            StackPane mainContent = (StackPane) dashboardRoot.lookup("#mainContent");

            if (mainContent != null) {
                mainContent.getChildren().setAll(form);
            } else {
                // Fallback si la structure est différente
                Stage stage = (Stage) btnAjouter.getScene().getWindow();
                stage.setScene(new Scene(form));
            }
        } catch (IOException e) {
            showErrorAlert("Erreur", "Impossible d'ouvrir le formulaire d'ajout");
            e.printStackTrace();
        }
    }

    private void handleModifier(Fournisseur fournisseur) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/ModifierFournisseur.fxml"));
            Parent form = loader.load();

            ModifierFournisseurController controller = loader.getController();
            controller.setFournisseur(fournisseur);

            // Accéder au DashboardController parent
            Scene currentScene = listViewFournisseurs.getScene();
            Parent dashboardRoot = currentScene.getRoot();
            StackPane mainContent = (StackPane) dashboardRoot.lookup("#mainContent");

            if (mainContent != null) {
                mainContent.getChildren().setAll(form);
            } else {
                // Fallback si la structure est différente
                Stage stage = (Stage) listViewFournisseurs.getScene().getWindow();
                stage.setScene(new Scene(form));
            }
        } catch (IOException e) {
            showErrorAlert("Erreur", "Impossible d'ouvrir le formulaire de modification");
            e.printStackTrace();
        }
    }

    private void handleSupprimer(Fournisseur fournisseur) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText("Supprimer " + fournisseur.getNomFournisseur());
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer ce fournisseur ?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                fournisseurService.delete(fournisseur.getId());
                showAlert("Succès", "Fournisseur supprimé avec succès");
                loadFournisseursPage(currentPage);
            } catch (SQLException e) {
                String msg = e.getMessage().contains("utilisé") ?
                        "Impossible de supprimer : ce fournisseur est utilisé par des matériaux" :
                        "Erreur : " + e.getMessage();
                showErrorAlert("Erreur", msg);
            }
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}