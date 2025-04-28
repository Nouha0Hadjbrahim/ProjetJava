package controller;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import model.Reclamation;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import service.ReclamationService;
import service.ReponseService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.File;
import java.io.FileOutputStream;

import javafx.stage.FileChooser;

public class ReclamationDetailsController {
    @FXML private Label idLabel;
    @FXML private Label titreLabel;
    @FXML private TextArea descriptionArea;
    @FXML private Label statutLabel;
    @FXML private Label dateLabel;
    @FXML private TextArea reponseArea;
    @FXML private HBox actionButtonsBox;
    @FXML private Button modifierButton;
    @FXML private Button supprimerButton;
    @FXML private Button pdfButton;

    private Runnable refreshCallback;
    private Reclamation reclamation;
    private final ReclamationService reclamationService = new ReclamationService();

    public void initialize() {
        modifierButton.setOnAction(event -> handleModifier());
        supprimerButton.setOnAction(event -> handleSupprimer());
    }

    public void setReclamation(Reclamation reclamation) {
        this.reclamation = reclamation;
        updateUI();
    }

    public void setRefreshCallback(Runnable callback) {
        this.refreshCallback = callback;
    }

    private void updateUI() {
        if (reclamation == null) {
            System.out.println("Aucune réclamation à afficher.");
            return;
        }

        idLabel.setText(String.valueOf(reclamation.getId()));
        titreLabel.setText(reclamation.getTitre());
        descriptionArea.setText(reclamation.getDescription());
        statutLabel.setText(reclamation.getStatut());
        dateLabel.setText(reclamation.getDateReclamation().toString());

        // Masquer tous les boutons par défaut
        modifierButton.setVisible(false);
        supprimerButton.setVisible(false);
        pdfButton.setVisible(false);

        String statut = reclamation.getStatut().trim().toLowerCase();

        try {
            // Gestion des statuts
            if (statut.equals("en attente")) {
                modifierButton.setVisible(true);
                supprimerButton.setVisible(true);
                reponseArea.setText("Pas encore répondue");
            }
            else if (statut.equals("en cours") || statut.equals("encours")) {
                reponseArea.setText("Pas encore répondue");
            }
            else if (statut.equals("répondu") || statut.equals("repondu") ||
                    statut.equals("répondue") || statut.equals("repondue")) {
                pdfButton.setVisible(true);
                ReponseService reponseService = new ReponseService();
                String reponse = reponseService.getReponseByReclamationId(reclamation.getId());
                reponseArea.setText(reponse != null && !reponse.trim().isEmpty() ? reponse : "Pas encore répondue");
            }
            else {
                reponseArea.setText("Statut inconnu : " + reclamation.getStatut());
            }
        } catch (Exception e) {
            e.printStackTrace();
            reponseArea.setText("Erreur lors du chargement de la réponse");
        }
    }


    @FXML
    private void handleModifier() {
        titreLabel.setVisible(false);
        descriptionArea.setEditable(true);

        TextField titreField = new TextField(reclamation.getTitre());
        titreField.setStyle("-fx-font-size: 14px; -fx-padding: 5px;");
        GridPane.setConstraints(titreField, 1, 1);

        GridPane gridPane = (GridPane) titreLabel.getParent();
        gridPane.getChildren().remove(titreLabel);
        gridPane.getChildren().add(titreField);

        modifierButton.setText("Enregistrer");
        modifierButton.setOnAction(e -> handleSaveModifications(titreField));
        supprimerButton.setText("Annuler");
        supprimerButton.setOnAction(e -> cancelModification(gridPane));
    }

    private void handleSaveModifications(TextField titreField) {
        String nouveauTitre = titreField.getText().trim();
        String nouvelleDescription = descriptionArea.getText().trim();

        if (nouveauTitre.isEmpty() || nouvelleDescription.isEmpty()) {
            showAlert("Erreur", "Le titre et la description ne peuvent pas être vides");
            return;
        }

        try {
            boolean success = reclamationService.updateReclamation(
                    reclamation.getId(),
                    nouveauTitre,
                    nouvelleDescription
            );

            if (success) {
                reclamation.setTitre(nouveauTitre);
                reclamation.setDescription(nouvelleDescription);

                GridPane gridPane = (GridPane) titreField.getParent();
                gridPane.getChildren().remove(titreField);
                gridPane.getChildren().add(titreLabel);
                titreLabel.setText(nouveauTitre);
                descriptionArea.setText(nouvelleDescription);

                titreLabel.setVisible(true);
                descriptionArea.setEditable(false);

                modifierButton.setText("Modifier");
                modifierButton.setOnAction(e -> handleModifier());
                supprimerButton.setText("Supprimer");
                supprimerButton.setOnAction(e -> handleSupprimer());

                if (refreshCallback != null) {
                    refreshCallback.run();
                }

                showAlert("Succès", "Réclamation modifiée avec succès");
            } else {
                showAlert("Erreur", "Échec de la mise à jour");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Une erreur est survenue lors de la mise à jour : " + e.getMessage());
        }
    }

    private void cancelModification(GridPane gridPane) {
        resetToViewMode(gridPane);
        descriptionArea.setText(reclamation.getDescription());
    }

    private void resetToViewMode(GridPane gridPane) {
        TextField titreField = (TextField) gridPane.getChildren().stream()
                .filter(node -> {
                    Integer col = GridPane.getColumnIndex(node);
                    Integer row = GridPane.getRowIndex(node);
                    return (col != null && col == 1) && (row != null && row == 1) && node instanceof TextField;
                })
                .findFirst()
                .orElse(null);

        if (titreField != null) {
            gridPane.getChildren().remove(titreField);
        }

        gridPane.getChildren().add(titreLabel);
        titreLabel.setVisible(true);
        descriptionArea.setEditable(false);

        modifierButton.setText("Modifier");
        modifierButton.setOnAction(e -> handleModifier());
        supprimerButton.setText("Supprimer");
        supprimerButton.setOnAction(e -> handleSupprimer());
    }

    @FXML
    private void handleSupprimer() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer cette réclamation ?");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cette réclamation ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                boolean deleted = reclamationService.deleteReclamation(reclamation.getId());

                if (deleted) {
                    showAlert("Succès", "Réclamation supprimée avec succès");
                    if (refreshCallback != null) {
                        refreshCallback.run(); // Rafraîchir la liste
                    }
                    ((Stage) supprimerButton.getScene().getWindow()).close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Erreur", "Erreur lors de la suppression : " + e.getMessage());
            }
        }
    }


    @FXML
    private void handleGeneratePDF() {
        if (reclamation == null || !"Répondu".equalsIgnoreCase(reclamation.getStatut().trim())) {
            showAlert("Erreur", "Seules les réclamations avec le statut 'Répondu' peuvent être exportées en PDF.");
            return;
        }

        try {
            // Configuration du FileChooser
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Enregistrer le PDF de la réclamation");
            fileChooser.setInitialFileName("reclamation_" + reclamation.getId() + "_" +
                    LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Documents PDF", "*.pdf"));
            File file = fileChooser.showSaveDialog(pdfButton.getScene().getWindow());

            if (file == null) return; // Annulation par l'utilisateur

            // Vérification de l'extension .pdf
            if (!file.getName().toLowerCase().endsWith(".pdf")) {
                file = new File(file.getAbsolutePath() + ".pdf");
            }

            // Création du document PDF avec marges personnalisées
            Document document = new Document(PageSize.A4, 40, 40, 60, 60);
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));

            // Ajout des métadonnées
            document.addTitle("Réclamation #" + reclamation.getId());
            document.addCreator("Système de Gestion des Réclamations");

            document.open();

            // ========== EN-TÊTE ========== //
            // Logo
            try {
                // Charger le SVG
                String svgPath = getClass().getResource("/assets/logo 1.svg").toString();
                TranscoderInput input = new TranscoderInput(svgPath);

                // Convertir en PNG en mémoire
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                TranscoderOutput output = new TranscoderOutput(outputStream);

                PNGTranscoder transcoder = new PNGTranscoder();
                transcoder.transcode(input, output);

                // Créer l'image PDF
                Image logo = Image.getInstance(outputStream.toByteArray());
                logo.scaleToFit(100, 60);
                document.add(logo);

            } catch (Exception e) {
                e.printStackTrace();
            }

            // Titre principal
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, BaseColor.DARK_GRAY);
            Paragraph title = new Paragraph("RÉCLAMATION #" + reclamation.getId(), titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20f);
            document.add(title);

            // Sous-titre
            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 14, BaseColor.GRAY);
            Paragraph subtitle = new Paragraph("Document généré le " +
                    LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    subtitleFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(30f);
            document.add(subtitle);

            // ========== SECTION DÉTAILS ========== //
            // Style des polices
            Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, BaseColor.DARK_GRAY);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.WHITE);
            Font contentFont = FontFactory.getFont(FontFactory.HELVETICA, 12);

            // Section "Détails de la réclamation"
            Paragraph sectionTitle = new Paragraph("Détails de la réclamation : ", sectionFont);
            sectionTitle.setSpacingBefore(20f);
            sectionTitle.setSpacingAfter(15f);
            document.add(sectionTitle);

            // Tableau des détails
            PdfPTable detailsTable = new PdfPTable(2);
            detailsTable.setWidthPercentage(100);
            detailsTable.setSpacingBefore(10f);
            detailsTable.setSpacingAfter(20f);
            detailsTable.setWidths(new float[]{1, 3});

            // Ajout des lignes avec style amélioré
            addStyledTableRow(detailsTable, "ID", String.valueOf(reclamation.getId()), headerFont, contentFont, BaseColor.DARK_GRAY);
            addStyledTableRow(detailsTable, "Titre", reclamation.getTitre(), headerFont, contentFont, BaseColor.DARK_GRAY);
            addStyledTableRow(detailsTable, "Description", reclamation.getDescription(), headerFont, contentFont, BaseColor.DARK_GRAY);
            addStyledTableRow(detailsTable, "Statut", reclamation.getStatut(), headerFont, contentFont, BaseColor.DARK_GRAY);
            addStyledTableRow(detailsTable, "Date",
                    reclamation.getDateReclamation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    headerFont, contentFont, BaseColor.DARK_GRAY);

            document.add(detailsTable);

            // ========== SECTION RÉPONSE ========== //
            Paragraph responseTitle = new Paragraph("Réponse officielle : ", sectionFont);
            responseTitle.setSpacingBefore(20f);
            responseTitle.setSpacingAfter(15f);
            document.add(responseTitle);

            // Style pour la réponse
            Font responseFont = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.BLACK);
            Paragraph response = new Paragraph(reponseArea.getText(), responseFont);
            response.setAlignment(Element.ALIGN_JUSTIFIED);
            response.setSpacingAfter(30f);
            document.add(response);

            // ========== PIED DE PAGE ========== //
            Paragraph footer = new Paragraph("Service Client - © " + LocalDate.now().getYear(),
                    FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10, BaseColor.GRAY));
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();

            showAlert("Succès", "Le PDF a été généré avec succès:\n" + file.getAbsolutePath());

        } catch (DocumentException | IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Une erreur est survenue lors de la génération du PDF:\n" + e.getMessage());
        }
    }

    private void addStyledTableRow(PdfPTable table, String header, String content, Font headerFont, Font contentFont, BaseColor headerColor) {
        // Cellule d'en-tête
        PdfPCell headerCell = new PdfPCell(new Phrase(header, headerFont));
        headerCell.setBackgroundColor(headerColor);
        headerCell.setPadding(8f);
        headerCell.setBorderColor(BaseColor.WHITE);
        headerCell.setBorderWidth(1.5f);

        // Cellule de contenu
        PdfPCell contentCell = new PdfPCell(new Phrase(content, contentFont));
        contentCell.setPadding(8f);
        contentCell.setBorderColor(new BaseColor(220, 220, 220));
        contentCell.setBackgroundColor(BaseColor.WHITE);

        table.addCell(headerCell);
        table.addCell(contentCell);
    }

    @FXML
    private void handleBack() {
        titreLabel.getScene().getWindow().hide();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(
                title.equals("Erreur") ? Alert.AlertType.ERROR :
                        title.equals("Succès") ? Alert.AlertType.INFORMATION : Alert.AlertType.INFORMATION
        );
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
