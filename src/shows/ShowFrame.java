package shows;

import booking.SeatDAO;
import booking.SeatSelectionFrame;
import customer.CustomerDashboard;
import model.Customer;
import model.Show;
import movies.MovieFrame;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Show listing screen for customers.
 * Can display all shows or shows filtered by movie.
 */
public class ShowFrame extends JFrame {
    private final Customer customer;
    private final int      movieId;

    public ShowFrame(Customer customer) {
        this(customer, -1);
    }

    public ShowFrame(Customer customer, int selectedMovieId) {
        this.customer = customer;
        this.movieId = selectedMovieId;

        setTitle(this.movieId == -1 ? "All Shows" : "Shows for Selected Movie");
        setSize(820, 460);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        List<Show> shows = (this.movieId == -1) ? ShowDAO.getAllShows() : ShowDAO.getShowsByMovieId(this.movieId);

        String[] cols = {"Show ID", "Movie", "Date", "Time", "Screen", "Price (Rs)", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        if (shows.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No scheduled shows found for the selected view.", "No Shows Available", JOptionPane.INFORMATION_MESSAGE);
        }

        for (Show s : shows) {
            boolean full = SeatDAO.isShowFull(s.getShowId());
            String status = full ? "HOUSEFULL / SHOW FULL" : "Open";
            model.addRow(new Object[] {
                s.getShowId(), s.getMovieTitle(), s.getShowDate(),
                s.getShowTime(), s.getScreen(), String.format("%.2f", s.getTicketPrice()), status
            });
        }

        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JButton bookBtn = new JButton("Select Seats for This Show");
        bookBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Please select a show from the table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int showId = (int) model.getValueAt(row, 0);
            Show show = ShowDAO.getShowById(showId);
            if (show == null) {
                JOptionPane.showMessageDialog(this, "Unable to load details for the selected show.", "Show Not Found", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (SeatDAO.isShowFull(showId)) {
                JOptionPane.showMessageDialog(this, "HOUSEFULL / SHOW FULL. This show is fully booked.", "Booking Unavailable", JOptionPane.WARNING_MESSAGE);
                return;
            }
            new SeatSelectionFrame(this.customer, show).setVisible(true);
            dispose();
        });

        JButton backBtn = new JButton("Back");
        backBtn.addActionListener(e -> {
            if (movieId != -1) new MovieFrame(this.customer).setVisible(true);
            else new CustomerDashboard(this.customer).setVisible(true);
            dispose();
        });

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(bookBtn);
        bottom.add(backBtn);

        setLayout(new BorderLayout(4, 4));
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }
}
