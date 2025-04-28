package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Reclamation;
import model.User;
import service.ReclamationService;
import utils.SessionManager;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class ReclamationController {
    @FXML
    private TableView<Reclamation> reclamationTable;
    @FXML
    private TableColumn<Reclamation, Integer> idColumn;
    @FXML
    private TableColumn<Reclamation, String> titreColumn;
    @FXML
    private TableColumn<Reclamation, String> statutColumn;
    @FXML
    private TableColumn<Reclamation, LocalDate> dateColumn;
    @FXML
    private TableColumn<Reclamation, String> actionColumn;

    @FXML
    private VBox reclamationContainer;
    @FXML
    private Label noReclamationLabel;

    @FXML
    private TextField titreField;
    @FXML
    private TextArea descriptionField;
    @FXML
    private Button submitButton;

    @FXML
    private Label titreErrorLabel;

    @FXML
    private Label descriptionErrorLabel;

    @FXML private Pagination pagination;
    private final int itemsPerPage = 5;

    private final ReclamationService reclamationService = new ReclamationService();
    private static final int ITEMS_PER_PAGE = 5;
    private static final int TITRE_MIN_LENGTH = 3;
    private static final int TITRE_MAX_LENGTH = 6;
    private static final int DESCRIPTION_MIN_LENGTH = 3;
    private static final int DESCRIPTION_MAX_LENGTH = 500;
    private static final List<String> MOTS_INTERDITS = List.of("spam", "insulte");

    @FXML
    public void initialize() {
        setupTableColumns();
        setupActionColumn();
        setupFormValidation();
        loadInitialData();
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        titreColumn.setCellValueFactory(new PropertyValueFactory<>("titre"));
        statutColumn.setCellValueFactory(new PropertyValueFactory<>("statut"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("dateReclamation"));
    }

    private void setupActionColumn() {
        actionColumn.setCellFactory(column -> new TableCell<>() {
            private final Button btn = new Button("Consulter");

            {
                btn.setOnAction(event -> {
                    Reclamation reclamation = getTableView().getItems().get(getIndex());
                    openReclamationDetails(reclamation);
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
    }

    private void openReclamationDetails(Reclamation reclamation) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fornt views/ReclamationDetails.fxml"));
            Parent root = loader.load();
            ReclamationDetailsController controller = loader.getController();
            controller.setReclamation(reclamation);
            controller.setRefreshCallback(this::refreshData);

            Stage stage = new Stage();
            stage.setTitle("Détails de la réclamation");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupFormValidation() {
        titreField.textProperty().addListener((obs, oldText, newText) -> validateInputs());
        descriptionField.textProperty().addListener((obs, oldText, newText) -> validateInputs());
        submitButton.setOnAction(event -> addReclamation());
        validateInputs();
    }

    private void loadInitialData() {
        if (SessionManager.getCurrentUser() == null) {
            showAlert("Erreur", "Aucun utilisateur connecté");
            return;
        }
        refreshData();
        setupPagination();
    }

    private void refreshData() {
        loadReclamations();
        updateUI();
    }

    public void loadReclamations() {
        reclamationTable.getItems().clear();
        try {
            List<Reclamation> reclamations = reclamationService.getReclamationsForCurrentUser(
                    pagination.getCurrentPageIndex() * ITEMS_PER_PAGE,
                    ITEMS_PER_PAGE
            );
            reclamationTable.getItems().addAll(reclamations);
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors du chargement des réclamations");
            e.printStackTrace();
        }
    }

    private void updateUI() {
        boolean hasReclamations = !reclamationTable.getItems().isEmpty();
        reclamationTable.setVisible(hasReclamations);
        noReclamationLabel.setVisible(!hasReclamations);
        pagination.setVisible(hasReclamations);

        noReclamationLabel.setText(
                !hasReclamations && pagination.getCurrentPageIndex() > 0
                        ? "Aucune réclamation sur cette page"
                        : "Vous n'avez rien réclamé"
        );
    }

    @FXML
    private void addReclamation() {
        String titre = titreField.getText().trim();
        String description = descriptionField.getText().trim();

        if (!validateForm(titre, description)) {
            return;
        }

        try {
            Reclamation newReclamation = reclamationService.addReclamationForCurrentUser(titre, description);
            reclamationTable.getItems().add(newReclamation);
            clearForm();
            updateUI();
            showAlert("Succès", "Réclamation ajoutée avec succès");
        } catch (SQLException e) {
            showAlert("Erreur", "Échec de l'ajout de la réclamation");
            e.printStackTrace();
        }
    }

    private boolean validateForm(String titre, String description) {
        if (titre.isEmpty() || description.isEmpty()) {
            showAlert("Erreur", "Veuillez remplir tous les champs");
            return false;
        }

        if (titre.length() < TITRE_MIN_LENGTH || titre.length() > TITRE_MAX_LENGTH) {
            showAlert("Erreur", String.format(
                    "Le titre doit contenir entre %d et %d caractères",
                    TITRE_MIN_LENGTH, TITRE_MAX_LENGTH
            ));
            return false;
        }

        if (description.length() < DESCRIPTION_MIN_LENGTH || description.length() > DESCRIPTION_MAX_LENGTH) {
            showAlert("Erreur", String.format(
                    "La description doit contenir entre %d et %d caractères",
                    DESCRIPTION_MIN_LENGTH, DESCRIPTION_MAX_LENGTH
            ));
            return false;
        }

        for (String mot : MOTS_INTERDITS) {
            if (titre.toLowerCase().contains(mot.toLowerCase()) ||
                    description.toLowerCase().contains(mot.toLowerCase())) {
                showAlert("Erreur", String.format(
                        "Contenu interdit détecté : \"%s\"",
                        mot
                ));
                return false;
            }
        }
        return true;
    }

    private void validateInputs() {
        String titre = titreField.getText().trim();
        String description = descriptionField.getText().trim();
        boolean isValid = true;

        // Réinitialiser les erreurs
        titreErrorLabel.setVisible(false);
        titreErrorLabel.setManaged(false);
        descriptionErrorLabel.setVisible(false);
        descriptionErrorLabel.setManaged(false);

        // Validation titre
        if (titre.length() < TITRE_MIN_LENGTH || titre.length() > TITRE_MAX_LENGTH) {
            setError(titreErrorLabel, String.format(
                    "Le titre doit contenir entre %d et %d caractères",
                    TITRE_MIN_LENGTH, TITRE_MAX_LENGTH
            ));
            isValid = false;
        } else if (containsForbiddenWord(titre)) {
            setError(titreErrorLabel, "Le titre contient un mot interdit");
            isValid = false;
        }

        // Validation description
        if (description.length() < DESCRIPTION_MIN_LENGTH || description.length() > DESCRIPTION_MAX_LENGTH) {
            setError(descriptionErrorLabel, String.format(
                    "La description doit contenir entre %d et %d caractères",
                    DESCRIPTION_MIN_LENGTH, DESCRIPTION_MAX_LENGTH
            ));
            isValid = false;
        } else if (containsForbiddenWord(description)) {
            setError(descriptionErrorLabel, "La description contient un mot interdit");
            isValid = false;
        }

        submitButton.setDisable(!isValid);
    }

    private boolean containsForbiddenWord(String text) {
        return MOTS_INTERDITS.stream()
                .anyMatch(mot -> text.toLowerCase().contains(mot.toLowerCase()));
    }

    private void setError(Label label, String message) {
        label.setText(message);
        label.setVisible(true);
        label.setManaged(true);
    }

    private void clearForm() {
        titreField.clear();
        descriptionField.clear();
    }

    private void setupPagination() {
        int totalItems = reclamationService.getTotalReclamationsForCurrentUser();
        int pageCount = (int) Math.ceil((double) totalItems / ITEMS_PER_PAGE);
        pagination.setPageCount(Math.max(pageCount, 1));

        pagination.currentPageIndexProperty().addListener((obs, oldIndex, newIndex) -> {
            loadPage(newIndex.intValue());
        });
    }

    private void loadPage(int pageIndex) {
        try {
            List<Reclamation> reclamations = reclamationService.getReclamationsForCurrentUser(
                    pageIndex * ITEMS_PER_PAGE,
                    ITEMS_PER_PAGE
            );
            reclamationTable.getItems().setAll(reclamations);
            updateUI();
            reclamationTable.refresh();
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors du chargement de la page");
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}