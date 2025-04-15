package service;

import model.Commande;
import model.LigneDeCommande;
import utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CommandeService {

    private Connection cnx = DBConnection.getConnection();
    private LigneDeCommandeService ligneDeCommandeService;

    public CommandeService() {
        this.ligneDeCommandeService = new LigneDeCommandeService();
    }

    // Add a new commande
    public void addCommande(Commande commande) {
        String sql = "INSERT INTO commande (id_user_id, date_commande, montant_total, statut, adresse_livraison, paiement) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (commande.getIdUser() != null) {
                ps.setInt(1, commande.getIdUser());
            } else {
                ps.setNull(1, java.sql.Types.INTEGER);
            }
            ps.setDate(2, new java.sql.Date(commande.getDateCommande().getTime()));
            ps.setDouble(3, commande.getMontantTotal());
            ps.setString(4, commande.getStatut());
            ps.setString(5, commande.getAdresseLivraison());
            ps.setString(6, commande.getPaiement());
            ps.executeUpdate();

            // Retrieve the auto-generated ID
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                commande.setId(rs.getInt(1));
            }

            // Save associated lignes de commande
            for (LigneDeCommande ligne : commande.getLignesDeCommande()) {
                ligne.setIdCommande(commande.getId());
                ligneDeCommandeService.addLigneDeCommande(ligne);
            }

            System.out.println("✅ Commande insérée ! ID: " + commande.getId());
        } catch (SQLException e) {
            System.out.println("❌ Erreur insertion commande : " + e.getMessage());
        }
    }

    // Update an existing commande
    public void updateCommande(Commande commande) {
        String sql = "UPDATE commande SET id_user_id = ?, date_commande = ?, montant_total = ?, statut = ?, " +
                "adresse_livraison = ?, paiement = ? WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            if (commande.getIdUser() != null) {
                ps.setInt(1, commande.getIdUser());
            } else {
                ps.setNull(1, java.sql.Types.INTEGER);
            }
            ps.setDate(2, new java.sql.Date(commande.getDateCommande().getTime()));
            ps.setDouble(3, commande.getMontantTotal());
            ps.setString(4, commande.getStatut());
            ps.setString(5, commande.getAdresseLivraison());
            ps.setString(6, commande.getPaiement());
            ps.setInt(7, commande.getId());
            ps.executeUpdate();

            // Removed the deletion of lignes de commande
            // We don’t need to touch the lignes de commande here since they’re managed separately
            System.out.println("✅ Commande mise à jour ! ID: " + commande.getId());
        } catch (SQLException e) {
            System.out.println("❌ Erreur mise à jour commande : " + e.getMessage());
        }
    }

    // Delete a commande by ID
    public void deleteCommande(int id) {
        // First, delete associated lignes de commande
        ligneDeCommandeService.deleteLignesDeCommandeByCommandeId(id);

        // Then, delete the commande
        String sql = "DELETE FROM commande WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("✅ Commande supprimée ! ID: " + id);
        } catch (SQLException e) {
            System.out.println("❌ Erreur suppression commande : " + e.getMessage());
        }
    }

    // Get a commande by ID
    public Commande getCommandeById(int id) {
        String sql = "SELECT * FROM commande WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Commande commande = new Commande(
                        rs.getInt("id"),
                        rs.getObject("id_user_id") != null ? rs.getInt("id_user_id") : null,
                        rs.getDate("date_commande"),
                        rs.getDouble("montant_total"),
                        rs.getString("statut"),
                        rs.getString("adresse_livraison"),
                        rs.getString("paiement")
                );
                // Load associated lignes de commande
                commande.setLignesDeCommande(ligneDeCommandeService.getLignesDeCommandeByCommandeId(id));
                return commande;
            }
        } catch (SQLException e) {
            System.out.println("❌ Erreur récupération commande : " + e.getMessage());
        }
        return null;
    }

    // Get all commandes
    public List<Commande> getAllCommandes() {
        List<Commande> commandes = new ArrayList<>();
        String sql = "SELECT * FROM commande";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Commande commande = new Commande(
                        rs.getInt("id"),
                        rs.getObject("id_user_id") != null ? rs.getInt("id_user_id") : null,
                        rs.getDate("date_commande"),
                        rs.getDouble("montant_total"),
                        rs.getString("statut"),
                        rs.getString("adresse_livraison"),
                        rs.getString("paiement")
                );
                // Load associated lignes de commande
                commande.setLignesDeCommande(ligneDeCommandeService.getLignesDeCommandeByCommandeId(commande.getId()));
                commandes.add(commande);
            }
        } catch (SQLException e) {
            System.out.println("❌ Erreur récupération commandes : " + e.getMessage());
        }
        return commandes;
    }

    // Get commandes by user ID
    public List<Commande> getCommandesByUserId(int userId) {
        List<Commande> commandes = new ArrayList<>();
        String sql = "SELECT * FROM commande WHERE id_user_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Commande commande = new Commande(
                        rs.getInt("id"),
                        rs.getObject("id_user_id") != null ? rs.getInt("id_user_id") : null,
                        rs.getDate("date_commande"),
                        rs.getDouble("montant_total"),
                        rs.getString("statut"),
                        rs.getString("adresse_livraison"),
                        rs.getString("paiement")
                );
                // Load associated lignes de commande
                commande.setLignesDeCommande(ligneDeCommandeService.getLignesDeCommandeByCommandeId(commande.getId()));
                commandes.add(commande);
            }
        } catch (SQLException e) {
            System.out.println("❌ Erreur récupération commandes par utilisateur : " + e.getMessage());
        }
        return commandes;
    }

    // Get commandes with pagination
    public List<Commande> getCommandesPage(int page, int pageSize) {
        List<Commande> commandes = new ArrayList<>();
        String sql = "SELECT * FROM commande LIMIT ? OFFSET ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, pageSize);
            ps.setInt(2, (page - 1) * pageSize);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Commande commande = new Commande(
                        rs.getInt("id"),
                        rs.getObject("id_user_id") != null ? rs.getInt("id_user_id") : null,
                        rs.getDate("date_commande"),
                        rs.getDouble("montant_total"),
                        rs.getString("statut"),
                        rs.getString("adresse_livraison"),
                        rs.getString("paiement")
                );
                // Load associated lignes de commande
                commande.setLignesDeCommande(ligneDeCommandeService.getLignesDeCommandeByCommandeId(commande.getId()));
                commandes.add(commande);
            }
        } catch (SQLException e) {
            System.out.println("❌ Erreur récupération page commandes : " + e.getMessage());
        }
        return commandes;
    }

    // Count all commandes
    public int countCommandes() {
        String sql = "SELECT COUNT(*) FROM commande";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("❌ Erreur comptage commandes : " + e.getMessage());
        }
        return 0;
    }
}