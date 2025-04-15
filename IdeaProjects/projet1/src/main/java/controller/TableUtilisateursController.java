package controller;

import javafx.animation.FadeTransition;
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

import java.io.IOException;

import java.util.List;

public class TableUtilisateursController {

    @FXML private TextField searchField;
    @FXML private VBox userListContainer;
    @FXML private HBox paginationContainer;
    @FXML private HBox searchBox;

    private final UserService userService = new UserService();
    private int currentPage = 1;
    private final int rowsPerPage = 5;

    @FXML
    public void initialize() {
        loadUsersPage(currentPage);

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            currentPage = 1;
            loadUsersPage(currentPage);
        });

        animateSearchBar();
    }

    private void loadUsersPage(int page) {
        String keyword = searchField.getText();
        List<User> users = userService.searchUsersPageSQL(keyword, page, rowsPerPage);
        userListContainer.getChildren().clear();

        for (User user : users) {
            HBox row = new HBox(10);
            row.getStyleClass().add("table-row");
            row.setAlignment(Pos.CENTER);
            row.setMaxWidth(800);

            // Photo
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

            // Nom & Prénom
            Label nameLabel = new Label(user.getNom() + " " + user.getPrenom());
            nameLabel.setPrefWidth(150);
            nameLabel.getStyleClass().add("cell-text");

            // Email
            Label emailLabel = new Label(user.getEmail());
            emailLabel.setPrefWidth(170);
            emailLabel.getStyleClass().add("cell-text");

            // Statut avec style dynamique sécurisé
            Label statutLabel = new Label(user.getStatut());
            statutLabel.setPrefWidth(150);
            statutLabel.setAlignment(Pos.CENTER);
            statutLabel.setStyle("-fx-background-radius: 20;");

            String statut = user.getStatut();
            if (statut != null && statut.trim().equalsIgnoreCase("active")) {
                statutLabel.getStyleClass().add("statut-actif");
            } else {
                statutLabel.getStyleClass().add("statut-bloque");
            }

            // Actions
            HBox actions = new HBox(2);
            actions.setPrefWidth(160);
            actions.setAlignment(Pos.CENTER);

            Button btnInfo = createIconButton("/assets/icons/infos.png", () -> handleInfo(user));
            Button btnEdit = createIconButton("/assets/icons/modifier.png", () -> handleEdit(user));
            Button btnDelete = createIconButton("/assets/icons/poubelle.png", () -> handleDelete(user));
            Button btnHistory = createIconButton("/assets/icons/historique.png", () -> handleHistory(user));

            actions.getChildren().addAll(btnInfo, btnEdit, btnDelete, btnHistory);

            row.getChildren().addAll(imageView, nameLabel, emailLabel, statutLabel, actions);
            VBox.setMargin(row, new Insets(5, 0, 5, 0));
            userListContainer.getChildren().add(row);
        }

        generatePagination();
    }

    private Button createIconButton(String iconPath, Runnable action) {
        ImageView icon = new ImageView(getClass().getResource(iconPath).toExternalForm());
        icon.setFitHeight(20);
        icon.setFitWidth(20);
        Button btn = new Button("", icon);
        btn.getStyleClass().add("action-icon-button");
        btn.setOnAction(e -> action.run());
        return btn;
    }

    private void generatePagination() {
        paginationContainer.getChildren().clear();
        int total = userService.countUsersWithKeyword(searchField.getText());
        int totalPages = (int) Math.ceil((double) total / rowsPerPage);

        for (int i = 1; i <= totalPages; i++) {
            int pageIndex = i;

            Button pageBtn = new Button(String.valueOf(pageIndex));
            pageBtn.setOnAction(e -> {
                currentPage = pageIndex;
                loadUsersPage(pageIndex);
            });

            pageBtn.getStyleClass().add("pagination-button");

            if (pageIndex == currentPage) {
                pageBtn.setStyle("-fx-background-color: #3a4c68; -fx-text-fill: white;");
            }

            paginationContainer.getChildren().add(pageBtn);
        }
    }

    private void handleInfo(User user) {
        showAlert("Infos", "Utilisateur : " + user.getNom() + " " + user.getPrenom());
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
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
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
}
