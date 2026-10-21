package auth;

import database.DBConnection;
import model.Admin;
import model.Customer;
import model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Database operations for user authentication and registration.
 */
public class UserDAO {

    public static User login(String email, String password) {
        String normalizedEmail = email == null ? "" : email.trim();
        if (normalizedEmail.isEmpty()) {
            return null;
        }

        String[] candidateEmails = buildEmailCandidates(normalizedEmail);
        String sql = "SELECT * FROM Users WHERE email = ? AND password = ?";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                System.err.println("[UserDAO] Login failed: database connection is unavailable.");
                return null;
            }

            for (String candidate : candidateEmails) {
                if (candidate == null || candidate.isEmpty()) continue;
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, candidate);
                    pstmt.setString(2, password);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            int id = rs.getInt("user_id");
                            String name = rs.getString("name");
                            String role = rs.getString("role");
                            if ("BANNED".equalsIgnoreCase(role)) {
                                System.err.println("[UserDAO] User account is banned.");
                                return null;
                            }
                            return "Admin".equalsIgnoreCase(role)
                                ? new Admin(id, name, candidate, password)
                                : new Customer(id, name, candidate, password);
                        }
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("[UserDAO] Login failed: " + e.getMessage());
        }
        return null;
    }

    private static String[] buildEmailCandidates(String email) {
        String lower = email.toLowerCase();
        if ("admin@example.com".equals(lower)) {
            return new String[] { "admin@example.com", "admin@moviebooking.com" };
        }
        if ("admin@moviebooking.com".equals(lower)) {
            return new String[] { "admin@moviebooking.com", "admin@example.com" };
        }
        return new String[] { email };
    }

    public static boolean registerUser(String name, String email, String password) {
        String sql = "INSERT INTO Users (name, email, password, role) VALUES (?, ?, ?, 'Customer')";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                System.err.println("[UserDAO] Registration failed: database connection is unavailable.");
                return false;
            }

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, name);
                pstmt.setString(2, email);
                pstmt.setString(3, password);
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] Registration failed: " + e.getMessage());
            return false;
        }
    }

    public static List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM Users ORDER BY name";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                System.err.println("[UserDAO] Could not fetch users: database connection is unavailable.");
                return users;
            }

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    int userId = rs.getInt("user_id");
                    String name = rs.getString("name");
                    String email = rs.getString("email");
                    String password = rs.getString("password");
                    String role = rs.getString("role");
                    users.add(new User(userId, name, email, password, role));
                }
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] Could not fetch users: " + e.getMessage());
        }
        return users;
    }

    public static boolean hasActiveBookings(int userId) {
        String sql = "SELECT COUNT(*) FROM Bookings WHERE user_id = ? AND status <> 'CANCELLED'";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                System.err.println("[UserDAO] Active-booking check failed: database connection is unavailable.");
                return false;
            }
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, userId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1) > 0;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] Active-booking check failed: " + e.getMessage());
        }
        return false;
    }

    public static boolean banUser(int userId) {
        String sql = "UPDATE Users SET role = 'BANNED' WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                System.err.println("[UserDAO] Ban failed: database connection is unavailable.");
                return false;
            }
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, userId);
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] Ban failed: " + e.getMessage());
            return false;
        }
    }

    public static boolean deleteUser(int userId) {
        String sql = "DELETE FROM Users WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                System.err.println("[UserDAO] Delete user failed: database connection is unavailable.");
                return false;
            }
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, userId);
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] Delete user failed: " + e.getMessage());
            return false;
        }
    }

    public static void displayAllUsers() {
        String sql = "SELECT * FROM Users";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                System.err.println("[UserDAO] Could not fetch users: database connection is unavailable.");
                return;
            }

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                System.out.printf("%-5s | %-20s | %-25s | %-10s%n", "ID", "Name", "Email", "Role");
                System.out.println("-".repeat(65));
                while (rs.next()) {
                    System.out.printf("%-5d | %-20s | %-25s | %-10s%n",
                        rs.getInt("user_id"), rs.getString("name"),
                        rs.getString("email"), rs.getString("role"));
                }
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] Could not fetch users: " + e.getMessage());
        }
    }
}