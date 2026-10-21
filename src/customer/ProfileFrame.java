package customer;

import model.Customer;

import javax.swing.*;
import java.awt.*;

/**
 * Displays logged-in customer's profile information.
 */
public class ProfileFrame extends JFrame {

    public ProfileFrame(Customer customer) {
        setTitle("My Profile");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel = new JPanel(new GridLayout(4, 1, 6, 6));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        panel.add(new JLabel("User ID : " + customer.getUserId()));
        panel.add(new JLabel("Name    : " + customer.getName()));
        panel.add(new JLabel("Email   : " + customer.getEmail()));
        panel.add(new JLabel("Role    : " + customer.getRole()));

        add(panel);
    }
}
