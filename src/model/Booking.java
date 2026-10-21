package model;

import java.sql.Timestamp;

/**
 * Booking entity — links a user, show, seats, and payment.
 */
public class Booking {
    private int       bookingId;
    private int       userId;
    private int       showId;
    private Timestamp bookingDate;
    private double    totalAmount;
    private String    status;

    // Display fields populated by JOIN queries
    private String movieTitle;
    private String showDate;
    private String showTime;
    private String screen;
    private String seatNumbers;
    private String historyNote;

    public Booking(int bookingId, int userId, int showId,
                   Timestamp bookingDate, double totalAmount, String status) {
        this.bookingId   = bookingId;
        this.userId      = userId;
        this.showId      = showId;
        this.bookingDate = bookingDate;
        this.totalAmount = totalAmount;
        this.status      = status;
    }

    /** Called after a JOIN query to populate display fields. */
    public void setDisplayDetails(String movieTitle, String showDate,
                                  String showTime, String screen, String seatNumbers) {
        this.movieTitle  = movieTitle;
        this.showDate    = showDate;
        this.showTime    = showTime;
        this.screen      = screen;
        this.seatNumbers = seatNumbers;
    }

    // Core getters
    public int       getBookingId()   { return bookingId; }
    public int       getUserId()      { return userId; }
    public int       getShowId()      { return showId; }
    public Timestamp getBookingDate() { return bookingDate; }
    public double    getTotalAmount() { return totalAmount; }
    public String    getStatus()      { return status; }

    // Display getters
    public String getMovieTitle()  { return movieTitle; }
    public String getShowDate()    { return showDate; }
    public String getShowTime()    { return showTime; }
    public String getScreen()      { return screen; }
    public String getSeats()       { return seatNumbers; }
    public String getHistoryNote()  { return historyNote; }

    public void setHistoryNote(String historyNote) {
        this.historyNote = historyNote;
    }

    public void displayBooking() {
        System.out.println("Booking ID: " + bookingId +
            " | Movie: " + movieTitle +
            " | Date: " + showDate +
            " | Time: " + showTime +
            " | Screen: " + screen +
            " | Seats: " + seatNumbers +
            " | Total: Rs " + totalAmount +
            " | Status: " + status);
    }
}