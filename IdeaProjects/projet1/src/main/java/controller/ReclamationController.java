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
import service.ReclamationService;
import utils.DBConnection;

import java.io.IOException;
import java.sql.*;
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


    private static final int TITRE_MIN_LENGTH = 3;
    private static final int TITRE_MAX_LENGTH = 6;
    private static final int DESCRIPTION_MIN_LENGTH = 3;
    private static final int DESCRIPTION_MAX_LENGTH = 500;
    private static final List<String> MOTS_INTERDITS = List.of("spam", "insulte");


    @FXML
    public void initialize() {
        // Set up the columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        titreColumn.setCellValueFactory(new PropertyValueFactory<>("titre"));
        statutColumn.setCellValueFactory(new PropertyValueFactory<>("statut"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("dateReclamation"));

        // Capture the controller instance
        ReclamationController controllerInstance = this;

        // Add a button to the action column
        actionColumn.setCellFactory(column -> {
            return new TableCell<Reclamation, String>() {
                final Button btn = new Button("Consulter");

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        setGraphic(btn);
                        btn.setOnAction(event -> {
                            Reclamation reclamation = getTableView().getItems().get(getIndex());
                            try {
                                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fornt views/ReclamationDetails.fxml"));
                                Parent root = loader.load();
                                ReclamationDetailsController controller = loader.getController();
                                controller.setReclamation(reclamation);
                                controller.setRefreshCallback(() -> {
                                    loadReclamations();
                                    updateUI(); // Mettre à jour l'interface
                                });

                                Stage stage = new Stage();
                                stage.setTitle("Détails de la réclamation");
                                stage.setScene(new Scene(root));
                                stage.show();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                    }
                }
            };
        });

        // Load data
        loadReclamations();
        setupPagination();

        // Setup form submission
        submitButton.setOnAction(event -> addReclamation());

        // Check if table is empty and update UI accordingly
        updateUI();

        titreField.textProperty().addListener((obs, oldText, newText) -> validateInputs());
        descriptionField.textProperty().addListener((obs, oldText, newText) -> validateInputs());
        validateInputs(); // appel initial

    }

    public void loadReclamations() {
        reclamationTable.getItems().clear();

        int userId = 3; // ID de l'utilisateur actuellement connecté
        String query = "SELECT id, user_id, titre, description, statut, date_reclamation FROM reclamation WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String titre = rs.getString("titre");
                    String description = rs.getString("description");
                    String statut = rs.getString("statut");
                    LocalDate dateReclamation = rs.getDate("date_reclamation").toLocalDate();

                    Reclamation reclamation = new Reclamation(userId, titre, description, statut, dateReclamation);
                    reclamation.setId(id);

                    reclamationTable.getItems().add(reclamation);
                }
                updateUI(); // Mettre à jour l'interface après le chargement
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateUI() {
        boolean hasReclamations = !reclamationTable.getItems().isEmpty();

        reclamationTable.setVisible(hasReclamations);
        noReclamationLabel.setVisible(!hasReclamations);
        pagination.setVisible(hasReclamations);

        if (!hasReclamations && pagination.getCurrentPageIndex() > 0) {
            // Cas spécial : page vide mais pas la première page
            noReclamationLabel.setText("Aucune réclamation sur cette page");
        } else {
            noReclamationLabel.setText("Vous n'avez rien réclamé");
        }
    }

    @FXML
    private void addReclamation() {

        String titre = titreField.getText().trim();
        String description = descriptionField.getText().trim();

        // Vérification champs vides
        if (titre.isEmpty() || description.isEmpty()) {
            showAlert("Erreur", "Veuillez remplir tous les champs");
            return;
        }

        // Vérification longueur du titre
        if (titre.length() < TITRE_MIN_LENGTH || titre.length() > TITRE_MAX_LENGTH) {
            showAlert("Erreur", "Le titre doit contenir entre " + TITRE_MIN_LENGTH + " et " + TITRE_MAX_LENGTH + " caractères");
            return;
        }

        // Vérification longueur de la description
        if (description.length() < DESCRIPTION_MIN_LENGTH || description.length() > DESCRIPTION_MAX_LENGTH) {
            showAlert("Erreur", "La description doit contenir entre " + DESCRIPTION_MIN_LENGTH + " et " + DESCRIPTION_MAX_LENGTH + " caractères");
            return;
        }

        // Vérification des mots interdits (insensibles à la casse)
        for (String mot : MOTS_INTERDITS) {
            if (titre.toLowerCase().contains(mot.toLowerCase()) || description.toLowerCase().contains(mot.toLowerCase())) {
                showAlert("Erreur", "Le titre ou la description contient un mot interdit : \"" + mot + "\"");
                return;
            }
        }

        // Si tout est OK, procéder à l'insertion
        int userId = 3; // Remplacez par l'ID de l'utilisateur actuel
        String query = "INSERT INTO reclamation (user_id, titre, description, statut, date_reclamation) VALUES (?, ?, ?, 'En attente', ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, userId);
            pstmt.setString(2, titre);
            pstmt.setString(3, description);
            pstmt.setDate(4, Date.valueOf(LocalDate.now()));

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int id = generatedKeys.getInt(1);
                        Reclamation newReclamation = new Reclamation(userId, titre, description, "En attente", LocalDate.now());
                        newReclamation.setId(id);
                        reclamationTable.getItems().add(newReclamation);

                        // Clear fields
                        titreField.clear();
                        descriptionField.clear();

                        // Update UI
                        updateUI();

                        showAlert("Succès", "Réclamation ajoutée avec succès");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Une erreur est survenue lors de l'ajout de la réclamation");
        }
    }

    private void validateInputs() {
        // Récupération du texte des champs
        String titre = titreField.getText().trim();
        String description = descriptionField.getText().trim();

        // Initialiser la validité
        boolean isValid = true;

        // Réinitialiser les messages d'erreur (masquer les labels)
        titreErrorLabel.setVisible(false);
        titreErrorLabel.setManaged(false);
        descriptionErrorLabel.setVisible(false);
        descriptionErrorLabel.setManaged(false);

        // Validation du titre
        if (titre.length() < TITRE_MIN_LENGTH || titre.length() > TITRE_MAX_LENGTH) {
            titreErrorLabel.setText("Le titre doit contenir entre " + TITRE_MIN_LENGTH + " et " + TITRE_MAX_LENGTH + " caractères");
            titreErrorLabel.setVisible(true);
            titreErrorLabel.setManaged(true);
            isValid = false;
        } else {
            // Vérification des mots interdits
            for (String mot : MOTS_INTERDITS) {
                if (titre.toLowerCase().contains(mot)) {
                    titreErrorLabel.setText("Le titre contient un mot interdit : \"" + mot + "\"");
                    titreErrorLabel.setVisible(true);
                    titreErrorLabel.setManaged(true);
                    isValid = false;
                    break;
                }
            }
        }

        // Validation de la description
        if (description.length() < DESCRIPTION_MIN_LENGTH || description.length() > DESCRIPTION_MAX_LENGTH) {
            descriptionErrorLabel.setText("La description doit contenir entre " + DESCRIPTION_MIN_LENGTH + " et " + DESCRIPTION_MAX_LENGTH + " caractères");
            descriptionErrorLabel.setVisible(true);
            descriptionErrorLabel.setManaged(true);
            isValid = false;
        } else {
            // Vérification des mots interdits
            for (String mot : MOTS_INTERDITS) {
                if (description.toLowerCase().contains(mot)) {
                    descriptionErrorLabel.setText("La description contient un mot interdit : \"" + mot + "\"");
                    descriptionErrorLabel.setVisible(true);
                    descriptionErrorLabel.setManaged(true);
                    isValid = false;
                    break;
                }
            }
        }

        // Activation / désactivation du bouton de soumission
        submitButton.setDisable(!isValid);
    }


    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void setupPagination() {
        int userId = 3; // À remplacer par l'ID dynamique si nécessaire
        int totalItems = new ReclamationService().getTotalReclamationsForUser(userId);
        int pageCount = (totalItems + itemsPerPage - 1) / itemsPerPage; // Calcul robuste

        pagination.setPageCount(pageCount > 0 ? pageCount : 1); // Toujours au moins 1 page
        pagination.currentPageIndexProperty().addListener((obs, oldIndex, newIndex) -> {
            loadPage(newIndex.intValue());
        });

        // Charge la première page immédiatement
        loadPage(0);
    }

    private void loadPage(int pageIndex) {
        int userId = 3; // À remplacer par l'ID dynamique
        int offset = pageIndex * itemsPerPage;
        List<Reclamation> reclamations = new ReclamationService().getReclamationsForUser(userId, offset, itemsPerPage);

        reclamationTable.getItems().setAll(reclamations);
        updateUI();

        // Force la mise à jour visuelle
        reclamationTable.refresh();
    }

}