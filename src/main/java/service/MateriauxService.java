package service;
import model.Fournisseur;
import model.Material;
import utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
public class MateriauxService {
    private final Connection conn;

    public MateriauxService() throws SQLException {
        this.conn = DBConnection.getConnection();
        if (conn == null) {
            throw new SQLException("La connexion à la base de données a échoué.");
        }
    }

    public MateriauxService(Connection conn) {
        this.conn = conn;
    }

    // Ajouter un matériel
    public void ajouter(Material mat) throws SQLException {
        if (conn == null) {  // Vérifier si la connexion est bien récupérée
            throw new SQLException("La connexion à la base de données est nulle !");
        }
        String query = "INSERT INTO materiaux (nom_materiel, quantite_stock, seuil_min, prix_unitaire, categorie, description, photo, id_fournisseur_id, id_user_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pst = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {  // Ajout de Statement.RETURN_GENERATED_KEYS pour récupérer l'ID auto-généré
            pst.setString(1, mat.getNomMateriel());
            pst.setInt(2, mat.getQuantiteStock());
            pst.setInt(3, mat.getSeuilMin());
            pst.setDouble(4, mat.getPrixUnitaire());
            pst.setString(5, mat.getCategorie());
            pst.setString(6, mat.getDescription());
            pst.setString(7, mat.getPhoto());
            pst.setObject(8, mat.getFournisseur() != null ? mat.getFournisseur().getId() : null);
            pst.setInt(9, mat.getUser().getId());
            // Exécuter la requête d'insertion
            pst.executeUpdate();

            // Récupérer l'ID auto-généré
            try (ResultSet generatedKeys = pst.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    // Assigner l'ID généré à l'objet Materiaux
                    mat.setId(generatedKeys.getInt(1));  // Ici, nous mettons à jour l'ID du matériel avec l'ID généré
                    System.out.println("Matériel ajouté avec ID : " + mat.getId());
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout du matériel : " + e.getMessage());
        }
    }

    // Mettre à jour un matériel
    public void modifier(Material material) throws SQLException {
        String query = "UPDATE materiaux SET nom_materiel=?, quantite_stock=?, seuil_min=?, prix_unitaire=?, categorie=?, description=?, photo=?, id_fournisseur_id=? WHERE id=?";

        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setString(1, material.getNomMateriel());
            pst.setInt(2, material.getQuantiteStock());
            pst.setInt(3, material.getSeuilMin());
            pst.setDouble(4, material.getPrixUnitaire());
            pst.setString(5, material.getCategorie());
            pst.setString(6, material.getDescription());
            pst.setString(7, material.getPhoto());
            pst.setObject(8, material.getFournisseur() != null ? material.getFournisseur().getId() : null);
            pst.setInt(9, material.getId());

            pst.executeUpdate();
        }
    }

    // Supprimer un matériel
    public void delete(int id) throws SQLException {
        // Désactiver temporairement les vérifications de clés étrangères
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("SET FOREIGN_KEY_CHECKS=0");
        }

        try {
            // 1. Supprimer d'abord les références dans wishlist
            String deleteWishlistQuery = "DELETE FROM wishlistmateriaux_materiaux WHERE materiaux_id = ?";
            try (PreparedStatement pst = conn.prepareStatement(deleteWishlistQuery)) {
                pst.setInt(1, id);
                int wishlistDeleted = pst.executeUpdate();
                System.out.println(wishlistDeleted + " référence(s) supprimée(s) de la wishlist");
            }

            // 2. Vérifier si le matériel existe
            String checkQuery = "SELECT COUNT(*) FROM materiaux WHERE id = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                checkStmt.setInt(1, id);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        // 3. Supprimer le matériel
                        String deleteMaterialQuery = "DELETE FROM materiaux WHERE id = ?";
                        try (PreparedStatement pst = conn.prepareStatement(deleteMaterialQuery)) {
                            pst.setInt(1, id);
                            int rowsDeleted = pst.executeUpdate();
                            System.out.println(rowsDeleted + " matériel(s) supprimé(s)");
                        }
                    } else {
                        System.out.println("Le matériel avec l'ID " + id + " n'existe pas.");
                    }
                }
            }
        } finally {
            // Réactiver les vérifications de clés étrangères
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("SET FOREIGN_KEY_CHECKS=1");
            }
        }
    }
    // Afficher tous les matériaux
    public List<Material> afficherAll() throws SQLException {
        List<Material> liste = new ArrayList<>();
        String query = "SELECT * FROM materiaux";
        Statement ste = conn.createStatement();
        ResultSet rs = ste.executeQuery(query);

        while (rs.next()) {
            Material mat = new Material(
                    rs.getString("nom_materiel"),
                    rs.getInt("quantite_stock"),
                    rs.getInt("seuil_min"),
                    rs.getDouble("prix_unitaire"),
                    rs.getString("categorie"),
                    rs.getString("description"),
                    rs.getString("photo")
            );
            // Ajout du fournisseur
            if (rs.getObject("id_fournisseur_id") != null) {
                Fournisseur f = new Fournisseur();
                f.setId(rs.getInt("id_fournisseur_id"));
                mat.setFournisseur(f);
            }

            liste.add(mat);
        }

        rs.close();
        ste.close();
        return liste;
    }
    public List<Material> getMateriauxPage(int page, int pageSize) throws SQLException {
        List<Material> liste = new ArrayList<>();
        String query = "SELECT m.*, f.nom_fournisseur FROM materiaux m LEFT JOIN fournisseur f ON m.id_fournisseur_id = f.id LIMIT ? OFFSET ?";

        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setInt(1, pageSize);
            pst.setInt(2, (page - 1) * pageSize);

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Material mat = new Material(
                            rs.getString("nom_materiel"),
                            rs.getInt("quantite_stock"),
                            rs.getInt("seuil_min"),
                            rs.getDouble("prix_unitaire"),
                            rs.getString("categorie"),
                            rs.getString("description"),
                            rs.getString("photo")
                    );
                    mat.setId(rs.getInt("id"));

                    if (rs.getObject("id_fournisseur_id") != null) {
                        Fournisseur f = new Fournisseur();
                        f.setId(rs.getInt("id_fournisseur_id"));
                        f.setNomFournisseur(rs.getString("nom_fournisseur"));
                        mat.setFournisseur(f);
                    }

                    liste.add(mat);
                }
            }
        }
        return liste;
    }
    public int countMateriaux() throws SQLException {
        String query = "SELECT COUNT(*) FROM materiaux";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(query)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
    public List<Material> getAll() throws SQLException {
        List<Material> materials = new ArrayList<>();
        String query = "SELECT * FROM materiaux";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Material m = new Material(
                        rs.getString("nom_materiel"),
                        rs.getInt("quantite_stock"),
                        rs.getInt("seuil_min"),
                        rs.getDouble("prix_unitaire"),
                        rs.getString("categorie"),
                        rs.getString("description"),
                        rs.getString("photo")
                );
                m.setId(rs.getInt("id"));

                if (rs.getObject("id_fournisseur_id") != null) {
                    Fournisseur f = new Fournisseur();
                    f.setId(rs.getInt("id_fournisseur_id"));
                    m.setFournisseur(f);
                }

                materials.add(m);
            }
        }
        return materials;
    }

    public List<Material> getByCategory(String category) throws SQLException {
        List<Material> materials = new ArrayList<>();
        String query = "SELECT * FROM materiaux WHERE categorie = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, category);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Material m = new Material(
                        rs.getString("nom_materiel"),
                        rs.getInt("quantite_stock"),
                        rs.getInt("seuil_min"),
                        rs.getDouble("prix_unitaire"),
                        rs.getString("categorie"),
                        rs.getString("description"),
                        rs.getString("photo")
                );
                m.setId(rs.getInt("id"));

                if (rs.getObject("id_fournisseur_id") != null) {
                    Fournisseur f = new Fournisseur();
                    f.setId(rs.getInt("id_fournisseur_id"));
                    m.setFournisseur(f);
                }

                materials.add(m);
            }
        }
        return materials;
    }



}
