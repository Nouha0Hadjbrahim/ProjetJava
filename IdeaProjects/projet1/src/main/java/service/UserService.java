package service;

import model.User;
import utils.DBConnection;
import utils.PasswordUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserService {

    public void register(User user) {
        String sql = "INSERT INTO user(nom, prenom, email, password, roles, statut) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getNom());
            ps.setString(2, user.getPrenom());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getPassword());
            ps.setString(5, user.getRoles());
            ps.setString(6, user.getStatut());
            ps.executeUpdate();
            System.out.println("✅ Utilisateur inséré !");
        } catch (SQLException e) {
            System.out.println("❌ Erreur insertion : " + e.getMessage());
        }
    }

    public boolean emailExists(String email) {
        String sql = "SELECT COUNT(*) FROM user WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.out.println("Erreur vérification email : " + e.getMessage());
        }
        return false;
    }

    public List<User> getUsersPage(int page, int pageSize) {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM user LIMIT ? OFFSET ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageSize);
            ps.setInt(2, (page - 1) * pageSize);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                users.add(new User(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getString("roles"),
                        rs.getString("statut")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    public int countUsers() {
        String sql = "SELECT COUNT(*) FROM user";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public User login(String email, String passwordPlain) {
        String sql = "SELECT * FROM user WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String hashedPassword = rs.getString("password");

                if (PasswordUtils.checkPassword(passwordPlain, hashedPassword)) {
                    return new User(
                            rs.getInt("id"),
                            rs.getString("nom"),
                            rs.getString("prenom"),
                            rs.getString("email"),
                            hashedPassword,
                            rs.getString("roles"),
                            rs.getString("statut"),
                            rs.getString("photo") // ✅ maintenant la photo est bien prise en compte
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    public List<User> searchUsersPageSQL(String keyword, int page, int pageSize) {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM user WHERE nom LIKE ? OR prenom LIKE ? OR email LIKE ? LIMIT ? OFFSET ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String likeKeyword = "%" + keyword + "%";
            ps.setString(1, likeKeyword);
            ps.setString(2, likeKeyword);
            ps.setString(3, likeKeyword);
            ps.setInt(4, pageSize);
            ps.setInt(5, (page - 1) * pageSize);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setNom(rs.getString("nom"));
                user.setPrenom(rs.getString("prenom"));
                user.setEmail(rs.getString("email"));
                user.setPassword(rs.getString("password"));
                user.setRoles(rs.getString("roles"));
                user.setStatut(rs.getString("statut"));
                user.setPhoto(rs.getString("photo"));
                users.add(user);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return users;
    }

    public int countUsersWithKeyword(String keyword) {
        int total = 0;
        String sql = "SELECT COUNT(*) FROM user WHERE nom LIKE ? OR prenom LIKE ? OR email LIKE ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String likeKeyword = "%" + keyword + "%";
            ps.setString(1, likeKeyword);
            ps.setString(2, likeKeyword);
            ps.setString(3, likeKeyword);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                total = rs.getInt(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return total;
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String query = "SELECT id, nom, prenom, email, statut, photo FROM user";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setNom(rs.getString("nom"));
                user.setPrenom(rs.getString("prenom"));
                user.setEmail(rs.getString("email"));
                user.setStatut(rs.getString("statut"));
                user.setPhoto(rs.getString("photo"));
                users.add(user);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return users;
    }
    public void deleteUserById(int userId) {
        String sql = "DELETE FROM user WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
            System.out.println("🗑️ Utilisateur supprimé avec succès !");
        } catch (SQLException e) {
            System.out.println("❌ Erreur suppression : " + e.getMessage());
        }
    }
    public void updateUser(User user) {
        String sql = "UPDATE user SET nom=?, prenom=?, email=?, roles=?, statut=?, photo=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getNom());
            ps.setString(2, user.getPrenom());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getRoles());
            ps.setString(5, user.getStatut());
            ps.setString(6, user.getPhoto());
            ps.setInt(7, user.getId());
            ps.executeUpdate();
            System.out.println("✅ Utilisateur mis à jour avec la photo !");
        } catch (SQLException e) {
            System.out.println("❌ Erreur mise à jour : " + e.getMessage());
        }
    }

    public User getUserByEmail(String email) {
        User user = null;

        String sql = "SELECT * FROM user WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                user = new User();
                user.setId(rs.getInt("id"));
                user.setNom(rs.getString("nom"));
                user.setPrenom(rs.getString("prenom"));
                user.setEmail(rs.getString("email"));
                user.setPassword(rs.getString("password"));
                user.setRoles(rs.getString("roles"));
                user.setStatut(rs.getString("statut"));
                user.setPhoto(rs.getString("photo")); // si photo existe
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return user;
    }

    public void updateUserWithPassword(User user) {
        String sql = "UPDATE user SET nom=?, prenom=?, email=?, photo=?, password=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getNom());
            ps.setString(2, user.getPrenom());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getPhoto());
            ps.setString(5, user.getPassword());
            ps.setInt(6, user.getId());

            ps.executeUpdate();
            System.out.println("✅ Utilisateur mis à jour avec succès !");
        } catch (SQLException e) {
            System.out.println("❌ Erreur mise à jour utilisateur : " + e.getMessage());
        }
    }
}
