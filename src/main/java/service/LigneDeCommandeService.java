package service;

import model.LigneDeCommande;
import utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LigneDeCommandeService {

    private Connection cnx = DBConnection.getConnection();

    // Add a new ligne de commande
    public void addLigneDeCommande(LigneDeCommande ligne) {
        if (cnx == null) {
            System.err.println("❌ Connexion à la base de données nulle dans LigneDeCommandeService!");
            showAlert("Erreur BD", "Connexion à la base de données nulle!");
            return;
        }
        String sql = "INSERT INTO lignedecommande (id_commande_id, quantite, prix_unitaire, id_produit_id, id_materiau) " +
                "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            System.out.println("[SQL] " + sql);
            System.out.println("[SQL] id_commande_id=" + ligne.getIdCommande() + ", quantite=" + ligne.getQuantite() + ", prix_unitaire=" + ligne.getPrixUnitaire() + ", id_produit_id=" + ligne.getIdProduit() + ", id_materiau=" + ligne.getIdMateriau());
            ps.setInt(1, ligne.getIdCommande());
            ps.setInt(2, ligne.getQuantite());
            ps.setDouble(3, ligne.getPrixUnitaire());
            if (ligne.getIdProduit() == 0) {
                ps.setNull(4, java.sql.Types.INTEGER);
            } else {
                ps.setInt(4, ligne.getIdProduit());
            }
            if (ligne.getIdMateriau() == 0) {
                ps.setNull(5, java.sql.Types.INTEGER);
            } else {
                ps.setInt(5, ligne.getIdMateriau());
            }
            ps.executeUpdate();

            // Retrieve the auto-generated ID
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                ligne.setId(rs.getInt(1));
            }
            System.out.println("✅ Ligne de commande insérée ! ID: " + ligne.getId());
        } catch (SQLException e) {
            System.out.println("❌ Erreur insertion ligne de commande : " + e.getMessage());
            showAlert("Erreur BD", "Erreur insertion ligne de commande : " + e.getMessage());
        }
    }

    private void showAlert(String title, String msg) {
        try {
            javax.swing.JOptionPane.showMessageDialog(null, msg, title, javax.swing.JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            System.err.println("[ALERT] " + title + ": " + msg);
        }
    }

    // Update an existing ligne de commande
    public void updateLigneDeCommande(LigneDeCommande ligne) {
        String sql = "UPDATE lignedecommande SET id_commande_id = ?, quantite = ?, prix_unitaire = ?, id_produit_id = ?, id_materiau = ? " +
                "WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, ligne.getIdCommande());
            ps.setInt(2, ligne.getQuantite());
            ps.setDouble(3, ligne.getPrixUnitaire());
            if (ligne.getIdProduit() == 0) {
                ps.setNull(4, java.sql.Types.INTEGER);
            } else {
                ps.setInt(4, ligne.getIdProduit());
            }
            if (ligne.getIdMateriau() == 0) {
                ps.setNull(5, java.sql.Types.INTEGER);
            } else {
                ps.setInt(5, ligne.getIdMateriau());
            }
            ps.setInt(6, ligne.getId());
            ps.executeUpdate();
            System.out.println("✅ Ligne de commande mise à jour ! ID: " + ligne.getId());
        } catch (SQLException e) {
            System.out.println("❌ Erreur mise à jour ligne de commande : " + e.getMessage());
            showAlert("Erreur BD", "Erreur mise à jour ligne de commande : " + e.getMessage());
        }
    }

    // Delete a ligne de commande by ID
    public void deleteLigneDeCommande(int id) {
        String sql = "DELETE FROM lignedecommande WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("✅ Ligne de commande supprimée ! ID: " + id);
        } catch (SQLException e) {
            System.out.println("❌ Erreur suppression ligne de commande : " + e.getMessage());
        }
    }

    // Delete all lignes de commande by commande ID
    public void deleteLignesDeCommandeByCommandeId(int commandeId) {
        String sql = "DELETE FROM lignedecommande WHERE id_commande_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, commandeId);
            ps.executeUpdate();
            System.out.println("✅ Lignes de commande supprimées pour commande ID: " + commandeId);
        } catch (SQLException e) {
            System.out.println("❌ Erreur suppression lignes de commande : " + e.getMessage());
        }
    }

    // Get a ligne de commande by ID
    public LigneDeCommande getLigneDeCommandeById(int id) {
        String sql = "SELECT * FROM lignedecommande WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new LigneDeCommande(
                        rs.getInt("id"),
                        rs.getInt("id_commande_id"),
                        rs.getInt("quantite"),
                        rs.getDouble("prix_unitaire"),
                        rs.getInt("id_produit_id"),
                        rs.getInt("id_materiau")
                );
            }
        } catch (SQLException e) {
            System.out.println("❌ Erreur récupération ligne de commande : " + e.getMessage());
        }
        return null;
    }

    // Get all lignes de commande
    public List<LigneDeCommande> getAllLignesDeCommande() {
        List<LigneDeCommande> lignes = new ArrayList<>();
        String sql = "SELECT * FROM lignedecommande";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lignes.add(new LigneDeCommande(
                        rs.getInt("id"),
                        rs.getInt("id_commande_id"),
                        rs.getInt("quantite"),
                        rs.getDouble("prix_unitaire"),
                        rs.getInt("id_produit_id"),
                        rs.getInt("id_materiau")
                ));
            }
        } catch (SQLException e) {
            System.out.println("❌ Erreur récupération lignes de commande : " + e.getMessage());
        }
        return lignes;
    }

    // Get lignes de commande by commande ID
    public List<LigneDeCommande> getLignesDeCommandeByCommandeId(int commandeId) {
        List<LigneDeCommande> lignes = new ArrayList<>();
        String sql = "SELECT * FROM lignedecommande WHERE id_commande_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, commandeId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lignes.add(new LigneDeCommande(
                        rs.getInt("id"),
                        rs.getInt("id_commande_id"),
                        rs.getInt("quantite"),
                        rs.getDouble("prix_unitaire"),
                        rs.getInt("id_produit_id"),
                        rs.getInt("id_materiau")
                ));
            }
        } catch (SQLException e) {
            System.out.println("❌ Erreur récupération lignes de commande par commande : " + e.getMessage());
        }
        return lignes;
    }

    // Get lignes de commande with pagination
    public List<LigneDeCommande> getLignesDeCommandePage(int page, int pageSize) {
        List<LigneDeCommande> lignes = new ArrayList<>();
        String sql = "SELECT * FROM lignedecommande LIMIT ? OFFSET ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, pageSize);
            ps.setInt(2, (page - 1) * pageSize);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lignes.add(new LigneDeCommande(
                        rs.getInt("id"),
                        rs.getInt("id_commande_id"),
                        rs.getInt("quantite"),
                        rs.getDouble("prix_unitaire"),
                        rs.getInt("id_produit_id"),
                        rs.getInt("id_materiau")
                ));
            }
        } catch (SQLException e) {
            System.out.println("❌ Erreur récupération page lignes de commande : " + e.getMessage());
        }
        return lignes;
    }

    // Count all lignes de commande
    public int countLignesDeCommande() {
        String sql = "SELECT COUNT(*) FROM lignedecommande";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("❌ Erreur comptage lignes de commande : " + e.getMessage());
        }
        return 0;
    }
}