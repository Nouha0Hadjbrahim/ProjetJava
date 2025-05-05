package controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import model.Ateliers;
import org.apache.commons.text.StringEscapeUtils;
import service.AteliersService;
import utils.SessionManager;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;


public class CalendrierController implements Initializable {

    @FXML
    private WebView calendarWebView;

    private final AteliersService atelierService = new AteliersService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadCalendar();
    }

    private void loadCalendar() {
        WebEngine engine = calendarWebView.getEngine();
        String html = buildFullCalendarHtml(SessionManager.getCurrentUser().getId());
        engine.loadContent(html);

    }

    private String buildFullCalendarHtml(int artisanId) {
        List<Ateliers> ateliers = atelierService.getAllAteliers();
        StringBuilder events = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        LocalDateTime now = LocalDateTime.now();

        for (Ateliers atelier : ateliers) {
            if (atelier.getUser() != artisanId) continue;

            String start = atelier.getDatecours().format(formatter);
            String end = atelier.getDatecours().plusMinutes(atelier.getDuree()).format(formatter);

            // Couleur dynamique
            String color;
            if (atelier.getDatecours().toLocalDate().isEqual(now.toLocalDate())) {
                color = "#dc3545"; // 🔴 aujourd’hui
            } else if (atelier.getDatecours().isBefore(now)) {
                color = "#6c757d"; // ⚪ passé
            } else {
                color = "#28a745"; // 🟢 futur
            }

            // Heure formatée
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            String startTime = atelier.getDatecours().format(timeFormatter);
            String endTime = atelier.getDatecours().plusMinutes(atelier.getDuree()).format(timeFormatter);

            // Icône 🌙 ou ☀️
            int hour = atelier.getDatecours().getHour();
            String icon = (hour >= 18 || hour < 6) ? "🌙" : "☀";

            // Texte de la première ligne
            String line1 = StringEscapeUtils.escapeEcmaScript(icon + " " + startTime + " - " + endTime);
            String title = StringEscapeUtils.escapeEcmaScript(atelier.getTitre());

            // Création de l’événement
            events.append(String.format(
                    "{ title: '%s', start: '%s', end: '%s', color: '%s', extendedProps: { line1: '%s' } },",
                    title, start, end, color, line1));
        }

        return "<html>" +
                "<head>" +
                "<meta charset='UTF-8'>" +
                "<link href='https://cdn.jsdelivr.net/npm/fullcalendar@6.1.8/index.global.min.css' rel='stylesheet' />" +
                "<script src='https://cdn.jsdelivr.net/npm/fullcalendar@6.1.8/index.global.min.js'></script>" +
                "<style>" +
                ".fc-event {" +
                "  font-size: 0.85em;" +
                "  padding: 6px;" +
                "  border-radius: 6px;" +
                "  white-space: normal;" +
                "}" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div id='calendar'></div>" +
                "<script>" +
                "document.addEventListener('DOMContentLoaded', function() {" +
                "  var calendarEl = document.getElementById('calendar');" +
                "  var calendar = new FullCalendar.Calendar(calendarEl, {" +
                "    initialView: 'dayGridMonth'," +
                "    height: 'auto'," +
                "    contentHeight: 'auto'," +
                "    aspectRatio: 1.5," +
                "    headerToolbar: {" +
                "      left: 'prev,next today'," +
                "      center: 'title'," +
                "      right: 'dayGridMonth,timeGridWeek,timeGridDay'" +
                "    }," +
                "    views: {" +
                "      dayGridMonth: {" +
                "        dayMaxEventRows: 4," +
                "        dayHeaderFormat: { weekday: 'short' }" +
                "      }" +
                "    }," +
                "    eventTimeFormat: {" +
                "      hour: '2-digit'," +
                "      minute: '2-digit'," +
                "      hour12: false" +
                "    }," +

                // ✅ Personnalisation de l'affichage des événements
                "    eventContent: function(arg) {" +
                "      const wrapper = document.createElement('div');" +
                "      wrapper.style.backgroundColor = arg.event.backgroundColor || arg.event.color;" +
                "      wrapper.style.borderRadius = '6px';" +
                "      wrapper.style.padding = '4px';" +
                "      wrapper.style.color = 'white';" +

                "      const line1 = document.createElement('div');" +
                "      line1.innerText = arg.event.extendedProps.line1;" +
                "      line1.style.fontWeight = 'bold';" +

                "      const line2 = document.createElement('div');" +
                "      line2.innerText = arg.event.title;" +
                "      line2.style.textAlign = 'center';" +

                "      wrapper.appendChild(line1);" +
                "      wrapper.appendChild(line2);" +
                "      return { domNodes: [wrapper] };" +
                "    }," +

                "    events: [" + events.toString() + "]" +
                "  });" +
                "  calendar.render();" +
                "});" +
                "</script>" +
                "</body>" +
                "</html>";

    }


}