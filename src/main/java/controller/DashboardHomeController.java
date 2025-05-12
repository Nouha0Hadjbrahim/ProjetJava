package controller;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import model.Ateliers;
import model.Todo;
import model.User;
import service.TodoService;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.animation.FadeTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.concurrent.Task;
import org.json.JSONObject;
import javafx.scene.chart.PieChart;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import service.UserService;
import service.AteliersService;
import service.InscriptionAtelierService;

public class DashboardHomeController {

    @FXML private TextField taskInput;
    @FXML private VBox todoList;
    @FXML private ImageView artisanatImage;
    @FXML private Label welcomeLabel;
    @FXML private ImageView weatherIcon;
    @FXML private Label temperatureLabel;
    @FXML private Label locationLabel;
    @FXML private Label dateLabel;
    @FXML private Label dayLabel;
    @FXML private StackPane chartContainer;
    @FXML private Label totalUsersLabel;
    @FXML private Label totalArtisansLabel;
    @FXML
    private PieChart pieChartInscriptions;
    @FXML
    private Label totalInscriptionsLabel;
    @FXML
    private Label totalInscriptionsCardLabel;
    @FXML
    private HBox customLegend;


    private final TodoService todoService = new TodoService();
    private final UserService userService = new UserService();
    private final AteliersService atelierService = new AteliersService();
    private final InscriptionAtelierService inscriptionService = new InscriptionAtelierService();

    private User connectedUser;
    private List<Image> images = new ArrayList<>();
    private int currentImageIndex = 0;
    private SequentialTransition animation;

    @FXML
    public void initialize() {
        loadImages();
        setupImageAnimation();
        setupWeatherAndDate();
        startWeatherUpdateTask();
        setupStatistics();
        setupAteliersStatistics();

    }

    private void setupWeatherAndDate() {
        updateDateTime();

        Thread dateThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(60000);
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                updateDateTime();
                            }
                        });
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        });
        dateThread.setDaemon(true);
        dateThread.start();

        updateWeather();
    }

    private void updateDateTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.FRENCH);
        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("EEEE", Locale.FRENCH);

        dateLabel.setText(now.format(dateFormatter));
        dayLabel.setText(now.format(dayFormatter).substring(0, 1).toUpperCase() +
                        now.format(dayFormatter).substring(1));
    }

    private void startWeatherUpdateTask() {
        // Mise à jour initiale
        updateWeather();

        // Créer une tâche périodique pour mettre à jour la météo toutes les 30 minutes
        Task<Void> weatherTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                while (true) {
                    Thread.sleep(30 * 60 * 1000); // 30 minutes
                    updateWeather();
                }
            }
        };

        Thread weatherThread = new Thread(weatherTask);
        weatherThread.setDaemon(true);
        weatherThread.start();
    }

    private void updateWeather() {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    String urlString = WEATHER_API_URL + WEATHER_API_KEY;
                    URL url = new URL(urlString);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");

                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    JSONObject jsonResponse = new JSONObject(response.toString());
                    JSONObject main = jsonResponse.getJSONObject("main");
                    double temperature = main.getDouble("temp");
                    String weatherIconCode = jsonResponse.getJSONArray("weather")
                                                       .getJSONObject(0)
                                                       .getString("icon");

                    final String iconUrl = "http://openweathermap.org/img/w/" + weatherIconCode + ".png";
                    final String tempText = String.format("%.1f°C", temperature);
                    final String location = jsonResponse.getString("name") + ", Tunisie";

                    Platform.runLater(() -> {
                        temperatureLabel.setText(tempText);
                        weatherIcon.setImage(new Image(iconUrl));
                        locationLabel.setText(location);
                    });

                } catch (Exception e) {
                    System.err.println("Erreur lors de la récupération de la météo: " + e.getMessage());
                    // En cas d'erreur, afficher des données par défaut
                    Platform.runLater(() -> {
                        temperatureLabel.setText("--°C");
                        weatherIcon.setImage(new Image("http://openweathermap.org/img/w/01d.png"));
                        locationLabel.setText("Ariana, Tunisie");
                    });
                }
                return null;
            }
        };

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private void loadImages() {
        File imageDir = new File("src/main/resources/assets/artisanat");
        if (imageDir.exists() && imageDir.isDirectory()) {
            File[] files = imageDir.listFiles((dir, name) ->
                name.toLowerCase().endsWith(".jpg") ||
                name.toLowerCase().endsWith(".png") ||
                name.toLowerCase().endsWith(".jpeg"));

            if (files != null) {
                for (File file : files) {
                    images.add(new Image(file.toURI().toString()));
                }
            }
        }
    }

    private void setupImageAnimation() {
        if (images.isEmpty()) return;

        animation = new SequentialTransition();

        for (int i = 0; i < images.size(); i++) {
            final int index = i;

            FadeTransition fadeOut = new FadeTransition(Duration.seconds(1), artisanatImage);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);

            PauseTransition pause = new PauseTransition(Duration.seconds(3));

            FadeTransition fadeIn = new FadeTransition(Duration.seconds(1), artisanatImage);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);

            fadeOut.setOnFinished(e -> {
                artisanatImage.setImage(images.get(index));
            });

            animation.getChildren().addAll(fadeOut, fadeIn, pause);
        }

        animation.setCycleCount(SequentialTransition.INDEFINITE);
        animation.play();
    }

    public void setUser(User user) {
        this.connectedUser = user;
        welcomeLabel.setText("Bonjour " + user.getPrenom() + " " + user.getNom());
        loadTodos();
    }

    @FXML
    private void handleAddTask() {
        String description = taskInput.getText().trim();
        if (!description.isEmpty() && connectedUser != null) {
            todoService.addTodo(description, connectedUser.getId());
            taskInput.clear();
            loadTodos();
        }
    }

    private void loadTodos() {
        if (connectedUser == null) return;

        todoList.getChildren().clear();
        for (Todo todo : todoService.getAllTodos(connectedUser.getId())) {
            HBox todoItem = createTodoItem(todo);
            todoList.getChildren().add(todoItem);
        }
    }

    private HBox createTodoItem(Todo todo) {
        HBox todoItem = new HBox(10);
        todoItem.setAlignment(Pos.CENTER_LEFT);
        todoItem.getStyleClass().add("todo-item");

        CheckBox checkBox = new CheckBox();
        checkBox.setSelected(todo.isCompleted());
        checkBox.setOnAction(e -> {
            todoService.updateTodoStatus(todo.getId(), checkBox.isSelected());
            loadTodos();
        });

        Label description = new Label(todo.getDescription());
        description.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(description, Priority.ALWAYS);

        if (todo.isCompleted()) {
            description.getStyleClass().add("completed");
        }

        Button deleteBtn = new Button("×");
        deleteBtn.getStyleClass().add("delete-button");
        deleteBtn.setOnAction(e -> {
            todoService.deleteTodo(todo.getId());
            loadTodos();
        });

        todoItem.getChildren().addAll(checkBox, description, deleteBtn);
        return todoItem;
    }

    private void setupStatistics() {
        try {
            // Get total users count
            int totalUsers = userService.getTotalUsers();
            totalUsersLabel.setText(String.valueOf(totalUsers));

            // Get total artisans count
            int totalArtisans = userService.getTotalArtisans();
            totalArtisansLabel.setText(String.valueOf(totalArtisans));

            // Calculate user status distribution
            int activeUsers = userService.getActiveUsersCount();
            int blockedUsers = userService.getBlockedUsersCount();

            // Create pie chart data
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("Actifs", activeUsers),
                new PieChart.Data("Bloqués", blockedUsers)
            );

            // Create and configure pie chart
            PieChart chart = new PieChart(pieChartData);
            chart.setTitle("");
            chart.setLegendVisible(false);
            chart.setLabelsVisible(true);

            // Calculate percentages for labels
            double total = activeUsers + blockedUsers;
            pieChartData.forEach(data -> {
                String percentage = String.format("%.1f%%", (data.getPieValue() / total) * 100);
                data.setName(percentage);
            });

            // Style the chart
            chart.getStyleClass().add("custom-chart");

            // Add the chart to the container
            chartContainer.getChildren().setAll(chart);

        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des statistiques: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public void setupAteliersStatistics() {
        try {
            List<Ateliers> allAteliers = atelierService.getAllAteliers();
            if (allAteliers == null || allAteliers.isEmpty()) return;

            Map<String, Integer> stats = allAteliers.stream()
                    .collect(Collectors.groupingBy(
                            Ateliers::getCategorie,
                            Collectors.summingInt(atelier -> inscriptionService.getNombreInscriptions(atelier.getId()))
                    ));

            int totalInscriptions = stats.values().stream().mapToInt(Integer::intValue).sum();

            if (totalInscriptions == 0) {
                totalInscriptionsLabel.setText("0");
                totalInscriptionsCardLabel.setText("0");
                pieChartInscriptions.setData(FXCollections.observableArrayList());
                return;
            }

            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
            Map<PieChart.Data, String> originalNames = new HashMap<>();

            stats.forEach((categorie, count) -> {
                PieChart.Data data = new PieChart.Data(categorie, count);
                pieChartData.add(data);
                originalNames.put(data, categorie); // Sauvegarder le vrai nom
            });

            pieChartInscriptions.setData(pieChartData);
            pieChartInscriptions.setTitle("");
            pieChartInscriptions.setLegendVisible(false); // Désactiver la légende automatique
            pieChartInscriptions.setLabelsVisible(true);  // Activer les labels visibles

            totalInscriptionsLabel.setText(String.valueOf(totalInscriptions));
            totalInscriptionsCardLabel.setText(String.valueOf(totalInscriptions));

            if (!pieChartInscriptions.getStyleClass().contains("custom-chart")) {
                pieChartInscriptions.getStyleClass().add("custom-chart");
            }

            // ➡️ Créer la légende personnalisée
            customLegend.getChildren().clear();
            int index = 0;
            for (PieChart.Data data : pieChartInscriptions.getData()) {
                HBox legendItem = new HBox(5);
                Region colorRegion = new Region();
                colorRegion.setPrefSize(10, 10);
                colorRegion.getStyleClass().add("default-color" + index); // Utiliser la couleur standard JavaFX

                Label label = new Label(originalNames.get(data)); // Afficher le vrai nom de la catégorie
                legendItem.getChildren().addAll(colorRegion, label);
                customLegend.getChildren().add(legendItem);

                index++;
            }

            // ➡️ Modifier les labels visibles sur le camembert pour afficher pourcentage + nom
            pieChartData.forEach(data -> {
                double pourcentage = (data.getPieValue() / totalInscriptions) * 100;
                String vraiNom = originalNames.get(data); // Récupérer le nom original
                data.nameProperty().set(String.format("%.1f%% %s", pourcentage, vraiNom));
            });

        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des statistiques des ateliers: " + e.getMessage());
            e.printStackTrace();
        }
    }


}