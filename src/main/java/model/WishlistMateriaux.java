package model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class WishlistMateriaux {
    private int id;
    private User user;
    private List<Material> materials = new ArrayList<>();
    private Timestamp dateAjout;

    public WishlistMateriaux() {}

    public WishlistMateriaux(User user) {
        this.user = user;
        this.dateAjout = new Timestamp(System.currentTimeMillis());
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public List<Material> getMaterials() { return materials; }
    public void setMaterials(List<Material> materials) { this.materials = materials; }

    public Timestamp getDateAjout() { return dateAjout; }
    public void setDateAjout(Timestamp dateAjout) { this.dateAjout = dateAjout; }

    // Méthodes utilitaires
    public void addMaterial(Material material) {
        if (!materials.contains(material)) {
            materials.add(material);
        }
    }

    public void removeMaterial(Material material) {
        materials.remove(material);
    }
}