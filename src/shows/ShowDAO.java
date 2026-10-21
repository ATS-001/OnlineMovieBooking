package shows;

import booking.BookingDAO;
import database.DBConnection;
import model.Show;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

/**
 * Database operations for Shows table.
 * Includes transactional seat generation on show creation.
 */
public class ShowDAO {

    public static boolean addShow(int movieId, Date showDate, Time showTime,
                                  String screen, double ticketPrice) {
        String insertShowSQL = "INSERT INTO Shows (movie_id, show_date, show_time, screen, ticket_price) " +
                               "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                System.err.println("[ShowDAO] addShow failed: database connection is unavailable.");
                return false;
            }

            conn.setAutoCommit(false);
            try (PreparedStatement pstmt = conn.prepareStatement(insertShowSQL, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, movieId);
                pstmt.setDate(2, showDate);
                pstmt.setTime(3, showTime);
                pstmt.setString(4, screen);
                pstmt.setDouble(5, ticketPrice);

                if (pstmt.executeUpdate() == 0) { conn.rollback(); return false; }

                try (ResultSet keys = pstmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        generateSeatsForShow(conn, keys.getInt(1));
                        conn.commit();
                        return true;
                    }
                }
            } catch (SQLException ex) {
                conn.rollback();
                System.err.println("[ShowDAO] Transaction rolled back: " + ex.getMessage());
            }
        } catch (SQLException e) {
            System.err.println("[ShowDAO] addShow failed: " + e.getMessage());
        }
        return false;
    }

    public static boolean updateShow(int showId, Date showDate, Time showTime, String screen, double ticketPrice) {
        String sql = "UPDATE Shows SET show_date = ?, show_time = ?, screen = ?, ticket_price = ? WHERE show_id = ?";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                System.err.println("[ShowDAO] updateShow failed: database connection is unavailable.");
                return false;
            }
            conn.setAutoCommit(false);
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setDate(1, showDate);
                pstmt.setTime(2, showTime);
                pstmt.setString(3, screen);
                pstmt.setDouble(4, ticketPrice);
                pstmt.setInt(5, showId);
                int rows = pstmt.executeUpdate();
                if (rows == 0) {
                    conn.rollback();
                    return false;
                }
                BookingDAO.syncBookingHistoryForShow(conn, showId, showDate, showTime, screen);
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                System.err.println("[ShowDAO] updateShow failed: " + e.getMessage());
                return false;
            }
        } catch (SQLException e) {
            System.err.println("[ShowDAO] Connection failed while updating show: " + e.getMessage());
            return false;
        }
    }

    private static void generateSeatsForShow(Connection conn, int showId) throws SQLException {
        String sql = "INSERT INTO Seats (show_id, seat_number, status) VALUES (?, ?, 'AVAILABLE')";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (char row = 'A'; row <= 'J'; row++) {
                for (int i = 1; i <= 10; i++) {
                    pstmt.setInt(1, showId);
                    pstmt.setString(2, String.valueOf(row) + i);
                    pstmt.addBatch();
                }
            }
            pstmt.executeBatch();
        }
    }

    public static List<Show> getAllShows() {
        List<Show> shows = new ArrayList<>();
        String sql = "SELECT s.*, m.title AS movie_title " +
                     "FROM Shows s JOIN Movies m ON s.movie_id = m.movie_id " +
                     "ORDER BY s.show_date, s.show_time";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                System.err.println("[ShowDAO] getAllShows failed: database connection is unavailable.");
                return shows;
            }

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    shows.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[ShowDAO] getAllShows failed: " + e.getMessage());
        }
        return shows;
    }

    public static List<Show> getShowsByMovieId(int movieId) {
        List<Show> shows = new ArrayList<>();
        String sql = "SELECT s.*, m.title AS movie_title " +
                     "FROM Shows s JOIN Movies m ON s.movie_id = m.movie_id " +
                     "WHERE s.movie_id = ? ORDER BY s.show_date, s.show_time";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                System.err.println("[ShowDAO] getShowsByMovieId failed: database connection is unavailable.");
                return shows;
            }

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, movieId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        shows.add(mapRow(rs));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[ShowDAO] getShowsByMovieId failed: " + e.getMessage());
        }
        return shows;
    }

    public static Show getShowById(int id) {
        String sql = "SELECT s.*, m.title AS movie_title " +
                     "FROM Shows s JOIN Movies m ON s.movie_id = m.movie_id WHERE s.show_id = ?";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                System.err.println("[ShowDAO] getShowById failed: database connection is unavailable.");
                return null;
            }

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[ShowDAO] getShowById failed: " + e.getMessage());
        }
        return null;
    }

    public static boolean deleteShow(int id) {
        String sql = "DELETE FROM Shows WHERE show_id = ?";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                System.err.println("[ShowDAO] deleteShow failed: database connection is unavailable.");
                return false;
            }

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("[ShowDAO] deleteShow failed: " + e.getMessage());
            return false;
        }
    }

    private static Show mapRow(ResultSet rs) throws SQLException {
        return new Show(
            rs.getInt("show_id"), rs.getInt("movie_id"),
            rs.getDate("show_date"), rs.getTime("show_time"),
            rs.getString("screen"), rs.getDouble("ticket_price"),
            rs.getString("movie_title")
        );
    }
}