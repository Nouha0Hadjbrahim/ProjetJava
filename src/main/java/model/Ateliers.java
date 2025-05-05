package model;

import java.time.LocalDateTime;

public class Ateliers {
    private int id;
    private int user; // L'ID de l'utilisateur qui a créé l'atelier
    private String titre;
    private String categorie;
    private String description;
    private String niveau_diff;
    private double prix;
    private LocalDateTime datecours;
    private int duree;
    private String lien;



    // Constructeur complet
    public Ateliers() {
        this.id = id;
        this.user = user;
        this.titre = titre;
        this.categorie = categorie;
        this.description = description;
        this.niveau_diff = niveau_diff;
        this.prix = prix;
        this.datecours = datecours;
        this.duree = duree;
        this.lien = lien;
    }



    // Getters
    public int getId() { return id; }
    public int getUser() { return user; }
    public String getTitre() { return titre; }
    public String getCategorie() { return categorie; }
    public String getDescription() { return description; }
    public String getNiveau_diff() { return niveau_diff; }
    public double getPrix() { return prix; }
    public LocalDateTime getDatecours() { return datecours; }
    public int getDuree() { return duree; }
    public String getLien() { return lien; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setTitre(String titre) { this.titre = titre; }
    public void setCategorie(String categorie) { this.categorie = categorie; }
    public void setDescription(String description) { this.description = description; }
    public void setNiveau_diff(String niveau_diff) { this.niveau_diff = niveau_diff; }
    public void setPrix(double prix) { this.prix = prix; }
    public void setDatecours(LocalDateTime datecours) { this.datecours = datecours; }
    public void setDuree(int duree) { this.duree = duree; }
    public void setLien(String lien) { this.lien = lien; }
    public void setUser(int user) { this.user = user; }

    // toString()
    @Override
    public String toString() {
        return "AtelierEnLigne{" +
                "id=" + id +
                ", artisan_id=" + user +
                ", titre='" + titre + '\'' +
                ", categorie='" + categorie + '\'' +
                ", description='" + description + '\'' +
                ", niveau_diff='" + niveau_diff + '\'' +
                ", prix=" + prix +
                ", datecours=" + datecours +
                ", duree=" + duree +
                ", lien='" + lien + '\'' +
                '}';
    }
}




