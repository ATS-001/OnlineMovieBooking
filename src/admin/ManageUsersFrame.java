package admin;

import auth.UserDAO;
import auth.LoginFrame;
import model.Admin;
import model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Admin screen to manage registered users.
 */
public class ManageUsersFrame extends JFrame {

    public ManageUsersFrame(Admin admin) {
        setTitle("Manage Users");
        setSize(900, 420);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        String[] cols = {"User ID", "Name", "Email", "Role", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        List<User> users = UserDAO.getAllUsers();
        for (User user : users) {
            model.addRow(new Object[] {
                user.getUserId(), user.getName(), user.getEmail(), user.getRole(),
                "BANNED".equalsIgnoreCase(user.getRole()) ? "BANNED" : "ACTIVE"
            });
        }

        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JButton banBtn = new JButton("Ban User");
        banBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Select a user to ban.");
                return;
            }
            int userId = (int) model.getValueAt(row, 0);
            String role = (String) model.getValueAt(row, 3);
            if ("BANNED".equalsIgnoreCase(role)) {
                JOptionPane.showMessageDialog(this, "User is already banned.");
                return;
            }
            boolean success = UserDAO.banUser(userId);
            if (success) {
                JOptionPane.showMessageDialog(this, "User banned successfully.");
                refreshUsers(model);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to ban user.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton deleteBtn = new JButton("Delete User");
        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Select a user to delete.");
                return;
            }
            int userId = (int) model.getValueAt(row, 0);
            boolean deletingSelf = userId == admin.getUserId();
            if (!deletingSelf && UserDAO.hasActiveBookings(userId)) {
                JOptionPane.showMessageDialog(this,
                    "This user has active bookings. Cancel or resolve them before deleting the account.",
                    "Unable to Delete",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            String message = deletingSelf
                ? "Delete your admin account? Your booking history will also be permanently deleted, and you will be logged out."
                : "Delete selected user? This action cannot be undone.";
            int confirm = JOptionPane.showConfirmDialog(this,
                message,
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                boolean success = UserDAO.deleteUser(userId);
                if (success) {
                    if (deletingSelf) {
                        JOptionPane.showMessageDialog(this, "Your admin account has been deleted.");
                        new LoginFrame().setVisible(true);
                        dispose();
                        return;
                    }
                    JOptionPane.showMessageDialog(this, "User deleted successfully.");
                    refreshUsers(model);
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete user.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JButton backBtn = new JButton("Back");
        backBtn.addActionListener(e -> { new AdminDashboard(admin).setVisible(true); dispose(); });

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(banBtn);
        bottom.add(deleteBtn);
        bottom.add(backBtn);

        setLayout(new BorderLayout(4, 4));
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }

    private void refreshUsers(DefaultTableModel model) {
        model.setRowCount(0);
        List<User> users = UserDAO.getAllUsers();
        for (User user : users) {
            model.addRow(new Object[] {
                user.getUserId(), user.getName(), user.getEmail(), user.getRole(),
                "BANNED".equalsIgnoreCase(user.getRole()) ? "BANNED" : "ACTIVE"
            });
        }
    }
}
