package model;

import javafx.beans.property.*;

import java.time.LocalDate;

public class Reclamation {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final IntegerProperty userId = new SimpleIntegerProperty();
    private final StringProperty titre = new SimpleStringProperty();
    private final StringProperty description = new SimpleStringProperty();
    private final StringProperty statut = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> dateReclamation = new SimpleObjectProperty<>();

    public Reclamation(int userId, String titre, String description, String statut, LocalDate dateReclamation) {
        this.userId.set(userId);
        this.titre.set(titre);
        this.description.set(description);
        this.statut.set(statut);
        this.dateReclamation.set(dateReclamation);
    }

    // Propriétés JavaFX (utilisées dans les TableColumn)
    public IntegerProperty idProperty() { return id; }
    public IntegerProperty userIdProperty() { return userId; }
    public StringProperty titreProperty() { return titre; }
    public StringProperty descriptionProperty() { return description; }
    public StringProperty statutProperty() { return statut; }
    public ObjectProperty<LocalDate> dateReclamationProperty() { return dateReclamation; }

    // Getters et Setters classiques (compatibilité avec l'ancien code)
    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }

    public int getUserId() { return userId.get(); }
    public void setUserId(int userId) { this.userId.set(userId); }

    public String getTitre() { return titre.get(); }
    public void setTitre(String titre) { this.titre.set(titre); }

    public String getDescription() { return description.get(); }
    public void setDescription(String description) { this.description.set(description); }

    public String getStatut() { return statut.get(); }
    public void setStatut(String statut) { this.statut.set(statut); }

    public LocalDate getDateReclamation() { return dateReclamation.get(); }
    public void setDateReclamation(LocalDate dateReclamation) { this.dateReclamation.set(dateReclamation); }
}
