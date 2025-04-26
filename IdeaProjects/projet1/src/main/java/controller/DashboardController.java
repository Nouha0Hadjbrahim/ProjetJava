package controller;

import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane ;

import javafx.scene.layout.VBox;
import model.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import model.User;
import service.UserService;
import service.AteliersService;
import service.InscriptionAtelierService;
import model.Ateliers;

import java.awt.datatransfer.Clipboard;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


import java.io.IOException;
import java.util.regex.Pattern;

public class DashboardController {

    @FXML
    private Label userNameLabel;

    @FXML
    private ImageView userPhoto;

    @FXML
    private ImageView logoImage; // facultatif si tu veux changer le logo dynamiquement


    @FXML
    private Button btnTableUtilisateurs;

    @FXML
    private Button btnTableAteliers;

    @FXML private Button btnDashboard;
    @FXML
    private StackPane mainContent;
    @FXML
    private BarChart<String, Number> barChart;

    private final AteliersService atelierService = new AteliersService();
    private final InscriptionAtelierService inscriptionService = new InscriptionAtelierService();

    // Méthode appelée lors du chargement du dashboard avec l'utilisateur connecté
    public void initialize(User connectedUser) {
        btnDashboard.setOnAction(e -> loadDashboardHome());

// Charger par défaut au démarrage
        loadDashboardHome();

        if (connectedUser != null) {
            userNameLabel.setText(connectedUser.getPrenom() + "." + connectedUser.getNom());
        } else {
            userNameLabel.setText("Utilisateur");
        }

        // Charger une photo par défaut (tu peux la remplacer dynamiquement plus tard)
        logoImage.setImage(new Image(getClass().getResourceAsStream("/assets/logo2.png")));
        Image userImg = new Image(getClass().getResourceAsStream("/assets/userf.png"));
        userPhoto.setImage(userImg);
    }

    @FXML
    public void initialize() {
        btnTableUtilisateurs.setOnAction(event -> loadTableUtilisateurs());
        btnTableAteliers.setOnAction(event -> loadTableAteliers());
    }


    // Getters si besoin (pour injection dynamique)
    public StackPane  getMainContent() {
        return mainContent;
    }


    private void loadTableUtilisateurs() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/TableUtilisateurs.fxml"));
            Parent view = loader.load();
            mainContent.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadTableAteliers() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/TableAteliersAdmin.fxml"));
            Parent view = loader.load();
            mainContent.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void loadDashboardHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/DashboardHome.fxml"));
            AnchorPane dashboardView = loader.load();
            mainContent.getChildren().setAll(dashboardView);
        } catch (IOException e) {
            e.printStackTrace();
        }
        loadGraphiquesStatistiques();

    }

    public void loadGraphiquesStatistiques() {
        List<Ateliers> allAteliers = atelierService.getAllAteliers();
        if (allAteliers == null || allAteliers.isEmpty()) return;

        var stats = allAteliers.stream()
                .collect(Collectors.groupingBy(
                        Ateliers::getCategorie,
                        Collectors.summingInt(atelier -> inscriptionService.getNombreInscriptions(atelier.getId()))
                ));

        barChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Inscriptions par Catégorie");

        // TRIER les statistiques par nombre d'inscrits décroissant
        stats.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(entry -> {
                    series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
                });

        barChart.getData().add(series);

        // Modifier la couleur APRÈS affichage
        javafx.application.Platform.runLater(() -> {
            series.getData().forEach(data -> {
                if (data.getNode() != null) {
                    data.getNode().setStyle("-fx-bar-fill: #2b2f48;");
                }
            });

            barChart.lookupAll(".chart-legend-item").forEach(item -> {
                if (item instanceof Label) {
                    Label label = (Label) item;
                    if (label.getText().equals("Inscriptions par Catégorie")) {
                        label.setStyle("-fx-text-fill: #2b2f48;");
                    }
                }
            });

            barChart.lookupAll(".chart-legend-item-symbol").forEach(symbol -> {
                symbol.setStyle("-fx-background-color: #2b2f48;");
            });
        });

        // Ajuster l'axe Y pour afficher des entiers
        NumberAxis yAxis = (NumberAxis) barChart.getYAxis();
        yAxis.setTickUnit(1);
        yAxis.setMinorTickVisible(false);
        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(0);
        yAxis.setUpperBound(getMaxValue(stats) + 1);

        // Ajuster la taille du graphique
        barChart.setPrefWidth(400);
        barChart.setPrefHeight(300);
    }

    private int getMaxValue(Map<String, Integer> stats) {
        return stats.values().stream().max(Integer::compareTo).orElse(10);
    }











}
