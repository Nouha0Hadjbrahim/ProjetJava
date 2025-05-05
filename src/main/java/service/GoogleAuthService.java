package service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Userinfo;
import javafx.scene.control.Alert;
import model.User;
import utils.PasswordUtils;
import java.io.IOException;

import java.io.File;
import java.net.URI;
import java.net.InetSocketAddress;
import java.awt.Desktop;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.OutputStream;
import java.util.concurrent.CompletableFuture;
import java.util.Collections;
import java.util.List;

public class GoogleAuthService {
    private static final String APPLICATION_NAME = "SymfonyAuthGoogle";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final List<String> SCOPES = Collections.singletonList(
            "https://www.googleapis.com/auth/userinfo.email https://www.googleapis.com/auth/userinfo.profile");
    private static final String CLIENT_ID = "617376679510-g1aebddrvjul0dkbj9tvhqjjsa346tb3.apps.googleusercontent.com";
    private static final String CLIENT_SECRET = "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";
    private static final String REDIRECT_URI = "http://localhost:8888/oauth2callback";

    private final UserService userService;
    private final NetHttpTransport HTTP_TRANSPORT;

    public GoogleAuthService() throws Exception {
        this.userService = new UserService();
        this.HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
    }

    public User authenticateWithGoogle() throws Exception {
        // Créer le dossier tokens s’il n’existe pas
        File tokensDir = new File(TOKENS_DIRECTORY_PATH);
        if (!tokensDir.exists() && !tokensDir.mkdirs()) {
            throw new IOException("Impossible de créer le dossier: " + TOKENS_DIRECTORY_PATH);
        }

        // Préparer le flux OAuth2
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, CLIENT_ID, CLIENT_SECRET, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(tokensDir))
                .setAccessType("offline")
                .build();

        // Construire l'URL d'authentification
        String authorizationUrl = new GoogleAuthorizationCodeRequestUrl(
                CLIENT_ID, REDIRECT_URI, SCOPES)
                .setAccessType("offline")
                .setApprovalPrompt("force")
                .build();

        // Ouvrir dans le navigateur
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().browse(new URI(authorizationUrl));
        } else {
            Runtime.getRuntime().exec("xdg-open " + authorizationUrl);
        }

        // Démarrer serveur local
        HttpServer server = HttpServer.create(new InetSocketAddress(8888), 0);
        CompletableFuture<String> codeFuture = new CompletableFuture<>();

        server.createContext("/oauth2callback", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) {
                try {
                    String query = exchange.getRequestURI().getQuery();
                    String code = query.split("code=")[1].split("&")[0];

                    String response = "✅ Authentification réussie. Vous pouvez fermer cette fenêtre.";
                    byte[] responseBytes = response.getBytes("UTF-8");

                    exchange.sendResponseHeaders(200, responseBytes.length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(responseBytes);
                    os.close();

                    codeFuture.complete(code);
                } catch (Exception e) {
                    codeFuture.completeExceptionally(e);
                } finally {
                    server.stop(0);
                }
            }
        });

        server.start();

        try {
            // Attendre le code
            String authorizationCode = codeFuture.get();

            // Échanger le code contre des tokens
            GoogleTokenResponse tokenResponse = flow.newTokenRequest(authorizationCode)
                    .setRedirectUri(REDIRECT_URI)
                    .execute();

            // Créer un credential temporaire
            Credential credential = flow.createAndStoreCredential(tokenResponse, "temp_user");

            // Appel à l’API Google pour infos utilisateur
            Oauth2 oauth2 = new Oauth2.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                    .setApplicationName(APPLICATION_NAME)
                    .build();

            Userinfo userInfo = oauth2.userinfo().get().execute();

            // (Re)stocker credential sous l'email exact
            flow.createAndStoreCredential(tokenResponse, userInfo.getEmail());

            // Utilisateur déjà existant ?
            User existingUser = userService.findByEmail(userInfo.getEmail());
            if (existingUser != null) {
                return existingUser;
            }

            // Sinon, créer le nouvel utilisateur
            User newUser = new User();
            newUser.setEmail(userInfo.getEmail());
            newUser.setNom(userInfo.getFamilyName());
            newUser.setPrenom(userInfo.getGivenName());
            newUser.setPassword(PasswordUtils.hashPassword("Azert1"));
            newUser.setRoles("[\"ROLE_CLIENT\"]");

            userService.createUser(newUser);
            newUser = userService.getUserByEmail(newUser.getEmail());

            return newUser;

        } finally {
            server.stop(0);
        }
    }
}
