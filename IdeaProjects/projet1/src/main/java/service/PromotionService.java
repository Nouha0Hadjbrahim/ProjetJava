package service;

import model.Promotion;
import utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PromotionService {
    Connection cnx = DBConnection.getConnection();

    public void addPromotion(Promotion promotion) {
        String sql = "INSERT INTO promotion (code_coupon, prix_nouv, start_date, end_date) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, promotion.getCodeCoupon());
            ps.setDouble(2, promotion.getPrixNouv());
            ps.setDate(3, java.sql.Date.valueOf(promotion.getStartDate()));
            ps.setDate(4, java.sql.Date.valueOf(promotion.getEndDate()));
            ps.executeUpdate();
            System.out.println("✅ Promotion insérée !");
        } catch (SQLException e) {
            System.out.println("❌ Erreur insertion promotion : " + e.getMessage());
        }
    }

    public void updatePromotion(Promotion promotion) {
        String sql = "UPDATE promotion SET code_coupon = ?, prix_nouv = ?, start_date = ?, end_date = ? WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, promotion.getCodeCoupon());
            ps.setDouble(2, promotion.getPrixNouv());
            ps.setDate(3, java.sql.Date.valueOf(promotion.getStartDate()));
            ps.setDate(4, java.sql.Date.valueOf(promotion.getEndDate()));
            ps.setInt(5, promotion.getId());
            ps.executeUpdate();
            System.out.println("✅ Promotion mise à jour !");
        } catch (SQLException e) {
            System.out.println("❌ Erreur mise à jour promotion : " + e.getMessage());
        }
    }

    public void deletePromotion(int id) {
        String sql = "DELETE FROM promotion WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("✅ Promotion supprimée !");
        } catch (SQLException e) {
            System.out.println("❌ Erreur suppression promotion : " + e.getMessage());
        }
    }

    public Promotion getPromotionById(int id) {
        String sql = "SELECT * FROM promotion WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Promotion(
                        rs.getInt("id"),
                        rs.getString("code_coupon"),
                        rs.getDouble("prix_nouv"),
                        rs.getDate("start_date").toLocalDate(),
                        rs.getDate("end_date").toLocalDate()
                );
            }
        } catch (SQLException e) {
            System.out.println("❌ Erreur récupération promotion : " + e.getMessage());
        }
        return null;
    }

    public List<Promotion> getAll() {
        List<Promotion> promotions = new ArrayList<>();
        String sql = "SELECT * FROM promotion";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                promotions.add(new Promotion(
                        rs.getInt("id"),
                        rs.getString("code_coupon"),
                        rs.getDouble("prix_nouv"),
                        rs.getDate("start_date").toLocalDate(),
                        rs.getDate("end_date").toLocalDate()
                ));
            }
        } catch (SQLException e) {
            System.out.println("❌ Erreur récupération promotions : " + e.getMessage());
        }
        return promotions;
    }

    public int countPromotions() {
        String sql = "SELECT COUNT(*) FROM promotion";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("❌ Erreur comptage promotions : " + e.getMessage());
        }
        return 0;
    }

    public boolean codeCouponExists(String codeCoupon) {
        String sql = "SELECT COUNT(*) FROM promotion WHERE code_coupon = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, codeCoupon);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.out.println("❌ Erreur vérification code coupon : " + e.getMessage());
        }
        return false;
    }
}