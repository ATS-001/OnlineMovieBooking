package admin;

import model.Admin;
import model.Show;
import shows.ShowDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Date;
import java.sql.Time;


/**
 * Admin screen for managing Shows (Add / Delete / Refresh).
 */
public class ManageShowsFrame extends JFrame {
    private final Admin             admin;
    private final DefaultTableModel tableModel;

    public ManageShowsFrame(Admin admin) {
        this.admin = admin;
        setTitle("Manage Shows");
        setSize(780, 420);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        String[] cols = {"Show ID", "Movie", "Date", "Time", "Screen", "Price (Rs)"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        refreshTable();

        JButton addBtn     = new JButton("Add Show");
        JButton updateBtn  = new JButton("Update Show");
        JButton deleteBtn  = new JButton("Delete Show");
        JButton refreshBtn = new JButton("Refresh");
        JButton backBtn    = new JButton("Back");

        addBtn.addActionListener(e    -> addShowDialog());
        updateBtn.addActionListener(e -> updateShowDialog(table));
        deleteBtn.addActionListener(e -> deleteShow(table));
        refreshBtn.addActionListener(e-> refreshTable());
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
        for (Show s : ShowDAO.getAllShows()) {
            tableModel.addRow(new Object[]{
                s.getShowId(), s.getMovieTitle(), s.getShowDate(),
                s.getShowTime(), s.getScreen(), String.format("%.2f", s.getTicketPrice())
            });
        }
    }

    private void addShowDialog() {
        JTextField midF    = new JTextField(6);
        JTextField dateF   = new JTextField("YYYY-MM-DD", 12);
        JTextField timeF   = new JTextField("HH:MM:SS",   10);
        JTextField screenF = new JTextField(8);
        JTextField priceF  = new JTextField(8);

        JPanel p = new JPanel(new GridLayout(0, 2, 6, 6));
        p.add(new JLabel("Movie ID:"));      p.add(midF);
        p.add(new JLabel("Show Date:"));     p.add(dateF);
        p.add(new JLabel("Show Time:"));     p.add(timeF);
        p.add(new JLabel("Screen:"));        p.add(screenF);
        p.add(new JLabel("Ticket Price:")); p.add(priceF);

        int res = JOptionPane.showConfirmDialog(this, p, "Add Show", JOptionPane.OK_CANCEL_OPTION);
        if (res != JOptionPane.OK_OPTION) return;
        try {
            int    mid    = Integer.parseInt(midF.getText().trim());
            Date   date   = Date.valueOf(dateF.getText().trim());
            Time   time   = Time.valueOf(timeF.getText().trim());
            String screen = screenF.getText().trim();
            double price  = Double.parseDouble(priceF.getText().trim());

            if (ShowDAO.addShow(mid, date, time, screen, price)) {
                refreshTable();
                JOptionPane.showMessageDialog(this, "Show added and 100 seats generated.");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add show. Check Movie ID.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid input: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteShow(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a show to delete."); return; }
        int id  = (int) tableModel.getValueAt(row, 0);
        int res = JOptionPane.showConfirmDialog(this,
            "Delete Show ID " + id + "? All seats and bookings for this show will also be deleted.",
            "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (res == JOptionPane.YES_OPTION) {
            if (ShowDAO.deleteShow(id)) { refreshTable(); JOptionPane.showMessageDialog(this, "Show deleted."); }
            else JOptionPane.showMessageDialog(this, "Delete failed.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateShowDialog(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a show to update.");
            return;
        }

        int showId = (int) tableModel.getValueAt(row, 0);
        Show show = ShowDAO.getShowById(showId);
        if (show == null) {
            JOptionPane.showMessageDialog(this, "Unable to load the selected show.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JTextField dateF = new JTextField(show.getShowDate().toString(), 12);
        JTextField timeF = new JTextField(show.getShowTime().toString(), 10);
        JTextField screenF = new JTextField(show.getScreen(), 8);
        JTextField priceF = new JTextField(String.valueOf(show.getTicketPrice()), 8);

        JPanel panel = new JPanel(new GridLayout(0, 2, 6, 6));
        panel.add(new JLabel("Show Date:"));
        panel.add(dateF);
        panel.add(new JLabel("Show Time:"));
        panel.add(timeF);
        panel.add(new JLabel("Screen:"));
        panel.add(screenF);
        panel.add(new JLabel("Ticket Price:"));
        panel.add(priceF);

        int result = JOptionPane.showConfirmDialog(this, panel, "Update Show " + showId,
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            Date date = Date.valueOf(dateF.getText().trim());
            Time time = Time.valueOf(timeF.getText().trim());
            String screen = screenF.getText().trim();
            double price = Double.parseDouble(priceF.getText().trim());
            if (screen.isEmpty() || price < 0) {
                throw new IllegalArgumentException("Screen must not be empty and price cannot be negative.");
            }

            if (ShowDAO.updateShow(showId, date, time, screen, price)) {
                refreshTable();
                JOptionPane.showMessageDialog(this, "Show updated successfully.");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update show.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, "Invalid input: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
