# 🎬 Online Movie Ticket Booking System

[![Java](https://img.shields.io/badge/Java-17%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Java Swing](https://img.shields.io/badge/GUI-Java%20Swing-007396?style=for-the-badge&logo=java&logoColor=white)](https://docs.oracle.com/javase/tutorial/uiswing/)
[![MySQL](https://img.shields.io/badge/Database-MySQL%208.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white)](https://www.mysql.com/)
[![JDBC](https://img.shields.io/badge/Connector-JDBC%20MySQL%20Connector-F29111?style=for-the-badge&logo=mysql&logoColor=white)](https://dev.mysql.com/downloads/connector/j/)
[![Architecture](https://img.shields.io/badge/Architecture-OOP%20%7C%20DAO%20Pattern-4E5D6C?style=for-the-badge)](#-object-oriented-design--architecture)

A desktop-based **Online Movie Ticket Booking System** built using **Java (Swing)** and **MySQL via JDBC**. Designed around core **Object-Oriented Programming (OOP)** principles and robust transactional database operations, this application delivers a seamless experience for customers to browse movies, pick showtimes, select real-time seats, and book tickets, while offering an administrative suite for managing catalog, scheduling, and user accounts.

---

## 📌 Table of Contents

- ✨ [Key Features](#-key-features)
  - [Customer Portal](#customer-portal)
  - [Admin Portal](#admin-portal)
- 🏛 [Object-Oriented Design & Architecture](#-object-oriented-design--architecture)
- 📂 [Project Structure](#-project-structure)
- 🗄 [Database Schema & Setup](#-database-schema--setup)
- 📄 [Project Report](#-project-report)
- 📑 [Project Presentation](#-project-presentation)
- 🚀 [Quickstart & Installation](#-quickstart--installation)
- 🔐 [Demo Credentials](#-demo-credentials)
- 👥 [Project Team & Responsibilities](#-project-team--responsibilities)

---

## ✨ Key Features

### Customer Portal

- **User Authentication**: Customer account registration and login with input validation and session management. This academic demo stores passwords as plain text; do not use real credentials.
- **Browse & Search Catalog**: Interactive movie catalog with title-based keyword filtering and detailed metadata (genre, language, runtime, synopsis).
- **Showtime Schedule**: View scheduled dates, screening halls, and pricing per movie.

- **Interactive Seat Grid**:
  - Real-time seat status visualization (🟢 Available, 🟠 Selected, 🔴 Booked).
  - 100-seat theater arrangement (Rows A–J, Seats 1–10).
  - Auto-detection for sold-out screenings (**HOUSEFULL** indicator).
- **Polymorphic Payments**:
  - Checkout demonstrates **UPI** and **Card** payment methods with card-number masking. Payments are simulated; no payment gateway or real transaction processing is connected.
- **Booking History & Self-Cancellation**:
  - Complete audit trail with timestamps, total price, and allocated seat numbers.
  - Instant ticket cancellation with automatic seat release and status update.

### Admin Portal

- **Movie Management (CRUD)**: Add, edit, inspect, and delete movies. Deleting a movie also deletes its shows and dependent records through database cascade rules.
- **Show Scheduling**: Schedule shows with specific dates, times, screens, and custom ticket pricing with automated 100-seat layout generation.
- **Centralized Booking Ledger**: View all customer reservations, seat allocations, payment amounts, and transaction statuses across the platform.
- **User Administration**: View registered customers, ban fraudulent users, and safely remove accounts with active booking dependency protection.

---

## 🏛 Object-Oriented Design & Architecture

The application uses a modular design to demonstrate the 4 pillars of OOP:

| Principle | Implementation Details |
| :--- | :--- |
| **Encapsulation** | All entities (`Movie`, `Show`, `Seat`, `Booking`, `User`) protect internal state using `private` fields with strictly controlled public accessors and mutators. |
| **Inheritance** | Base class `User` encapsulates shared authentication logic and fields, extended by concrete `Customer` and `Admin` subclasses (`IS-A` relationship). |
| **Polymorphism** | The `Payment` interface defines `makePayment(double amount)` and `getMethodName()`. `PaymentFrame` handles payments polymorphically using either `UPIPayment` or `CardPayment` at runtime. |
| **Abstraction** | Clear layer separation between UI Presentation (`*Frame`), Data Access Objects (`*DAO`), Business Domain Models, and Database Connectivity (`DBConnection`, `Config`). |
| **ACID Transactions** | Booking, seat reservation, and payment-record insertion run in a database transaction with `SELECT ... FOR UPDATE` row-level locks, batch updates, and rollback on database errors or seat conflicts. Payment itself is simulated and is not an external transaction. |

---

## 📂 Project Structure

```text
OnlineMovieBooking/
│
├── .env.example              # Template for local MySQL credentials
├── compile.ps1               # Automated build script (PowerShell)
├── run.ps1                   # Application execution script (PowerShell)
│
├── docs/
│   ├── dbdocs/
│   │   └── README.md         # Complete Database Schema, ER Diagram & Data Dictionary
│   ├── pptdocs/
│   │   ├── OOPS PROJECT - Presentation.pptx # Project viva & architecture slide deck
│   │   └── README.md         # Presentation overview
│   └── report/
│       └── ONLINE_MOVIE_TICKET_BOOKING_SYSTEM.docx # Project report
│
├── lib/
│   └── mysql-connector-j-26.7.0.jar  # JDBC MySQL Driver
│
├── sql/
│   └── movie_booking.sql     # Database schema, table definitions, & sample data
│
└── src/
    ├── admin/                # Administrator UI Frames
    │   ├── AdminDashboard.java
    │   ├── ManageBookingsFrame.java
    │   ├── ManageMoviesFrame.java
    │   ├── ManageShowsFrame.java
    │   └── ManageUsersFrame.java
    │
    ├── auth/                 # Authentication & Registration
    │   ├── LoginFrame.java
    │   ├── RegisterFrame.java
    │   └── UserDAO.java
    │
    ├── booking/              # Seat selection & Booking transactions
    │   ├── BookingDAO.java
    │   ├── SeatDAO.java
    │   └── SeatSelectionFrame.java
    │
    ├── customer/             # Customer UI Frames
    │   ├── BookingHistoryFrame.java
    │   ├── CustomerDashboard.java
    │   └── ProfileFrame.java
    │
    ├── database/             # Centralized JDBC connection management & .env reader
    │   ├── Config.java
    │   └── DBConnection.java
    │
    ├── main/                 # Application Entry Point
    │   └── Main.java
    │
    ├── model/                # Domain Entities (OOP Models)
    │   ├── Admin.java
    │   ├── Booking.java
    │   ├── Customer.java
    │   ├── Movie.java
    │   ├── Seat.java
    │   ├── Show.java
    │   └── User.java
    │
    ├── movies/               # Movie browsing & catalog DAO
    │   ├── MovieDAO.java
    │   └── MovieFrame.java
    │
    ├── payment/              # Polymorphic Payment Subsystem
    │   ├── CardPayment.java
    │   ├── Payment.java
    │   ├── PaymentDAO.java
    │   ├── PaymentFrame.java
    │   └── UPIPayment.java
    │
    └── shows/                # Show scheduling & listings
        ├── ShowDAO.java
        └── ShowFrame.java
```

---

## 🗄 Database Schema & Setup

The database utilizes relational integrity with foreign keys, cascade rules, and automated timestamping across 7 interconnected tables (`Users`, `Movies`, `Shows`, `Seats`, `Bookings`, `BookingSeats`, `Payments`).

> 📘 **Full Schema & Architecture Documentation:**  
> For the complete Entity-Relationship (ER) diagram, relational ASCII architecture map, integrity cascade rules, and full data dictionary tables, see **[Database Schema Documentation (`docs/dbdocs/README.md`)](docs/dbdocs/README.md)**.

### Quick Table Overview

| Table | Primary Key | Key Foreign References | Purpose |
| :--- | :--- | :--- | :--- |
| **`Users`** | `user_id` | — | Customer accounts & admin credentials |
| **`Movies`** | `movie_id` | — | Catalog of movies, genres, runtimes, descriptions |
| **`Shows`** | `show_id` | `movie_id` ➔ `Movies` | Screening schedules, dates, times, screens & ticket rates |
| **`Seats`** | `seat_id` | `show_id` ➔ `Shows` | Real-time seat inventory (100 seats per show: A1–J10) |
| **`Bookings`** | `booking_id` | `user_id` ➔ `Users`, `show_id` ➔ `Shows` | Reservations, total amounts, audit snapshot & status |
| **`BookingSeats`** | `(booking_id, seat_id)` | `Bookings`, `Seats` | Junction mapping bookings to allocated seat rows |
| **`Payments`** | `payment_id` | `booking_id` ➔ `Bookings` | Polymorphic payment records (`UPI` / `Card`) |

👉 **[Open Complete Schema & ER Diagrams in `docs/dbdocs/README.md` ➔](docs/dbdocs/README.md)**

### Initializing MySQL Database

The setup script **drops and recreates the project's existing tables**, including their data. Back up any data you need before running it.

1. Open MySQL Workbench or connect to MySQL with the command-line client.
2. From the project root, execute the setup script:

   ```sql
   SOURCE sql/movie_booking.sql;
   ```

---

## 📄 Project Report

The project report contains the written documentation for the Online Movie Ticket Booking System:

> **[ONLINE_MOVIE_TICKET_BOOKING_SYSTEM.docx](docs/report/ONLINE_MOVIE_TICKET_BOOKING_SYSTEM.docx)**

---

## 📑 Project Presentation

A comprehensive presentation slide deck covering system architecture, Object-Oriented Programming (OOP) design patterns, database workflows, and viva points is available in the `docs/pptdocs/` folder:

> 📽️ **Presentation Slide Deck:**  
> **[OOPS PROJECT - Presentation.pptx](docs/pptdocs/OOPS%20PROJECT%20-%20Presentation.pptx)** *(1.5 MB PowerPoint Presentation)*

👉 **[Open the Presentation README in `docs/pptdocs/README.md` ➔](docs/pptdocs/README.md)**

---

## 🚀 Quickstart & Installation

### 1. Prerequisites

- **Java JDK 17+** installed and added to `PATH`.
- **MySQL Server 8.0+** running locally.

### 2. Configure Environment (`.env`)

Copy `.env.example` to `.env` in the project root:

```properties
DB_URL=jdbc:mysql://localhost:3306/movie_booking
DB_USER=root
DB_PASSWORD=your_mysql_password
```

> ⚠️ **Note**: `.env` is ignored by git to keep your local credentials private.

### 3. Compile Project

Run the automated PowerShell compile script:

```powershell
.\compile.ps1
```

*(This deletes and recreates `bin/`, then builds all `.java` files from `src/` with the connector library on the classpath.)*

### 4. Run Application

Launch the graphical interface:

```powershell
.\run.ps1
```

Or run directly via standard Java CLI:

```powershell
java -cp "lib\mysql-connector-j-26.7.0.jar;bin" main.Main
```

---

## 🔐 Demo Credentials

The database script pre-seeds standard test accounts:

| Portal | Email Address | Password | Role |
| :--- | :--- | :--- | :--- |
| **Administrator** | `admin@example.com` *(or `admin@moviebooking.com`)* | `admin123` | `Admin` |
| **Customer** | `aaron@example.com` | `pass123` | `Customer` |
| **New Customer** | *Any registered account via UI* | *(Custom)* | `Customer` |

These are demo credentials stored in plain text by the sample database script. Change them for local testing and do not reuse real passwords.

---

## 👥 Project Team & Responsibilities

| Roll Number | Team Member | Primary Modules & Responsibilities |
| :--- | :--- | :--- |
| **VAS25CS001** | **Aaron Thalakkottor Sooraj** | **Database Architecture & Integration**; MySQL schema design and setup (`sql/`); JDBC connection management and transaction handling (`database/`); environment configuration and build management |
| **VAS25CS012** | **Abinandh P S** | **Movie & Show Management**; catalog presentation and search filtering (`movies/`); showtime scheduling and hall mapping (`shows/`); Movie and Show DAOs and admin interfaces |
| **VAS25CS017** | **Adhin Seby** | **Seat Booking & Payment Processing**; theater seat grid UI and availability state (`booking/`); polymorphic payment-method demonstration (`payment/`); booking transaction coordination and validation |
| **VAS25CS026** | **Adwaith S Kumar** | **User Authentication & Domain Modeling**; OOP inheritance hierarchy (`User`, `Customer`, `Admin`); user login, registration, and session validation (`auth/`); user management and account authorization controls |

---

### Built for Academic Submission • Object-Oriented Programming & Database Systems

---

### 📄 Credits & License

- **Project:** Online Movie Ticket Booking System
- **Developed by:** Group-03 (Aaron Thalakkottor Sooraj, Abinandh P. S., Adhin Seby, Adwaith S. Kumar)

© 2023 ATS_PDZ. All rights reserved.  
*This project was created for educational and academic purposes.*
