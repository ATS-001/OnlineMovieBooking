package model;

import java.sql.Date;

/**
 * Movie entity — Encapsulation via private fields + getters/setters.
 */
public class Movie {
    private int    movieId;
    private String title;
    private String genre;
    private String language;
    private String description;
    private Date   releaseDate;
    private int    duration;

    public Movie(int movieId, String title, String genre, String language,
                 String description, Date releaseDate, int duration) {
        this.movieId     = movieId;
        this.title       = title;
        this.genre       = genre;
        this.language    = language;
        this.description = description;
        this.releaseDate = releaseDate;
        this.duration    = duration;
    }

    public int    getMovieId()     { return movieId; }
    public String getTitle()       { return title; }
    public String getGenre()       { return genre; }
    public String getLanguage()    { return language; }
    public String getDescription() { return description; }
    public Date   getReleaseDate() { return releaseDate; }
    public int    getDuration()    { return duration; }

    public void setTitle(String title)           { this.title = title; }
    public void setGenre(String genre)           { this.genre = genre; }
    public void setLanguage(String language)     { this.language = language; }
    public void setDescription(String desc)      { this.description = desc; }
    public void setReleaseDate(Date releaseDate) { this.releaseDate = releaseDate; }
    public void setDuration(int duration)        { this.duration = duration; }
}