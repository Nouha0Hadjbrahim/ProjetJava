package model;

public class User {
    private int id;
    private String nom;
    private String prenom;
    private String email;
    private String password;
    private String roles;
    private String statut;
    private String photo;

    public User() {
        // Constructeur par défaut nécessaire pour instancier via `new User()`
    }

    // Constructeur sans ID (pour inscription)
    public User(String nom, String prenom, String email, String password) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.password = password;
        this.roles = "[\"ROLE_CLIENT\"]"; // chaîne JSON valide
        this.statut = "active";     // valeur par défaut
    }


    // Constructeur complet
    public User(int id, String nom, String prenom, String email, String password, String roles, String statut) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.password = password;
        this.roles = roles;
        this.statut = statut;
    }
    public User(int id, String nom, String prenom, String email, String password, String roles, String statut, String photo) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.password = password;
        this.roles = roles;
        this.statut = statut;
        this.photo = photo;
    }
    // Getters
    public int getId() { return id; }
    public String getNom() { return nom; }
    public String getPrenom() { return prenom; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getRoles() { return roles; }
    public String getStatut() { return statut; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setNom(String nom) { this.nom = nom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setRoles(String roles) { this.roles = roles; }
    public void setStatut(String statut) { this.statut = statut; }

    // toString()
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", roles='" + roles + '\'' +
                ", statut='" + statut + '\'' +
                '}';
    }
    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photoProfil) {
        this.photo = photoProfil;
    }

}
