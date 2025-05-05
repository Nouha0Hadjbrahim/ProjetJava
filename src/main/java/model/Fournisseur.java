package model;

import java.sql.Timestamp;

public class Fournisseur {
    private int id;
    private String nomFournisseur;
    private String adresse;
    private String contact;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Constructeurs
    public Fournisseur() {}

    public Fournisseur(String nomFournisseur, String adresse, String contact) {
        this.nomFournisseur = nomFournisseur;
        this.adresse = adresse;
        this.contact = contact;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNomFournisseur() { return nomFournisseur; }
    public void setNomFournisseur(String nomFournisseur) { this.nomFournisseur = nomFournisseur; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return nomFournisseur;
    }
}