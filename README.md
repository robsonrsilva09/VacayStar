 V a c a y S t a r 
 VacayStar Booking System
Overview
VacayStar Booking System is a console-based Java application for managing accommodation bookings at a holiday park. The system provides a smooth experience for guests to make their reservations and includes an admin area for managing all bookings. Data is persisted in a CSV file for easy backup and review.

Features
🌟 User Features (Guest Area)
Step-by-step Booking Wizard:
The guest is guided through each step—entering name, phone, email, selecting accommodation, choosing check-in and check-out dates, and confirming the booking.

Name Validation:
Only accepts valid names (letters and spaces).

UK Phone Number Validation:
Ensures the contact number starts with zero and has 11 digits (auto-fixes if the user enters 10 digits).

Email Validation:
Accepts only properly formatted emails.

Accommodation Selection:
Presents a menu of 5 accommodation types, each with its own daily rate, card fee, and long-stay discount.

Date Selection:
The user selects check-in and check-out dates; the system auto-calculates the number of days. Flexible date format input.

Price Calculation:

Calculates total price, including VAT and card fee.

Applies automatic discounts for stays of 14+ days (discount rate varies by accommodation).

Shows the exact value of any discount earned.

Booking Summary:
Displays all booking details, including name, contact, accommodation, dates, price, and discount, before confirmation.

Data Persistence:
Bookings are saved to a CSV file (bookings.csv), including all details and price breakdown.

User-friendly Prompts and Navigation:

At each stage, the user can go back or cancel.

Clear error messages guide the user to correct input mistakes.

🔒 Admin Features (Admin Area)
Secure Admin Login:
Requires username (admin) and password (1234).

View All Bookings:
Shows all current bookings in a formatted table, including booking number, guest details, dates, discounts, and total price.

Delete All Bookings:
Option to clear all bookings at once, after confirmation.

Delete Specific Booking:
Delete an individual booking by number, with a confirmation prompt.

CSV Data Handling:
All admin actions update the same CSV file, ensuring all data is kept in one place.

Data Validation
Names: Only letters and spaces accepted.

Phones: Must be a valid UK number (auto-fix for 10 digits, strict check for format).

Email: Validates common email format.

Dates: Flexible input (e.g., 25062025, 25/06/2025, 25-06-2025), but always checks for valid and logical dates (check-out must be after check-in).

How it Works
Start the program.

Main menu: Choose to make a booking or enter admin area.

Booking:

Enter personal info (name, phone, email)

Choose accommodation

Select check-in and check-out dates

View price summary and confirm

Data is saved to CSV

Admin area:

Login

View all bookings

Delete all or specific bookings as needed

Project Structure
VacayStarBooking.java – Main application file, containing all logic and state management.

bookings.csv – All bookings are saved here (auto-created if not found).

Technologies Used
Java (no external libraries required, uses only standard Java classes)

File I/O for CSV management

Basic regex for input validation

Java time API for date management

To Do / Possible Improvements
Password encryption for admin

Support for multiple admins

UI/UX upgrade (GUI or web version)

Integration with email notification or SMS confirmation

More accommodation features (room numbers, add-ons)

Author
Robson da Silva
 
