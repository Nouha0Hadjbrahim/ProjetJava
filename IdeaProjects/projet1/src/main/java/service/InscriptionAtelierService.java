package service;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import model.InscriptionAtelier;
import utils.EmailSender;


import static utils.DBConnection.getConnection;

public class InscriptionAtelierService {
    private Connection cnx;

    public InscriptionAtelierService() {
        cnx = getConnection();
    }

    // Créer une inscription
    public boolean createInscription(InscriptionAtelier inscription) {
        // Vérifier que l'utilisateur existe
        String checkUserSql = "SELECT COUNT(*) FROM user WHERE id = ?";
        try (PreparedStatement checkPs = cnx.prepareStatement(checkUserSql)) {
            checkPs.setInt(1, inscription.getUserId());
            ResultSet rs = checkPs.executeQuery();
            if (rs.next() && rs.getInt(1) == 0) {
                System.err.println("❌ Utilisateur introuvable : ID = " + inscription.getUserId());
                return false;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur vérification utilisateur : " + e.getMessage());
            return false;
        }

        // Insérer l'inscription
        String sql = "INSERT INTO inscriptionatelier (atelier_id, id_user_id, dateinscri, statut) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, inscription.getAtelierId());
            ps.setInt(2, inscription.getUserId());
            ps.setDate(3, Date.valueOf(inscription.getDateInscription()));
            ps.setString(4, inscription.getStatut());

            boolean success = ps.executeUpdate() > 0;

            if (!success) {
                System.err.println("❌ Échec de l'insertion dans la base.");
                return false;
            }

            // 🔄 Récupérer infos utilisateur
            String email = null;
            String nomUtilisateur = "";
            String prenomUtilisateur = "";
            String userQuery = "SELECT email, nom, prenom FROM user WHERE id = ?";
            try (PreparedStatement userPs = cnx.prepareStatement(userQuery)) {
                userPs.setInt(1, inscription.getUserId());
                ResultSet userRs = userPs.executeQuery();
                if (userRs.next()) {
                    email = userRs.getString("email");
                    nomUtilisateur = userRs.getString("nom");
                    prenomUtilisateur = userRs.getString("prenom");
                }
            }

            // 🔄 Récupérer infos atelier + formateur
            String titreAtelier = "";
            LocalDateTime dateAtelier = null;
            String nomFormateur = "";
            String atelierQuery = "SELECT a.titre, a.datecours, u.nom AS formateurNom, u.prenom AS formateurPrenom " +
                    "FROM atelierenligne a JOIN user u ON a.id_user_id = u.id WHERE a.id = ?";
            try (PreparedStatement atelierPs = cnx.prepareStatement(atelierQuery)) {
                atelierPs.setInt(1, inscription.getAtelierId());
                ResultSet atelierRs = atelierPs.executeQuery();
                if (atelierRs.next()) {
                    titreAtelier = atelierRs.getString("titre");
                    dateAtelier = atelierRs.getTimestamp("datecours").toLocalDateTime();
                    nomFormateur = atelierRs.getString("formateurPrenom") + " " + atelierRs.getString("formateurNom");
                }
            }

            // ✉️ Envoi de l'email
            if (email != null && dateAtelier != null) {
                String dateStr = dateAtelier.format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'à' HH:mm"));

                String contenu = "Bonjour " + prenomUtilisateur + " " + nomUtilisateur + ",\n\n" +
                        "Votre inscription à l'atelier \"" + titreAtelier + "\" animé par " + nomFormateur + " a bien été enregistrée.\n\n" +
                        "L’atelier aura lieu le " + dateStr + ".\n\n" +
                        "Merci et à bientôt !";

                EmailSender.sendEmail(email, "Confirmation d'inscription à l'atelier", contenu);
            } else {
                System.err.println("⚠️ Email non envoyé : données manquantes.");
            }

            return true;

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la création de l'inscription : " + e.getMessage());
            return false;
        }
    }


    // Méthode pour supprimer une inscription
    public boolean deleteInscription(int id) {
        String sql = "DELETE FROM inscriptionatelier WHERE id = ?";
        try (PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ Erreur supprimerINSCRI: " + e.getMessage());
        }
        return false;
    }
    public boolean checkExistingInscription(int userId, int atelierId) {
        String sql = "SELECT COUNT(*) FROM inscriptionatelier " +
                "WHERE id_user_id = ? AND atelier_id = ? AND statut != 'annulée'";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, atelierId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Erreur vérification inscription: " + e.getMessage());
        }
        return false;
    }
    public List<InscriptionAtelier> getInscriptionsByUser(int userId) {
        List<InscriptionAtelier> inscriptions = new ArrayList<>();
        String sql = "SELECT * FROM inscriptionatelier WHERE id_user_id = ?";

        try (
             PreparedStatement stmt = cnx.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                InscriptionAtelier inscription = new InscriptionAtelier();
                inscription.setId(rs.getInt("id"));
                inscription.setAtelierId(rs.getInt("atelier_id"));
                inscription.setUserId(rs.getInt("id_user_id"));
                inscription.setDateInscription(rs.getDate("dateinscri").toLocalDate());
                inscription.setStatut(rs.getString("statut"));

                inscriptions.add(inscription);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return inscriptions;
    }

    public int getNombreInscriptions(int atelierId) {
        String sql = "SELECT COUNT(*) FROM inscriptionatelier WHERE atelier_id = ?";
        try (
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, atelierId);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public List<String> getNomsInscrits(int atelierId) {
        List<String> inscrits = new ArrayList<>();
        String sql = "SELECT u.nom, u.prenom FROM user u " +
                "JOIN inscriptionatelier i ON u.id = i.id_user_id " +
                "WHERE i.atelier_id = ?";

        try (
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, atelierId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                inscrits.add(rs.getString("prenom") + " " + rs.getString("nom"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return inscrits;
    }
}
