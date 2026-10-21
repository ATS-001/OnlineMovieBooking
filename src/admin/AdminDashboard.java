package admin;

import auth.LoginFrame;
import model.Admin;

import javax.swing.*;
import java.awt.*;

/**
 * Admin home screen shown after admin login.
 */
public class AdminDashboard extends JFrame {

    public AdminDashboard(Admin admin) {
        setTitle("Admin Dashboard — " + admin.getName());
        setSize(450, 380);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JLabel welcome = new JLabel("Admin: " + admin.getName(), SwingConstants.CENTER);
        welcome.setFont(new Font("SansSerif", Font.BOLD, 15));
        welcome.setBorder(BorderFactory.createEmptyBorder(14, 0, 10, 0));

        JPanel btnPanel = new JPanel(new GridLayout(5, 1, 8, 8));
        btnPanel.setBorder(BorderFactory.createEmptyBorder(10, 40, 20, 40));

        JButton moviesBtn   = new JButton("Manage Movies");
        JButton showsBtn    = new JButton("Manage Shows");
        JButton bookingsBtn = new JButton("View All Bookings");
        JButton usersBtn    = new JButton("Manage Users");
        JButton logoutBtn   = new JButton("Logout");

        moviesBtn.addActionListener(e   -> { new ManageMoviesFrame(admin).setVisible(true); dispose(); });
        showsBtn.addActionListener(e    -> { new ManageShowsFrame(admin).setVisible(true); dispose(); });
        bookingsBtn.addActionListener(e -> { new ManageBookingsFrame(admin).setVisible(true); dispose(); });
        usersBtn.addActionListener(e    -> { new ManageUsersFrame(admin).setVisible(true); dispose(); });
        logoutBtn.addActionListener(e   -> { new LoginFrame().setVisible(true); dispose(); });

        btnPanel.add(moviesBtn);
        btnPanel.add(showsBtn);
        btnPanel.add(bookingsBtn);
        btnPanel.add(usersBtn);
        btnPanel.add(logoutBtn);

        setLayout(new BorderLayout());
        add(welcome, BorderLayout.NORTH);
        add(btnPanel, BorderLayout.CENTER);
    }
}
