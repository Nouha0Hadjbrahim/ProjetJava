package service;

import utils.DBConnection;

import java.sql.*;
import java.time.LocalDate;

public class ReponseService {

    public boolean enregistrerReponse(int reclamationId, String description, boolean finale) throws SQLException {
        // D'abord vérifier s'il existe déjà une réponse
        if (reponseExiste(reclamationId)) {
            // Si elle existe, faire un UPDATE
            return mettreAJourReponse(reclamationId, description, finale);
        } else {
            // Sinon faire un INSERT
            return creerReponse(reclamationId, description, finale);
        }
    }

    private boolean reponseExiste(int reclamationId) throws SQLException {
        String query = "SELECT COUNT(*) FROM reponse WHERE reclamation_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, reclamationId);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    private boolean creerReponse(int reclamationId, String description, boolean finale) throws SQLException {
        String query = "INSERT INTO reponse (reclamation_id, description, date_reponse, finale) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, reclamationId);
            pstmt.setString(2, description);
            pstmt.setDate(3, Date.valueOf(LocalDate.now()));
            pstmt.setBoolean(4, finale);
            return pstmt.executeUpdate() > 0;
        }
    }

    private boolean mettreAJourReponse(int reclamationId, String description, boolean finale) throws SQLException {
        String query = "UPDATE reponse SET description = ?, date_reponse = ?, finale = ? WHERE reclamation_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, description);
            pstmt.setDate(2, Date.valueOf(LocalDate.now()));
            pstmt.setBoolean(3, finale);
            pstmt.setInt(4, reclamationId);
            return pstmt.executeUpdate() > 0;
        }
    }

    public String getReponseByReclamationId(int reclamationId) throws SQLException {
        String query = "SELECT description FROM reponse WHERE reclamation_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, reclamationId);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() ? rs.getString("description") : null;
        }
    }

    public boolean isReponseFinale(int reclamationId) throws SQLException {
        String query = "SELECT finale FROM reponse WHERE reclamation_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, reclamationId);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() && rs.getBoolean("finale");
        }
    }
}

