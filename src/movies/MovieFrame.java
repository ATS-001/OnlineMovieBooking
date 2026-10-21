package movies;

import customer.CustomerDashboard;
import model.Customer;
import model.Movie;
import shows.ShowFrame;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Movie browsing screen for customers.
 * Lists all movies, allows search, and navigates to shows for a selected movie.
 */
public class MovieFrame extends JFrame {
    private final Customer customer;
    private final DefaultTableModel tableModel;
    private final JTable table;

    public MovieFrame(Customer customer) {
        this.customer = customer;
        setTitle("Browse Movies");
        setSize(640, 420);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // Table
        String[] cols = {"ID", "Title", "Genre", "Language", "Duration (mins)"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        loadMovies(MovieDAO.getAllMovies());

        // Search bar
        JTextField searchField = new JTextField(14);
        JButton searchBtn = new JButton("Search");
        searchBtn.addActionListener(e -> {
            String kw = searchField.getText().trim();
            List<Movie> list = kw.isEmpty() ? MovieDAO.getAllMovies() : MovieDAO.searchMovies(kw);
            if (list.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No movies found matching \"" + kw + "\".", "Search Results", JOptionPane.INFORMATION_MESSAGE);
            }
            loadMovies(list);
        });
        JButton clearBtn = new JButton("Clear");
        clearBtn.addActionListener(e -> { searchField.setText(""); loadMovies(MovieDAO.getAllMovies()); });

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(searchBtn);
        searchPanel.add(clearBtn);

        // Bottom buttons
        JButton viewShowsBtn = new JButton("View Shows for Selected Movie");
        viewShowsBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Please select a movie from the table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int movieId = (int) tableModel.getValueAt(row, 0);
            new ShowFrame(this.customer, movieId).setVisible(true);
            dispose();
        });

        JButton backBtn = new JButton("Back");
        backBtn.addActionListener(e -> { new CustomerDashboard(this.customer).setVisible(true); dispose(); });

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(viewShowsBtn);
        bottomPanel.add(backBtn);

        setLayout(new BorderLayout(4, 4));
        add(searchPanel, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void loadMovies(List<Movie> movies) {
        tableModel.setRowCount(0);
        for (Movie m : movies) {
            tableModel.addRow(new Object[]{
                m.getMovieId(), m.getTitle(), m.getGenre(), m.getLanguage(), m.getDuration()
            });
        }
    }
}
