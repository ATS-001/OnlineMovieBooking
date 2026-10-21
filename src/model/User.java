package model;

/**
 * Base class — demonstrates Inheritance.
 * Customer and Admin extend this class.
 */
public class User {
    private int userId;
    private String name;
    private String email;
    private String password;
    private String role; // "Customer" or "Admin"

    public User(int userId, String name, String email, String password, String role) {
        this.userId   = userId;
        this.name     = name;
        this.email    = email;
        this.password = password;
        this.role     = role;
    }

    // Getters (Encapsulation)
    public int    getUserId() { return userId; }
    public String getName()   { return name; }
    public String getEmail()  { return email; }
    public String getRole()   { return role; }

    public boolean matchesPassword(String entered) {
        return this.password.equals(entered);
    }

    public void displayInfo() {
        System.out.println("User ID : " + userId);
        System.out.println("Name    : " + name);
        System.out.println("Email   : " + email);
        System.out.println("Role    : " + role);
    }
}