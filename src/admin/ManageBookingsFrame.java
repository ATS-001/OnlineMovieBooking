package admin;

import booking.BookingDAO;
import model.Admin;
import model.Booking;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Admin screen — view all customer bookings in a table.
 */
public class ManageBookingsFrame extends JFrame {

    public ManageBookingsFrame(Admin admin) {
        setTitle("All Bookings");
        setSize(860, 420);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        String[] cols = {"Booking ID", "User ID", "Movie", "Date", "Time", "Seats", "Amount (Rs)", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        List<Booking> bookings = BookingDAO.getAllBookings();
        for (Booking b : bookings) {
            model.addRow(new Object[]{
                b.getBookingId(), b.getUserId(), b.getMovieTitle(),
                b.getShowDate(), b.getShowTime(), b.getSeats(),
                String.format("%.2f", b.getTotalAmount()), b.getStatus()
            });
        }

        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);

        JButton backBtn = new JButton("Back");
        backBtn.addActionListener(e -> { new AdminDashboard(admin).setVisible(true); dispose(); });

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(backBtn);

        setLayout(new BorderLayout(4, 4));
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }
}
