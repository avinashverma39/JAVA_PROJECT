package Bank_Management_System_2;

import java.util.*;

/*
 * ==========================================================
 *  BANK MANAGEMENT SYSTEM - Simple Console Application
 * ==========================================================
 *  A beginner-friendly Java project covering:
 *  1. Create Account
 *  2. Login (Account Number + PIN)
 *  3. Deposit Money
 *  4. Withdraw Money
 *  5. Transfer Money
 *  6. Check Balance
 *  7. Change PIN
 *  8. Add Nominee
 *  9. Update Mobile Number
 * 10. Update Address
 * 11. Mini Statement (Last 5 Transactions)
 * 12. Wrong Login Protection (3 Attempts -> Account Blocked)
 *
 *  Data is stored only in memory (HashMap) - no database needed.
 *  This keeps the code simple enough to explain line by line
 *  in a viva / project demo.
 * ==========================================================
 */

class Account {
    int accountNumber;
    String name;
    String mobile;
    String address;
    String pin;
    double balance;
    String nominee;
    int wrongPinAttempts;
    boolean isBlocked;
    List<String> transactions; // mini statement

    Account(int accountNumber, String name, String mobile, String address, String pin, double balance) {
        this.accountNumber = accountNumber;
        this.name = name;
        this.mobile = mobile;
        this.address = address;
        this.pin = pin;
        this.balance = balance;
        this.nominee = "Not Added";
        this.wrongPinAttempts = 0;
        this.isBlocked = false;
        this.transactions = new ArrayList<>();
    }

    // Keeps only the last 5 transactions for the mini statement
    void addTransaction(String detail) {
        transactions.add(detail);
        if (transactions.size() > 5) {
            transactions.remove(0);
        }
    }
}

public class BankManagementSystem {

    static Scanner sc = new Scanner(System.in);
    static Map<Integer, Account> accounts = new HashMap<>();
    static int nextAccountNumber = 1001; // auto-generated account numbers

    public static void main(String[] args) {
        int choice;
        do {
            System.out.println("\n===== BANK MANAGEMENT SYSTEM =====");
            System.out.println("1. Create Account");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Enter your choice: ");
            choice = readInt();

            switch (choice) {
                case 1 -> createAccount();
                case 2 -> login();
                case 3 -> System.out.println("Thank you for using our Bank System!");
                default -> System.out.println("Invalid choice! Try again.");
            }
        } while (choice != 3);

        sc.close();
    }

    // ---------------- CREATE ACCOUNT ----------------
    static void createAccount() {
        System.out.println("\n--- Create New Account ---");
        System.out.print("Enter Name: ");
        String name = sc.nextLine();

        System.out.print("Enter Mobile Number: ");
        String mobile = sc.nextLine();

        System.out.print("Enter Address: ");
        String address = sc.nextLine();

        System.out.print("Set a 4-digit PIN: ");
        String pin = sc.nextLine();

        System.out.print("Enter Initial Deposit Amount: ");
        double balance = readDouble();

        int accNo = nextAccountNumber++;
        Account acc = new Account(accNo, name, mobile, address, pin, balance);
        acc.addTransaction("Account Opened with Balance: " + balance);
        accounts.put(accNo, acc);

        System.out.println("\nAccount Created Successfully!");
        System.out.println("Your Account Number is: " + accNo);
        System.out.println("(Please note this down, you will need it to login)");
    }

    // ---------------- LOGIN ----------------
    static void login() {
        System.out.println("\n--- Login ---");
        System.out.print("Enter Account Number: ");
        int accNo = readInt();

        Account acc = accounts.get(accNo);
        if (acc == null) {
            System.out.println("Account not found!");
            return;
        }

        if (acc.isBlocked) {
            System.out.println("This account is BLOCKED due to 3 wrong PIN attempts.");
            System.out.println("Please contact the bank to unblock it.");
            return;
        }

        System.out.print("Enter PIN: ");
        String pin = sc.nextLine();

        if (pin.equals(acc.pin)) {
            acc.wrongPinAttempts = 0; // reset on successful login
            System.out.println("\nLogin Successful! Welcome, " + acc.name);
            accountMenu(acc);
        } else {
            acc.wrongPinAttempts++;
            int remaining = 3 - acc.wrongPinAttempts;

            if (acc.wrongPinAttempts >= 3) {
                acc.isBlocked = true;
                System.out.println("Wrong PIN entered 3 times. Account BLOCKED for security.");
            } else {
                System.out.println("Wrong PIN! Attempts remaining: " + remaining);
            }
        }
    }

    // ---------------- ACCOUNT MENU (after login) ----------------
    static void accountMenu(Account acc) {
        int choice;
        do {
            System.out.println("\n----- Account Menu (" + acc.accountNumber + ") -----");
            System.out.println("1. Deposit Money");
            System.out.println("2. Withdraw Money");
            System.out.println("3. Transfer Money");
            System.out.println("4. Check Balance");
            System.out.println("5. Change PIN");
            System.out.println("6. Add Nominee");
            System.out.println("7. Update Mobile Number");
            System.out.println("8. Update Address");
            System.out.println("9. Mini Statement");
            System.out.println("10. Logout");
            System.out.print("Enter your choice: ");
            choice = readInt();

            switch (choice) {
                case 1 -> deposit(acc);
                case 2 -> withdraw(acc);
                case 3 -> transfer(acc);
                case 4 -> checkBalance(acc);
                case 5 -> changePin(acc);
                case 6 -> addNominee(acc);
                case 7 -> updateMobile(acc);
                case 8 -> updateAddress(acc);
                case 9 -> miniStatement(acc);
                case 10 -> System.out.println("Logging out...");
                default -> System.out.println("Invalid choice! Try again.");
            }
        } while (choice != 10);
    }

    // ---------------- DEPOSIT ----------------
    static void deposit(Account acc) {
        System.out.print("Enter amount to deposit: ");
        double amount = readDouble();

        if (amount <= 0) {
            System.out.println("Invalid amount!");
            return;
        }

        acc.balance += amount;
        acc.addTransaction("Deposited: " + amount);
        System.out.println("Deposit Successful! New Balance: " + acc.balance);
    }

    // ---------------- WITHDRAW ----------------
    static void withdraw(Account acc) {
        System.out.print("Enter amount to withdraw: ");
        double amount = readDouble();

        if (amount <= 0) {
            System.out.println("Invalid amount!");
        } else if (amount > acc.balance) {
            System.out.println("Insufficient Balance!");
        } else {
            acc.balance -= amount;
            acc.addTransaction("Withdrew: " + amount);
            System.out.println("Withdrawal Successful! New Balance: " + acc.balance);
        }
    }

    // ---------------- TRANSFER ----------------
    static void transfer(Account acc) {
        System.out.print("Enter receiver's Account Number: ");
        int receiverAccNo = readInt();

        Account receiver = accounts.get(receiverAccNo);
        if (receiver == null) {
            System.out.println("Receiver account not found!");
            return;
        }
        if (receiver.accountNumber == acc.accountNumber) {
            System.out.println("You cannot transfer money to your own account!");
            return;
        }

        System.out.print("Enter amount to transfer: ");
        double amount = readDouble();

        if (amount <= 0) {
            System.out.println("Invalid amount!");
        } else if (amount > acc.balance) {
            System.out.println("Insufficient Balance!");
        } else {
            acc.balance -= amount;
            receiver.balance += amount;

            acc.addTransaction("Transferred " + amount + " to Acc: " + receiver.accountNumber);
            receiver.addTransaction("Received " + amount + " from Acc: " + acc.accountNumber);

            System.out.println("Transfer Successful! New Balance: " + acc.balance);
        }
    }

    // ---------------- CHECK BALANCE ----------------
    static void checkBalance(Account acc) {
        System.out.println("Current Balance: " + acc.balance);
    }

    // ---------------- CHANGE PIN ----------------
    static void changePin(Account acc) {
        System.out.print("Enter current PIN: ");
        String oldPin = sc.nextLine();

        if (!oldPin.equals(acc.pin)) {
            System.out.println("Incorrect current PIN!");
            return;
        }

        System.out.print("Enter new PIN: ");
        String newPin = sc.nextLine();
        acc.pin = newPin;
        System.out.println("PIN changed successfully!");
    }

    // ---------------- ADD NOMINEE ----------------
    static void addNominee(Account acc) {
        System.out.print("Enter Nominee Name: ");
        String nominee = sc.nextLine();
        acc.nominee = nominee;
        System.out.println("Nominee added successfully!");
    }

    // ---------------- UPDATE MOBILE ----------------
    static void updateMobile(Account acc) {
        System.out.print("Enter New Mobile Number: ");
        String mobile = sc.nextLine();
        acc.mobile = mobile;
        System.out.println("Mobile number updated successfully!");
    }

    // ---------------- UPDATE ADDRESS ----------------
    static void updateAddress(Account acc) {
        System.out.print("Enter New Address: ");
        String address = sc.nextLine();
        acc.address = address;
        System.out.println("Address updated successfully!");
    }

    // ---------------- MINI STATEMENT ----------------
    static void miniStatement(Account acc) {
        System.out.println("\n--- Mini Statement (Last 5 Transactions) ---");
        if (acc.transactions.isEmpty()) {
            System.out.println("No transactions yet.");
        } else {
            for (String t : acc.transactions) {
                System.out.println("- " + t);
            }
        }
    }

    // ---------------- HELPER METHODS FOR SAFE INPUT ----------------
    static int readInt() {
        while (!sc.hasNextInt()) {
            System.out.print("Please enter a valid number: ");
            sc.next();
        }
        int value = sc.nextInt();
        sc.nextLine(); // consume leftover newline
        return value;
    }

    static double readDouble() {
        while (!sc.hasNextDouble()) {
            System.out.print("Please enter a valid amount: ");
            sc.next();
        }
        double value = sc.nextDouble();
        sc.nextLine(); // consume leftover newline
        return value;
    }
}
