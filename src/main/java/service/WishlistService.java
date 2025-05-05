package service;

import model.WishlistMateriaux;
import model.Material;
import model.User;
import utils.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WishlistService implements AutoCloseable {
    private final Connection conn;

    public WishlistService() throws SQLException {
        this.conn = DBConnection.getConnection();
        if (conn == null) {
            throw new SQLException("La connexion à la base de données a échoué.");
        }
    }

    public WishlistService(Connection conn) {
        this.conn = conn;
    }

    public void addToWishlist(User user, Material material) throws SQLException {
        validateParameters(user, material);

        try {
            WishlistMateriaux wishlist = findOrCreateWishlist(user);

            if (!isMaterialInWishlist(wishlist, material)) {
                addMaterialToWishlist(wishlist, material);
            }
        } catch (SQLException e) {
            throw new SQLException("Erreur lors de l'ajout à la wishlist: " + e.getMessage(), e);
        }
    }

    public void removeFromWishlist(User user, Material material) throws SQLException {
        validateParameters(user, material);

        try {
            WishlistMateriaux wishlist = findWishlistByUser(user);
            if (wishlist != null && isMaterialInWishlist(wishlist, material)) {
                removeMaterialFromWishlist(wishlist, material);
            }
        } catch (SQLException e) {
            throw new SQLException("Erreur lors de la suppression de la wishlist: " + e.getMessage(), e);
        }
    }

    public List<Material> getUserWishlist(User user) throws SQLException {
        if (user == null) {
            return Collections.emptyList();
        }

        try {
            WishlistMateriaux wishlist = findWishlistByUser(user);
            return wishlist != null ? new ArrayList<>(wishlist.getMaterials()) : Collections.emptyList();
        } catch (SQLException e) {
            throw new SQLException("Erreur lors de la récupération de la wishlist: " + e.getMessage(), e);
        }
    }

    private WishlistMateriaux findOrCreateWishlist(User user) throws SQLException {
        WishlistMateriaux wishlist = findWishlistByUser(user);
        if (wishlist == null) {
            wishlist = new WishlistMateriaux(user);
            saveWishlist(wishlist);
        }
        return wishlist;
    }

    private WishlistMateriaux findWishlistByUser(User user) throws SQLException {
        String sql = "SELECT id, user_id, date_ajout FROM wishlistmateriaux WHERE user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, user.getId());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    WishlistMateriaux wishlist = new WishlistMateriaux(user);
                    wishlist.setId(rs.getInt("id"));
                    java.sql.Date date = rs.getDate("date_ajout");
                    loadMaterialsForWishlist(wishlist);
                    return wishlist;
                }
            }
        }
        return null;
    }

    // Dans la méthode loadMaterialsForWishlist()
    private void loadMaterialsForWishlist(WishlistMateriaux wishlist) throws SQLException {
        // Correction de la requête SQL
        String sql = "SELECT m.id, m.nom_materiel, m.prix_unitaire, m.categorie, m.description, m.photo, "
                + "m.quantite_stock, m.seuil_min " // Ajout des colonnes manquantes
                + "FROM materiaux m "
                + "JOIN wishlistmateriaux_materiaux wm ON m.id = wm.materiaux_id "
                + "WHERE wm.wishlistmateriaux_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, wishlist.getId());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Material material = new Material();
                    material.setId(rs.getInt("id"));
                    material.setNomMateriel(rs.getString("nom_materiel"));
                    material.setPrixUnitaire(rs.getDouble("prix_unitaire"));
                    material.setCategorie(rs.getString("categorie"));
                    material.setDescription(rs.getString("description"));
                    material.setPhoto(rs.getString("photo"));

                    // Ajout des valeurs de stock
                    material.setQuantiteStock(rs.getInt("quantite_stock"));
                    material.setSeuilMin(rs.getInt("seuil_min"));

                    // Log de vérification
                    System.out.println("[BDD] Matériau chargé: " + material.getNomMateriel()
                            + " | Stock: " + material.getQuantiteStock()
                            + " | Seuil: " + material.getSeuilMin());

                    wishlist.addMaterial(material);
                }
            }
        }
    }

    private void saveWishlist(WishlistMateriaux wishlist) throws SQLException {
        String sql = "INSERT INTO wishlistmateriaux (user_id, date_ajout) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, wishlist.getUser().getId());
            stmt.setDate(2, new java.sql.Date(wishlist.getDateAjout().getTime()));
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    wishlist.setId(rs.getInt(1));
                }
            }
        }
    }

    private boolean isMaterialInWishlist(WishlistMateriaux wishlist, Material material) throws SQLException {
        String sql = "SELECT COUNT(*) FROM wishlistmateriaux_materiaux " +
                "WHERE wishlistmateriaux_id = ? AND materiaux_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, wishlist.getId());
            stmt.setInt(2, material.getId());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private void addMaterialToWishlist(WishlistMateriaux wishlist, Material material) throws SQLException {
        String sql = "INSERT INTO wishlistmateriaux_materiaux (wishlistmateriaux_id, materiaux_id) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, wishlist.getId());
            stmt.setInt(2, material.getId());
            stmt.executeUpdate();
            wishlist.addMaterial(material);
        }
    }

    private void removeMaterialFromWishlist(WishlistMateriaux wishlist, Material material) throws SQLException {
        String sql = "DELETE FROM wishlistmateriaux_materiaux " +
                "WHERE wishlistmateriaux_id = ? AND materiaux_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, wishlist.getId());
            stmt.setInt(2, material.getId());
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                wishlist.removeMaterial(material);
            }
        }
    }

    private void validateParameters(User user, Material material) {
        if (user == null) {
            throw new IllegalArgumentException("L'utilisateur ne peut pas être null");
        }
        if (material == null) {
            throw new IllegalArgumentException("Le matériau ne peut pas être null");
        }
        if (user.getId() <= 0) {
            throw new IllegalArgumentException("L'ID utilisateur est invalide");
        }
        if (material.getId() <= 0) {
            throw new IllegalArgumentException("L'ID matériau est invalide");
        }
    }

    @Override
    public void close() throws SQLException {
        if (conn != null && !conn.isClosed()) {
            conn.close();
        }
    }
}