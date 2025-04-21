package service;

import model.Fournisseur;
import utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FournisseurService {
    private final Connection conn;

    public FournisseurService() throws SQLException {
        this.conn = DBConnection.getConnection();
    }

    public void create(Fournisseur fournisseur) throws SQLException {
        String query = "INSERT INTO fournisseur (nom_fournisseur, adresse, contact) VALUES (?, ?, ?)";
        try (PreparedStatement pst = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, fournisseur.getNomFournisseur());
            pst.setString(2, fournisseur.getAdresse());
            pst.setString(3, fournisseur.getContact());
            pst.executeUpdate();

            try (ResultSet generatedKeys = pst.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    fournisseur.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    public List<Fournisseur> getAll() throws SQLException {
        List<Fournisseur> fournisseurs = new ArrayList<>();
        String query = "SELECT id, nom_fournisseur, adresse, contact FROM fournisseur";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                Fournisseur f = new Fournisseur();
                f.setId(rs.getInt("id"));
                f.setNomFournisseur(rs.getString("nom_fournisseur"));
                f.setAdresse(rs.getString("adresse"));
                f.setContact(rs.getString("contact"));
                fournisseurs.add(f);
            }
        }
        return fournisseurs;
    }
    public List<Fournisseur> getFournisseursPage(int page, int pageSize) throws SQLException {
        List<Fournisseur> fournisseurs = new ArrayList<>();
        String query = "SELECT * FROM fournisseur LIMIT ? OFFSET ?";

        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setInt(1, pageSize);
            pst.setInt(2, (page - 1) * pageSize);

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Fournisseur f = new Fournisseur();
                    f.setId(rs.getInt("id"));
                    f.setNomFournisseur(rs.getString("nom_fournisseur"));
                    f.setAdresse(rs.getString("adresse"));
                    f.setContact(rs.getString("contact"));
                    fournisseurs.add(f);
                }
            }
        }
        return fournisseurs;
    }

    public void update(Fournisseur fournisseur) throws SQLException {
        String query = "UPDATE fournisseur SET nom_fournisseur=?, adresse=?, contact=? WHERE id=?";

        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setString(1, fournisseur.getNomFournisseur());
            pst.setString(2, fournisseur.getAdresse());
            pst.setString(3, fournisseur.getContact());
            pst.setInt(4, fournisseur.getId());

            pst.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        // Vérifie si le fournisseur est utilisé dans materiaux
        String checkQuery = "SELECT COUNT(*) FROM materiaux WHERE id_fournisseur_id = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
            checkStmt.setInt(1, id);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new SQLException("Ce fournisseur est utilisé par des matériaux et ne peut pas être supprimé");
                }
            }
        }

        // Si non utilisé, procède à la suppression
        String deleteQuery = "DELETE FROM fournisseur WHERE id = ?";
        try (PreparedStatement pst = conn.prepareStatement(deleteQuery)) {
            pst.setInt(1, id);
            pst.executeUpdate();
        }
    }
    public int countFournisseurs() throws SQLException {
        String query = "SELECT COUNT(*) AS total FROM fournisseur";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            if (rs.next()) {
                return rs.getInt("total");
            }
        }
        return 0;
    }


}