package controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.HistoriqueConnexion;
import model.User;
import service.HistoriqueConnexionService;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class HistoriqueConnexionController implements Initializable {

    @FXML private VBox historiqueList;
    @FXML private Label btnPrev;
    @FXML private Label btnNext;
    @FXML private HBox paginationBox;

    private final HistoriqueConnexionService service = new HistoriqueConnexionService();
    private User user;

    private int currentPage = 0;
    private final int itemsPerPage = 3;
    private List<HistoriqueConnexion> allConnexions;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        btnPrev.setOnMouseClicked(e -> {
            if (currentPage > 0) {
                currentPage--;
                afficherPage();
            }
        });

        btnNext.setOnMouseClicked(e -> {
            if ((currentPage + 1) * itemsPerPage < allConnexions.size()) {
                currentPage++;
                afficherPage();
            }
        });
    }

    public void setUser(User user) {
        this.user = user;
        allConnexions = service.findByEmail(user.getEmail());
        currentPage = 0;
        afficherPage();
    }

    private void afficherPage() {
        historiqueList.getChildren().clear();

        int start = currentPage * itemsPerPage;
        int end = Math.min(start + itemsPerPage, allConnexions.size());
        List<HistoriqueConnexion> pageData = allConnexions.subList(start, end);

        for (HistoriqueConnexion h : pageData) {
            VBox card = new VBox(5);
            card.getStyleClass().add("historique-card");

            Label dateLabel = new Label("📅 " + h.getDateConnexion().toString());
            dateLabel.getStyleClass().add("historique-date");

            Label ipLabel = new Label("🌐 IP : " + h.getAdresseIp());
            ipLabel.getStyleClass().add("historique-ip");

            card.getChildren().addAll(dateLabel, ipLabel);
            historiqueList.getChildren().add(card);
        }
    }
}
