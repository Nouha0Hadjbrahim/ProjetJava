package service;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class TranslationService {


    private final Translate translate; //requêtes de traduction.
    private final Cache<String, String> translationCache; //mémoire locale

    public TranslationService() {
        this.translate = TranslateOptions.newBuilder()
                .setApiKey(loadApiKeyFromConfig())
                .build()
                .getService();

        this.translationCache = CacheBuilder.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(1, TimeUnit.HOURS)
                .build();
    }

    private String loadApiKeyFromConfig() {
        Properties prop = new Properties();
        Path configPath = Paths.get("config.properties");

        try (FileInputStream input = new FileInputStream(configPath.toFile())) {
            prop.load(input);
            String apiKey = prop.getProperty("google.translate.api.key");

            if (apiKey == null || apiKey.trim().isEmpty()) {
                throw new RuntimeException("Clé API manquante dans config.properties");
            }
            return apiKey.trim();
        } catch (IOException e) {
            throw new RuntimeException(
                    "Erreur de chargement de config.properties\n" +
                            "Créez un fichier 'config.properties' avec le contenu:\n" +
                            "google.translate.api.key=votre_clé_api\n" +
                            "Emplacement: " + configPath.toAbsolutePath(),
                    e
            );
        }
    }

    public String translateText(String text, String targetLanguage) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }

        String cacheKey = text + "|" + targetLanguage;
        String cachedTranslation = translationCache.getIfPresent(cacheKey);
        if (cachedTranslation != null) {
            return cachedTranslation;
        }

        try {
            if (text.length() > 5000) {
                text = text.substring(0, 5000) + "... [texte tronqué]";
            }

            Translation translation = translate.translate(
                    text,
                    Translate.TranslateOption.targetLanguage(targetLanguage),
                    Translate.TranslateOption.model("base"),
                    Translate.TranslateOption.format("text")
            );

            String translatedText = translation.getTranslatedText();
            translationCache.put(cacheKey, translatedText);
            return translatedText;
        } catch (Exception e) {
            System.err.println("Erreur de traduction: " + e.getMessage());
            return text;
        }
    }

    /*

    private static final String API_KEY = " clé API "; // Remplacez par votre clé API
    private final Translate translate;

    // Cache pour stocker les traductions (améliore les performances)
    private final Cache<String, String> translationCache;

    public TranslationService() {
        this.translate = TranslateOptions.newBuilder().setApiKey(API_KEY).build().getService();
        this.translationCache = CacheBuilder.newBuilder()
                .maximumSize(1000) // Taille maximale du cache
                .expireAfterWrite(1, TimeUnit.HOURS) // Expire après 1 heure
                .build();
    }

    public String translateText(String text, String targetLanguage) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }

        String cacheKey = text + "|" + targetLanguage;
        String cachedTranslation = translationCache.getIfPresent(cacheKey);
        if (cachedTranslation != null) {
            return cachedTranslation;
        }

        try {
            // Limite la taille du texte à traduire (l'API a des limites)
            if (text.length() > 5000) {
                text = text.substring(0, 5000) + "... [texte tronqué]";
            }

            Translation translation = translate.translate(
                    text,
                    Translate.TranslateOption.targetLanguage(targetLanguage),
                    Translate.TranslateOption.model("base"),
                    Translate.TranslateOption.format("text") // Format texte simple
            );

            String translatedText = translation.getTranslatedText();
            translationCache.put(cacheKey, translatedText);

            return translatedText;
        } catch (Exception e) {
            System.err.println("Erreur de traduction: " + e.getMessage());
            return text; // Retourne le texte original en cas d'erreur
        }
}*/
}


