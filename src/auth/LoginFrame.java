package auth;

import admin.AdminDashboard;
import customer.CustomerDashboard;
import model.Admin;
import model.Customer;
import model.User;

import javax.swing.*;
import java.awt.*;

/**
 * Application entry screen — Login form.
 */
public class LoginFrame extends JFrame {
    private final JTextField     emailField;
    private final JPasswordField passwordField;

    public LoginFrame() {
        setTitle("Online Movie Ticket Booking — Login");
        setSize(380, 260);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel = new JPanel(new GridLayout(5, 2, 8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        panel.add(new JLabel("Email:"));
        emailField = new JTextField();
        panel.add(emailField);

        panel.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        panel.add(passwordField);

        JButton loginBtn = new JButton("Login");
        loginBtn.addActionListener(e -> attemptLogin());
        panel.add(loginBtn);

        JButton registerBtn = new JButton("Register");
        registerBtn.addActionListener(e -> { new RegisterFrame().setVisible(true); dispose(); });
        panel.add(registerBtn);

        JButton exitBtn = new JButton("Exit");
        exitBtn.addActionListener(e -> System.exit(0));
        panel.add(exitBtn);

        panel.add(new JLabel()); // spacer

        add(panel);

        // Allow Enter key to trigger login
        getRootPane().setDefaultButton(loginBtn);
    }

    private void attemptLogin() {
        String email    = emailField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both email and password.",
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!email.contains("@") || !email.contains(".")) {
            JOptionPane.showMessageDialog(this, "Please enter a valid email address (e.g. user@example.com).",
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            User user = UserDAO.login(email, password);
            if (user instanceof Admin) {
                new AdminDashboard((Admin) user).setVisible(true);
                dispose();
            } else if (user instanceof Customer) {
                new CustomerDashboard((Customer) user).setVisible(true);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid email or password. Please verify your credentials.",
                    "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "An error occurred during login: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
