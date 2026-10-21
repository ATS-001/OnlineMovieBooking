package customer;

import booking.BookingDAO;
import model.Booking;
import model.Customer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Shows the logged-in customer's booking history.
 */
public class BookingHistoryFrame extends JFrame {

    public BookingHistoryFrame(Customer customer) {
        setTitle("My Bookings — " + customer.getName());
        setSize(900, 420);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        String[] cols = {"Booking ID", "Movie", "Date", "Time", "Seats", "Amount (Rs)", "Status", "Note"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        List<Booking> bookings = BookingDAO.getBookingsByUser(customer.getUserId());
        if (bookings.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No bookings found.", "My Bookings", JOptionPane.INFORMATION_MESSAGE);
        }
        for (Booking b : bookings) {
            model.addRow(new Object[]{
                b.getBookingId(), b.getMovieTitle(), b.getShowDate(),
                b.getShowTime(), b.getSeats(),
                String.format("%.2f", b.getTotalAmount()), b.getStatus(),
                (b.getHistoryNote() == null || b.getHistoryNote().isBlank()) ? "" : b.getHistoryNote()
            });
        }

        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        JButton cancelBtn = new JButton("Cancel Booking");
        cancelBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Please select a booking from the table to cancel.", "Selection Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int bookingId = (int) model.getValueAt(row, 0);
            String status = (String) model.getValueAt(row, 6);
            if (!"CONFIRMED".equalsIgnoreCase(status)) {
                JOptionPane.showMessageDialog(this, "Only confirmed bookings can be cancelled.", "Action Not Allowed", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(this,
                "Cancel booking #" + bookingId + "? All reserved seats will be immediately released back to available.",
                "Confirm Cancellation", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    boolean success = BookingDAO.cancelBooking(bookingId, customer.getUserId());
                    if (success) {
                        JOptionPane.showMessageDialog(this, "Booking #" + bookingId + " cancelled successfully. Seats have been released.", "Success", JOptionPane.INFORMATION_MESSAGE);
                        dispose();
                        new BookingHistoryFrame(customer).setVisible(true);
                    } else {
                        JOptionPane.showMessageDialog(this, "Cancellation failed. The booking could not be cancelled.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "An error occurred during cancellation: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JButton backBtn = new JButton("Back");
        backBtn.addActionListener(e -> { new CustomerDashboard(customer).setVisible(true); dispose(); });

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(cancelBtn);
        bottom.add(backBtn);

        setLayout(new BorderLayout(4, 4));
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }
}
