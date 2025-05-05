package controller;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.Commande;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import service.CommandeService;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TableCommandesController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> statutFilter;
    @FXML private ComboBox<String> paiementFilter;
    @FXML private TextField minMontantFilter;
    @FXML private TextField maxMontantFilter;
    @FXML private TextField minDateFilter;
    @FXML private TextField maxDateFilter;
    @FXML private ComboBox<String> sortFilter;
    @FXML private TableView<Commande> commandesTable;
    @FXML private TableColumn<Commande, Integer> idColumn;
    @FXML private TableColumn<Commande, Date> dateColumn;
    @FXML private TableColumn<Commande, Double> montantColumn;
    @FXML private TableColumn<Commande, String> statutColumn;
    @FXML private TableColumn<Commande, String> adresseColumn;
    @FXML private Button modifierStatutBtn;
    @FXML private Button modifierAdresseBtn;
    @FXML private Button showMapBtn;
    @FXML private Button exportExcelBtn;
    @FXML private PieChart statusPieChart;
    @FXML private BarChart<String, Number> amountBarChart;
    @FXML private CategoryAxis amountXAxis;
    @FXML private NumberAxis amountYAxis;
    @FXML private LineChart<String, Number> ordersLineChart;
    @FXML private CategoryAxis ordersXAxis;
    @FXML private NumberAxis ordersYAxis;

    private CommandeService commandeService;
    private ObservableList<Commande> commandesList;
    private ObservableList<String> statuts;
    private ObservableList<String> paiements;
    private ObservableList<String> sortOptions;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @FXML
    public void initialize() {
        commandeService = new CommandeService();
        commandesList = FXCollections.observableArrayList();
        statuts = FXCollections.observableArrayList();
        paiements = FXCollections.observableArrayList();
        sortOptions = FXCollections.observableArrayList("Date Asc", "Date Desc");

        // Initialize table columns
        idColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        dateColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getDateCommande()));
        dateColumn.setCellFactory(column -> new TableCell<Commande, Date>() {
            @Override
            protected void updateItem(Date item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(dateFormat.format(item));
                }
            }
        });
        montantColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getMontantTotal()).asObject());
        montantColumn.setCellFactory(column -> new TableCell<Commande, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f DT", item));
                }
            }
        });
        statutColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatut()));
        adresseColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getAdresseLivraison()));

        // Load data
        loadCommandes();
        loadFilters();
        loadCharts();

        // Set table and filter data
        commandesTable.setItems(commandesList);
        commandesTable.setPlaceholder(new Label("")); // évite les lignes vides
        commandesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); // ajuste automatiquement les colonnes

        statutFilter.setItems(statuts);
        paiementFilter.setItems(paiements);
        sortFilter.setItems(sortOptions);

        // Add "Tous" option to filters
        statuts.add(0, "Tous");
        paiements.add(0, "Tous");

        // Set default sort option
        sortFilter.setValue("Date Asc");

        // Disable buttons by default
        modifierStatutBtn.setDisable(true);
        modifierAdresseBtn.setDisable(true);
        showMapBtn.setDisable(false);
        exportExcelBtn.setDisable(false);

        // Enable buttons when a row is selected
        commandesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            modifierStatutBtn.setDisable(newSelection == null);
            modifierAdresseBtn.setDisable(newSelection == null);
        });
    }

    private void loadCommandes() {
        try {
            List<Commande> commandes = commandeService.getAllCommandes();
            commandesList.setAll(commandes);
            System.out.println("TableCommandes: Loaded " + commandes.size() + " commandes");
        } catch (Exception e) {
            System.err.println("❌ TableCommandes: Erreur lors du chargement des commandes: " + e.getMessage());
            showAlert("Erreur", "Impossible de charger les commandes: " + e.getMessage());
        }
    }

    private void loadFilters() {
        statuts.addAll(commandeService.getAllCommandes()
                .stream()
                .map(Commande::getStatut)
                .distinct()
                .collect(Collectors.toList()));
        paiements.addAll(commandeService.getAllCommandes()
                .stream()
                .map(Commande::getPaiement)
                .distinct()
                .collect(Collectors.toList()));
    }

    private void loadCharts() {
        // Pie Chart: Order distribution by status
        Map<String, Integer> statusCounts = commandeService.getOrderCountByStatus();
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        statusCounts.forEach((status, count) ->
                pieChartData.add(new PieChart.Data(status, count))
        );
        statusPieChart.setData(pieChartData);
        statusPieChart.setTitle("Commandes par Statut");

        // Bar Chart: Total amount by status
        Map<String, Double> amountByStatus = commandeService.getAmountByStatus();
        XYChart.Series<String, Number> amountSeries = new XYChart.Series<>();
        amountSeries.setName("Montant");
        amountByStatus.forEach((status, amount) ->
                amountSeries.getData().add(new XYChart.Data<>(status, amount))
        );
        amountBarChart.getData().add(amountSeries);
        amountBarChart.setTitle("Montant Total par Statut");

        // Line Chart: Orders over time
        Map<String, Integer> ordersByDate = commandeService.getOrdersByDate();
        XYChart.Series<String, Number> ordersSeries = new XYChart.Series<>();
        ordersSeries.setName("Commandes");
        ordersByDate.forEach((date, count) ->
                ordersSeries.getData().add(new XYChart.Data<>(date, count))
        );
        ordersLineChart.getData().add(ordersSeries);
        ordersLineChart.setTitle("Commandes par Date");
    }

    @FXML
    private void filtrerCommandes() {
        String searchText = searchField.getText() != null ? searchField.getText().toLowerCase() : "";
        String selectedStatut = statutFilter.getValue() != null ? statutFilter.getValue() : "Tous";
        String selectedPaiement = paiementFilter.getValue() != null ? paiementFilter.getValue() : "Tous";
        String selectedSort = sortFilter.getValue() != null ? sortFilter.getValue() : "Date Asc";

        // Parse montant filters
        double minMontant = parseDouble(minMontantFilter.getText(), 0.0);
        double maxMontant = parseDouble(maxMontantFilter.getText(), Double.MAX_VALUE);

        // Parse date filters
        Date minDate = parseDate(minDateFilter.getText(), null);
        Date maxDate = parseDate(maxDateFilter.getText(), null);

        List<Commande> filteredList = commandeService.getAllCommandes()
                .stream()
                .filter(commande -> matchesSearch(commande, searchText))
                .filter(commande -> selectedStatut.equals("Tous") || commande.getStatut().equals(selectedStatut))
                .filter(commande -> selectedPaiement.equals("Tous") || (commande.getPaiement() != null && commande.getPaiement().equals(selectedPaiement)))
                .filter(commande -> commande.getMontantTotal() >= minMontant && commande.getMontantTotal() <= maxMontant)
                .filter(commande -> matchesDate(commande, minDate, maxDate))
                .sorted(getComparator(selectedSort))
                .collect(Collectors.toList());

        commandesList.clear();
        commandesList.addAll(filteredList);
        // Charts remain static, no updateCharts call
        updateCharts(filteredList); // ⬅️ nouvelle ligne

    }
    private void updateCharts(List<Commande> commandes) {
        // Update PieChart
        Map<String, Long> statusCounts = commandes.stream()
                .collect(Collectors.groupingBy(Commande::getStatut, Collectors.counting()));
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        statusCounts.forEach((status, count) ->
                pieChartData.add(new PieChart.Data(status, count))
        );
        statusPieChart.setData(pieChartData);

        // Update BarChart
        Map<String, Double> amountByStatus = commandes.stream()
                .collect(Collectors.groupingBy(Commande::getStatut, Collectors.summingDouble(Commande::getMontantTotal)));
        amountBarChart.getData().clear();
        XYChart.Series<String, Number> amountSeries = new XYChart.Series<>();
        amountSeries.setName("Montant");
        amountByStatus.forEach((status, amount) ->
                amountSeries.getData().add(new XYChart.Data<>(status, amount))
        );
        amountBarChart.getData().add(amountSeries);

        // Update LineChart
        Map<String, Long> ordersByDate = commandes.stream()
                .collect(Collectors.groupingBy(c -> dateFormat.format(c.getDateCommande()), Collectors.counting()));
        ordersLineChart.getData().clear();
        XYChart.Series<String, Number> ordersSeries = new XYChart.Series<>();
        ordersSeries.setName("Commandes");
        ordersByDate.forEach((date, count) ->
                ordersSeries.getData().add(new XYChart.Data<>(date, count))
        );
        ordersLineChart.getData().add(ordersSeries);
    }

    private boolean matchesSearch(Commande commande, String searchText) {
        if (searchText.isEmpty()) {
            return true;
        }
        return String.valueOf(commande.getId()).contains(searchText) ||
                (commande.getAdresseLivraison() != null && commande.getAdresseLivraison().toLowerCase().contains(searchText));
    }

    private boolean matchesDate(Commande commande, Date minDate, Date maxDate) {
        if (minDate == null && maxDate == null) {
            return true;
        }
        Date commandeDate = commande.getDateCommande();
        if (minDate != null && commandeDate.before(minDate)) {
            return false;
        }
        if (maxDate != null && commandeDate.after(maxDate)) {
            return false;
        }
        return true;
    }

    private double parseDouble(String text, double defaultValue) {
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private Date parseDate(String text, Date defaultValue) {
        try {
            return dateFormat.parse(text.trim());
        } catch (ParseException e) {
            return defaultValue;
        }
    }

    private Comparator<Commande> getComparator(String sortOption) {
        if ("Date Desc".equals(sortOption)) {
            return (c1, c2) -> c2.getDateCommande().compareTo(c1.getDateCommande());
        }
        return (c1, c2) -> c1.getDateCommande().compareTo(c2.getDateCommande());
    }

    @FXML
    private void showMap() {
        try {
            // Load Map.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/commande_ligne/Map.fxml"));
            Scene scene = new Scene(loader.load(), 800, 600);
            Stage mapStage = new Stage();
            mapStage.setTitle("Carte des Adresses de Livraison");
            mapStage.setScene(scene);

            // Get MapController and pass addresses
            MapController mapController = loader.getController();
            List<String> addresses = commandesList.stream()
                    .map(Commande::getAdresseLivraison)
                    .filter(addr -> addr != null && !addr.trim().isEmpty())
                    .collect(Collectors.toList());
            mapController.setAddresses(addresses);

            mapStage.show();
        } catch (IOException e) {
            System.err.println("❌ TableCommandes: Erreur lors du chargement de Map.fxml: " + e.getMessage());
            showAlert("Erreur", "Impossible de charger la carte: " + e.getMessage());
        }
    }

    @FXML
    private void modifierStatut() {
        Commande selectedCommande = commandesTable.getSelectionModel().getSelectedItem();
        if (selectedCommande == null) {
            showAlert("Erreur", "Veuillez sélectionner une commande.");
            return;
        }

        ChoiceDialog<String> dialog = new ChoiceDialog<>(selectedCommande.getStatut(),
                statuts.stream().filter(s -> !"Tous".equals(s)).collect(Collectors.toList()));
        dialog.setTitle("Modifier Statut");
        dialog.setHeaderText("Modifier le statut de la commande ID: " + selectedCommande.getId());
        dialog.setContentText("Nouveau statut:");

        dialog.showAndWait().ifPresent(newStatut -> {
            try {
                selectedCommande.setStatut(newStatut);
                commandeService.updateCommande(selectedCommande);
                System.out.println("TableCommandes: Statut updated for commande ID: " + selectedCommande.getId() + " to " + newStatut);
                loadCommandes();
                showAlert("Succès", "Statut mis à jour avec succès !");
            } catch (Exception e) {
                System.err.println("❌ TableCommandes: Erreur lors de la mise à jour du statut: " + e.getMessage());
                showAlert("Erreur", "Impossible de mettre à jour le statut: " + e.getMessage());
            }
        });
    }

    @FXML
    private void modifierAdresse() {
        Commande selectedCommande = commandesTable.getSelectionModel().getSelectedItem();
        if (selectedCommande == null) {
            showAlert("Erreur", "Veuillez sélectionner une commande.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog(selectedCommande.getAdresseLivraison());
        dialog.setTitle("Modifier Adresse");
        dialog.setHeaderText("Modifier l'adresse de livraison de la commande ID: " + selectedCommande.getId());
        dialog.setContentText("Nouvelle adresse:");

        dialog.showAndWait().ifPresent(newAdresse -> {
            try {
                selectedCommande.setAdresseLivraison(newAdresse);
                commandeService.updateCommande(selectedCommande);
                System.out.println("TableCommandes: Adresse updated for commande ID: " + selectedCommande.getId() + " to " + newAdresse);
                loadCommandes();
                showAlert("Succès", "Adresse mise à jour avec succès !");
            } catch (Exception e) {
                System.err.println("❌ TableCommandes: Erreur lors de la mise à jour de l'adresse: " + e.getMessage());
                showAlert("Erreur", "Impossible de mettre à jour l'adresse: " + e.getMessage());
            }
        });
    }

    @FXML
    private void exportToExcel() {
        try {
            // Calculate total revenue
            double totalRevenue = commandesList.stream()
                    .mapToDouble(Commande::getMontantTotal)
                    .sum();

            // Create workbook and sheet
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Commandes");

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "Date", "Montant (DT)", "Statut", "Adresse Livraison"};
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Populate data rows
            int rowNum = 1;
            for (Commande commande : commandesList) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(commande.getId());
                row.createCell(1).setCellValue(dateFormat.format(commande.getDateCommande()));
                row.createCell(2).setCellValue(String.format("%.2f", commande.getMontantTotal()));
                row.createCell(3).setCellValue(commande.getStatut());
                row.createCell(4).setCellValue(commande.getAdresseLivraison() != null ? commande.getAdresseLivraison() : "");
            }

            // Add total revenue row
            Row totalRow = sheet.createRow(rowNum);
            totalRow.createCell(0).setCellValue("Total Revenue");
            Cell totalCell = totalRow.createCell(2);
            totalCell.setCellValue(String.format("%.2f DT", totalRevenue));
            CellStyle totalStyle = workbook.createCellStyle();
            Font totalFont = workbook.createFont();
            totalFont.setBold(true);
            totalStyle.setFont(totalFont);
            totalCell.setCellStyle(totalStyle);

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Prompt user to save file
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Enregistrer le rapport Excel");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
            fileChooser.setInitialFileName("Commandes_Rapport.xlsx");
            java.io.File file = fileChooser.showSaveDialog(commandesTable.getScene().getWindow());

            if (file != null) {
                try (FileOutputStream fileOut = new FileOutputStream(file)) {
                    workbook.write(fileOut);
                    showAlert("Succès", "Rapport Excel exporté avec succès à " + file.getAbsolutePath());
                }
            }

            workbook.close();
        } catch (Exception e) {
            System.err.println("❌ TableCommandes: Erreur lors de l'exportation Excel: " + e.getMessage());
            showAlert("Erreur", "Impossible d'exporter le rapport Excel: " + e.getMessage());
        }

        // For PDF export, use iText library (add dependency and implement similar logic)
        // Example: Create PdfPTable with headers and data, add total revenue, save to file
    }

    @FXML
    private void handleStatutBtnHover(MouseEvent event) {
        modifierStatutBtn.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 4; -fx-padding: 8 15;");
    }

    @FXML
    private void handleStatutBtnUnhover(MouseEvent event) {
        modifierStatutBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 4; -fx-padding: 8 15;");
    }

    @FXML
    private void handleAdresseBtnHover(MouseEvent event) {
        modifierAdresseBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 4; -fx-padding: 8 15;");
    }

    @FXML
    private void handleAdresseBtnUnhover(MouseEvent event) {
        modifierAdresseBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 4; -fx-padding: 8 15;");
    }

    @FXML
    private void handleMapBtnHover(MouseEvent event) {
        showMapBtn.setStyle("-fx-background-color: #d35400; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 4; -fx-padding: 8 15;");
    }

    @FXML
    private void handleMapBtnUnhover(MouseEvent event) {
        showMapBtn.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 4; -fx-padding: 8 15;");
    }

    @FXML
    private void handleExportBtnHover(MouseEvent event) {
        exportExcelBtn.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 4; -fx-padding: 8 15;");
    }

    @FXML
    private void handleExportBtnUnhover(MouseEvent event) {
        exportExcelBtn.setStyle("-fx-background-color: #8e44ad; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 4; -fx-padding: 8 15;");
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}