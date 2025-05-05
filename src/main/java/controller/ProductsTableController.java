package controller;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.Produit;
import org.controlsfx.control.Notifications;
import service.ProduitService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

// Ton AddProduitController
import controller.AddProduitController;

// Ton modèle d'utilisateur
import model.User;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProductsTableController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> categorieFilter;
    @FXML private ComboBox<String> statutFilter;
    @FXML private TextField minPrixFilter;
    @FXML private TextField maxPrixFilter;
    @FXML private TextField minStockFilter;
    @FXML private TextField maxStockFilter;
    @FXML private ComboBox<String> sortFilter;
    @FXML private TableView<Produit> produitsTable;
    @FXML private TableColumn<Produit, Integer> idColumn;
    @FXML private TableColumn<Produit, String> nomProduitColumn;
    @FXML private TableColumn<Produit, String> categorieColumn;
    @FXML private TableColumn<Produit, Double> prixColumn;
    @FXML private TableColumn<Produit, String> statutColumn;
    @FXML private TableColumn<Produit, Integer> stockColumn;
    @FXML private PieChart categoryPieChart;
    @FXML private BarChart<String, Number> stockBarChart;
    @FXML private CategoryAxis stockXAxis;
    @FXML private NumberAxis stockYAxis;
    @FXML private LineChart<String, Number> priceLineChart;
    @FXML private CategoryAxis priceXAxis;
    @FXML private NumberAxis priceYAxis;
    @FXML private Button exportPdfButton;
    @FXML private Button addProductButton;
    private User connectedUser;

    private ProduitService produitService;
    private ObservableList<Produit> produitsList;
    private ObservableList<String> categories;
    private ObservableList<String> statuts;
    private ObservableList<String> sortOptions;

    @FXML
    public void initialize() {
        produitService = new ProduitService();
        produitsList = FXCollections.observableArrayList();
        categories = FXCollections.observableArrayList();
        statuts = FXCollections.observableArrayList();
        sortOptions = FXCollections.observableArrayList("A-Z", "Z-A");

        // Initialize table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nomProduitColumn.setCellValueFactory(new PropertyValueFactory<>("nomProduit"));
        categorieColumn.setCellValueFactory(new PropertyValueFactory<>("categorie"));
        prixColumn.setCellValueFactory(new PropertyValueFactory<>("prix"));
        statutColumn.setCellValueFactory(new PropertyValueFactory<>("statut"));
        stockColumn.setCellValueFactory(new PropertyValueFactory<>("stock"));

        // Load data
        loadProduits();
        loadFilters();
        loadCharts();

        // Set table and filter data
        produitsTable.setItems(produitsList);
        categorieFilter.setItems(categories);
        produitsTable.setPlaceholder(new Label("")); // évite les lignes vides
        produitsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); // ajuste automatiquement les colonnes

        statutFilter.setItems(statuts);
        sortFilter.setItems(sortOptions);

        // Add "Tous" option to filters
        categories.add(0, "Toutes");
        statuts.add(0, "Tous");

        // Set default sort option
        sortFilter.setValue("A-Z");
    }
    public void setConnectedUser(User user) {
        this.connectedUser = user;
    }

    private void loadProduits() {
        produitsList.clear();
        produitsList.addAll(produitService.getProduitsPage(1, Integer.MAX_VALUE));
    }

    private void loadFilters() {
        categories.addAll(produitService.getProduitsPage(1, Integer.MAX_VALUE)
                .stream()
                .map(Produit::getCategorie)
                .distinct()
                .collect(Collectors.toList()));
        statuts.addAll(produitService.getProduitsPage(1, Integer.MAX_VALUE)
                .stream()
                .map(Produit::getStatut)
                .distinct()
                .collect(Collectors.toList()));
    }

    private void loadCharts() {
        // Pie Chart: Product distribution by category
        Map<String, Integer> categoryCounts = produitService.getProductCountByCategory();
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        categoryCounts.forEach((category, count) ->
                pieChartData.add(new PieChart.Data(category, count))
        );
        categoryPieChart.setData(pieChartData);
        categoryPieChart.setTitle("Produits par Catégorie");

        // Bar Chart: Stock by category
        Map<String, Integer> stockByCategory = produitService.getStockByCategory();
        XYChart.Series<String, Number> stockSeries = new XYChart.Series<>();
        stockSeries.setName("Stock");
        stockByCategory.forEach((category, stock) ->
                stockSeries.getData().add(new XYChart.Data<>(category, stock))
        );
        stockBarChart.getData().add(stockSeries);
        stockBarChart.setTitle("Stock par Catégorie");

        // Line Chart: Prices of products with promotions
        List<Produit> promotedProducts = produitService.getTopProductsWithPromotions(5);
        XYChart.Series<String, Number> priceSeries = new XYChart.Series<>();
        priceSeries.setName("Prix");
        promotedProducts.forEach(produit ->
                priceSeries.getData().add(new XYChart.Data<>(produit.getNomProduit(), produit.getPrix()))
        );
        priceLineChart.getData().add(priceSeries);
        priceLineChart.setTitle("Prix des Produits en Promotion");
    }

    @FXML
    private void filterProduits() {
        String searchText = searchField.getText().trim().toLowerCase();
        String selectedCategorie = categorieFilter.getValue() != null ? categorieFilter.getValue() : "Toutes";
        String selectedStatut = statutFilter.getValue() != null ? statutFilter.getValue() : "Tous";
        String selectedSort = sortFilter.getValue() != null ? sortFilter.getValue() : "A-Z";

        // Parse price filters
        double minPrix = parseDouble(minPrixFilter.getText(), 0.0);
        double maxPrix = parseDouble(maxPrixFilter.getText(), Double.MAX_VALUE);

        // Parse stock filters
        int minStock = parseInt(minStockFilter.getText(), 0);
        int maxStock = parseInt(maxStockFilter.getText(), Integer.MAX_VALUE);

        List<Produit> filteredList = produitService.getProduitsPage(1, Integer.MAX_VALUE)
                .stream()
                .filter(produit -> produit.getNomProduit().toLowerCase().contains(searchText))
                .filter(produit -> selectedCategorie.equals("Toutes") || produit.getCategorie().equals(selectedCategorie))
                .filter(produit -> selectedStatut.equals("Tous") || produit.getStatut().equals(selectedStatut))
                .filter(produit -> produit.getPrix() >= minPrix && produit.getPrix() <= maxPrix)
                .filter(produit -> produit.getStock() >= minStock && produit.getStock() <= maxStock)
                .sorted(getComparator(selectedSort))
                .collect(Collectors.toList());

        produitsList.clear();
        produitsList.addAll(filteredList);

        // Update charts based on filtered data
        updateCharts(filteredList);
    }



    @FXML
    private void exportToPDF() {
        if (produitsList.isEmpty()) {
            showNotification("❌ Erreur", "Aucun produit à exporter.", "error");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));
        fileChooser.setInitialFileName("products_export_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".pdf");
        File file = fileChooser.showSaveDialog(produitsTable.getScene().getWindow());

        if (file == null) {
            return;
        }

        try {
            Document document = new Document(PageSize.A4.rotate()); // Landscape
            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();

            // Title
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, BaseColor.BLACK);
            Paragraph title = new Paragraph("Liste des Produits", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(10);
            document.add(title);

            // Timestamp
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);
            Paragraph timestamp = new Paragraph("Exporté le: " + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()), normalFont);
            timestamp.setAlignment(Element.ALIGN_LEFT);
            timestamp.setSpacingAfter(10);
            document.add(timestamp);

            // Table
            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1, 3, 2, 1.5f, 1.5f, 1});
            table.setSpacingBefore(10);

            // Headers
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLACK);
            String[] headers = {"ID", "Nom Produit", "Catégorie", "Prix (DT)", "Statut", "Stock"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(5);
                table.addCell(cell);
            }

            // Data
            Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);
            for (Produit produit : produitsList) {
                table.addCell(createCell(String.valueOf(produit.getId()), cellFont));
                table.addCell(createCell(truncate(produit.getNomProduit(), 25), cellFont));
                table.addCell(createCell(produit.getCategorie(), cellFont));
                table.addCell(createCell(String.format("%.2f", produit.getPrix()), cellFont));
                table.addCell(createCell(produit.getStatut(), cellFont));
                table.addCell(createCell(String.valueOf(produit.getStock()), cellFont));
            }

            document.add(table);
            document.close();

            showNotification("✅ Succès", "PDF exporté avec succès : " + file.getAbsolutePath(), "success");
        } catch (DocumentException | IOException e) {
            System.err.println("Error exporting PDF: " + e.getMessage());
            showNotification("❌ Erreur", "Échec de l'exportation du PDF : " + e.getMessage(), "error");
        }
    }

    private PdfPCell createCell(String content, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(content, font));
        cell.setPadding(5);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        return cell;
    }

    private String truncate(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3) + "...";
    }

    private void showNotification(String title, String message, String type) {
        Notifications notification = Notifications.create()
                .title(title)
                .text(message)
                .position(Pos.TOP_RIGHT)
                .hideAfter(Duration.seconds(4));
        Platform.runLater(() -> {
            if (type.equals("success")) {
                notification.showInformation();
            } else {
                notification.showError();
            }
        });
    }

    private void updateCharts(List<Produit> filteredList) {
        // Update Pie Chart
        Map<String, Long> categoryCounts = filteredList.stream()
                .collect(Collectors.groupingBy(Produit::getCategorie, Collectors.counting()));
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        categoryCounts.forEach((category, count) ->
                pieChartData.add(new PieChart.Data(category, count))
        );
        categoryPieChart.setData(pieChartData);

        // Update Bar Chart
        Map<String, Integer> stockByCategory = filteredList.stream()
                .collect(Collectors.groupingBy(Produit::getCategorie,
                        Collectors.summingInt(Produit::getStock)));
        XYChart.Series<String, Number> stockSeries = new XYChart.Series<>();
        stockSeries.setName("Stock");
        stockByCategory.forEach((category, stock) ->
                stockSeries.getData().add(new XYChart.Data<>(category, stock))
        );
        stockBarChart.getData().clear();
        stockBarChart.getData().add(stockSeries);

        // Update Line Chart
        List<Produit> promotedProducts = filteredList.stream()
                .filter(produit -> produit.getIdPromotion() != null)
                .limit(5)
                .collect(Collectors.toList());
        XYChart.Series<String, Number> priceSeries = new XYChart.Series<>();
        priceSeries.setName("Prix");
        promotedProducts.forEach(produit ->
                priceSeries.getData().add(new XYChart.Data<>(produit.getNomProduit(), produit.getPrix()))
        );
        priceLineChart.getData().clear();
        priceLineChart.getData().add(priceSeries);
    }

    private double parseDouble(String text, double defaultValue) {
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private int parseInt(String text, int defaultValue) {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private Comparator<Produit> getComparator(String sortOption) {
        if ("Z-A".equals(sortOption)) {
            return (p1, p2) -> p2.getNomProduit().compareToIgnoreCase(p1.getNomProduit());
        }
        return (p1, p2) -> p1.getNomProduit().compareToIgnoreCase(p2.getNomProduit());
    }

    @FXML
    private void handleAddProduct() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Produit_Promotion/add_produit.fxml"));
            Parent root = loader.load();

            AddProduitController controller = loader.getController();
            controller.setConnectedUser(connectedUser); // 🔥 Très important

            Stage stage = new Stage();
            stage.setTitle("Ajouter un Produit");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showNotification("❌ Erreur", "Impossible de charger la fenêtre d'ajout de produit.", "error");
        }
    }



}