package model;

/**
 * Demonstrates Inheritance — Customer IS-A User.
 */
public class Customer extends User {

    public Customer(int userId, String name, String email, String password) {
        super(userId, name, email, password, "Customer");
    }

    @Override
    public void displayInfo() {
        System.out.println("--- Customer Profile ---");
        super.displayInfo();
    }
}