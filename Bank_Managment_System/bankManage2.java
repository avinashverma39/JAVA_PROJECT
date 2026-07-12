package Bank_Managment_System;

import java.util.*;

class Account {

    int accNo;
    String name;
    String mobile;
    String address;
    String pin;
    double balance;
    String nominee = "Not Added";
    int wrongAttempts = 0;
    boolean blocked = false;

    ArrayList<String> history = new ArrayList<>();

    Account(int accNo, String name, String mobile, String address, String pin, double balance) {
        this.accNo = accNo;
        this.name = name;
        this.mobile = mobile;
        this.address = address;
        this.pin = pin;
        this.balance = balance;
    }

    void addHistory(String msg) {
        history.add(msg);

        if (history.size() > 5) {
            history.remove(0);
        }
    }
}

public class bankManage2 {

    static Scanner sc = new Scanner(System.in);
    static HashMap<Integer, Account> bank = new HashMap<>();

    static int accountNumber = 1001;

    public static void main(String[] args) {

        int choice;

        do {

            System.out.println("\n----- BANK MANAGEMENT SYSTEM ------");
            System.out.println("1. Create Account");
            System.out.println("2. Login");
            System.out.println("3. Exit");

            System.out.print("Enter Choice: ");
            choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {

                case 1:
                    createAccount();
                    break;

                case 2:
                    login();
                    break;

                case 3:
                    System.out.println("Thank You!");
                    break;

                default:
                    System.out.println("Wrong Choice");

            }

        } while (choice != 3);

    }

    static void createAccount() {

        System.out.println("\n--- Create Account ---");

        System.out.print("Name: ");
        String name = sc.nextLine();

        System.out.print("Mobile: ");
        String mobile = sc.nextLine();

        System.out.print("Address: ");
        String address = sc.nextLine();

        System.out.print("Set PIN: ");
        String pin = sc.nextLine();

        System.out.print("Initial Deposit: ");
        double amount = sc.nextDouble();
        sc.nextLine();

        Account user = new Account(
                accountNumber,
                name,
                mobile,
                address,
                pin,
                amount);

        user.addHistory("Account Created : " + amount);

        bank.put(accountNumber, user);

        System.out.println("Account Created Successfully");
        System.out.println("Your Account Number : " + accountNumber);

        accountNumber++;

    }

    static void login() {

        System.out.println("\n--- Login ---");

        System.out.print("Account Number: ");
        int no = sc.nextInt();
        sc.nextLine();

        Account user = bank.get(no);

        if (user == null) {

            System.out.println("Account Not Found");
            return;
        }

        if (user.blocked) {

            System.out.println("Account Blocked");
            return;
        }

        System.out.print("Enter PIN: ");
        String pin = sc.nextLine();

        if (pin.equals(user.pin)) {

            user.wrongAttempts = 0;

            System.out.println("Login Successful");

            menu(user);

        }

        else {

            user.wrongAttempts++;

            if (user.wrongAttempts == 3) {

                user.blocked = true;
                System.out.println("Account Blocked");

            }

            else {

                System.out.println(
                        "Wrong PIN Attempts Left : "
                                + (3 - user.wrongAttempts));

            }

        }

    }

    static void menu(Account user) {

        int choice;

        do {

            System.out.println("\n--- Account Menu ---");

            System.out.println("1. Deposit");
            System.out.println("2. Withdraw");
            System.out.println("3. Transfer");
            System.out.println("4. Check Balance");
            System.out.println("5. Change PIN");
            System.out.println("6. Add Nominee");
            System.out.println("7. Update Mobile");
            System.out.println("8. Update Address");
            System.out.println("9. Mini Statement");
            System.out.println("10. Logout");

            System.out.print("Choice: ");

            choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {

                case 1:
                    deposit(user);
                    break;

                case 2:
                    withdraw(user);
                    break;

                case 3:
                    transfer(user);
                    break;

                case 4:
                    System.out.println("Balance : " + user.balance);
                    break;

                case 5:
                    changePin(user);
                    break;

                case 6:
                    nominee(user);
                    break;

                case 7:
                    mobile(user);
                    break;

                case 8:
                    address(user);
                    break;

                case 9:
                    statement(user);
                    break;

                case 10:
                    System.out.println("Logout");
                    break;

                default:
                    System.out.println("Invalid Choice");

            }

        } while (choice != 10);

    }

    static void deposit(Account user) {

        System.out.print("Enter Amount: ");
        double money = sc.nextDouble();

        user.balance += money;

        user.addHistory("Deposited : " + money);

        System.out.println("Deposit Successful");

    }

    static void withdraw(Account user) {

        System.out.print("Enter Amount: ");
        double money = sc.nextDouble();

        if (money > user.balance) {

            System.out.println("Insufficient Balance");

        }

        else {

            user.balance -= money;

            user.addHistory("Withdraw : " + money);

            System.out.println("Withdrawal Successful");

        }

    }

    static void transfer(Account user) {

        System.out.print("Receiver Account Number: ");

        int receiver = sc.nextInt();

        Account r = bank.get(receiver);

        if (r == null) {

            System.out.println("Account Not Found");
            return;

        }

        System.out.print("Amount: ");

        double money = sc.nextDouble();

        if (money > user.balance) {

            System.out.println("Insufficient Balance");

        }

        else {

            user.balance -= money;

            r.balance += money;

            user.addHistory("Send " + money + " to " + receiver);

            r.addHistory("Received " + money + " from " + user.accNo);

            System.out.println("Transfer Successful");

        }

    }

    static void changePin(Account user) {

        System.out.print("Old PIN: ");

        String old = sc.next();

        if (old.equals(user.pin)) {

            System.out.print("New PIN: ");

            user.pin = sc.next();

            System.out.println("PIN Changed");

        }

        else {

            System.out.println("Wrong PIN");

        }

    }

    static void nominee(Account user) {

        System.out.print("Nominee Name: ");

        user.nominee = sc.next();

        System.out.println("Nominee Added");

    }

    static void mobile(Account user) {

        System.out.print("New Mobile: ");

        user.mobile = sc.next();

        System.out.println("Mobile Updated");

    }

    static void address(Account user) {

        System.out.print("New Address: ");

        user.address = sc.next();

        System.out.println("Address Updated");

    }

    static void statement(Account user) {

        System.out.println("\nLast Transactions");

        for (String s : user.history) {

            System.out.println(s);

        }

    }

}