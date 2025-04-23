package controller;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Side;
import javafx.util.Duration;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import model.Material;
import model.User;
import service.MateriauxService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.PieChart;
import service.WhatsAppService;

public class TableMateriauxController {

    @FXML private TextField searchField;
    @FXML private ListView<Material> listViewMateriaux;
    @FXML private HBox paginationContainer;
    @FXML private Button btnAjouter;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private PieChart pieChartQuantites;

    private MateriauxService materiauxService;
    private int currentPage = 1;
    private final int rowsPerPage = 5;
    private List<Material> allMateriaux;
    private User connectedUser;
    private WhatsAppService whatsappService;


    @FXML
    public void initialize() {
        try {
            materiauxService = new MateriauxService();
            whatsappService = new WhatsAppService();
            allMateriaux = materiauxService.getAll();
            //stat
            populatePieChart(allMateriaux);
            configureListView();
            setupCategoryComboBox();

            loadMateriauxPage(currentPage);
            generatePagination();
            setupButtons();

            //recherche
            setupSearchListener();
            List<Material> lowGlobal = allMateriaux.stream()
                    .filter(m -> m.getQuantiteStock() < m.getSeuilMin())
                    .collect(Collectors.toList());
            if (!lowGlobal.isEmpty()) {
                showLowStockAlert(lowGlobal);
                // envoi asynchrone du WhatsApp
                new Thread(() -> {
                    try {
                        whatsappService.sendLowStockAlert(lowGlobal);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
            }


        } catch (SQLException e) {
            showErrorAlert("Erreur d'initialisation", "Impossible de se connecter à la base de données");
            e.printStackTrace();
        }
    }

    private void setupCategoryComboBox() {
        categoryComboBox.getItems().addAll(
                "Toutes",
                "Peinture",
                "Poterie",
                "Tissu",
                "Bois",
                "Bijoux",
                "Autre"
        );
        categoryComboBox.getSelectionModel().selectFirst();

        categoryComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                filterMateriauxBySelectedCategory(newValue);
            }
        });
    }

    private void setupSearchListener() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                searchMateriauxByName(newValue);
            } catch (SQLException e) {
                showErrorAlert("Erreur", "Impossible de filtrer les matériaux");
                e.printStackTrace();
            }
        });
    }

    private void filterMateriauxBySelectedCategory(String selectedCategory) {
        List<Material> filteredList;

        if (selectedCategory.equals("Toutes")) {
            filteredList = allMateriaux;
        } else {
            filteredList = allMateriaux.stream()
                    .filter(m -> m.getCategorie().equalsIgnoreCase(selectedCategory))
                    .collect(Collectors.toList());
        }

        updateListView(filteredList);
        currentPage = 1; // Reset à la première page après filtrage
    }

    private void configureListView() {
        listViewMateriaux.setCellFactory(param -> new ListCell<Material>() {
            @Override
            protected void updateItem(Material material, boolean empty) {
                super.updateItem(material, empty);
                if (empty || material == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    HBox row = new HBox(10);
                    row.setStyle("-fx-padding: 5;");

                    Label photoLabel = new Label(material.getPhoto());
                    photoLabel.setPrefWidth(100);

                    Label nameLabel = new Label(material.getNomMateriel());
                    nameLabel.setPrefWidth(150);

                    Label categoryLabel = new Label(material.getCategorie());
                    categoryLabel.setPrefWidth(100);

                    Label quantityLabel = new Label(String.valueOf(material.getQuantiteStock()));
                    quantityLabel.setPrefWidth(80);

                    if (material.getQuantiteStock() < material.getSeuilMin()) {
                        quantityLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                        row.setStyle("-fx-background-color: #ffcccc;");
                    }

                    Label priceLabel = new Label(String.valueOf(material.getPrixUnitaire()));
                    priceLabel.setPrefWidth(80);

                    Button modifyButton = new Button();
                    ImageView editIcon = new ImageView(new Image(getClass().getResourceAsStream("/assets/icons/modifier.png")));
                    editIcon.setFitHeight(23);
                    editIcon.setFitWidth(23);
                    modifyButton.setGraphic(editIcon);
                    modifyButton.setStyle("-fx-background-color: transparent;");
                    modifyButton.setOnAction(event -> handleModifier(material));

                    Button deleteButton = new Button();
                    ImageView deleteIcon = new ImageView(new Image(getClass().getResourceAsStream("/assets/icons/supprimer.png")));
                    deleteIcon.setFitHeight(23);
                    deleteIcon.setFitWidth(23);
                    deleteButton.setGraphic(deleteIcon);
                    deleteButton.setStyle("-fx-background-color: transparent;");
                    deleteButton.setOnAction(event -> handleDelete(material));

                    HBox actionsBox = new HBox(5, modifyButton, deleteButton);
                    actionsBox.setPrefWidth(160);

                    row.getChildren().addAll(photoLabel, nameLabel, categoryLabel, quantityLabel, priceLabel, actionsBox);
                    setGraphic(row);
                }
            }
        });
    }

    private void loadMateriauxPage(int page) throws SQLException {
        List<Material> materiaux = materiauxService.getMateriauxPage(page, rowsPerPage);
        Platform.runLater(() -> {
            listViewMateriaux.getItems().setAll(materiaux);
            currentPage = page;

            List<Material> lowStockMaterials = materiaux.stream()
                    .filter(m -> m.getQuantiteStock() < m.getSeuilMin())
                    .collect(Collectors.toList());

            if (!lowStockMaterials.isEmpty()) {
                showLowStockAlert(lowStockMaterials);
            }
            // rafraîchir les boutons
            try {
                generatePagination();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
    }

    private void updateListView(List<Material> materials) {
        Platform.runLater(() -> {
            listViewMateriaux.getItems().setAll(materials);
        });
    }

    private void generatePagination() throws SQLException {
        paginationContainer.getChildren().clear();
        int total = materiauxService.countMateriaux();
        int totalPages = (int) Math.ceil((double) total / rowsPerPage);

        for (int i = 1; i <= totalPages; i++) {
            Button pageBtn = new Button(String.valueOf(i));
            final int pageIndex = i;

            pageBtn.setOnAction(e -> {
                try {
                    loadMateriauxPage(pageIndex);
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

    private void setupButtons() {
        ImageView addIcon = new ImageView(new Image(getClass().getResourceAsStream("/assets/icons/ajouter.png")));
        addIcon.setFitHeight(23);
        addIcon.setFitWidth(23);
        btnAjouter.setGraphic(addIcon);
        btnAjouter.setOnAction(event -> handleAjouter());
    }

    @FXML
    private void handleAjouter() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/AjouterMateriaux.fxml"));
            Parent form = loader.load();
            AjoutMaterielController controller = loader.getController();
            controller.setConnectedUser(connectedUser);

            Scene currentScene = btnAjouter.getScene();
            Parent dashboardRoot = currentScene.getRoot();
            StackPane mainContent = (StackPane) dashboardRoot.lookup("#mainContent");

            if (mainContent != null) {
                mainContent.getChildren().setAll(form);
            } else {
                Stage stage = (Stage) btnAjouter.getScene().getWindow();
                stage.setScene(new Scene(form));
            }
        } catch (IOException e) {
            showErrorAlert("Erreur", "Impossible d'ouvrir le formulaire d'ajout");
            e.printStackTrace();
        }
    }

    private void handleModifier(Material material) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/ModifierMateriaux.fxml"));
            Parent form = loader.load();

            ModifierMaterielController controller = loader.getController();
            controller.initData(material);

            Scene currentScene = listViewMateriaux.getScene();
            Parent dashboardRoot = currentScene.getRoot();
            StackPane mainContent = (StackPane) dashboardRoot.lookup("#mainContent");

            if (mainContent != null) {
                mainContent.getChildren().setAll(form);
            } else {
                Stage stage = (Stage) listViewMateriaux.getScene().getWindow();
                stage.setScene(new Scene(form));
            }
        } catch (IOException e) {
            showErrorAlert("Erreur", "Impossible d'ouvrir le formulaire de modification");
            e.printStackTrace();
        }
    }

    private void handleDelete(Material material) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText("Supprimer " + material.getNomMateriel());
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer ce matériel ?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                materiauxService.delete(material.getId());
                showAlert("Succès", "Matériel supprimé avec succès");
                // Recharger tous les matériaux après suppression
                allMateriaux = materiauxService.getAll();
                loadMateriauxPage(currentPage);
            } catch (SQLException e) {
                showErrorAlert("Erreur", "Échec de la suppression : " + e.getMessage());
            }
        }
    }

    private void searchMateriauxByName(String query) throws SQLException {
        if (query == null || query.isEmpty()) {
            loadMateriauxPage(currentPage);
        } else {
            List<Material> filteredMaterials = allMateriaux.stream()
                    .filter(material -> material.getNomMateriel().toLowerCase().contains(query.toLowerCase()))
                    .collect(Collectors.toList());

            updateListView(filteredMaterials);
        }
    }

    private void showLowStockAlert(List<Material> lowStockMaterials) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Alerte de stock faible");
        alert.setHeaderText("Matériaux en stock critique");

        ImageView icon = new ImageView(new Image(getClass().getResourceAsStream("/assets/icons/alerte.png")));
        icon.setFitHeight(30);
        icon.setFitWidth(30);
        alert.setGraphic(icon);

        StringBuilder content = new StringBuilder("Les matériaux suivants sont en dessous du seuil minimal:\n\n");
        for (Material material : lowStockMaterials) {
            content.append("- ").append(material.getNomMateriel())
                    .append(" (Stock: ").append(material.getQuantiteStock())
                    .append(", Seuil min: ").append(material.getSeuilMin()).append(")\n");
        }

        alert.setContentText(content.toString());
        alert.getDialogPane().setOpacity(0);
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(alert.getDialogPane().opacityProperty(), 0)),
                new KeyFrame(Duration.seconds(0.3), new KeyValue(alert.getDialogPane().opacityProperty(), 1))
        );
        timeline.play();
        alert.showAndWait();
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

    public void setConnectedUser(User user) {
        this.connectedUser = user;
    }
    private void populatePieChart(List<Material> materials) {
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        for (Material m : materials) {
            if (m.getQuantiteStock() > 0) {
                pieData.add(new PieChart.Data(
                        m.getNomMateriel() + " (" + m.getQuantiteStock() + ")",
                        m.getQuantiteStock()
                ));
            }
        }
        pieChartQuantites.setData(pieData);
        pieChartQuantites.setTitle("Répartition des stocks de matériaux");
        pieChartQuantites.setLegendVisible(true);
        pieChartQuantites.setLabelsVisible(true);

        // taille
        pieChartQuantites.setPrefSize(300, 200);
        pieChartQuantites.setMinSize(300, 200);

        // légende à droite
        pieChartQuantites.setLegendSide(Side.RIGHT);

        // tooltips
        for (PieChart.Data d : pieChartQuantites.getData()) {
            Tooltip.install(d.getNode(),
                    new Tooltip(d.getName() + " : " + (int)d.getPieValue()));
        }
    }




}