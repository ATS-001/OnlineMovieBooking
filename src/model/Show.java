package model;

import java.sql.Date;
import java.sql.Time;

/**
 * Show entity — one screening of a movie.
 */
public class Show {
    private int    showId;
    private int    movieId;
    private Date   showDate;
    private Time   showTime;
    private String screen;
    private double ticketPrice;
    private String movieTitle; // Joined from Movies table for display

    public Show(int showId, int movieId, Date showDate, Time showTime,
                String screen, double ticketPrice, String movieTitle) {
        this.showId      = showId;
        this.movieId     = movieId;
        this.showDate    = showDate;
        this.showTime    = showTime;
        this.screen      = screen;
        this.ticketPrice = ticketPrice;
        this.movieTitle  = movieTitle;
    }

    public int    getShowId()      { return showId; }
    public int    getMovieId()     { return movieId; }
    public Date   getShowDate()    { return showDate; }
    public Time   getShowTime()    { return showTime; }
    public String getScreen()      { return screen; }
    public double getTicketPrice() { return ticketPrice; }
    public String getMovieTitle()  { return movieTitle; }
}