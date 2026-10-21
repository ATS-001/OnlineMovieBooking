package payment;

import booking.BookingDAO;
import customer.CustomerDashboard;
import model.Customer;
import model.Seat;
import model.Show;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Payment and booking summary screen.
 * Uses polymorphism: Payment reference holds either UPIPayment or CardPayment.
 */
public class PaymentFrame extends JFrame {

    public PaymentFrame(Customer customer, Show show, List<Seat> seats) {
        setTitle("Payment — " + show.getMovieTitle());
        setSize(400, 380);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        double total = seats.size() * show.getTicketPrice();

        // Build seat list string
        StringBuilder sb = new StringBuilder();
        for (Seat s : seats) sb.append(s.getSeatNumber()).append("  ");

        // --- Summary panel ---
        JPanel summary = new JPanel(new GridLayout(0, 1, 4, 4));
        summary.setBorder(BorderFactory.createTitledBorder("Booking Summary"));
        summary.add(new JLabel("Movie    : " + show.getMovieTitle()));
        summary.add(new JLabel("Date     : " + show.getShowDate()));
        summary.add(new JLabel("Time     : " + show.getShowTime()));
        summary.add(new JLabel("Screen   : " + show.getScreen()));
        summary.add(new JLabel("Seats    : " + sb.toString().trim()));
        summary.add(new JLabel("Unit Price: Rs " + show.getTicketPrice()));
        summary.add(new JLabel("Total    : Rs " + String.format("%.2f", total)));

        // --- Payment method ---
        JPanel methodPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        methodPanel.setBorder(BorderFactory.createTitledBorder("Payment Method"));
        JRadioButton upiBtn  = new JRadioButton("UPI",  true);
        JRadioButton cardBtn = new JRadioButton("Card", false);
        ButtonGroup bg = new ButtonGroup();
        bg.add(upiBtn);
        bg.add(cardBtn);
        methodPanel.add(upiBtn);
        methodPanel.add(cardBtn);

        // --- Buttons ---
        JButton payBtn = new JButton("Pay & Confirm Booking");
        payBtn.setBackground(new Color(34, 139, 34));
        payBtn.setForeground(Color.WHITE);
        payBtn.addActionListener(e -> {
            Payment payment;
            if (upiBtn.isSelected()) {
                String id = JOptionPane.showInputDialog(this, "Enter UPI ID (e.g. user@okaxis):", "UPI Payment", JOptionPane.PLAIN_MESSAGE);
                if (id == null) return; // User pressed Cancel
                String cleanId = id.trim();
                if (cleanId.isEmpty() || !cleanId.contains("@")) {
                    JOptionPane.showMessageDialog(this, "Please enter a valid UPI ID (e.g. yourname@bank).", "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                payment = new UPIPayment(cleanId);
            } else {
                String num = JOptionPane.showInputDialog(this, "Enter 16-digit Card Number:", "Card Payment", JOptionPane.PLAIN_MESSAGE);
                if (num == null) return; // User pressed Cancel
                String cleanNum = num.trim().replaceAll("[\\s-]", "");
                if (cleanNum.length() != 16 || !cleanNum.matches("\\d+")) {
                    JOptionPane.showMessageDialog(this, "Please enter a valid 16-digit card number (numbers only).", "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                payment = new CardPayment(cleanNum);
            }

            try {
                boolean success = BookingDAO.bookTicket(
                    customer.getUserId(), show.getShowId(), seats, total, payment);

                if (success) {
                    JOptionPane.showMessageDialog(this,
                        "Booking Confirmed!\n\nMovie : " + show.getMovieTitle() +
                        "\nDate  : " + show.getShowDate() +
                        "\nTime  : " + show.getShowTime() +
                        "\nSeats : " + sb.toString().trim() +
                        "\nAmount: Rs " + String.format("%.2f", total) +
                        "\nMethod: " + payment.getMethodName(),
                        "Booking Confirmed", JOptionPane.INFORMATION_MESSAGE);
                    new CustomerDashboard(customer).setVisible(true);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Booking failed. One or more selected seats were concurrently booked by another user. Please re-select your seats.",
                        "Booking Collision", JOptionPane.WARNING_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                    "An unexpected error occurred while processing booking: " + ex.getMessage(),
                    "Booking Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> { new CustomerDashboard(customer).setVisible(true); dispose(); });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(payBtn);
        btnPanel.add(cancelBtn);

        setLayout(new BorderLayout(6, 6));
        add(summary, BorderLayout.NORTH);
        add(methodPanel, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);
    }
}
