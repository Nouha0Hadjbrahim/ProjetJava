package service;

import utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class EpingleService {
    private final Connection cnx = DBConnection.getConnection();

    public boolean isEpingle(int userId, int atelierId) {
        String sql = "SELECT * FROM epingle_atelier WHERE user_id = ? AND atelier_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, atelierId);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void epingler(int userId, int atelierId) {
        String sql = "INSERT IGNORE INTO epingle_atelier (user_id, atelier_id) VALUES (?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, atelierId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deseingler(int userId, int atelierId) {
        String sql = "DELETE FROM epingle_atelier WHERE user_id = ? AND atelier_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, atelierId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Set<Integer> getEpinglesForUser(int userId) {
        Set<Integer> epingledIds = new HashSet<>();
        String sql = "SELECT atelier_id FROM epingle_atelier WHERE user_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                epingledIds.add(rs.getInt("atelier_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return epingledIds;
    }
}
