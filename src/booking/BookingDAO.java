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
 * Handles the complete booking transaction following ACID properties:
 * 
 * 1. Atomicity: All sub-operations (seat check, booking insert, seat-booking mapping,
 *    seat status update, and payment record) execute as one indivisible unit of work.
 *    If any step fails, conn.rollback() cancels all previous database changes.
 * 
 * 2. Consistency: Database constraints (foreign keys, non-null fields, and seat availability)
 *    are strictly preserved. A booking is only committed if all chosen seats are valid and AVAILABLE.
 * 
 * 3. Isolation & Concurrency Control (Preventing Double-Booking):
 *    - Uses conn.setAutoCommit(false) to establish an explicit transaction boundary.
 *    - Executes "SELECT status FROM Seats WHERE seat_id = ? FOR UPDATE", applying a
 *      Pessimistic Row-Level Lock (exclusive X-lock in InnoDB).
 *    - If two users concurrently attempt to book the same seat, the second transaction is blocked
 *      until the first transaction completes. Once unlocked, the second transaction checks the dynamic
 *      status, detects 'BOOKED', triggers conn.rollback(), and safely returns false without collision.
 * 
 * 4. Durability: Upon conn.commit(), all confirmed bookings and seat statuses are permanently
 *    flushed to persistent storage in MySQL.
 */
public class BookingDAO {

    public static boolean bookTicket(int userId, int showId, List<Seat> seats,
                                     double totalAmount, Payment payment) {
        // PESSIMISTIC LOCKING: "FOR UPDATE" locks the matching rows against concurrent modifications.
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

            // [ACID - Isolation & Atomicity] Begin transaction boundary: disable auto-commit
            conn.setAutoCommit(false);
            try {
                // STEP 1: Verify each seat's status under row-level lock
                try (PreparedStatement checkStmt = conn.prepareStatement(checkSeatSQL)) {
                    for (Seat seat : seats) {
                        checkStmt.setInt(1, seat.getSeatId());
                        try (ResultSet rs = checkStmt.executeQuery()) {
                            if (rs.next() && "BOOKED".equalsIgnoreCase(rs.getString("status"))) {
                                // Conflict detected! Concurrent transaction already secured this seat.
                                conn.rollback();
                                System.err.println("[BookingDAO] Concurrency collision: Seat " + seat.getSeatNumber() + " is already booked.");
                                return false;
                            }
                        }
                    }
                }

                // STEP 2: Insert master Booking record
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

                    try (ResultSet keys = bookStmt.getGeneratedKeys()) {
                        if (!keys.next()) {
                            conn.rollback();
                            return false;
                        }
                        bookingId = keys.getInt(1);
                    }
                }

                // STEP 3: Batch insert BookingSeats mapping & batch update Seats status to 'BOOKED'
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

                // STEP 4: Process payment polymorphically and record transaction
                payment.makePayment(totalAmount);
                PaymentDAO.recordPayment(conn, bookingId, totalAmount, payment);

                // STEP 5: [ACID - Durability] Atomically commit the entire transaction
                conn.commit();
                System.out.println("[BookingDAO] Booking committed successfully. ID: " + bookingId);
                return true;

            } catch (SQLException ex) {
                // [ACID - Atomicity] Any failure causes full rollback to ensure zero partial bookings
                conn.rollback();
                System.err.println("[BookingDAO] Transaction rolled back due to error: " + ex.getMessage());
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
            try (ResultSet rs = showStmt.executeQuery()) {
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
                try (ResultSet rs = pstmt.executeQuery()) {
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
            }
        } catch (SQLException e) {
            System.err.println("[BookingDAO] fetchBookings failed: " + e.getMessage());
        }
        return bookings;
    }
}