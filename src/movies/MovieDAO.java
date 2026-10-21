package movies;

import database.DBConnection;
import model.Movie;

import java.sql.Connection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Database operations for Movies table.
 */
public class MovieDAO {

    public static boolean addMovie(Movie movie) {
        String sql = "INSERT INTO Movies (title, genre, language, description, release_date, duration) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                System.err.println("[MovieDAO] Failed to add movie: database connection is unavailable.");
                return false;
            }

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, movie.getTitle());
                pstmt.setString(2, movie.getGenre());
                pstmt.setString(3, movie.getLanguage());
                pstmt.setString(4, movie.getDescription());
                pstmt.setDate(5, movie.getReleaseDate());
                pstmt.setInt(6, movie.getDuration());
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("[MovieDAO] Failed to add movie: " + e.getMessage());
            return false;
        }
    }

    public static List<Movie> getAllMovies() {
        List<Movie> movies = new ArrayList<>();
        String sql = "SELECT * FROM Movies ORDER BY title";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                System.err.println("[MovieDAO] Failed to fetch movies: database connection is unavailable.");
                return movies;
            }

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                while (rs.next()) {
                    movies.add(new Movie(
                        rs.getInt("movie_id"), rs.getString("title"), rs.getString("genre"),
                        rs.getString("language"), rs.getString("description"),
                        rs.getDate("release_date"), rs.getInt("duration")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("[MovieDAO] Failed to fetch movies: " + e.getMessage());
        }
        return movies;
    }

    public static List<Movie> searchMovies(String titleKeyword) {
        List<Movie> movies = new ArrayList<>();
        String sql = "SELECT * FROM Movies WHERE title LIKE ? ORDER BY title";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                System.err.println("[MovieDAO] Search failed: database connection is unavailable.");
                return movies;
            }

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, "%" + titleKeyword + "%");
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        movies.add(new Movie(
                            rs.getInt("movie_id"), rs.getString("title"), rs.getString("genre"),
                            rs.getString("language"), rs.getString("description"),
                            rs.getDate("release_date"), rs.getInt("duration")
                        ));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[MovieDAO] Search failed: " + e.getMessage());
        }
        return movies;
    }

    public static boolean updateMovie(int id, String title, String genre,
                                      String language, String description, int duration) {
        String sql = "UPDATE Movies SET title=?, genre=?, language=?, description=?, duration=? " +
                     "WHERE movie_id=?";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                System.err.println("[MovieDAO] Update failed: database connection is unavailable.");
                return false;
            }

            conn.setAutoCommit(false);
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, title);
                pstmt.setString(2, genre);
                pstmt.setString(3, language);
                pstmt.setString(4, description);
                pstmt.setInt(5, duration);
                pstmt.setInt(6, id);
                int rows = pstmt.executeUpdate();
                if (rows == 0) {
                    conn.rollback();
                    return false;
                }
                booking.BookingDAO.syncBookingHistoryForMovie(conn, id, title);
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                System.err.println("[MovieDAO] Update failed: " + e.getMessage());
                return false;
            }
        } catch (SQLException e) {
            System.err.println("[MovieDAO] Connection failed while updating movie: " + e.getMessage());
            return false;
        }
    }

    public static boolean deleteMovie(int id) {
        String sql = "DELETE FROM Movies WHERE movie_id=?";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                System.err.println("[MovieDAO] Delete failed: database connection is unavailable.");
                return false;
            }

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("[MovieDAO] Delete failed: " + e.getMessage());
            return false;
        }
    }
}