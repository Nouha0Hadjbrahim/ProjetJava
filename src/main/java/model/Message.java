package model;

import java.time.LocalDateTime;

public class Message {
    private int id;
    private String objet;
    private String contenu;
    private String email;
    private LocalDateTime dateEnvoi;
    private boolean lu;

    public Message(String objet, String contenu, String email) {
        this.objet = objet;
        this.contenu = contenu;
        this.email = email;
        this.dateEnvoi = LocalDateTime.now();
        this.lu = false;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getObjet() {
        return objet;
    }

    public void setObjet(String objet) {
        this.objet = objet;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDateTime getDateEnvoi() {
        return dateEnvoi;
    }

    public void setDateEnvoi(LocalDateTime dateEnvoi) {
        this.dateEnvoi = dateEnvoi;
    }

    public boolean isLu() {
        return lu;
    }

    public void setLu(boolean lu) {
        this.lu = lu;
    }
} 