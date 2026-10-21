# run.ps1  — Run from the project root AFTER compile.ps1 succeeds
# Usage:  .\run.ps1

$JAR = "lib\mysql-connector-j-26.7.0.jar"

if (!(Test-Path "bin\main\Main.class")) {
    Write-Error "bin\main\Main.class not found. Run .\compile.ps1 first."
    exit 1
}

if (!(Test-Path ".env")) {
    Write-Error ".env not found. Copy .env.example to .env and fill in your MySQL credentials."
    exit 1
}

Write-Host "Starting Online Movie Ticket Booking System..."
java -cp "$JAR;bin" main.Main
