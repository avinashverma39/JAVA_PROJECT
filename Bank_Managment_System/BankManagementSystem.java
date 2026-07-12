package Bank_Managment_System;

import java.util.Scanner;

public class BankManagementSystem {

    static Scanner sc = new Scanner(System.in);

    static String name = "";
    static String address = "";
    static String mobile = "";
    static String nominee = "";

    static int accountNumber = 1001;
    static int pin = 1234;

    static double balance = 0;

    static String lastTransaction = "No Transaction";

    public static void main(String[] args) {

        createAccount();

        if (!login()) {
            System.out.println("Account Locked!");
            return;
        }

        int choice;

        do {
            System.out.println("\n------ BANK MENU ------");
            System.out.println("1. Deposit");
            System.out.println("2. Withdraw");
            System.out.println("3. Transfer");
            System.out.println("4. Check Balance");
            System.out.println("5. Change PIN");
            System.out.println("6. Add Nominee");
            System.out.println("7. Update Mobile");
            System.out.println("8. Update Address");
            System.out.println("9. Mini Statement");
            System.out.println("10. Exit");

            System.out.print("Enter Choice: ");
            choice = sc.nextInt();

            switch (choice) {

                case 1:
                    deposit();
                    break;

                case 2:
                    withdraw();
                    break;

                case 3:
                    transfer();
                    break;

                case 4:
                    System.out.println("Current Balance = ₹" + balance);
                    break;

                case 5:
                    changePIN();
                    break;

                case 6:
                    addNominee();
                    break;

                case 7:
                    updateMobile();
                    break;

                case 8:
                    updateAddress();
                    break;

                case 9:
                    miniStatement();
                    break;

                case 10:
                    System.out.println("Thank You!");
                    break;

                default:
                    System.out.println("Invalid Choice");
            }

        } while (choice != 10);
    }

    static void createAccount() {

        sc.nextLine();

        System.out.println("=== CREATE ACCOUNT ===");

        System.out.print("Enter Name: ");
        name = sc.nextLine();

        System.out.print("Enter Address: ");
        address = sc.nextLine();

        System.out.print("Enter Mobile: ");
        mobile = sc.nextLine();

        System.out.println("Account Created Successfully");
        System.out.println("Account Number = " + accountNumber);
        System.out.println("PIN = " + pin);
    }

    static boolean login() {

        int attempts = 3;

        while (attempts > 0) {

            System.out.print("\nEnter Account Number: ");
            int acc = sc.nextInt();

            System.out.print("Enter PIN: ");
            int p = sc.nextInt();

            if (acc == accountNumber && p == pin) {
                System.out.println("Login Successful");
                return true;
            }

            attempts--;
            System.out.println("Wrong Details");
            System.out.println("Attempts Left = " + attempts);
        }

        return false;
    }

    static void deposit() {

        System.out.print("Enter Amount: ");
        double amt = sc.nextDouble();

        balance += amt;

        lastTransaction = "Deposited ₹" + amt;

        System.out.println("Deposit Successful");
    }

    static void withdraw() {

        System.out.print("Enter Amount: ");
        double amt = sc.nextDouble();

        if (amt <= balance) {

            balance -= amt;

            lastTransaction = "Withdraw ₹" + amt;

            System.out.println("Withdrawal Successful");
        } else {
            System.out.println("Insufficient Balance");
        }
    }

    static void transfer() {

        System.out.print("Enter Receiver Account Number: ");
        int acc = sc.nextInt();

        System.out.print("Enter Amount: ");
        double amt = sc.nextDouble();

        if (amt <= balance) {

            balance -= amt;

            lastTransaction = "Transfer ₹" + amt + " to " + acc;

            System.out.println("Transfer Successful");
        } else {
            System.out.println("Insufficient Balance");
        }
    }

    static void changePIN() {

        System.out.print("Enter New PIN: ");
        pin = sc.nextInt();

        System.out.println("PIN Changed Successfully");
    }

    static void addNominee() {

        sc.nextLine();

        System.out.print("Enter Nominee Name: ");
        nominee = sc.nextLine();

        System.out.println("Nominee Added");
    }

    static void updateMobile() {

        sc.nextLine();

        System.out.print("Enter New Mobile Number: ");
        mobile = sc.nextLine();

        System.out.println("Mobile Updated");
    }

    static void updateAddress() {

        sc.nextLine();

        System.out.print("Enter New Address: ");
        address = sc.nextLine();

        System.out.println("Address Updated");
    }

    static void miniStatement() {

        System.out.println("\n===== MINI STATEMENT =====");

        System.out.println("Account Holder : " + name);
        System.out.println("Balance        : ₹" + balance);
        System.out.println("Last Transaction : " + lastTransaction);
    }
}