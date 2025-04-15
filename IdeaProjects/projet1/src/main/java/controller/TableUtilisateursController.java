package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import model.User;
import service.UserService;
import javafx.beans.property.SimpleStringProperty;

import java.util.List;

public class TableUtilisateursController {

    @FXML private TextField searchField;
    @FXML private TableView<User> userTable;
    @FXML private HBox paginationContainer;
    @FXML private TableColumn<User, String> colNom;
    @FXML private TableColumn<User, String> colPrenom;
    @FXML private TableColumn<User, String> colEmail;
    @FXML private TableColumn<User, String> colStatut;
    @FXML private TableColumn<User, Void> colActions;
    @FXML private TableColumn<User, ImageView> colPhoto;

    private final UserService userService = new UserService();
    private int currentPage = 1;
    private final int rowsPerPage = 5;

    @FXML
    public void initialize() {
        setupTableColumns();
        loadUsersPage(currentPage);
    }

    private void setupTableColumns() {
        TableColumn<User, String> nomCol = new TableColumn<>("Nom");
        nomCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNom()));

        TableColumn<User, String> prenomCol = new TableColumn<>("Prénom");
        prenomCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPrenom()));

        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));

        TableColumn<User, String> statutCol = new TableColumn<>("Statut");
        statutCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatut()));

        userTable.getColumns().addAll(nomCol, prenomCol, emailCol, statutCol);
    }


    private void loadUsersPage(int page) {
        List<User> users = userService.getUsersPage(page, rowsPerPage);
        userTable.getItems().setAll(users);
        generatePagination();
    }

    private void generatePagination() {
        paginationContainer.getChildren().clear();
        int total = userService.countUsers();
        int totalPages = (int) Math.ceil((double) total / rowsPerPage);

        for (int i = 1; i <= totalPages; i++) {
            int pageIndex = i; // 👈 final/effectively final

            Button pageBtn = new Button(String.valueOf(pageIndex));
            pageBtn.setOnAction(e -> loadUsersPage(pageIndex));

            pageBtn.getStyleClass().add("pagination-button");

            if (pageIndex == currentPage) {
                pageBtn.setStyle("-fx-background-color: #3a4c68; -fx-text-fill: white;");
            }

            paginationContainer.getChildren().add(pageBtn);
        }

    }
}
