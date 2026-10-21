package payment;

/**
 * Concrete Payment implementation — UPI.
 * Demonstrates Polymorphism.
 */
public class UPIPayment implements Payment {
    private final String upiId;

    public UPIPayment(String upiId) {
        this.upiId = upiId;
    }

    @Override
    public boolean makePayment(double amount) {
        System.out.println("Processing Rs " + amount + " via UPI (" + upiId + ") ... SUCCESS");
        return true;
    }

    @Override
    public String getMethodName() {
        return "UPI";
    }
}