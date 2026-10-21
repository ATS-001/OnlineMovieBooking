package model;

/**
 * Demonstrates Inheritance — Admin IS-A User.
 */
public class Admin extends User {

    public Admin(int userId, String name, String email, String password) {
        super(userId, name, email, password, "Admin");
    }

    @Override
    public void displayInfo() {
        System.out.println("--- Administrator Profile ---");
        super.displayInfo();
    }
}