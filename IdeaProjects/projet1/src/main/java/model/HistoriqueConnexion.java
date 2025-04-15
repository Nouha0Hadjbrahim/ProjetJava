package model;

import java.time.LocalDateTime;

public class HistoriqueConnexion {

    private int id;
    private LocalDateTime dateConnexion;
    private String adresseIp;
    private User user;

    // ===== Constructeurs =====

    public HistoriqueConnexion() {}

    public HistoriqueConnexion(LocalDateTime dateConnexion, String adresseIp, User user) {
        this.dateConnexion = dateConnexion;
        this.adresseIp = adresseIp;
        this.user = user;
    }

    // ===== Getters & Setters =====

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDateTime getDateConnexion() {
        return dateConnexion;
    }

    public void setDateConnexion(LocalDateTime dateConnexion) {
        this.dateConnexion = dateConnexion;
    }

    public String getAdresseIp() {
        return adresseIp;
    }

    public void setAdresseIp(String adresseIp) {
        this.adresseIp = adresseIp;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    // ===== Optionnel : méthode toString pour affichage debug =====

    @Override
    public String toString() {
        return "Connexion le " + dateConnexion + " depuis IP : " + adresseIp;
    }

}
