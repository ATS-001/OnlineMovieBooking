package payment;

/**
 * Payment interface — demonstrates Abstraction and Polymorphism.
 * UPIPayment and CardPayment implement this interface.
 */
public interface Payment {
    boolean makePayment(double amount);
    String  getMethodName();
}