package controller;

import com.google.zxing.WriterException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.transform.Rotate;
import model.Material;
import model.User;
import service.MateriauxService;
import service.WishlistService;
import utils.QRCodeGenerator;
import utils.SessionManager;

import java.awt.Desktop;
import javafx.scene.input.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MateriauxController {
    private static final int COLUMNS = 4;
    private static final int CARD_WIDTH = 250;
    private static final int CARD_HEIGHT = 300;
    private static final int IMAGE_SIZE = 150;
    @FXML private TextField searchField;
    @FXML private GridPane materialsGrid;
    @FXML private ComboBox<String> filterCombo;
    private User connectedUser;
    private MateriauxService materiauxService;
    private WishlistService wishlistService;

    @FXML
    public void initialize() {
        System.out.println("[MATERIAUX] Initialisation");
        try {
            materiauxService = new MateriauxService();
            wishlistService = new WishlistService();
            startHttpServer();
            setupFilters();
            loadMaterials();
        } catch (Exception e) {
            handleError("Erreur d'initialisation", e);
        }
    }

    @FXML
    private void handleSearch(KeyEvent event) {
        try {
            String searchText = searchField.getText().trim().toLowerCase();
            if (searchText.isEmpty()) {
                loadMaterials();
            } else {
                searchMaterialsByName(searchText);
            }
        } catch (Exception e) {
            handleError("Recherche", e);
        }
    }
    private void searchMaterialsByName(String searchText) throws SQLException {
        List<Material> allMaterials = materiauxService.getAll();
        List<Material> filteredMaterials = allMaterials.stream()
                .filter(m -> m.getNomMateriel().toLowerCase().contains(searchText))
                .collect(Collectors.toList());
        displayMaterials(filteredMaterials);
    }

    private void startHttpServer() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
            server.createContext("/material", new HttpHandler() {
                @Override
                public void handle(HttpExchange exchange) throws IOException {
                    String query = exchange.getRequestURI().getQuery(); // id=5
                    int id = Integer.parseInt(query.split("=")[1]);
                    Material m = null;
                    try {
                        m = materiauxService.getAll().stream()
                                .filter(x -> x.getId() == id)
                                .findFirst().orElse(null);
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                    String html = buildHtmlForMaterial(m);
                    exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
                    byte[] bytes = html.getBytes("UTF-8");
                    exchange.sendResponseHeaders(200, bytes.length);
                    exchange.getResponseBody().write(bytes);
                    exchange.close();
                }
            });
            server.setExecutor(null);
            server.start();
            String host = InetAddress.getLocalHost().getHostAddress();
            System.out.println("[HTTP] Server at http://" + host + ":8000/");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupFilters() {
        filterCombo.getItems().setAll("Tous", "peinture", "tissu", "papier", "poterie");
        filterCombo.setValue("Tous");
        filterCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            try {
                if ("Tous".equals(newVal)) loadMaterials();
                else loadMaterialsByCategory(newVal);
            } catch (Exception e) {
                handleError("Erreur de filtrage", e);
            }
        });
    }

    private void loadMaterialsByCategory(String category) throws SQLException {
        materialsGrid.getChildren().clear();
        List<Material> materials = materiauxService.getByCategory(category);
        displayMaterials(materials);
    }

    private void loadMaterials() {
        try {
            List<Material> materials = materiauxService.getAll();
            displayMaterials(materials);
        } catch (Exception e) {
            handleError("Chargement des matériaux", e);
        }
    }

    private void displayMaterials(List<Material> materials) {
        materialsGrid.getChildren().clear();
        int col = 0, row = 0;
        for (Material material : materials) {
            StackPane flipCard = createFlipCard(material);
            materialsGrid.add(flipCard, col, row);
            col = (col + 1) % COLUMNS;
            if (col == 0) row++;
        }
    }

    private StackPane createFlipCard(Material material) {
        StackPane flipCard = new StackPane();
        flipCard.getStyleClass().add("flip-card");
        flipCard.setPrefSize(CARD_WIDTH, CARD_HEIGHT);
        VBox front = createFrontFace(material);
        VBox back = createBackFace(material, flipCard);
        flipCard.getChildren().addAll(front, back);
        back.setVisible(false);
        return flipCard;
    }

    private VBox createFrontFace(Material material) {
        VBox front = new VBox(10);
        front.getStyleClass().add("flip-card-front");
        front.setAlignment(Pos.CENTER);
        front.setPadding(new Insets(15));
        ImageView imageView = loadMaterialImage(material);
        Label nameLabel = new Label(material.getNomMateriel());
        nameLabel.getStyleClass().add("material-name");
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(CARD_WIDTH - 70);
        Label priceLabel = new Label(String.format("%.2f DT", material.getPrixUnitaire()));
        priceLabel.getStyleClass().add("material-price");
        Button detailsBtn = new Button("Détails");
        detailsBtn.getStyleClass().add("flip-btn");
        detailsBtn.setOnAction(e -> flipCard((StackPane) detailsBtn.getParent().getParent()));
        front.getChildren().addAll(imageView, nameLabel, priceLabel, detailsBtn);
        return front;
    }

    private VBox createBackFace(Material material, StackPane flipCard) {
        VBox back = new VBox(10);
        back.getStyleClass().add("flip-card-back");
        back.setAlignment(Pos.CENTER);
        back.setPadding(new Insets(15));

        try {
            // Get host address
            String host;
            try {
                host = InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException uhe) {
                host = "localhost";
            }

            String url = "http://" + host + ":8000/material?id=" + material.getId();

            // Generate QR code
            Image qrImage;
            try {
                qrImage = QRCodeGenerator.generateQRCodeImage(url, 160, 160);
            } catch (WriterException e) {
                showAlert("Erreur QR-code", "Impossible de générer le code QR");
                qrImage = new Image(getClass().getResourceAsStream("/assets/default_qr.png"));
            }

            ImageView qrView = new ImageView(qrImage);
            qrView.setFitWidth(160);
            qrView.setFitHeight(160);
            qrView.getStyleClass().add("qr-code");

            // Click handler to open URL
            qrView.setOnMouseClicked(e -> {
                try {
                    Desktop.getDesktop().browse(new java.net.URI(url));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            Label scanLabel = new Label("Scannez pour voir détails");
            scanLabel.getStyleClass().add("scan-label");
            scanLabel.setWrapText(true);
            scanLabel.setMaxWidth(CARD_WIDTH - 70);



            back.getChildren().addAll(scanLabel, qrView, createActionButtons(material, flipCard));

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Une erreur inattendue est survenue");
        }

        return back;
    }

    private HBox createActionButtons(Material material, StackPane flipCard) {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER);
        ImageView cartIcon = new ImageView(new Image(getClass().getResourceAsStream("/assets/icons/panier.png")));
        cartIcon.setFitHeight(20); cartIcon.setFitWidth(20);
        Button addToCart = new Button("", cartIcon);
        addToCart.getStyleClass().add("action-btn");
        addToCart.setOnAction(e -> handleAddToCart(material));
        ImageView wishIcon = new ImageView(new Image(getClass().getResourceAsStream("/assets/icons/heart.png")));
        wishIcon.setFitHeight(20); wishIcon.setFitWidth(20);
        Button wishBtn = new Button("", wishIcon);
        wishBtn.getStyleClass().add("action-btn");
        wishBtn.setOnAction(e -> handleWishlistAction(material));
        ImageView backIcon = new ImageView(new Image(getClass().getResourceAsStream("/assets/icons/retour.png")));
        backIcon.setFitHeight(20); backIcon.setFitWidth(20);
        Button backBtn = new Button("", backIcon);
        backBtn.getStyleClass().add("flip-btn");
        backBtn.setOnAction(e -> flipCard(flipCard));
        box.getChildren().addAll(addToCart, wishBtn, backBtn);
        return box;
    }

    private void handleAddToCart(Material material) {
        showAlert("Info", "Ajout au panier non implémenté");
    }

    private void handleWishlistAction(Material material) {
        SessionManager sm = SessionManager.getInstance();
        if (!sm.isLoggedIn()) { showAlert("Connexion requise", "Veuillez vous connecter."); return; }
        try {
            wishlistService.addToWishlist(sm.getCurrentUser(), material);
            openWishlistWindow();
        } catch (SQLException e) { handleError("Wishlist", e); }
    }

    private void openWishlistWindow() {
        try {
            // Load the Wishlist FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fornt views/Wishlist.fxml"));
            Parent root = loader.load();

            // Get the controller of the Wishlist FXML
            WishlistController ctrl = loader.getController();
            ctrl.loadWishlistMaterials(); // Assuming this method loads the wishlist data

            // Check if the main layout is a StackPane
            StackPane mainStackPane = (StackPane) materialsGrid.getScene().lookup("#contentPane");
            if (mainStackPane != null) {
                mainStackPane.getChildren().setAll(root); // Add the root node to the StackPane
            }

            // Set the window title
            Stage stage = (Stage) materialsGrid.getScene().getWindow();
            stage.setTitle("Ma Wishlist");
        } catch (IOException e) {
            handleError("Ouverture Wishlist", e); // Handle the error (you can adjust the error message if necessary)
        }
    }



    private ImageView loadMaterialImage(Material material) {
        ImageView iv = new ImageView();
        try {
            String path = "/assets/prod_mat/" + material.getPhoto();
            URL url = getClass().getResource(path);
            iv.setImage(url!=null? new Image(url.toExternalForm()): new Image(getClass().getResourceAsStream("/assets/placeholder.png")));
        } catch (Exception e) { iv.setImage(new Image(getClass().getResourceAsStream("/assets/default_material.png"))); }
        iv.setFitWidth(IMAGE_SIZE); iv.setFitHeight(IMAGE_SIZE); iv.setPreserveRatio(true);
        return iv;
    }

    private void flipCard(StackPane card) {
        Node front = card.getChildren().get(0), back = card.getChildren().get(1);
        card.setCache(true); card.setCacheHint(CacheHint.ROTATE); card.setRotationAxis(Rotate.Y_AXIS);
        RotateTransition out = new RotateTransition(Duration.millis(250), card);
        out.setFromAngle(0); out.setToAngle(90); out.setInterpolator(Interpolator.EASE_OUT);
        RotateTransition in = new RotateTransition(Duration.millis(250), card);
        in.setFromAngle(-90); in.setToAngle(0); in.setInterpolator(Interpolator.EASE_IN);
        out.setOnFinished(ev -> { front.setVisible(!front.isVisible()); back.setVisible(!back.isVisible()); in.play(); }); out.play();
    }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }

    private Optional<ButtonType> showConfirmation(String t, String m, String y, String n) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle(t); a.setHeaderText(null); a.setContentText(m);
        ButtonType yes = new ButtonType(y, ButtonBar.ButtonData.YES);
        ButtonType no = new ButtonType(n, ButtonBar.ButtonData.NO);
        a.getButtonTypes().setAll(yes,no);
        return a.showAndWait();
    }

    private void handleError(String ctx, Exception e) {
        System.err.println("[ERR]"+ctx+":"+e.getMessage()); showAlert("Erreur", ctx+":"+e.getMessage()); e.printStackTrace();
    }

    public void setConnectedUser(User user) {
        this.connectedUser = user;
        loadMaterials();
    }

    private String buildHtmlForMaterial(Material m) {
        // encode l’image en base64 comme avant
        String base64 = "";
        try (InputStream is = getClass().getResourceAsStream("/assets/prod_mat/" + m.getPhoto())) {
            byte[] b = is.readAllBytes();
            base64 = Base64.getEncoder().encodeToString(b);
        } catch (Exception e) { /* silent */ }

        return String.format("""
        <!DOCTYPE html><html lang="fr"><head><meta charset="UTF-8"><title>%s</title>
        <style>body{font-family:Arial;text-align:center;padding:20px;} .card{display:inline-block;
          background:#fff;padding:20px;border-radius:8px;box-shadow:0 2px 8px rgba(0,0,0,.2);}
          img{max-width:100%%;border-radius:8px;}</style>
        </head><body>
          <div class="card">
            <h1>%s</h1>
            <img src="data:image/png;base64,%s" alt="">
            <p><strong>Description:</strong> %s</p>
            <p><strong>Prix:</strong> %.2f TND</p>
          </div>
        </body></html>
        """,
                m.getNomMateriel(),
                m.getNomMateriel(),
                base64,
                m.getDescription(),
                m.getPrixUnitaire()
        );
    }

}
