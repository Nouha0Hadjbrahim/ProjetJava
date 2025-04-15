package model;

public class Produit {
    private int id;
    private Integer idPromotion;
    private Integer idUser;
    private String nomProduit;
    private String categorie;
    private double prix;
    private String statut;
    private int stock;
    private String image;

    public Produit() {}

    public Produit(int id, Integer idPromotion, Integer idUser, String nomProduit, String categorie,
                   double prix, String statut, int stock, String image) {
        this.id = id;
        this.idPromotion = idPromotion;
        this.idUser = idUser;
        this.nomProduit = nomProduit;
        this.categorie = categorie;
        this.prix = prix;
        this.statut = statut;
        this.stock = stock;
        this.image = image;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Integer getIdPromotion() { return idPromotion; }
    public void setIdPromotion(Integer idPromotion) { this.idPromotion = idPromotion; }

    public Integer getIdUser() { return idUser; }
    public void setIdUser(Integer idUser) { this.idUser = idUser; }

    public String getNomProduit() { return nomProduit; }
    public void setNomProduit(String nomProduit) { this.nomProduit = nomProduit; }

    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }

    public double getPrix() { return prix; }
    public void setPrix(double prix) { this.prix = prix; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
}