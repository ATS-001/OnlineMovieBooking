package booking;

import database.DBConnection;
import model.Seat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Database operations for Seats table.
 */
public class SeatDAO {

    public static List<Seat> getSeatsByShow(int showId) {
        List<Seat> seats = new ArrayList<>();
        String sql = "SELECT * FROM Seats WHERE show_id = ? ORDER BY seat_number";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                System.err.println("[SeatDAO] getSeatsByShow failed: database connection is unavailable.");
                return seats;
            }

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, showId);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    seats.add(new Seat(
                        rs.getInt("seat_id"), rs.getInt("show_id"),
                        rs.getString("seat_number"), rs.getString("status")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("[SeatDAO] getSeatsByShow failed: " + e.getMessage());
        }
        return seats;
    }

    public static Seat getSeatByNumber(int showId, String seatNumber) {
        String sql = "SELECT * FROM Seats WHERE show_id = ? AND seat_number = ?";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                System.err.println("[SeatDAO] getSeatByNumber failed: database connection is unavailable.");
                return null;
            }

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, showId);
                pstmt.setString(2, seatNumber);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    return new Seat(
                        rs.getInt("seat_id"), rs.getInt("show_id"),
                        rs.getString("seat_number"), rs.getString("status")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("[SeatDAO] getSeatByNumber failed: " + e.getMessage());
        }
        return null;
    }

    public static int getAvailableSeatCount(int showId) {
        String sql = "SELECT COUNT(*) FROM Seats WHERE show_id = ? AND status = 'AVAILABLE'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, showId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[SeatDAO] getAvailableSeatCount failed: " + e.getMessage());
        }
        return 0;
    }

    public static boolean isShowFull(int showId) {
        return getAvailableSeatCount(showId) == 0;
    }
}