package model;

import java.time.LocalDate;

public class InscriptionAtelier {
    private int id;
    private int atelierId;
    private int userId;
    private LocalDate dateInscription;
    private String statut; // "confirmée", "annulée", etc.

    // Constructeurs
    public InscriptionAtelier() {}

    public InscriptionAtelier(int atelierId, int userId, LocalDate dateInscription, String statut) {
        this.atelierId = atelierId;
        this.userId = userId;
        this.dateInscription = dateInscription;
        this.statut = statut;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getAtelierId() { return atelierId; }
    public void setAtelierId(int atelierId) { this.atelierId = atelierId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public LocalDate getDateInscription() { return dateInscription; }
    public void setDateInscription(LocalDate dateInscription) { this.dateInscription = dateInscription; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
}
