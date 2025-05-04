package model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Commande {

    private int id;
    private Integer idUser;
    private Date dateCommande;
    private double montantTotal;
    private String statut;
    private String adresseLivraison;
    private String paiement;
    private List<LigneDeCommande> lignesDeCommande;

    // Constructors
    public Commande() {
        this.lignesDeCommande = new ArrayList<>();
    }

    public Commande(int id, Integer idUser, Date dateCommande, double montantTotal, String statut,
                    String adresseLivraison, String paiement) {
        this.id = id;
        this.idUser = idUser;
        this.dateCommande = dateCommande;
        this.montantTotal = montantTotal;
        this.statut = statut;
        this.adresseLivraison = adresseLivraison;
        this.paiement = paiement;
        this.lignesDeCommande = new ArrayList<>();
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getIdUser() {
        return idUser;
    }

    public void setIdUser(Integer idUser) {
        this.idUser = idUser;
    }

    public Date getDateCommande() {
        return dateCommande;
    }

    public void setDateCommande(Date dateCommande) {
        this.dateCommande = dateCommande;
    }

    public double getMontantTotal() {
        return montantTotal;
    }

    public void setMontantTotal(double montantTotal) {
        this.montantTotal = montantTotal;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getAdresseLivraison() {
        return adresseLivraison;
    }

    public void setAdresseLivraison(String adresseLivraison) {
        this.adresseLivraison = adresseLivraison;
    }

    public String getPaiement() {
        return paiement;
    }

    public void setPaiement(String paiement) {
        this.paiement = paiement;
    }

    public List<LigneDeCommande> getLignesDeCommande() {
        return lignesDeCommande;
    }

    public void setLignesDeCommande(List<LigneDeCommande> lignesDeCommande) {
        this.lignesDeCommande = lignesDeCommande;
    }

    // Helper methods for managing the list
    public void addLigneDeCommande(LigneDeCommande ligne) {
        lignesDeCommande.add(ligne);
        ligne.setIdCommande(this.id);
    }

    public void removeLigneDeCommande(LigneDeCommande ligne) {
        lignesDeCommande.remove(ligne);
        ligne.setIdCommande(null);
    }
}