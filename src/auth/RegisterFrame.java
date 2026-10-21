package auth;

import javax.swing.*;
import java.awt.*;

/**
 * User registration form — new accounts default to Customer role.
 */
public class RegisterFrame extends JFrame {
    private final JTextField     nameField;
    private final JTextField     emailField;
    private final JPasswordField passwordField;
    private final JPasswordField confirmField;

    public RegisterFrame() {
        setTitle("Register — New Account");
        setSize(380, 290);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel = new JPanel(new GridLayout(5, 2, 8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        panel.add(new JLabel("Name:"));
        nameField = new JTextField();
        panel.add(nameField);

        panel.add(new JLabel("Email:"));
        emailField = new JTextField();
        panel.add(emailField);

        panel.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        panel.add(passwordField);

        panel.add(new JLabel("Confirm Password:"));
        confirmField = new JPasswordField();
        panel.add(confirmField);

        JButton registerBtn = new JButton("Register");
        registerBtn.addActionListener(e -> attemptRegister());
        panel.add(registerBtn);

        JButton backBtn = new JButton("Back to Login");
        backBtn.addActionListener(e -> { new LoginFrame().setVisible(true); dispose(); });
        panel.add(backBtn);

        add(panel);
        getRootPane().setDefaultButton(registerBtn);
    }

    private void attemptRegister() {
        String name    = nameField.getText().trim();
        String email   = emailField.getText().trim();
        String pass    = new String(passwordField.getPassword());
        String confirm = new String(confirmField.getPassword());

        if (name.isEmpty() || email.isEmpty() || pass.isEmpty() || confirm.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required. Please fill in all fields.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!email.contains("@") || !email.contains(".")) {
            JOptionPane.showMessageDialog(this, "Please enter a valid email address (e.g. user@example.com).", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (pass.length() < 4) {
            JOptionPane.showMessageDialog(this, "Password must be at least 4 characters long.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!pass.equals(confirm)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match. Please re-enter your password.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            if (UserDAO.registerUser(name, email, pass)) {
                JOptionPane.showMessageDialog(this, "Registration successful! You can now login.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                new LoginFrame().setVisible(true);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                    "Registration failed.\nEmail may already be registered.",
                    "Registration Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "An error occurred during registration: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
