package controller;

import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.util.List;
import java.util.stream.Collectors;

public class MapController {

    @FXML private WebView mapWebView;
    @FXML private Button closeBtn;

    private List<String> addresses;

    @FXML
    public void initialize() {
        // Load the map HTML content
        String mapHtml = """
            <!DOCTYPE html>
            <html>
            <head>
                <title>Map of Delivery Addresses</title>
                <meta charset="utf-8" />
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.3/dist/leaflet.css" />
                <script src="https://unpkg.com/leaflet@1.9.3/dist/leaflet.js"></script>
                <style>
                    #map { height: 540px; width: 100%; }
                </style>
            </head>
            <body>
                <div id="map"></div>
                <script>
                    var map = L.map('map').setView([36.8065, 10.1815], 10); // Default to Tunis, Tunisia
                    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                        attribution: '© <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
                    }).addTo(map);

                    function displayAddresses(addresses) {
                        var bounds = L.latLngBounds();
                        addresses.forEach(function(address) {
                            fetch(`https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(address)}`)
                                .then(response => response.json())
                                .then(data => {
                                    if (data && data.length > 0) {
                                        var lat = parseFloat(data[0].lat);
                                        var lon = parseFloat(data[0].lon);
                                        var marker = L.marker([lat, lon])
                                            .addTo(map)
                                            .bindPopup(address);
                                        bounds.extend([lat, lon]);
                                        if (bounds.isValid()) {
                                            map.fitBounds(bounds);
                                        }
                                    } else {
                                        console.warn(`No coordinates found for address: ${address}`);
                                    }
                                })
                                .catch(error => {
                                    console.error(`Error geocoding address ${address}:`, error);
                                });
                        });
                    }
                </script>
            </body>
            </html>
            """;

        mapWebView.getEngine().loadContent(mapHtml);

        // Load addresses after the WebView is ready
        mapWebView.getEngine().getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED && addresses != null) {
                // Convert addresses to JSON string
                String addressesJson = "[" + addresses.stream()
                        .map(addr -> "\"" + addr.replace("\"", "\\\"") + "\"")
                        .collect(Collectors.joining(",")) + "]";

                // Execute JavaScript to display addresses
                mapWebView.getEngine().executeScript("displayAddresses(" + addressesJson + ");");
            }
        });
    }

    public void setAddresses(List<String> addresses) {
        this.addresses = addresses;
        // If WebView is already loaded, display addresses immediately
        if (mapWebView.getEngine().getLoadWorker().getState() == Worker.State.SUCCEEDED) {
            String addressesJson = "[" + addresses.stream()
                    .map(addr -> "\"" + addr.replace("\"", "\\\"") + "\"")
                    .collect(Collectors.joining(",")) + "]";
            mapWebView.getEngine().executeScript("displayAddresses(" + addressesJson + ");");
        }
    }

    @FXML
    private void close() {
        Stage stage = (Stage) closeBtn.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleCloseBtnHover(MouseEvent event) {
        closeBtn.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 4; -fx-padding: 8 15;");
    }

    @FXML
    private void handleCloseBtnUnhover(MouseEvent event) {
        closeBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 4; -fx-padding: 8 15;");
    }
}