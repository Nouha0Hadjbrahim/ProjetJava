package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/webproject";
    private static final String USER = "root"; // à adapter si tu as un mot de passe
    private static final String PASSWORD = ""; // ex : "root" ou "" si vide

    private static Connection conn;

    public static Connection getConnection() {
        if (conn == null) {
            try {
                conn = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("✅ Connexion établie avec la base de données !");
            } catch (SQLException e) {
                System.out.println("❌ Erreur de connexion : " + e.getMessage());
            }
        }
        return conn;
    }
}
