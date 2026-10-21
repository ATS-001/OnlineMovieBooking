package payment;

/**
 * Concrete Payment implementation — Card.
 * Demonstrates Polymorphism.
 */
public class CardPayment implements Payment {
    private final String cardNumber;

    public CardPayment(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    @Override
    public boolean makePayment(double amount) {
        String masked = "XXXX-XXXX-XXXX-" +
            cardNumber.substring(Math.max(0, cardNumber.length() - 4));
        System.out.println("Processing Rs " + amount + " via Card (" + masked + ") ... SUCCESS");
        return true;
    }

    @Override
    public String getMethodName() {
        return "Card";
    }
}