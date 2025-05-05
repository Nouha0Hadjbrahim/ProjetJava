package model;

import javafx.beans.property.*;

import java.time.LocalDate;

public class Reponse {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final IntegerProperty reclamationId = new SimpleIntegerProperty();
    private final StringProperty description = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> dateReponse = new SimpleObjectProperty<>();
    private final BooleanProperty finale = new SimpleBooleanProperty();

    // Constructeur
    public Reponse(int reclamationId, String description, LocalDate dateReponse, boolean finale) {
        this.reclamationId.set(reclamationId);
        this.description.set(description);
        this.dateReponse.set(dateReponse);
        this.finale.set(finale);
    }

    // Propriétés JavaFX
    public IntegerProperty idProperty() { return id; }
    public IntegerProperty reclamationIdProperty() { return reclamationId; }
    public StringProperty descriptionProperty() { return description; }
    public ObjectProperty<LocalDate> dateReponseProperty() { return dateReponse; }
    public BooleanProperty finaleProperty() { return finale; }

    // Getters/Setters
    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }

    public int getReclamationId() { return reclamationId.get(); }
    public void setReclamationId(int reclamationId) { this.reclamationId.set(reclamationId); }

    public String getDescription() { return description.get(); }
    public void setDescription(String description) { this.description.set(description); }

    public LocalDate getDateReponse() { return dateReponse.get(); }
    public void setDateReponse(LocalDate dateReponse) { this.dateReponse.set(dateReponse); }

    public boolean isFinale() { return finale.get(); }
    public void setFinale(boolean finale) { this.finale.set(finale); }
}