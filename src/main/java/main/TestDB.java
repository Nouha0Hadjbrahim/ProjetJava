package main;

import utils.DBConnection;

import java.sql.Connection;

public class TestDB {
    public static void main(String[] args) {
        Connection conn = DBConnection.getConnection();

        if (conn != null) {
            System.out.println("✅ Test réussi : base de données connectée !");
        } else {
            System.out.println("❌ Test échoué : connexion non établie !");
        }
    }
}
