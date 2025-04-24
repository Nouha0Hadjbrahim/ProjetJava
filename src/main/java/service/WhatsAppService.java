package service;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.Material;

public class WhatsAppService {
    private static final String API_URL = "https://graph.facebook.com/v19.0/";
    private static final String PHONE_NUMBER_ID = "609959038874010";
    private static final String ACCESS_TOKEN = "EAAOM00FWLgcBOZBIgnT4W6bEWUylQRsA5gV4lncJNHSZCSWKpDPWbUhCKBjxkfZCol32mYC55oNihU0h9sRNFiG5ZAYHVK9PZAty2nIZCiTy1B1eOQpX1mr1c9ZC4n7eVO6bds3XMzxkBr3l8DARqMeV1zlDtSRRJM7CBySFfPbRlVa3oLuu1rkNZBRpZA3SSiz4IZAs0OZBGJklSlQoQmGyu2HCMMDNQTCReXzjLTEE49E";
    private static final String RECIPIENT = "21658761296";
    private final ObjectMapper mapper = new ObjectMapper();

    public void sendLowStockAlert(List<Material> lowStockMaterials) throws Exception {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("messaging_product", "whatsapp");
            payload.put("to", RECIPIENT);
            payload.put("type", "template");


            List<Map<String, Object>> parameters = new ArrayList<>();
            for (int i = 0; i < 3; i++) { // 3 matériaux max
                if (i < lowStockMaterials.size()) {
                    Material m = lowStockMaterials.get(i);
                    parameters.add(createParam(m.getNomMateriel()));
                    parameters.add(createParam(m.getQuantiteStock() + " unités"));
                } else {
                    parameters.add(createParam("N/A")); // Valeur par défaut
                    parameters.add(createParam("N/A"));
                }
            }


            Map<String, Object> template = new HashMap<>();
            template.put("name", "alerte_low_stock"); // Nom exact du template
            template.put("language", Collections.singletonMap("code", "fr"));

            Map<String, Object> bodyComponent = new HashMap<>();
            bodyComponent.put("type", "body");
            bodyComponent.put("parameters", parameters);

            template.put("components", Collections.singletonList(bodyComponent));
            payload.put("template", template);

            String json = mapper.writeValueAsString(payload);
            System.out.println("Request payload: " + json);

            URL url = new URL(API_URL + PHONE_NUMBER_ID + "/messages");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + ACCESS_TOKEN);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.getBytes(StandardCharsets.UTF_8));
            }

            handleApiResponse(conn);

        } catch (Exception e) {
            throw new RuntimeException("Échec de l'envoi WhatsApp: " + e.getMessage(), e);
        }
    }

    private List<Map<String, Object>> generateParameters(List<Material> materials) {
        List<Map<String, Object>> parameters = new ArrayList<>();
        int paramCount = 0;

        for (Material m : materials) {
            if(paramCount >= 6) break;

            parameters.add(createParam(m.getNomMateriel()));
            parameters.add(createParam(String.valueOf(m.getQuantiteStock())));
            paramCount += 2;
        }

        while(paramCount < 6) {
            parameters.add(createParam("-")); // Remplissage des paramètres manquants
            paramCount++;
        }

        return parameters;
    }

    private Map<String, Object> createParam(String value) {
        Map<String, Object> param = new HashMap<>();
        param.put("type", "text");
        param.put("text", value);
        return param;
    }

    private void handleApiResponse(HttpURLConnection conn) throws Exception {
        int code = conn.getResponseCode();
        if (code < 200 || code >= 300) {
            String errorResponse = readErrorStream(conn);
            throw new RuntimeException("Erreur API WhatsApp [" + code + "]: " + errorResponse);
        }
    }

    private String readErrorStream(HttpURLConnection conn) throws Exception {
        return new String(conn.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
    }
}