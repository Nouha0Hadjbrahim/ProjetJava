package controller;

import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import model.User;
import service.UserService;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.beans.Observable;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.Arrays;
import java.io.IOException;
import javafx.scene.input.MouseEvent;

import java.util.ArrayList;
import java.util.stream.Collectors;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.Interpolator;
public class TableUtilisateursController {

    @FXML private TextField searchField;
    @FXML private VBox userListContainer;
    @FXML private HBox paginationContainer;
    @FXML private HBox searchBox;
    @FXML private ComboBox<String> statusFilter;
    private final UserService userService = new UserService();
    private int currentPage = 1;
    private final int rowsPerPage = 5;
    private boolean isAscendingOrder = true;
    private List<User> currentUsers = new ArrayList<>();
    @FXML private StackPane micContainer;
    @FXML private ImageView micIcon;
    @FXML private Circle recordingIndicator;
    private ScaleTransition recordingAnimation;


    @FXML
    public void initialize() {
        // Initialisation du ComboBox de filtrage
        statusFilter.getItems().addAll("Tous", "Active", "Blocked");
        statusFilter.setValue("Tous");
        micIcon.setImage(new Image(getClass().getResourceAsStream("/assets/icons/micro.png")));

        // Ajout des écouteurs pour la recherche et le filtre
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            currentPage = 1;
            loadUsersPage(currentPage);
        });

        statusFilter.valueProperty().addListener((obs, oldVal, newVal) -> {
            currentPage = 1;
            loadUsersPage(currentPage);
        });

        // Chargement initial
        loadUsersPage(currentPage);
        animateSearchBar();
    }

    private void loadUsersPage(int page) {
        try {
            String keyword = searchField.getText();
            String status = statusFilter.getValue();

            // Récupération des utilisateurs filtrés
            List<User> filteredUsers = userService.searchUsersPageSQL(
                keyword != null ? keyword.trim() : "", 
                status != null ? status : "Tous", 
                page, 
                rowsPerPage
            );

            // Mise à jour de l'interface utilisateur
            updateUserList(filteredUsers);
            generatePagination();
        } catch (Exception e) {
            e.printStackTrace();
            // Optionnel : Afficher un message d'erreur à l'utilisateur
            showAlert("Erreur", "Une erreur est survenue lors du chargement des données.");
        }
    }

    private void updateUserList(List<User> users) {
        currentUsers = new ArrayList<>(users); // Garde une copie de la liste actuelle
        userListContainer.getChildren().clear();
        
        // Ajouter l'en-tête
        userListContainer.getChildren().add(createTableHeader());
        
        // Ajouter les lignes d'utilisateurs
        users.forEach(user -> {
            HBox row = createUserRow(user);
            VBox.setMargin(row, new Insets(5, 0, 5, 0));
            userListContainer.getChildren().add(row);
        });
    }

    private HBox createTableHeader() {
        HBox header = new HBox(10);
        header.getStyleClass().add("table-header");
        header.setAlignment(Pos.CENTER_LEFT);
        header.setMaxWidth(900);
        header.setMinWidth(900);

        // Espace pour la photo
        Label photoLabel = new Label("");
        photoLabel.setPrefWidth(40);

        // En-tête Nom avec icône de tri
        HBox nameHeader = new HBox(5);
        nameHeader.setAlignment(Pos.CENTER_LEFT);
        Label nameLabel = new Label("Nom & Prénom");
        nameLabel.getStyleClass().add("header-label");
        
        // Utilisation d'un Label avec caractère Unicode au lieu d'une image
        Button sortButton = new Button("↕");
        sortButton.getStyleClass().addAll("sort-button");
        sortButton.setOnAction(e -> handleSort());

        nameHeader.getChildren().addAll(nameLabel, sortButton);
        nameHeader.setPrefWidth(250);

        // Autres en-têtes
        Label emailLabel = createHeaderLabel("Email", 200);
        Label statusLabel = createHeaderLabel("Statut", 180);
        Label actionsLabel = createHeaderLabel("Actions", 160);

        header.getChildren().addAll(photoLabel, nameHeader, emailLabel, statusLabel, actionsLabel);
        return header;
    }

    private Label createHeaderLabel(String text, double width) {
        Label label = new Label(text);
        label.setPrefWidth(width);
        label.getStyleClass().add("header-label");
        return label;
    }

    private void handleSort() {
        isAscendingOrder = !isAscendingOrder;
        
        // Mettre à jour l'icône de tri
        Button sortButton = (Button) ((HBox) userListContainer.getChildren().get(0))
            .getChildren().stream()
            .filter(node -> node instanceof HBox)
            .findFirst()
            .map(hbox -> ((HBox) hbox).getChildren().get(1))
            .orElse(null);
        
        if (sortButton != null) {
            sortButton.setText(isAscendingOrder ? "↓" : "↑");
        }
        
        // Trier la liste
        currentUsers = currentUsers.stream()
            .sorted((u1, u2) -> {
                String name1 = (u1.getNom() + " " + u1.getPrenom()).toLowerCase();
                String name2 = (u2.getNom() + " " + u2.getPrenom()).toLowerCase();
                return isAscendingOrder ? name1.compareTo(name2) : name2.compareTo(name1);
            })
            .collect(Collectors.toList());
        
        updateUserList(currentUsers);
    }

    private HBox createUserRow(User user) {
        HBox row = new HBox(10);
        row.getStyleClass().add("table-row");
        row.setAlignment(Pos.CENTER);
        row.setMaxWidth(900);
        row.setMinWidth(900);


        // Photo de l'utilisateur
        ImageView imageView = createUserPhoto(user);

        // Création des labels
        Label nameLabel = createLabel(user.getNom() + " " + user.getPrenom(), 200);
        Label emailLabel = createLabel(user.getEmail(), 170);
        Label statusLabel =  createStatusLabel(user.getStatut());

        // Création des boutons d'action
        HBox actions = createActionButtons(user);

        // Ajout des éléments à la ligne
        row.getChildren().add(imageView);
        row.getChildren().add(nameLabel);
        row.getChildren().add(emailLabel);
        row.getChildren().add(statusLabel);
        row.getChildren().add(actions);

        return row;
    }

    private ImageView createUserPhoto(User user) {
        ImageView imageView = new ImageView();
        String imagePath = "/assets/users/" + user.getPhoto();
        
        try {
            Image image = new Image(getClass().getResourceAsStream(imagePath));
            if (image.isError()) throw new Exception();
            imageView.setImage(image);
        } catch (Exception e) {
            imageView.setImage(new Image(getClass().getResourceAsStream("/assets/userf.png")));
        }
        
        imageView.setFitWidth(40);
        imageView.setFitHeight(40);
        Circle clip = new Circle(20, 20, 20);
        imageView.setClip(clip);
        imageView.setSmooth(true);
        imageView.setCache(true);
        
        return imageView;
    }

    private Label createLabel(String text, double width) {
        Label label = new Label(text);
        label.setPrefWidth(width);
        label.getStyleClass().add("cell-text");
        return label;
    }

    private Label createStatusLabel(String statut) {
        Label label = new Label(statut);
        label.setPrefWidth(150);
        label.setAlignment(Pos.CENTER);
        label.setStyle("-fx-background-radius: 20;");
        
        if (statut != null && statut.trim().toLowerCase().equals("active")) {
            label.getStyleClass().add("statut-actif");
        } else {
            label.getStyleClass().add("statut-bloque");
        }

        return label;
    }

    private HBox createActionButtons(User user) {
        HBox actions = new HBox(2);
        actions.setPrefWidth(180);
        actions.setAlignment(Pos.CENTER_RIGHT);

        // Création des boutons individuellement
        Button infoButton = createIconButton("/assets/icons/infos.png", () -> handleInfo(user));
        Button editButton = createIconButton("/assets/icons/modifier.png", () -> handleEdit(user));
        Button deleteButton = createIconButton("/assets/icons/poubelle.png", () -> handleDelete(user));
        Button historyButton = createIconButton("/assets/icons/historique.png", () -> handleHistory(user));

        // Ajout des boutons un par un
        actions.getChildren().add(infoButton);
        actions.getChildren().add(editButton);
        actions.getChildren().add(deleteButton);
        actions.getChildren().add(historyButton);

        return actions;
    }

    private Button createIconButton(String iconPath, Runnable action) {
        ImageView icon = new ImageView(new Image(getClass().getResourceAsStream(iconPath)));
        icon.setFitHeight(20);
        icon.setFitWidth(20);
        
        Button button = new Button("", icon);
        button.getStyleClass().add("action-icon-button");
        button.setOnAction(e -> action.run());
        
        return button;
    }

    private void generatePagination() {
        paginationContainer.getChildren().clear();
        int total = userService.countFilteredUsers(searchField.getText(), statusFilter.getValue());
        int totalPages = (int) Math.ceil((double) total / rowsPerPage);

        for (int i = 1; i <= totalPages; i++) {
            final int pageNumber = i;
            Button pageBtn = new Button(String.valueOf(i));
            pageBtn.getStyleClass().add("pagination-button");
            
            if (i == currentPage) {
                pageBtn.setStyle("-fx-background-color: #3a4c68; -fx-text-fill: white;");
            }
            
            pageBtn.setOnAction(e -> {
                currentPage = pageNumber;
                loadUsersPage(pageNumber);
            });
            
            paginationContainer.getChildren().add(pageBtn);
        }
    }

    private void handleInfo(User user) {
        String info = String.format(
                "Nom & Prénom : %s %s\nEmail        : %s\nStatut       : %s\nRôle         : %s",
                user.getNom(),
                user.getPrenom(),
                user.getEmail(),
                user.getStatut(),
                user.getRoles()
        );

        showAlert("🧾 Informations Utilisateur", info);
    }

    private void handleEdit(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/EditUser.fxml"));
            Parent root = loader.load();

            EditUserController controller = loader.getController();
            controller.setUser(user);

            Stage stage = new Stage();
            stage.setTitle("Modifier Utilisateur");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadUsersPage(currentPage); // refresh
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleDelete(User user) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation de suppression");
        confirmAlert.setHeaderText("Êtes-vous sûr de vouloir supprimer cet utilisateur ?");
        confirmAlert.setContentText("Utilisateur : " + user.getNom() + " " + user.getPrenom());

        ButtonType btnOui = new ButtonType("Oui", ButtonBar.ButtonData.YES);
        ButtonType btnNon = new ButtonType("Non", ButtonBar.ButtonData.NO);

        confirmAlert.getButtonTypes().setAll(btnOui, btnNon);

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == btnOui) {
                userService.deleteUserById(user.getId());
                showAlert("✅ Supprimé", "L'utilisateur a été supprimé avec succès.");
                loadUsersPage(currentPage); // Recharge la liste
            }
        });
    }


    private void handleHistory(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/HistoriqueConnexion.fxml"));
            Parent root = loader.load();

            HistoriqueConnexionController controller = loader.getController();
            controller.setUser(user);

            Stage stage = new Stage();
            stage.setTitle("Historique de " + user.getNom());
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void animateSearchBar() {
        FadeTransition fade = new FadeTransition(Duration.seconds(1), searchBox);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }


    public void handleMicButton(MouseEvent event) {
        try {
            recordingIndicator.setVisible(true);
            startRecordingAnimation();

            new Thread(() -> {
                try {
                    VoiceRecognition.recognizeSpeech(searchField);
                } catch (Exception e) {
                    e.printStackTrace();
                    javafx.application.Platform.runLater(() -> showAlert("Erreur lors de la reconnaissance vocale:", e.getMessage()));
                } finally {
                    javafx.application.Platform.runLater(() -> {
                        recordingIndicator.setVisible(false);
                        stopRecordingAnimation();
                    });
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur lors de la reconnaissance vocale:", e.getMessage());
        }
    }



    private void startRecordingAnimation() {
        recordingAnimation = new ScaleTransition(Duration.seconds(0.6), recordingIndicator);
        recordingAnimation.setFromX(1.0);
        recordingAnimation.setFromY(1.0);
        recordingAnimation.setToX(1.4);
        recordingAnimation.setToY(1.4);
        recordingAnimation.setCycleCount(Timeline.INDEFINITE);
        recordingAnimation.setAutoReverse(true);
        recordingAnimation.setInterpolator(Interpolator.EASE_BOTH);
        recordingAnimation.play();
    }

    private void stopRecordingAnimation() {
        if (recordingAnimation != null) {
            recordingAnimation.stop();
            recordingIndicator.setScaleX(1.0);
            recordingIndicator.setScaleY(1.0);
        }
    }

}
