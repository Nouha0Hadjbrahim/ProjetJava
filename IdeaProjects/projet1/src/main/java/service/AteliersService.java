package service;

import model.Ateliers;
import utils.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.sql.Timestamp;

import java.util.ArrayList;
import java.util.List;

public class AteliersService {
    Connection cnx = DBConnection.getConnection();

    public List<Ateliers> getAteliersPage(int page, int pageSize) {
        List<Ateliers> ateliers = new ArrayList<>();
        String sql = "SELECT * FROM atelierenligne LIMIT ? OFFSET ?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, pageSize);
            ps.setInt(2, (page - 1) * pageSize);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                // Récupérer la date et l'heure sous forme de Timestamp
                Timestamp timestamp = rs.getTimestamp("datecours");

                // Convertir le Timestamp en LocalDateTime
                LocalDateTime datecours = timestamp != null ? timestamp.toLocalDateTime() : null;

                // Créer l'objet Ateliers
                Ateliers a = new Ateliers(
                        rs.getInt("id"),
                        rs.getInt("id_user_id"),
                        rs.getString("titre"),
                        rs.getString("categorie"),
                        rs.getString("description"),
                        rs.getString("niveau_diff"),
                        rs.getDouble("prix"),
                        datecours,  // Utiliser le LocalDateTime
                        rs.getInt("duree"),
                        rs.getString("lien")
                );

                ateliers.add(a);
            }
        } catch (SQLException e) {
            System.out.println("❌ Erreur lors du chargement des ateliers : " + e.getMessage());
        }

        return ateliers;
    }


    public int countAteliers() {
        String sql = "SELECT COUNT(*) FROM atelierenligne";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.out.println("❌ Erreur lors du comptage des ateliers : " + e.getMessage());
        }
        return 0;
    }
    public void ajouterAtelier(Ateliers atelier) {
        // Inclure id_user_id dans la requête d'insertion
        String sql = "INSERT INTO atelierenligne (id_user_id, titre, categorie, description, niveau_diff, prix, datecours, duree, lien) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = cnx.prepareStatement(sql)) {
            // Ajouter l'ID de l'utilisateur connecté à l'insertion
            stmt.setInt(1, atelier.getUser()); // Utilisation de l'ID de l'utilisateur (currentUser.getId())
            stmt.setString(2, atelier.getTitre());
            stmt.setString(3, atelier.getCategorie());
            stmt.setString(4, atelier.getDescription());
            stmt.setString(5, atelier.getNiveau_diff());
            stmt.setDouble(6, atelier.getPrix());
            stmt.setTimestamp(7, Timestamp.valueOf(atelier.getDatecours()));  // Convertir LocalDateTime en Timestamp
            stmt.setInt(8, atelier.getDuree());
            stmt.setString(9, atelier.getLien());

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void supprimerAtelier(int id) {
        String sql = "DELETE FROM atelierenligne WHERE id = ?";
        try (PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void modifierAtelier(Ateliers atelier) {
        String sql = "UPDATE atelierenligne SET titre = ?, categorie = ?, description = ?, niveau_diff = ?, prix = ?, datecours = ?, duree = ?, lien = ? WHERE id = ?";
        try (PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setString(1, atelier.getTitre());
            stmt.setString(2, atelier.getCategorie());
            stmt.setString(3, atelier.getDescription());
            stmt.setString(4, atelier.getNiveau_diff());
            stmt.setDouble(5, atelier.getPrix());
            stmt.setTimestamp(6, Timestamp.valueOf(atelier.getDatecours()));
            stmt.setInt(7, atelier.getDuree());
            stmt.setString(8, atelier.getLien());
            stmt.setInt(9, atelier.getId());

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



}
