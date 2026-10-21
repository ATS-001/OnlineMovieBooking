package booking;

import database.DBConnection;
import model.Booking;
import model.Seat;
import model.Show;
import payment.Payment;
import payment.PaymentDAO;
import shows.ShowDAO;

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
 * Handles the complete booking transaction:
 * 1. Verify seat availability (row-level lock)
 * 2. Insert Booking record
 * 3. Insert BookingSeats
 * 4. Mark Seats as BOOKED
 * 5. Record Payment
 * 6. COMMIT — or ROLLBACK on any failure
 */
public class BookingDAO {

    public static boolean bookTicket(int userId, int showId, List<Seat> seats,
                                     double totalAmount, Payment payment) {
        String checkSeatSQL       = "SELECT status FROM Seats WHERE seat_id = ? FOR UPDATE";
        String updateSeatSQL      = "UPDATE Seats SET status = 'BOOKED' WHERE seat_id = ?";
        String insertBookingSQL   = "INSERT INTO Bookings (user_id, show_id, total_amount, status, movie_title, show_date, show_time, screen, seat_numbers, history_note) VALUES (?, ?, ?, 'CONFIRMED', ?, ?, ?, ?, ?, NULL)";
        String insertBkSeatSQL    = "INSERT INTO BookingSeats (booking_id, seat_id) VALUES (?, ?)";

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                System.err.println("[BookingDAO] booking failed: database connection is unavailable.");
                return false;
            }

            ensureBookingHistoryColumns(conn);
            Show show = ShowDAO.getShowById(showId);
            if (show == null) {
                System.err.println("[BookingDAO] Booking failed: show not found for ID " + showId);
                return false;
            }

            conn.setAutoCommit(false);
            try {
                try (PreparedStatement checkStmt = conn.prepareStatement(checkSeatSQL)) {
                    for (Seat seat : seats) {
                        checkStmt.setInt(1, seat.getSeatId());
                        ResultSet rs = checkStmt.executeQuery();
                        if (rs.next() && "BOOKED".equals(rs.getString("status"))) {
                            conn.rollback();
                            System.err.println("[BookingDAO] Seat " + seat.getSeatNumber() + " already booked.");
                            return false;
                        }
                    }
                }

                int bookingId;
                String joinedSeats = seats.stream()
                    .map(Seat::getSeatNumber)
                    .sorted()
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("");

                try (PreparedStatement bookStmt = conn.prepareStatement(insertBookingSQL, Statement.RETURN_GENERATED_KEYS)) {
                    bookStmt.setInt(1, userId);
                    bookStmt.setInt(2, showId);
                    bookStmt.setDouble(3, totalAmount);
                    bookStmt.setString(4, show.getMovieTitle());
                    bookStmt.setDate(5, show.getShowDate());
                    bookStmt.setTime(6, show.getShowTime());
                    bookStmt.setString(7, show.getScreen());
                    bookStmt.setString(8, joinedSeats);
                    bookStmt.executeUpdate();

                    ResultSet keys = bookStmt.getGeneratedKeys();
                    if (!keys.next()) {
                        conn.rollback();
                        return false;
                    }
                    bookingId = keys.getInt(1);
                }

                try (PreparedStatement bsStmt = conn.prepareStatement(insertBkSeatSQL);
                     PreparedStatement updSeat = conn.prepareStatement(updateSeatSQL)) {
                    for (Seat seat : seats) {
                        bsStmt.setInt(1, bookingId);
                        bsStmt.setInt(2, seat.getSeatId());
                        bsStmt.addBatch();

                        updSeat.setInt(1, seat.getSeatId());
                        updSeat.addBatch();
                    }
                    bsStmt.executeBatch();
                    updSeat.executeBatch();
                }

                payment.makePayment(totalAmount);
                PaymentDAO.recordPayment(conn, bookingId, totalAmount, payment);
                conn.commit();
                System.out.println("[BookingDAO] Booking committed. ID: " + bookingId);
                return true;

            } catch (SQLException ex) {
                conn.rollback();
                System.err.println("[BookingDAO] Rolled back: " + ex.getMessage());
            }
        } catch (SQLException e) {
            System.err.println("[BookingDAO] Connection failed: " + e.getMessage());
        }
        return false;
    }

    public static boolean cancelBooking(int bookingId, int userId) {
        String updateBookingSQL = "UPDATE Bookings SET status = 'CANCELLED' WHERE booking_id = ? AND user_id = ? AND status <> 'CANCELLED'";
        String updateSeatsSQL = "UPDATE Seats SET status = 'AVAILABLE' WHERE seat_id IN (SELECT seat_id FROM BookingSeats WHERE booking_id = ?)";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                System.err.println("[BookingDAO] Cancel failed: database connection is unavailable.");
                return false;
            }
            conn.setAutoCommit(false);
            try (PreparedStatement bookingStmt = conn.prepareStatement(updateBookingSQL);
                 PreparedStatement seatStmt = conn.prepareStatement(updateSeatsSQL)) {
                bookingStmt.setInt(1, bookingId);
                bookingStmt.setInt(2, userId);
                int updated = bookingStmt.executeUpdate();
                if (updated == 0) {
                    conn.rollback();
                    return false;
                }
                seatStmt.setInt(1, bookingId);
                seatStmt.executeUpdate();
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                System.err.println("[BookingDAO] Cancel booking failed: " + e.getMessage());
                return false;
            }
        } catch (SQLException e) {
            System.err.println("[BookingDAO] Connection failed while cancelling booking: " + e.getMessage());
            return false;
        }
    }

    public static void syncBookingHistoryForShow(Connection conn, int showId, Date showDate, Time showTime, String screen) throws SQLException {
        ensureBookingHistoryColumns(conn);
        String sql = "UPDATE Bookings SET show_date = ?, show_time = ?, screen = ?, history_note = CASE " +
            "WHEN history_note IS NULL OR history_note = '' THEN '[Note: Show timings were modified by admin]' " +
            "ELSE CONCAT(history_note, ' [Note: Show timings were modified by admin]') END " +
            "WHERE show_id = ? AND status <> 'CANCELLED'";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDate(1, showDate);
            pstmt.setTime(2, showTime);
            pstmt.setString(3, screen);
            pstmt.setInt(4, showId);
            pstmt.executeUpdate();
        }
    }

    public static void syncBookingHistoryForMovie(Connection conn, int movieId, String movieTitle) throws SQLException {
        ensureBookingHistoryColumns(conn);
        String showSql = "SELECT show_id FROM Shows WHERE movie_id = ?";
        try (PreparedStatement showStmt = conn.prepareStatement(showSql)) {
            showStmt.setInt(1, movieId);
            ResultSet rs = showStmt.executeQuery();
            while (rs.next()) {
                int showId = rs.getInt("show_id");
                String sql = "UPDATE Bookings SET movie_title = ?, history_note = CASE " +
                    "WHEN history_note IS NULL OR history_note = '' THEN '[Movie Updated]' " +
                    "ELSE CONCAT(history_note, ' [Movie Updated]') END " +
                    "WHERE show_id = ? AND status <> 'CANCELLED'";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, movieTitle);
                    pstmt.setInt(2, showId);
                    pstmt.executeUpdate();
                }
            }
        }
    }

    private static void ensureBookingHistoryColumns(Connection conn) throws SQLException {
        String[] columns = {
            "movie_title VARCHAR(100) NULL",
            "show_date DATE NULL",
            "show_time TIME NULL",
            "screen VARCHAR(50) NULL",
            "seat_numbers VARCHAR(255) NULL",
            "history_note VARCHAR(255) NULL"
        };

        for (String column : columns) {
            String colName = column.split(" ")[0];
            boolean exists = false;
            try (ResultSet rs = conn.getMetaData().getColumns(null, null, "Bookings", colName)) {
                exists = rs.next();
            }
            if (!exists) {
                String alterSql = "ALTER TABLE Bookings ADD COLUMN " + column;
                try (PreparedStatement stmt = conn.prepareStatement(alterSql)) {
                    stmt.executeUpdate();
                }
            }
        }
    }

    public static List<Booking> getBookingsByUser(int userId) {
        return fetchBookings("WHERE b.user_id = ?", userId);
    }

    public static List<Booking> getAllBookings() {
        return fetchBookings("", -1);
    }

    private static List<Booking> fetchBookings(String whereClause, int param) {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT b.booking_id, b.user_id, b.show_id, b.booking_date, " +
                     "b.total_amount, b.status, " +
                     "COALESCE(b.movie_title, m.title) AS movie_title, " +
                     "COALESCE(b.show_date, s.show_date) AS show_date, " +
                     "COALESCE(b.show_time, s.show_time) AS show_time, " +
                     "COALESCE(b.screen, s.screen) AS screen, " +
                     "COALESCE(b.seat_numbers, GROUP_CONCAT(st.seat_number ORDER BY st.seat_number SEPARATOR ', ')) AS seats, " +
                     "b.history_note AS history_note " +
                     "FROM Bookings b " +
                     "LEFT JOIN Shows s        ON b.show_id = s.show_id " +
                     "LEFT JOIN Movies m       ON s.movie_id = m.movie_id " +
                     "LEFT JOIN BookingSeats bs ON b.booking_id = bs.booking_id " +
                     "LEFT JOIN Seats st       ON bs.seat_id = st.seat_id " +
                     whereClause +
                     " GROUP BY b.booking_id ORDER BY b.booking_date DESC";

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                System.err.println("[BookingDAO] fetchBookings failed: database connection is unavailable.");
                return bookings;
            }

            ensureBookingHistoryColumns(conn);

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                if (param != -1) pstmt.setInt(1, param);
                ResultSet rs = pstmt.executeQuery();

                while (rs.next()) {
                    Booking b = new Booking(
                        rs.getInt("booking_id"), rs.getInt("user_id"), rs.getInt("show_id"),
                        rs.getTimestamp("booking_date"), rs.getDouble("total_amount"), rs.getString("status")
                    );
                    b.setDisplayDetails(
                        rs.getString("movie_title"),
                        rs.getDate("show_date") != null ? rs.getDate("show_date").toString() : "N/A",
                        rs.getTime("show_time") != null ? rs.getTime("show_time").toString() : "N/A",
                        rs.getString("screen") != null ? rs.getString("screen") : "N/A",
                        rs.getString("seats") != null ? rs.getString("seats") : ""
                    );
                    b.setHistoryNote(rs.getString("history_note"));
                    bookings.add(b);
                }
            }
        } catch (SQLException e) {
            System.err.println("[BookingDAO] fetchBookings failed: " + e.getMessage());
        }
        return bookings;
    }
}