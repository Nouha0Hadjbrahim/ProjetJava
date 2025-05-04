package model;

public class LigneDeCommande {

    private int id;
    private Integer idCommande; // Nullable in practice, but should always be set
    private int quantite;
    private double prixUnitaire;
    private int idProduit;

    // Constructors
    public LigneDeCommande() {
    }

    public LigneDeCommande(int id, Integer idCommande, int quantite, double prixUnitaire, int idProduit) {
        this.id = id;
        this.idCommande = idCommande;
        this.quantite = quantite;
        this.prixUnitaire = prixUnitaire;
        this.idProduit = idProduit;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getIdCommande() {
        return idCommande;
    }

    public void setIdCommande(Integer idCommande) {
        this.idCommande = idCommande;
    }

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

    public double getPrixUnitaire() {
        return prixUnitaire;
    }

    public void setPrixUnitaire(double prixUnitaire) {
        this.prixUnitaire = prixUnitaire;
    }

    public int getIdProduit() {
        return idProduit;
    }

    public void setIdProduit(int idProduit) {
        this.idProduit = idProduit;
    }
}