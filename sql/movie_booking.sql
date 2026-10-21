-- ============================================
-- Online Movie Ticket Booking System
-- Database Setup Script
-- ============================================

CREATE DATABASE IF NOT EXISTS movie_booking;
USE movie_booking;

-- Drop existing tables (in reverse dependency order) for a clean setup
DROP TABLE IF EXISTS Payments;
DROP TABLE IF EXISTS BookingSeats;
DROP TABLE IF EXISTS Bookings;
DROP TABLE IF EXISTS Seats;
DROP TABLE IF EXISTS Shows;
DROP TABLE IF EXISTS Movies;
DROP TABLE IF EXISTS Users;

-- Users table
CREATE TABLE Users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    role VARCHAR(20) DEFAULT 'Customer'
);

-- Movies table
CREATE TABLE Movies (
    movie_id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    genre VARCHAR(50),
    language VARCHAR(50),
    description TEXT,
    release_date DATE,
    duration INT
);

-- Shows table
CREATE TABLE Shows (
    show_id INT AUTO_INCREMENT PRIMARY KEY,
    movie_id INT NOT NULL,
    show_date DATE NOT NULL,
    show_time TIME NOT NULL,
    screen VARCHAR(50) NOT NULL,
    ticket_price DOUBLE NOT NULL,
    FOREIGN KEY (movie_id) REFERENCES Movies(movie_id) ON DELETE CASCADE
);

-- Seats table
CREATE TABLE Seats (
    seat_id INT AUTO_INCREMENT PRIMARY KEY,
    show_id INT NOT NULL,
    seat_number VARCHAR(10) NOT NULL,
    status VARCHAR(20) DEFAULT 'AVAILABLE', -- 'AVAILABLE' or 'BOOKED'
    FOREIGN KEY (show_id) REFERENCES Shows(show_id) ON DELETE CASCADE,
    UNIQUE(show_id, seat_number) -- Prevent duplicate seats for same show
);

-- Bookings table
CREATE TABLE Bookings (
    booking_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    show_id INT NOT NULL,
    booking_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_amount DOUBLE NOT NULL,
    status VARCHAR(20) DEFAULT 'CONFIRMED',
    movie_title VARCHAR(100) NULL,
    show_date DATE NULL,
    show_time TIME NULL,
    screen VARCHAR(50) NULL,
    seat_numbers VARCHAR(255) NULL,
    history_note VARCHAR(255) NULL,
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (show_id) REFERENCES Shows(show_id) ON DELETE CASCADE
);

-- BookingSeats (Mapping table for Bookings and Seats)
CREATE TABLE BookingSeats (
    booking_id INT NOT NULL,
    seat_id INT NOT NULL,
    PRIMARY KEY (booking_id, seat_id),
    FOREIGN KEY (booking_id) REFERENCES Bookings(booking_id) ON DELETE CASCADE,
    FOREIGN KEY (seat_id) REFERENCES Seats(seat_id) ON DELETE CASCADE
);

-- Payments table
CREATE TABLE Payments (
    payment_id INT AUTO_INCREMENT PRIMARY KEY,
    booking_id INT NOT NULL,
    amount DOUBLE NOT NULL,
    payment_method VARCHAR(50) NOT NULL, -- 'UPI' or 'Card'
    payment_status VARCHAR(20) DEFAULT 'SUCCESS',
    payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (booking_id) REFERENCES Bookings(booking_id) ON DELETE CASCADE
);

-- ============================================
-- Sample Data
-- ============================================

-- Users
INSERT INTO Users (name, email, password, role) VALUES
('Admin User', 'admin@example.com', 'admin123', 'Admin'),
('Aaron TS', 'aaron@example.com', 'pass123', 'Customer');

-- Movies
INSERT INTO Movies (title, genre, language, description, release_date, duration) VALUES
('Interstellar', 'Sci-Fi', 'English', 'A team of explorers travel through a wormhole in space.', '2014-11-07', 169),
('Inception', 'Sci-Fi', 'English', 'A thief who steals corporate secrets through the use of dream-sharing technology.', '2010-07-16', 148),
('The Dark Knight', 'Action', 'English', 'When the menace known as the Joker wreaks havoc and chaos on the people of Gotham.', '2008-07-18', 152);

-- Shows (assuming current date is around 2026-09-20)
INSERT INTO Shows (movie_id, show_date, show_time, screen, ticket_price) VALUES
(1, '2026-09-20', '10:00:00', 'Screen 1', 180.0),
(1, '2026-09-20', '18:00:00', 'Screen 2', 200.0),
(2, '2026-09-20', '14:00:00', 'Screen 1', 150.0);

-- Generate all 100 seats (A1 to J10) for each sample show
DELIMITER $$
CREATE PROCEDURE GenerateSampleSeats()
BEGIN
    DECLARE show_idx INT;
    DECLARE row_char CHAR(1);
    DECLARE seat_idx INT;
    DECLARE row_num INT;

    SET show_idx = 1;
    WHILE show_idx <= 3 DO
        SET row_num = 0;
        WHILE row_num < 10 DO
            SET row_char = CHAR(65 + row_num);
            SET seat_idx = 1;
            WHILE seat_idx <= 10 DO
                INSERT IGNORE INTO Seats (show_id, seat_number, status)
                VALUES (show_idx, CONCAT(row_char, seat_idx), 'AVAILABLE');
                SET seat_idx = seat_idx + 1;
            END WHILE;
            SET row_num = row_num + 1;
        END WHILE;
        SET show_idx = show_idx + 1;
    END WHILE;
END$$
DELIMITER ;

CALL GenerateSampleSeats();
DROP PROCEDURE GenerateSampleSeats;

SELECT 'Database setup complete!' AS status;