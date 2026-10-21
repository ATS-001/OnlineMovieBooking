package customer;

import auth.LoginFrame;
import model.Customer;
import movies.MovieFrame;
import shows.ShowFrame;

import javax.swing.*;
import java.awt.*;

/**
 * Customer home screen shown after login.
 */
public class CustomerDashboard extends JFrame {
    private final Customer customer;

    public CustomerDashboard(Customer customer) {
        this.customer = customer;
        setTitle("Customer Dashboard — " + this.customer.getName());
        setSize(400, 360);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JLabel welcome = new JLabel("Welcome, " + this.customer.getName() + "!", SwingConstants.CENTER);
        welcome.setFont(new Font("SansSerif", Font.BOLD, 16));
        welcome.setBorder(BorderFactory.createEmptyBorder(14, 0, 10, 0));

        JPanel btnPanel = new JPanel(new GridLayout(5, 1, 8, 8));
        btnPanel.setBorder(BorderFactory.createEmptyBorder(10, 40, 20, 40));

        JButton moviesBtn  = btn("Browse Movies");
        JButton showsBtn   = btn("View Shows");
        JButton historyBtn = btn("My Bookings");
        JButton profileBtn = btn("My Profile");
        JButton logoutBtn  = btn("Logout");

        moviesBtn.addActionListener(e  -> { new MovieFrame(this.customer).setVisible(true); dispose(); });
        showsBtn.addActionListener(e   -> { new ShowFrame(this.customer).setVisible(true); dispose(); });
        historyBtn.addActionListener(e -> { new BookingHistoryFrame(this.customer).setVisible(true); dispose(); });
        profileBtn.addActionListener(e -> new ProfileFrame(this.customer).setVisible(true));
        logoutBtn.addActionListener(e  -> { new LoginFrame().setVisible(true); dispose(); });

        btnPanel.add(moviesBtn);
        btnPanel.add(showsBtn);
        btnPanel.add(historyBtn);
        btnPanel.add(profileBtn);
        btnPanel.add(logoutBtn);

        setLayout(new BorderLayout());
        add(welcome,  BorderLayout.NORTH);
        add(btnPanel, BorderLayout.CENTER);
    }

    private JButton btn(String text) { return new JButton(text); }
}
