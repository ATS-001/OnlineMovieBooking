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

        String title = titleF.getText().trim();
        String genre = genreF.getText().trim();
        String lang  = langF.getText().trim();
        String desc  = descF.getText().trim();
        String dateStr = dateF.getText().trim();
        String durStr  = durF.getText().trim();

        if (title.isEmpty() || genre.isEmpty() || lang.isEmpty() || dateStr.isEmpty() || durStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all movie fields (Title, Genre, Language, Date, Duration).", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int dur;
        try {
            dur = Integer.parseInt(durStr);
            if (dur <= 0) {
                JOptionPane.showMessageDialog(this, "Duration must be a positive integer (e.g. 120 minutes).", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Duration must be a valid integer number of minutes.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Date rd;
        try {
            rd = Date.valueOf(dateStr);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, "Release Date must follow format YYYY-MM-DD (e.g. 2026-09-20).", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Movie m = new Movie(0, title, genre, lang, desc, rd, dur);
            if (MovieDAO.addMovie(m)) {
                refreshTable();
                JOptionPane.showMessageDialog(this, "Movie \"" + title + "\" added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add movie to database.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "An unexpected error occurred: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateMovieDialog(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a movie from the table to update.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
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

        int res = JOptionPane.showConfirmDialog(this, p, "Update Movie ID " + id, JOptionPane.OK_CANCEL_OPTION);
        if (res != JOptionPane.OK_OPTION) return;

        String newTitle = titleF.getText().trim();
        String newGenre = genreF.getText().trim();
        String newLang  = langF.getText().trim();
        String newDesc  = descF.getText().trim();
        String durInput = durF.getText().trim();

        if (newTitle.isEmpty() || newGenre.isEmpty() || newLang.isEmpty() || durInput.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Title, Genre, Language, and Duration cannot be left empty.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int newDur;
        try {
            newDur = Integer.parseInt(durInput);
            if (newDur <= 0) {
                JOptionPane.showMessageDialog(this, "Duration must be a positive number of minutes.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Duration must be a valid integer number.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            if (MovieDAO.updateMovie(id, newTitle, newGenre, newLang, newDesc, newDur)) {
                refreshTable();
                JOptionPane.showMessageDialog(this, "Movie ID " + id + " updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update movie.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "An unexpected error occurred: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteMovie(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a movie from the table to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        int res = JOptionPane.showConfirmDialog(this,
            "Delete movie ID " + id + "? This will also cascade and delete all associated shows and bookings.",
            "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (res == JOptionPane.YES_OPTION) {
            try {
                if (MovieDAO.deleteMovie(id)) {
                    refreshTable();
                    JOptionPane.showMessageDialog(this, "Movie deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Delete failed. Movie may not exist.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "An error occurred during deletion: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
