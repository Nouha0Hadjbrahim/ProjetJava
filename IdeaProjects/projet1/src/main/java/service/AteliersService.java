package service;

import model.Ateliers;
import utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AteliersService {
    private final Connection cnx;

    public AteliersService() {
        this.cnx = DBConnection.getConnection();
    }

    public List<Ateliers> getAteliersPage(int page, int pageSize) {
        List<Ateliers> ateliers = new ArrayList<>();
        String sql = "SELECT * FROM atelierenligne LIMIT ? OFFSET ?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, pageSize);
            ps.setInt(2, (page - 1) * pageSize);

            System.out.println("Executing query: " + ps.toString()); // Debug

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Ateliers a = new Ateliers();
                a.setId(rs.getInt("id"));
                a.setTitre(rs.getString("titre"));
                a.setUser(rs.getInt("id_user_id"));
                a.setCategorie(rs.getString("categorie"));
                a.setDescription(rs.getString("description"));
                a.setNiveau_diff(rs.getString("niveau_diff"));
                a.setPrix(rs.getDouble("prix"));

                Timestamp timestamp = rs.getTimestamp("datecours");
                a.setDatecours(timestamp != null ? timestamp.toLocalDateTime() : null);

                a.setDuree(rs.getInt("duree"));
                a.setLien(rs.getString("lien"));

                ateliers.add(a);

                System.out.println("Atelier chargé: " + a.getTitre()); // Debug
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur getAteliersPage: " + e.getMessage());
            e.printStackTrace();
        }
        return ateliers;
    }

    public int countAteliers() {
        String sql = "SELECT COUNT(*) FROM atelierenligne";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur countAteliers: " + e.getMessage());
        }
        return 0;
    }

    public boolean ajouterAtelier(Ateliers atelier) {
        String sql = "INSERT INTO atelierenligne (id_user_id, titre, categorie, description, niveau_diff, prix, datecours, duree, lien) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            // Vérification si l'utilisateur est null
            Integer userId = atelier.getUser();  // On laisse l'utilisateur être un Integer qui peut être null
            if (userId == null) {
                System.err.println("❌ L'utilisateur est null.");
                return false;
            }

            // Set les paramètres de la requête
            stmt.setInt(1, userId); // ID de l'utilisateur

            stmt.setString(2, atelier.getTitre());
            stmt.setString(3, atelier.getCategorie());
            stmt.setString(4, atelier.getDescription());
            stmt.setString(5, atelier.getNiveau_diff());
            stmt.setDouble(6, atelier.getPrix());
            stmt.setTimestamp(7, atelier.getDatecours() != null ? Timestamp.valueOf(atelier.getDatecours()) : null);
            stmt.setInt(8, atelier.getDuree());
            stmt.setString(9, atelier.getLien());

            // Exécution de la requête d'insertion
            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        atelier.setId(rs.getInt(1)); // Récupère la clé générée (ID de l'atelier)
                        System.out.println("Atelier ajouté avec succès ! ID généré : " + atelier.getId());
                        return true;
                    }
                }
            } else {
                System.err.println("Aucune ligne affectée, l'ajout a échoué.");
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'ajout de l'atelier : " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public boolean supprimerAtelier(int id) {
        String sql = "DELETE FROM atelierenligne WHERE id = ?";
        try (PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ Erreur supprimerAtelier: " + e.getMessage());
        }
        return false;
    }

    public boolean modifierAtelier(Ateliers atelier) {
        String sql = "UPDATE atelierenligne SET titre = ?, categorie = ?, description = ?, " +
                "niveau_diff = ?, prix = ?, datecours = ?, duree = ?, lien = ? WHERE id = ?";

        try (PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setString(1, atelier.getTitre());
            stmt.setString(2, atelier.getCategorie());
            stmt.setString(3, atelier.getDescription());
            stmt.setString(4, atelier.getNiveau_diff());
            stmt.setDouble(5, atelier.getPrix());
            stmt.setTimestamp(6, atelier.getDatecours() != null ?
                    Timestamp.valueOf(atelier.getDatecours()) : null);
            stmt.setInt(7, atelier.getDuree());
            stmt.setString(8, atelier.getLien());
            stmt.setInt(9, atelier.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ Erreur modifierAtelier: " + e.getMessage());
        }
        return false;
    }

    public String getNomFormateur(int userId) {
        String sql = "SELECT nom, prenom FROM user WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("prenom") + " " + rs.getString("nom");
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur getNomFormateur: " + e.getMessage());
        }
        return "Formateur inconnu";
    }

    public Ateliers getAtelierById(int atelierId) {
        String sql = "SELECT * FROM atelierenligne WHERE id = ?";

        try (PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setInt(1, atelierId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Ateliers atelier = new Ateliers();
                atelier.setId(rs.getInt("id"));
                atelier.setTitre(rs.getString("titre"));
                atelier.setUser(rs.getInt("id_user_id"));
                atelier.setCategorie(rs.getString("categorie"));
                atelier.setDescription(rs.getString("description"));
                atelier.setNiveau_diff(rs.getString("niveau_diff"));
                atelier.setPrix(rs.getDouble("prix"));

                Timestamp timestamp = rs.getTimestamp("datecours");
                atelier.setDatecours(timestamp != null ? timestamp.toLocalDateTime() : null);

                atelier.setDuree(rs.getInt("duree"));
                atelier.setLien(rs.getString("lien"));

                return atelier;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur getAtelierById: " + e.getMessage());
        }
        return null;
    }

    public boolean titreExists(String titre) {
        try (
             PreparedStatement statement = cnx.prepareStatement(
                     "SELECT COUNT(*) FROM atelierenligne WHERE titre = ?")) {
            statement.setString(1, titre);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}