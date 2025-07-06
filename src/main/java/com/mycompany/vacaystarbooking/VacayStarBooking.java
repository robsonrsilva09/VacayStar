/*0

 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.vacaystarbooking;

import java.io.*; // Import all classes for file operations
import java.util.Scanner; // Import for keyboard input
import java.util.regex.Pattern; // Format validation
import java.time.LocalDate; // Import LocalDate to work with dates (e.g., check-in/check-out) without time information.
import java.time.format.DateTimeFormatter; // Import DateTimeFormatter to format LocalDate objects as strings and parse strings as dates.
import java.time.temporal.ChronoUnit; // Import ChronoUnit for calculations such as finding the number of days between dates.

/**
 * Booking System for VacayStar
 * Handles customer bookings and admin functions.
 * Admin Login: user = admin / pass = 1234
 * @author Robson da Silva
 */
public class VacayStarBooking {

    // --- GLOBAL CONSTANTS ---
    // Constants are used for fixed values that don't change, making the code more readable and easier to maintain.
    static final double VAT_RATE = 0.15;
    static final String CSV_FILE = "bookings.csv";
    static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // --- CENTRALIZED ACCOMMODATION DATA ---
    // All accommodation info (names, prices, fees, discounts) in one place.
    // Using arrays avoids code duplication and simplifies updates.
    private static final String[] ACCOMMODATION_NAMES = {
        "Imperial Lodge", "Sunshine Apt.", "Standard Cabin", "Rustic Shed", "Classic Caravan"
    };

    // Constants for the data array indices. This avoids using "magic numbers".
    private static final int IDX_DAILY_RATE = 0;
    private static final int IDX_CARD_FEE = 1;
    private static final int IDX_DISCOUNT_RATE = 2;

    private static final double[][] ACCOMMODATION_DATA = {
        {350.0, 4.00, 0.10}, // Option 1: Imperial Lodge
        {280.0, 3.50, 0.10}, // Option 2: Sunshine Apt.
        {200.0, 3.00, 0.05}, // Option 3: Standard Cabin
        {150.0, 2.50, 0.05}, // Option 4: Rustic Shed
        {90.0,  2.00, 0.05}  // Option 5: Classic Caravan
    };

    // --- STATE MACHINE CONSTANTS ---
    // These constants represent each stage of the booking process,
    // making the state machine code more readable.
    private static final int STATE_GETTING_NAME = 1;
    private static final int STATE_GETTING_PHONE = 2;
    private static final int STATE_GETTING_EMAIL = 3;
    private static final int STATE_SELECTING_ACCOMMODATION = 4;
    private static final int STATE_GETTING_DATES = 5;
    private static final int STATE_CONFIRMING_BOOKING = 6;
    private static final int STATE_DONE = 7;
    private static final int STATE_CANCELLED = 8;    
    
        // --- BOOKING CONTEXT DATA CLASS ---
    // This inner class stores all data related to one booking in a single object.
    // It helps to pass user data through all states of the booking process.
    private static class BookingContext {
        String name = "";
        String contact = "";
        String email = "";
        String accommodation = "";
        int accOption = -1;
        int days = 0;
        LocalDate inDate = null;
        LocalDate outDate = null;
    }


    /**
    * Main entry point for the program.
    * Displays a menu and routes user to booking or admin functions.
    * Loops until the user chooses to exit.
    */
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        System.out.println("\n==============================================================");
        System.out.println("||||||||||||||||********** VacayStar *********||||||||||||||||");
        System.out.println("||||||||||||||| Welcome to the Booking System! |||||||||||||||");
        System.out.println("==============================================================\n");

        while (true) {
            showMainMenu();
            int option = getIntInput(input);

            switch (option) {
                case 1:
                    makeBooking(input);
                    break;
                case 2:
                    if (adminLogin(input)) {
                        adminMenu(input);
                    }
                    break;
                case 3:
                    System.out.println("\n==============================================================");
                    System.out.println("##########  Thank you for using the Booking System!  #########");
                    System.out.println("                 #########  See you soon!  #########");
                    System.out.println("==============================================================\n");
                    return;
                default:
                    System.out.println("\n==============================================================");
                    System.out.println("####### Oops! Looks like you chose an invalid option. ########");
                    System.out.println("        #########  Try again with 1, 2 or 3.  #########");
                    System.out.println("==============================================================\n");
            }
        }
    }

    // --- MAIN MENU DISPLAY ---
    // Shows the primary options to the user (booking, admin, exit).
    private static void showMainMenu() {
        System.out.println("==============================================================");
        System.out.println("------------------------  Main Menu  -------------------------");
        System.out.println("==============================================================");
        System.out.println("--------------------  1. Make a Booking  ---------------------");
        System.out.println("----------------------  2. Admin Area  -----------------------");
        System.out.println("------------------------- 3. Exit  ---------------------------");
        System.out.print("Choose an option: ");
        System.out.println("\n==============================================================\n");
    }
    
    // --- ACCOMMODATION MENU DISPLAY ---
    // Shows all available accommodation options with details.
    private static void showAccommodationMenu() {
        System.out.println("\n=====================================================================================");
        System.out.println("------------------------------- Select Accommodation --------------------------------");
        System.out.println("------------------- Please choose an option (or 00 to go back): ---------------------");
        System.out.println("=====================================================================================");
              for (int i = 0; i < ACCOMMODATION_NAMES.length; i++) {
            String name = ACCOMMODATION_NAMES[i];
            double dailyRate = ACCOMMODATION_DATA[i][IDX_DAILY_RATE];
            double cardFee = ACCOMMODATION_DATA[i][IDX_CARD_FEE];
            double discountPercentage = ACCOMMODATION_DATA[i][IDX_DISCOUNT_RATE] * 100;
        System.out.println("-------------------------------------------------------------------------------------");

            // Using String.format to align the text neatly.
            System.out.printf("%d. %-18s (GBP %.2f/day, card fee GBP %.2f, %.0f%% discount for 14+ days)\n",
                i + 1, name, dailyRate, cardFee, discountPercentage);  
        }
        System.out.println("-------------------------------------------------------------------------------------");   
        System.out.print("Your choice: ");
        System.out.println("\n=====================================================================================\n");  
    }
    // --- BOOKING SUMMARY DISPLAY ---
    // Shows a summary of the current booking, including all details and discounts.
    private static void showBookingSummary(BookingContext context, double discount, double total) {
        System.out.println("\n==============================================================");
        System.out.println("---------------------  Booking Summary  ----------------------");
        System.out.println("==============================================================");
        System.out.println("Name: " + context.name);
        System.out.println("Contact: " + context.contact);
        System.out.println("Email: " + context.email);
        System.out.println("Accommodation: " + context.accommodation);
        System.out.println("Check-in: " + context.inDate.format(DATE_FORMAT));
        System.out.println("Check-out: " + context.outDate.format(DATE_FORMAT));
        System.out.println("Days: " + context.days);
        if (discount > 0) {
            System.out.printf("Great! You earned a discount of: GBP %.2f\n", discount);
        } else {
             System.out.println("Did you know?");
             System.out.println("Get special discount when you book 14 days or more!");
        }
        System.out.printf("Total price: GBP %.2f", total);
    }
    
    // --- ADMIN MENU DISPLAY ---
    // Lists admin operations: view, clear, delete bookings.
    private static void showAdminMenu() {
        System.out.println("\n==============================================================");
        System.out.println("------------------------- Admin Menu -------------------------");    
        System.out.println("=============================================================="); 
        System.out.println("1. View All Bookings");
        System.out.println("--------------------------------------------------------------");
        System.out.println("2. Delete All Bookings");
        System.out.println("--------------------------------------------------------------");
        System.out.println("3. Delete a Booking by Number");
        System.out.println("--------------------------------------------------------------");
        System.out.println("4. Return to Main Menu");
        System.out.println("--------------------------------------------------------------");
        System.out.print("Choose an option: ");
        System.out.println("\n==============================================================");
    }

    // --- SAFE INTEGER INPUT ---
    // Prompts user until a valid integer is entered.
    // Protects the program from crashing on invalid input.
    public static int getIntInput(Scanner input) {
        int value;
        while (true) {
            try {
                value = Integer.parseInt(input.nextLine());
                break;
            } catch (Exception e) {
                System.out.println("\n==============================================================");
                System.out.println("####### Oops! Looks like you chose an invalid input. ########");
                System.out.println("############## Try again. Please enter a number #############");
                System.out.println("==============================================================\n");
            }
        }
        return value;
    }

    /**
    * BOOKING STATE MACHINE.
    * Controls the step-by-step user interaction for making a booking.
    * Each state is handled by a separate method for clarity.
    */
    public static void makeBooking(Scanner input) {
        int currentState = STATE_GETTING_NAME;
        BookingContext context = new BookingContext();

        while (currentState != STATE_DONE && currentState != STATE_CANCELLED) {
            currentState = switch (currentState) {
                case STATE_GETTING_NAME -> handleGetNameState(input, context);
                case STATE_GETTING_PHONE -> handleGetPhoneState(input, context);
                case STATE_GETTING_EMAIL -> handleGetEmailState(input, context);
                case STATE_SELECTING_ACCOMMODATION -> handleSelectAccommodationState(input, context);
                case STATE_GETTING_DATES -> handleGetDatesState(input, context);
                case STATE_CONFIRMING_BOOKING -> handleConfirmBookingState(input, context);
                default -> STATE_CANCELLED;
            };
        }
        
        if (currentState == STATE_CANCELLED) {
            System.out.println("\n==============================================================");
            System.out.println("######## Booking cancelled. Returning to main menu. ##########");
            System.out.println("==============================================================\n");
        }
    }
    
    // --- NAME STATE HANDLER ---
    // Gets and validates the user's name. Returns next state or repeats on error.
    private static int handleGetNameState(Scanner input, BookingContext context) {
        System.out.println("\n==============================================================");
        System.out.println("------------- Enter your name (or 0 to cancel): --------------");
        System.out.print("==============================================================\n");       
        String nameInput = input.nextLine().trim();
        if (nameInput.equals("0")) {
            return STATE_CANCELLED;
        }
        if (isValidName(nameInput)) {
            context.name = nameInput;
            return STATE_GETTING_PHONE;
        } else {
            System.out.println("\n==============================================================");
            System.out.println("###### Invalid name. Please use only letters and spaces ######");
            System.out.println("\n==============================================================\n");
            return STATE_GETTING_NAME;
        }
    }
    
    // --- PHONE STATE HANDLER ---
    // Gets, formats, and validates a UK phone number.
    // Returns next state or repeats on error.
    private static int handleGetPhoneState(Scanner input, BookingContext context) {
        System.out.println("\n==============================================================");
        System.out.println("------- Enter your UK phone number (or 00 to go back): -------");
        System.out.print("==============================================================\n");       
        String phoneInput = input.nextLine().trim();
        if (phoneInput.equals("00")) {
            return STATE_GETTING_NAME;
        }
        String processedPhone = phoneInput.replaceAll("[^0-9]", "");
        if (processedPhone.length() == 10 && !processedPhone.startsWith("0")) {
            processedPhone = "0" + processedPhone;
        }
        if (isValidUKPhone(processedPhone)) {
            context.contact = processedPhone;
            return STATE_GETTING_EMAIL;
        }
            System.out.println("\n==============================================================");
            System.out.println("################## Invalid UK phone number. ##################");
            System.out.println("#### Please enter a valid 11digit number starting with 0. ####");
            System.out.println("==============================================================\n");
        return STATE_GETTING_PHONE;
    }
    
    // --- EMAIL STATE HANDLER ---
    // Gets and validates user email. Returns next state or repeats on error.
    private static int handleGetEmailState(Scanner input, BookingContext context) {
        System.out.println("\n==============================================================");
        System.out.println("------------ Enter your email (or 00 to go back): ------------");
        System.out.println("==============================================================\n");       
        String emailInput = input.nextLine().trim();
        if (emailInput.equals("00")) {
            return STATE_GETTING_PHONE;
        }
        if (isValidEmail(emailInput)) {
            context.email = emailInput;
            return STATE_SELECTING_ACCOMMODATION;
        }
        System.out.println("\n==============================================================");
        System.out.println("# Invalid email format. Please enter a valid email address. #");
        System.out.println("==============================================================\n");
        return STATE_GETTING_EMAIL;
    }
    
    // --- ACCOMMODATION STATE HANDLER ---
    // Gets user's accommodation choice. Returns next state or repeats on error.
    private static int handleSelectAccommodationState(Scanner input, BookingContext context) {
        showAccommodationMenu();
        String accInputStr = input.nextLine().trim();
        if (accInputStr.equals("00")) {
            return STATE_GETTING_EMAIL;
        }
        try {
            int accOption = Integer.parseInt(accInputStr);
            if (accOption >= 1 && accOption <= ACCOMMODATION_NAMES.length) {
                context.accOption = accOption;
                context.accommodation = ACCOMMODATION_NAMES[accOption - 1];
                return STATE_GETTING_DATES;
            }
            System.out.println("\n=====================================================================================");
            System.out.println("############### Oops! Looks like you chose an invalid accommodation. ################");
            System.out.println("######################### Please enter a number from 1 to 5 #########################");
            System.out.println("=====================================================================================\n");
            } catch (NumberFormatException e) {
            System.out.println("\n=====================================================================================");
            System.out.println("################### Oops! Looks like you chose an invalid input. ####################");
            System.out.println("######################### Try again. Please enter a number ##########################");
            System.out.println("=====================================================================================\n");
        }
        return STATE_SELECTING_ACCOMMODATION;
    }
    
    // --- DATES STATE HANDLER ---
    // Gets and validates check-in and check-out dates, calculates number of days.
    private static int handleGetDatesState(Scanner input, BookingContext context) {
        System.out.println("\n==============================================================");
        System.out.println("Enter check-in date (dd/mm/yyyy)/(ddmmyyyy) or 00 to go back: ");
        System.out.print("==============================================================\n");
        String inDateStr = input.nextLine().trim();
        if (inDateStr.equals("00")) {
            return STATE_SELECTING_ACCOMMODATION;
        }
        LocalDate inDate = parseDate(inDateStr, true);
        if (inDate == null) {
            return STATE_GETTING_DATES;
        }
        
        while (true) {
            System.out.println("\n==============================================================");
            System.out.println("Enter check-out date (dd/mm/yyyy)/(ddmmyyyy) or 00 to go back: ");
            System.out.print("==============================================================\n");
            String outDateStr = input.nextLine().trim();
            if (outDateStr.equals("00")) {
                return STATE_GETTING_DATES;
            }

            LocalDate outDate = parseDate(outDateStr, false);
            if (outDate == null) {
                continue;
            }

            if (outDate.isEqual(inDate)) {
                System.out.println("\n==============================================================");
                System.out.println("### Incorrect checkout date, please add at least one day. ####");
                System.out.println("==============================================================\n");
                continue;
            }

            if (!outDate.isAfter(inDate)) {
                System.out.println("\n==============================================================");
                System.out.println("######## Check-out date must be after check-in date. ########");
                System.out.println("==============================================================\n");
                continue;
            }
            
            context.inDate = inDate;
            context.outDate = outDate;
            break;
        }

        context.days = (int) ChronoUnit.DAYS.between(context.inDate, context.outDate);
        return STATE_CONFIRMING_BOOKING;
    }

    // --- CONFIRM BOOKING STATE HANDLER ---
    //  Shows summary, confirms booking, or allows user to change dates
    private static int handleConfirmBookingState(Scanner input, BookingContext context) {
        int optionIndex = context.accOption - 1;
        double dailyRate = ACCOMMODATION_DATA[optionIndex][IDX_DAILY_RATE];
        double cardFee = ACCOMMODATION_DATA[optionIndex][IDX_CARD_FEE];
        double discountRate = ACCOMMODATION_DATA[optionIndex][IDX_DISCOUNT_RATE];
        
        double[] pricingResult = calculateTotal(context.days, cardFee, dailyRate, discountRate);
        double total = pricingResult[0];
        double discount = pricingResult[1];

        showBookingSummary(context, discount, total);
        System.out.println("\n==============================================================");
        System.out.print("Confirm booking? (Y = yes, N = change dates): ");
        String confirm = input.nextLine().trim().toUpperCase();

        if (confirm.equals("Y")) {
            saveBooking(context, discount, total);
            System.out.println("\n==============================================================");
            System.out.println("################ Booking saved successfully! ################"); 
            System.out.println("==============================================================\n");
            return STATE_DONE;
        } else if (confirm.equals("N") || confirm.equals("00")) {
            return STATE_GETTING_DATES;
        }
        
        System.out.println("Invalid option.");
        return STATE_CONFIRMING_BOOKING;
    }

    // --- VALIDATION METHODS ---
    // Check if user's input matches required format.
    public static boolean isValidName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        return name.matches("^[\\p{L}\\s]+$");
    }

    public static boolean isValidUKPhone(String phone) {
        return phone.matches("^0\\d{10}$");
    }

    public static boolean isValidEmail(String email) {
        return Pattern.matches("^[\\w\\.-]+@[\\w\\.-]+\\.[a-zA-Z]{2,}$", email);
    }
    
    // --- PRICE CALCULATION ---
    // Returns final price and discount applied (if eligible).    
    public static double[] calculateTotal(int days, double cardFee, double dailyRate, double discountRate) {
        double subtotal = days * dailyRate;
        double discountAmount = 0.0;
        if (days >= 14) {
            discountAmount = subtotal * discountRate;
            subtotal -= discountAmount;
        }
        double totalWithFee = subtotal + cardFee;
        double finalTotal = totalWithFee + (totalWithFee * VAT_RATE);
        return new double[] { finalTotal, discountAmount };
    }

    // --- SAVE BOOKING TO CSV FILE ---
    // Adds booking record (with discount and price) to the CSV file.
    public static void saveBooking(BookingContext context, double discount, double total) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(CSV_FILE, true))) {
            File file = new File(CSV_FILE);
            if (file.length() == 0) {
                pw.println("Name,Contact,Email,Accommodation,Days,Check-in,Check-out,Discount,Total Price");
            }
            pw.printf("%s,%s,%s,%s,%d,%s,%s,%.2f,%.2f\n",
                context.name, context.contact, context.email, context.accommodation,
                context.days, context.inDate.format(DATE_FORMAT), context.outDate.format(DATE_FORMAT),
                discount, total);
        } catch (IOException e) {
            System.out.println("Error saving booking: " + e.getMessage());
        }
    }
    
    // --- ADMIN LOGIN ---
    // Asks for credentials and checks if they match the admin account.
    public static boolean adminLogin(Scanner input) {
        System.out.println("\n==============================================================");
        System.out.println("----------------------- Admin Login ------------------------");  
        System.out.println("==============================================================");
        System.out.print("Admin username: ");
        String user = input.nextLine();
        System.out.print("Admin password: ");
        String pass = input.nextLine();
        if (user.equals("admin") && pass.equals("1234")) {        
            System.out.println("\nAdmin login successful!");
             System.out.println("==============================================================\n");    
            return true;
        } else {
            System.out.println("\nIncorrect username or password.");
             System.out.println("==============================================================\n");    
            return false;
        }
    }
    
    // --- ADMIN AREA MENU HANDLER ---
    // Lets admin view, clear, or delete bookings.
    public static void adminMenu(Scanner input) {
        while (true) {
            showAdminMenu();
            int adminOption = getIntInput(input);
            switch (adminOption) {
                case 1 -> viewBookings();
                case 2 -> confirmClearBookings(input);
                case 3 -> confirmDeleteBookingByNumber(input);
                case 4 -> { return; }
                default -> System.out.println("Invalid option. Try again.");
            }
        }
    }
 
    // --- VIEW BOOKINGS ---
    // Prints all bookings from the CSV in a formatted table.
    public static void viewBookings() {
        System.out.println("\n==============================================================");
        System.out.println("-----------------------  All Bookings  -----------------------");
        System.out.println("==============================================================");
        try (BufferedReader br = new BufferedReader(new FileReader(CSV_FILE))) {
            String line = br.readLine();
            if (line == null) {
                System.out.println("Bookings file not found or is empty. Please make a booking first.");
                return;
            }
            System.out.println("-".repeat(165));
            System.out.printf("%-3s | %-20s | %-13s | %-35s | %-18s | %-5s | %-10s | %-10s | %-11s | %-13s\n", 
                              "Nr.", "Name", "Contact", "Email", "Accommodation", "Days", "Check-in", "Check-out", "Discount", "Total Price");
            System.out.println("-".repeat(165));

            int bookingNumber = 1;
            boolean hasBookings = false;
            
            while ((line = br.readLine()) != null) {
                hasBookings = true;
                String[] data = line.split(",");
                if (data.length == 9) {
                    System.out.printf("%-3d | %-20s | %-13s | %-35s | %-18s | %-5s | %-10s | %-10s | GBP %-7s | GBP %-9s\n",
                        bookingNumber, data[0], data[1], data[2], data[3], data[4], data[5], data[6], data[7], data[8]);
                    System.out.println("-".repeat(165));
                }
                bookingNumber++;
            }
            
            if (!hasBookings) {
                System.out.println("No bookings available to display.");
            }

        } catch (FileNotFoundException e) {
            System.out.println("Bookings file not found. Please make a booking to create one.");
        } catch (IOException e) {
            System.out.println("An error occurred while reading the bookings file: " + e.getMessage());
        }
    }
    
    // --- CLEAR ALL BOOKINGS ---
    // Removes all records from the bookings CSV (keeps the header  
    public static void clearBookings() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(CSV_FILE))) {
            pw.println("Name,Contact,Email,Accommodation,Days,Check-in,Check-out,Discount,Total Price");
        } catch (IOException e) {
            System.out.println("Error clearing bookings: " + e.getMessage());
        }
    }

    // --- CONFIRM AND DELETE ALL BOOKINGS ---
    // Asks for confirmation before deleting all bookings.
    public static void confirmClearBookings(Scanner input) {
        System.out.println("\n==============================================================");
        System.out.println("--------------------  Delete All Bookings  -------------------");
        System.out.println("==============================================================");
        System.out.print("Are you sure you want to DELETE ALL bookings? (Y/N): ");
        String confirmation = input.nextLine().trim().toUpperCase();
        if (confirmation.equals("Y")) {
            clearBookings();
            System.out.println("\nAll bookings deleted. CSV cleared!");
        } else {
            System.out.println("\nOperation cancelled.");
        }
    }

    // --- CONFIRM AND DELETE SPECIFIC BOOKING ---
    // Allows admin to delete a booking by its number.
    public static void confirmDeleteBookingByNumber(Scanner input) {
        System.out.println("\n==============================================================");
        System.out.println("-----------------  Delete a Specific Booking  ----------------");
        System.out.println("==============================================================");
        viewBookings();
        try {
            File inputFile = new File(CSV_FILE);
            if (!inputFile.exists() || inputFile.length() == 0) return;
            java.util.ArrayList<String> lines = new java.util.ArrayList<>();
            try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
                 String line;
                 while ((line = br.readLine()) != null) lines.add(line);
            }
            if (lines.size() <= 1) return;
            
            System.out.print("\nEnter the booking number to delete or 0 to cancel: ");
            int num = getIntInput(input);
            if (num == 0) {
                System.out.println("\nOperation cancelled.");
                return;
            }
            if (num < 1 || num >= lines.size()) {
                System.out.println("\nInvalid number.");
                return;
            }

            System.out.print("\nAre you sure you want to delete booking number " + num + "? (Y/N): ");
            String confirmation = input.nextLine().trim().toUpperCase();
            if (confirmation.equals("Y")) {
                lines.remove(num);
                try (PrintWriter pw = new PrintWriter(new FileWriter(CSV_FILE))) {
                    for (String line : lines) pw.println(line);
                }
                System.out.println("\nBooking number " + num + " deleted successfully!");
            } else {
                System.out.println("\nOperation cancelled.");
            }
        } catch (IOException e) {
            System.out.println("\nError processing file: " + e.getMessage());
        }
    }
    
    // --- PARSE DATE INPUT ---
    // Parses various date formats and ensures valid/future dates as required.
    public static LocalDate parseDate(String dateStr, boolean mustBeFuture) {
        if (dateStr.matches("\\d{8}")) {
            dateStr = dateStr.substring(0, 2) + "/" + dateStr.substring(2, 4) + "/" + dateStr.substring(4, 8);
        }
        dateStr = dateStr.replaceAll("[\\-\\.\\s]", "/");
        try {
            LocalDate date = LocalDate.parse(dateStr, DATE_FORMAT);
            if (mustBeFuture && date.isBefore(LocalDate.now())) {
                System.out.println("\n==============================================================");
                System.out.println("#################### Enter a valid date. #####################");
                System.out.println("################# Date cannot be in the past. ################");
                System.out.println("==============================================================\n");
                return null;
            }
            return date;
        } catch (Exception e) {
            System.out.println("\n==============================================================");
            System.out.println("################### Invalid date format. #####################");
            System.out.println("############# Please use dd/mm/yyyy or ddmmyyyy. #############");
            System.out.println("==============================================================\n");
                        return null;
        }
    }
}
