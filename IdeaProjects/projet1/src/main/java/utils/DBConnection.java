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
        try {
            // Chargement du driver JDBC
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("✅ Connexion à la base de données réussie !");
            return connection;
        } catch (SQLException | ClassNotFoundException e) {
            System.out.println("❌ Erreur de connexion à la base de données : " + e.getMessage());
            return null;
        }

    }

}
