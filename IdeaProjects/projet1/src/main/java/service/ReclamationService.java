package service;

import model.Reclamation;
import utils.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReclamationService {

    public List<Reclamation> getReclamationsByUserId(int userId) {
        List<Reclamation> reclamations = new ArrayList<>();
        String query = "SELECT id, user_id, titre, description, statut, date_reclamation FROM reclamation WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Reclamation rec = new Reclamation(
                        rs.getInt("user_id"),
                        rs.getString("titre"),
                        rs.getString("description"),
                        rs.getString("statut"),
                        rs.getDate("date_reclamation").toLocalDate()
                );
                rec.setId(rs.getInt("id"));
                reclamations.add(rec);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reclamations;
    }

    public Reclamation addReclamation(int userId, String titre, String description) throws SQLException {
        String query = "INSERT INTO reclamation (user_id, titre, description, statut, date_reclamation) VALUES (?, ?, ?, 'En attente', ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, userId);
            pstmt.setString(2, titre);
            pstmt.setString(3, description);
            pstmt.setDate(4, Date.valueOf(LocalDate.now()));

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet keys = pstmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        int id = keys.getInt(1);
                        Reclamation rec = new Reclamation(userId, titre, description, "En attente", LocalDate.now());
                        rec.setId(id);
                        return rec;
                    }
                }
            }
        }
        throw new SQLException("Erreur lors de l'ajout de la réclamation.");
    }

    public boolean deleteReclamation(int id) throws SQLException {
        String query = "DELETE FROM reclamation WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    public boolean updateReclamation(int id, String nouveauTitre, String nouvelleDescription) throws SQLException {
        String query = "UPDATE reclamation SET titre = ?, description = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, nouveauTitre);
            pstmt.setString(2, nouvelleDescription);
            pstmt.setInt(3, id);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    public List<Reclamation> getReclamations(int offset, int limit) {
        List<Reclamation> reclamations = new ArrayList<>();
        String query = "SELECT * FROM reclamation ORDER BY date_reclamation DESC LIMIT ?, ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, offset);
            stmt.setInt(2, limit);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Reclamation rec = new Reclamation(
                        rs.getInt("user_id"),
                        rs.getString("titre"),
                        rs.getString("description"),
                        rs.getString("statut"),
                        rs.getDate("date_reclamation").toLocalDate()
                );
                rec.setId(rs.getInt("id"));
                reclamations.add(rec);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reclamations;
    }

    public int getTotalReclamations() {
        String query = "SELECT COUNT(*) as total FROM reclamation";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            return rs.next() ? rs.getInt("total") : 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public boolean updateStatut(int reclamationId, String statut) throws SQLException {
        String query = "UPDATE reclamation SET statut = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, statut);
            pstmt.setInt(2, reclamationId);

            return pstmt.executeUpdate() > 0;
        }
    }
}