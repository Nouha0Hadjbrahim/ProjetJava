package service;

import model.Reclamation;
import model.User;
import utils.DBConnection;
import utils.SessionManager;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReclamationService {

    // ==================== METHODES POUR L'UTILISATEUR CONNECTÉ ====================

    public List<Reclamation> getReclamationsForCurrentUser(int offset, int limit) {
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) return new ArrayList<>();
        return getReclamationsForUser(currentUser.getId(), offset, limit);
    }

    public int getTotalReclamationsForCurrentUser() {
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) return 0;
        return getTotalReclamationsForUser(currentUser.getId());
    }

    public Reclamation addReclamationForCurrentUser(String titre, String description) throws SQLException {
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) throw new SQLException("Aucun utilisateur connecté");

        String query = "INSERT INTO reclamation (user_id, titre, description, statut, date_reclamation) " +
                "VALUES (?, ?, ?, 'En attente', ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, currentUser.getId());
            pstmt.setString(2, titre);
            pstmt.setString(3, description);
            pstmt.setDate(4, Date.valueOf(LocalDate.now()));

            if (pstmt.executeUpdate() > 0) {
                try (ResultSet keys = pstmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        Reclamation rec = new Reclamation(
                                currentUser.getId(),
                                titre,
                                description,
                                "En attente",
                                LocalDate.now()
                        );
                        rec.setId(keys.getInt(1));
                        return rec;
                    }
                }
            }
            throw new SQLException("Échec de la création de la réclamation");
        }
    }

    // ==================== Front : METHODES UTILITAIRES (INTERNES/ADMIN) ====================

    public List<Reclamation> getReclamationsForUser(int userId, int offset, int limit) {
        List<Reclamation> reclamations = new ArrayList<>();
        String query = "SELECT id, user_id, titre, description, statut, date_reclamation " +
                "FROM reclamation WHERE user_id = ? ORDER BY date_reclamation DESC LIMIT ?, ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, offset);
            stmt.setInt(3, limit);

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

    public int getTotalReclamationsForUser(int userId) {
        String query = "SELECT COUNT(*) as total FROM reclamation WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt("total") : 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    // ==================== METHODES ADMIN/GENERALES ====================

    public boolean deleteReclamation(int id) throws SQLException {
        String query = "DELETE FROM reclamation WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean updateReclamation(int id, String nouveauTitre, String nouvelleDescription) throws SQLException {
        String query = "UPDATE reclamation SET titre = ?, description = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, nouveauTitre);
            pstmt.setString(2, nouvelleDescription);
            pstmt.setInt(3, id);
            return pstmt.executeUpdate() > 0;
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



    public List<Reclamation> getActiveReclamations(int offset, int limit) {
        return getReclamationsByStatut(offset, limit, "IN ('En attente', 'En cours')");
    }

    public List<Reclamation> getArchivedReclamations(int offset, int limit) {
        return getReclamationsByStatut(offset, limit, "= 'Répondu'");
    }

    private List<Reclamation> getReclamationsByStatut(int offset, int limit, String statutCondition) {
        List<Reclamation> reclamations = new ArrayList<>();
        String query = String.format("SELECT * FROM reclamation WHERE statut %s ORDER BY date_reclamation DESC LIMIT ?, ?", statutCondition);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, offset);
            stmt.setInt(2, limit);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                reclamations.add(createReclamationFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reclamations;
    }




    public int getTotalActiveReclamations() {
        return getTotalReclamationsByStatut("IN ('En attente', 'En cours')");
    }

    public int getTotalArchivedReclamations() {
        return getTotalReclamationsByStatut("= 'Répondu'");
    }

    private int getTotalReclamationsByStatut(String statutCondition) {
        String query = String.format("SELECT COUNT(*) as total FROM reclamation WHERE statut %s", statutCondition);
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            return rs.next() ? rs.getInt("total") : 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public Map<String, Integer> getReclamationStats() {
        Map<String, Integer> stats = new HashMap<>();
        String query = "SELECT statut, COUNT(*) as count FROM reclamation GROUP BY statut";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                stats.put(rs.getString("statut"), rs.getInt("count"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Assure que tous les statuts existent dans la map
        stats.putIfAbsent("En attente", 0);
        stats.putIfAbsent("En cours", 0);
        stats.putIfAbsent("Répondu", 0);

        return stats;
    }

    private Reclamation createReclamationFromResultSet(ResultSet rs) throws SQLException {
        Reclamation rec = new Reclamation(
                rs.getInt("user_id"),
                rs.getString("titre"),
                rs.getString("description"),
                rs.getString("statut"),
                rs.getDate("date_reclamation").toLocalDate()
        );
        rec.setId(rs.getInt("id"));
        return rec;
    }
}