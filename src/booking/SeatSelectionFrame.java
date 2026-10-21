package booking;

import model.Customer;
import model.Seat;
import model.Show;
import payment.PaymentFrame;
import shows.ShowFrame;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Seat selection grid for a specific show.
 * Green = available, Orange = selected, Red = booked.
 */
public class SeatSelectionFrame extends JFrame {
    private final List<Seat> selectedSeats = new ArrayList<>();

    public SeatSelectionFrame(Customer customer, Show show) {
        setTitle("Select Seats — " + show.getMovieTitle());
        setSize(780, 660);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        if (SeatDAO.isShowFull(show.getShowId())) {
            JOptionPane.showMessageDialog(this, "HOUSEFULL / SHOW FULL. No seats available.", "Booking Unavailable", JOptionPane.WARNING_MESSAGE);
            dispose();
            return;
        }

        List<Seat> seats = SeatDAO.getSeatsByShow(show.getShowId());

        JLabel screenLabel = new JLabel("[ SCREEN ]", SwingConstants.CENTER);
        screenLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        screenLabel.setOpaque(true);
        screenLabel.setBackground(Color.LIGHT_GRAY);
        screenLabel.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));

        JPanel seatPanel = new JPanel(new GridLayout(10, 10, 6, 6));
        seatPanel.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        for (Seat seat : seats) {
            JButton btn = new JButton(seat.getSeatNumber());
            btn.setFont(new Font("SansSerif", Font.BOLD, 10));
            btn.setPreferredSize(new Dimension(50, 34));
            btn.setMargin(new Insets(2, 2, 2, 2));

            if (!seat.isAvailable()) {
                btn.setBackground(new Color(220, 53, 69));
                btn.setForeground(Color.WHITE);
                btn.setEnabled(false);
                btn.setToolTipText("Booked");
            } else {
                btn.setBackground(new Color(40, 167, 69));
                btn.setForeground(Color.WHITE);
                btn.addActionListener(e -> {
                    if (selectedSeats.contains(seat)) {
                        selectedSeats.remove(seat);
                        btn.setBackground(new Color(40, 167, 69));
                        btn.setForeground(Color.WHITE);
                    } else {
                        selectedSeats.add(seat);
                        btn.setBackground(new Color(255, 153, 0));
                        btn.setForeground(Color.BLACK);
                    }
                });
            }
            seatPanel.add(btn);
        }

        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        legend.add(colorBox(new Color(40, 167, 69))); legend.add(new JLabel("Available"));
        legend.add(colorBox(new Color(255, 153, 0))); legend.add(new JLabel("Selected"));
        legend.add(colorBox(new Color(220, 53, 69))); legend.add(new JLabel("Booked"));

        JButton proceedBtn = new JButton("Proceed to Payment");
        proceedBtn.addActionListener(e -> {
            if (selectedSeats.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please select at least one seat before proceeding to payment.", "No Seats Selected", JOptionPane.WARNING_MESSAGE);
                return;
            }
            new PaymentFrame(customer, show, new ArrayList<>(selectedSeats)).setVisible(true);
            dispose();
        });

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> { new ShowFrame(customer).setVisible(true); dispose(); });

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(proceedBtn);
        bottom.add(cancelBtn);

        setLayout(new BorderLayout(6, 6));
        add(screenLabel, BorderLayout.NORTH);
        add(new JScrollPane(seatPanel), BorderLayout.CENTER);
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(legend, BorderLayout.WEST);
        southPanel.add(bottom, BorderLayout.EAST);
        add(southPanel, BorderLayout.SOUTH);
    }

    private JLabel colorBox(Color color) {
        JLabel lbl = new JLabel("  ");
        lbl.setOpaque(true);
        lbl.setBackground(color);
        lbl.setPreferredSize(new Dimension(18, 18));
        return lbl;
    }
}
