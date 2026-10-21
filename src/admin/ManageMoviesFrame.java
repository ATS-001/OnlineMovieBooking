package admin;

import model.Admin;
import model.Movie;
import movies.MovieDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Date;


/**
 * Admin screen for full CRUD on Movies table.
 */
public class ManageMoviesFrame extends JFrame {
    private final Admin             admin;
    private final DefaultTableModel tableModel;

    public ManageMoviesFrame(Admin admin) {
        this.admin = admin;
        setTitle("Manage Movies");
        setSize(680, 420);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        String[] cols = {"ID", "Title", "Genre", "Language", "Duration"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        refreshTable();

        // Buttons
        JButton addBtn    = new JButton("Add Movie");
        JButton updateBtn = new JButton("Update Movie");
        JButton deleteBtn = new JButton("Delete Movie");
        JButton refreshBtn= new JButton("Refresh");
        JButton backBtn   = new JButton("Back");

        addBtn.addActionListener(e -> addMovieDialog());
        updateBtn.addActionListener(e -> updateMovieDialog(table));
        deleteBtn.addActionListener(e -> deleteMovie(table));
        refreshBtn.addActionListener(e -> refreshTable());
        backBtn.addActionListener(e -> { new AdminDashboard(this.admin).setVisible(true); dispose(); });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        btnPanel.add(addBtn);
        btnPanel.add(updateBtn);
        btnPanel.add(deleteBtn);
        btnPanel.add(refreshBtn);
        btnPanel.add(backBtn);

        setLayout(new BorderLayout(4, 4));
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        for (Movie m : MovieDAO.getAllMovies()) {
            tableModel.addRow(new Object[]{
                m.getMovieId(), m.getTitle(), m.getGenre(), m.getLanguage(), m.getDuration()
            });
        }
    }

    private void addMovieDialog() {
        JTextField titleF  = new JTextField(16);
        JTextField genreF  = new JTextField(12);
        JTextField langF   = new JTextField(10);
        JTextField descF   = new JTextField(20);
        JTextField dateF   = new JTextField(12);
        JTextField durF    = new JTextField(6);

        JPanel p = new JPanel(new GridLayout(0, 2, 6, 6));
        p.add(new JLabel("Title:"));        p.add(titleF);
        p.add(new JLabel("Genre:"));        p.add(genreF);
        p.add(new JLabel("Language:"));     p.add(langF);
        p.add(new JLabel("Description:"));  p.add(descF);
        p.add(new JLabel("Release Date (YYYY-MM-DD):")); p.add(dateF);
        p.add(new JLabel("Duration (mins):")); p.add(durF);

        int res = JOptionPane.showConfirmDialog(this, p, "Add Movie", JOptionPane.OK_CANCEL_OPTION);
        if (res != JOptionPane.OK_OPTION) return;
        try {
            int dur = Integer.parseInt(durF.getText().trim());
            Date rd = Date.valueOf(dateF.getText().trim());
            Movie m = new Movie(0, titleF.getText().trim(), genreF.getText().trim(),
                langF.getText().trim(), descF.getText().trim(), rd, dur);
            if (MovieDAO.addMovie(m)) { refreshTable(); JOptionPane.showMessageDialog(this, "Movie added."); }
            else JOptionPane.showMessageDialog(this, "Failed to add movie.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid input: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateMovieDialog(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a movie to update."); return; }
        int    id    = (int)    tableModel.getValueAt(row, 0);
        String title = (String) tableModel.getValueAt(row, 1);
        String genre = (String) tableModel.getValueAt(row, 2);
        String lang  = (String) tableModel.getValueAt(row, 3);
        int    dur   = (int)    tableModel.getValueAt(row, 4);

        JTextField titleF = new JTextField(title, 16);
        JTextField genreF = new JTextField(genre, 12);
        JTextField langF  = new JTextField(lang,  10);
        JTextField descF  = new JTextField(14);
        JTextField durF   = new JTextField(String.valueOf(dur), 6);

        JPanel p = new JPanel(new GridLayout(0, 2, 6, 6));
        p.add(new JLabel("Title:"));        p.add(titleF);
        p.add(new JLabel("Genre:"));        p.add(genreF);
        p.add(new JLabel("Language:"));     p.add(langF);
        p.add(new JLabel("Description:"));  p.add(descF);
        p.add(new JLabel("Duration (mins):")); p.add(durF);

        int res = JOptionPane.showConfirmDialog(this, p, "Update Movie", JOptionPane.OK_CANCEL_OPTION);
        if (res != JOptionPane.OK_OPTION) return;
        try {
            if (MovieDAO.updateMovie(id, titleF.getText().trim(), genreF.getText().trim(),
                    langF.getText().trim(), descF.getText().trim(),
                    Integer.parseInt(durF.getText().trim()))) {
                refreshTable();
                JOptionPane.showMessageDialog(this, "Movie updated.");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid input: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteMovie(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a movie to delete."); return; }
        int id = (int) tableModel.getValueAt(row, 0);
        int res = JOptionPane.showConfirmDialog(this,
            "Delete movie ID " + id + "? This will also delete related shows and bookings.",
            "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (res == JOptionPane.YES_OPTION) {
            if (MovieDAO.deleteMovie(id)) { refreshTable(); JOptionPane.showMessageDialog(this, "Movie deleted."); }
            else JOptionPane.showMessageDialog(this, "Delete failed.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
