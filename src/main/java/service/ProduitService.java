package service;

import model.Produit;
import utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProduitService {
    Connection cnx = DBConnection.getConnection();

    public void addProduit(Produit produit) {
        String sql = "INSERT INTO produit (id_promotion_id, id_user_id, nom_produit, categorie, prix, statut, stock, image) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (produit.getIdPromotion() != null) {
                ps.setInt(1, produit.getIdPromotion());
            } else {
                ps.setNull(1, java.sql.Types.INTEGER);
            }
            if (produit.getIdUser() != null) {
                ps.setInt(2, produit.getIdUser());
            } else {
                ps.setNull(2, java.sql.Types.INTEGER);
            }
            ps.setString(3, produit.getNomProduit());
            ps.setString(4, produit.getCategorie());
            ps.setDouble(5, produit.getPrix());
            ps.setString(6, produit.getStatut());
            ps.setInt(7, produit.getStock());
            ps.setString(8, produit.getImage());
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                produit.setId(rs.getInt(1));
            }
            System.out.println("✅ Produit inséré !");
        } catch (SQLException e) {
            System.out.println("❌ Erreur insertion produit : " + e.getMessage());
        }
    }

    public void updateProduit(Produit produit) {
        String sql = "UPDATE produit SET id_promotion_id = ?, id_user_id = ?, nom_produit = ?, categorie = ?, " +
                "prix = ?, statut = ?, stock = ?, image = ? WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            if (produit.getIdPromotion() != null) {
                ps.setInt(1, produit.getIdPromotion());
            } else {
                ps.setNull(1, java.sql.Types.INTEGER);
            }
            if (produit.getIdUser() != null) {
                ps.setInt(2, produit.getIdUser());
            } else {
                ps.setNull(2, java.sql.Types.INTEGER);
            }
            ps.setString(3, produit.getNomProduit());
            ps.setString(4, produit.getCategorie());
            ps.setDouble(5, produit.getPrix());
            ps.setString(6, produit.getStatut());
            ps.setInt(7, produit.getStock());
            ps.setString(8, produit.getImage());
            ps.setInt(9, produit.getId());
            ps.executeUpdate();
            System.out.println("✅ Produit mis à jour !");
        } catch (SQLException e) {
            System.out.println("❌ Erreur mise à jour produit : " + e.getMessage());
        }
    }

    public void deleteProduit(int id) {
        String sql = "DELETE FROM produit WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("✅ Produit supprimé !");
        } catch (SQLException e) {
            System.out.println("❌ Erreur suppression produit : " + e.getMessage());
        }
    }

    public Produit getProduitById(int id) {
        String sql = "SELECT * FROM produit WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Produit(
                        rs.getInt("id"),
                        rs.getObject("id_promotion_id") != null ? rs.getInt("id_promotion_id") : null,
                        rs.getObject("id_user_id") != null ? rs.getInt("id_user_id") : null,
                        rs.getString("nom_produit"),
                        rs.getString("categorie"),
                        rs.getDouble("prix"),
                        rs.getString("statut"),
                        rs.getInt("stock"),
                        rs.getString("image")
                );
            }
        } catch (SQLException e) {
            System.out.println("❌ Erreur récupération produit : " + e.getMessage());
        }
        return null;
    }

    public List<Produit> getProduitsPage(int page, int pageSize) {
        List<Produit> produits = new ArrayList<>();
        String sql = "SELECT * FROM produit LIMIT ? OFFSET ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, pageSize);
            ps.setInt(2, (page - 1) * pageSize);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                produits.add(new Produit(
                        rs.getInt("id"),
                        rs.getObject("id_promotion_id") != null ? rs.getInt("id_promotion_id") : null,
                        rs.getObject("id_user_id") != null ? rs.getInt("id_user_id") : null,
                        rs.getString("nom_produit"),
                        rs.getString("categorie"),
                        rs.getDouble("prix"),
                        rs.getString("statut"),
                        rs.getInt("stock"),
                        rs.getString("image")
                ));
            }
        } catch (SQLException e) {
            System.out.println("❌ Erreur récupération page produits : " + e.getMessage());
        }
        return produits;
    }

    public int countProduits() {
        String sql = "SELECT COUNT(*) FROM produit";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("❌ Erreur comptage produits : " + e.getMessage());
        }
        return 0;
    }

    public Map<String, Integer> getProductCountByCategory() {
        Map<String, Integer> categoryCounts = new HashMap<>();
        String sql = "SELECT categorie, COUNT(*) as count FROM produit GROUP BY categorie";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                categoryCounts.put(rs.getString("categorie"), rs.getInt("count"));
            }
        } catch (SQLException e) {
            System.out.println("❌ Erreur récupération compte par catégorie : " + e.getMessage());
        }
        return categoryCounts;
    }

    public Map<String, Integer> getStockByCategory() {
        Map<String, Integer> stockByCategory = new HashMap<>();
        String sql = "SELECT categorie, SUM(stock) as total_stock FROM produit GROUP BY categorie";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                stockByCategory.put(rs.getString("categorie"), rs.getInt("total_stock"));
            }
        } catch (SQLException e) {
            System.out.println("❌ Erreur récupération stock par catégorie : " + e.getMessage());
        }
        return stockByCategory;
    }

    public List<Produit> getTopProductsWithPromotions(int limit) {
        List<Produit> products = new ArrayList<>();
        String sql = "SELECT p.* FROM produit p " +
                "INNER JOIN promotion pr ON p.id_promotion_id = pr.id " +
                "ORDER BY p.prix DESC LIMIT ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                products.add(new Produit(
                        rs.getInt("id"),
                        rs.getObject("id_promotion_id") != null ? rs.getInt("id_promotion_id") : null,
                        rs.getObject("id_user_id") != null ? rs.getInt("id_user_id") : null,
                        rs.getString("nom_produit"),
                        rs.getString("categorie"),
                        rs.getDouble("prix"),
                        rs.getString("statut"),
                        rs.getInt("stock"),
                        rs.getString("image")
                ));
            }
        } catch (SQLException e) {
            System.out.println("❌ Erreur récupération produits avec promotions : " + e.getMessage());
        }
        return products;
    }
}