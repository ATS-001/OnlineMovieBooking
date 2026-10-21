package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Centralized JDBC connection class.
 * Reads DB credentials from .env via Config.
 * All DAO classes use this class — never open raw connections elsewhere.
 */
public class DBConnection {

    public static Connection getConnection() {
        String url      = Config.get("DB_URL");
        String user     = Config.get("DB_USER");
        String password = Config.get("DB_PASSWORD");

        if (url == null || url.isEmpty()) {
            System.err.println(
                "[DBConnection] DB_URL is not set.\n" +
                "Please create a .env file from .env.example in the project root\n" +
                "and fill in your MySQL credentials.");
            return null;
        }

        try {
            // Explicit driver load (safe for older classpath setups)
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(url, user, password != null ? password : "");
        } catch (ClassNotFoundException e) {
            System.err.println("[DBConnection] MySQL JDBC driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("[DBConnection] Failed to connect to database: " + e.getMessage());
        }
        return null;
    }
}