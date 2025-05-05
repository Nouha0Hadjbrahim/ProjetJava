package model;

public class Material {

    private int id;
    private String nomMateriel;
    private int quantiteStock;
    private int seuilMin;
    private double prixUnitaire;
    private String categorie;
    private String description;
    private String photo;
    private Fournisseur fournisseur;
    private User user;

    // Constructeur
    public Material(){};
    public Material(String nomMateriel, int quantiteStock, int seuilMin, double prixUnitaire, String categorie, String description, String photo) {
        this.nomMateriel = nomMateriel;
        this.quantiteStock = quantiteStock;
        this.seuilMin = seuilMin;
        this.prixUnitaire = prixUnitaire;
        this.categorie = categorie;
        this.description = description;
        this.photo = photo;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNomMateriel() { return nomMateriel; }
    public void setNomMateriel(String nomMateriel) { this.nomMateriel = nomMateriel; }

    public int getQuantiteStock() { return quantiteStock; }
    public void setQuantiteStock(int quantiteStock) { this.quantiteStock = quantiteStock; }

    public int getSeuilMin() { return seuilMin; }
    public void setSeuilMin(int seuilMin) { this.seuilMin = seuilMin; }

    public double getPrixUnitaire() { return prixUnitaire; }
    public void setPrixUnitaire(double prixUnitaire) { this.prixUnitaire = prixUnitaire; }

    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPhoto() { return photo; }
    public void setPhoto(String photo) { this.photo = photo; }

    public Fournisseur getFournisseur() { return fournisseur; }
    public void setFournisseur(Fournisseur fournisseur) { this.fournisseur = fournisseur; }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
    // toString() pour affichage
    @Override
    public String toString() {
        return "Materiaux{" +
                "id=" + id +
                ", nomMateriel='" + nomMateriel + '\'' +
                ", quantiteStock=" + quantiteStock +
                ", seuilMin=" + seuilMin +
                ", prixUnitaire=" + prixUnitaire +
                ", categorie='" + categorie + '\'' +
                ", description='" + description + '\'' +
                ", photo='" + photo + '\'' +
                '}';
    }

}
