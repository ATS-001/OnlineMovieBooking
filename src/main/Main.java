package main;

import auth.LoginFrame;

import javax.swing.SwingUtilities;

/**
 * Application entry point.
 * Run from the project root:
 *   java -cp "lib/mysql-connector-j-26.7.0.jar;bin" main.Main
 */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}