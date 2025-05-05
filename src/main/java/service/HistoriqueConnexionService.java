package service;

import model.HistoriqueConnexion;
import model.User;
import utils.DBConnection;

import java.net.InetAddress;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HistoriqueConnexionService {

    public List<HistoriqueConnexion> findByEmail(String email) {
        List<HistoriqueConnexion> list = new ArrayList<>();
        String sql =
                "SELECT h.date_connexion, h.adresse_ip " +
                        "FROM login_history h " +
                        "JOIN user u ON h.user_id = u.id " +
                        "WHERE u.email = ? " +
                        "ORDER BY h.date_connexion DESC";


        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                HistoriqueConnexion h = new HistoriqueConnexion();
                h.setDateConnexion(rs.getTimestamp("date_connexion").toLocalDateTime());
                h.setAdresseIp(rs.getString("adresse_ip"));
                list.add(h);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    public void enregistrerConnexion(User user) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "INSERT INTO login_history (date_connexion, adresse_ip, user_id) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);

            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setString(2, InetAddress.getLocalHost().getHostAddress());
            stmt.setInt(3, user.getId());

            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
