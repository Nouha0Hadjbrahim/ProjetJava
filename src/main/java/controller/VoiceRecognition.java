package controller;

import com.microsoft.cognitiveservices.speech.*;
import com.microsoft.cognitiveservices.speech.audio.*;
import javafx.scene.control.TextField;

public class VoiceRecognition {

    public static void recognizeSpeech(TextField textField) {
        String apiKey = "1eCsjj8QtNgjjGA2YvOdgr95EDMOAlRMeqiWNwvyCbhDtMuZ8MazJQQJ99BDACYeBjFXJ3w3AAAYACOGpY8Y";
        String region = "eastus";

        try {
            SpeechConfig speechConfig = SpeechConfig.fromSubscription(apiKey, region);
            speechConfig.setSpeechRecognitionLanguage("fr-FR");

            AudioConfig audioConfig = AudioConfig.fromDefaultMicrophoneInput();

            try (SpeechRecognizer recognizer = new SpeechRecognizer(speechConfig, audioConfig)) {
                System.out.println("Parlez maintenant...");

                recognizer.recognized.addEventListener((s, e) -> {
                    if (e.getResult().getReason() == ResultReason.RecognizedSpeech) {
                        String recognizedText = e.getResult().getText();
                        // Mettre à jour le TextField sur le thread JavaFX
                        javafx.application.Platform.runLater(() -> {
                            // Au lieu d'ajouter du texte comme un TextArea, ici on REMPLACE ou COMPLETE dans un TextField
                            String currentText = textField.getText();
                            textField.setText(currentText + recognizedText + " ");
                        });
                    }
                });

                // Démarrer la reconnaissance
                recognizer.startContinuousRecognitionAsync().get();

                // Laisser parler pendant 5 secondes
                Thread.sleep(5000);

                recognizer.stopContinuousRecognitionAsync().get();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            javafx.application.Platform.runLater(() -> {
                textField.setText(textField.getText() + "\nErreur vocale: " + ex.getMessage());
            });
        }
    }
}
