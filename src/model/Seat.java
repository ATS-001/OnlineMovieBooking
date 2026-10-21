package model;

/**
 * Seat entity — represents one seat for a specific show.
 */
public class Seat {
    private int    seatId;
    private int    showId;
    private String seatNumber;
    private String status; // "AVAILABLE" or "BOOKED"

    public Seat(int seatId, int showId, String seatNumber, String status) {
        this.seatId     = seatId;
        this.showId     = showId;
        this.seatNumber = seatNumber;
        this.status     = status;
    }

    public int    getSeatId()     { return seatId; }
    public int    getShowId()     { return showId; }
    public String getSeatNumber() { return seatNumber; }
    public String getStatus()     { return status; }

    public boolean isAvailable() {
        return "AVAILABLE".equalsIgnoreCase(status);
    }
}