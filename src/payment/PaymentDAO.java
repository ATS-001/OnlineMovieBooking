package payment;



import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Handles recording payment records into the Payments table.
 * Called inside a transaction started by BookingDAO.
 */
public class PaymentDAO {

    /**
     * Inserts a payment record using the shared transactional connection.
     * @param conn        Shared connection (must already be in a transaction)
     * @param bookingId   The booking this payment belongs to
     * @param amount      Amount paid
     * @param payment     Payment object (UPI or Card) — polymorphism
     */
    public static boolean recordPayment(Connection conn, int bookingId,
                                        double amount, Payment payment) throws SQLException {
        String sql = "INSERT INTO Payments (booking_id, amount, payment_method, payment_status) " +
                     "VALUES (?, ?, ?, 'SUCCESS')";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, bookingId);
            pstmt.setDouble(2, amount);
            pstmt.setString(3, payment.getMethodName());
            return pstmt.executeUpdate() > 0;
        }
    }
}