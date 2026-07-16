package Bank_Management_System_2;

import java.io.*;
import java.util.*;

class Transaction implements Serializable {
    private static final long serialVersionUID = 1L;
    String type;
    double amount;
    double balanceAfter;

    Transaction(String type, double amount, double balanceAfter) {
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
    }
}

class Account implements Serializable {

    private static final long serialVersionUID = 1L;
    static final int MAX_TRANS = 5;

    int accNo;
    String name;
    String pin;
    double balance;
    String mobile;
    String address;
    String nomineeName;
    int wrongAttempts;
    boolean blocked;

    Transaction[] history = new Transaction[MAX_TRANS];
    int transCount = 0;

    void addTransaction(String type, double amount) {
        int slot = transCount % MAX_TRANS;
        history[slot] = new Transaction(type, amount, balance);
        transCount++;
    }

    List<Transaction> lastTransactions() {
        List<Transaction> list = new ArrayList<>();
        int total = Math.min(transCount, MAX_TRANS);
        int start = transCount < MAX_TRANS ? 0 : transCount % MAX_TRANS;
        for (int i = 0; i < total; i++) {
            int idx = (start + i) % MAX_TRANS;
            list.add(history[idx]);
        }
        return list;
    }
}

class BankData implements Serializable {
    private static final long serialVersionUID = 1L;
    List<Account> accounts = new ArrayList<>();
    int nextAccNo = 1001;
}

public class BankSystem {

    static final String FILE_NAME = "bank.dat";
    static final int PIN_LEN = 4;
    static final int MAX_WRONG_PIN = 3;

    static BankData data = new BankData();
    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        loadData();
        System.out.println("Loaded " + data.accounts.size() + " existing account(s) from " + FILE_NAME + ".");
        mainMenu();
        sc.close();
    }

    /* ---------------- File Persistence ---------------- */

    static void saveData() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            out.writeObject(data);
        } catch (IOException e) {
            System.out.println("Error: could not save data to " + FILE_NAME + " (" + e.getMessage() + ")");
        }
    }

    static void loadData() {
        File f = new File(FILE_NAME);
        if (!f.exists()) {
            data = new BankData();
            return;
        }
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(FILE_NAME))) {
            data = (BankData) in.readObject();
        }

        catch (IOException | ClassNotFoundException e) {
            System.out.println("Warning: could not load existing data, starting fresh (" + e.getMessage() + ")");
            data = new BankData();
        }
    }

    /* ---------------- Helpers ---------------- */

    static Account findAccount(int accNo) {
        for (Account a : data.accounts) {
            if (a.accNo == accNo)
                return a;
        }
        return null;
    }

    static boolean isValidPinFormat(String pin) {
        if (pin == null || pin.length() != PIN_LEN)
            return false;
        for (char c : pin.toCharArray()) {
            if (!Character.isDigit(c))
                return false;
        }
        return true;
    }

    static String readLine(String prompt) {
        System.out.print(prompt);
        return sc.nextLine().trim();
    }

    static int readInt(String prompt) {
        System.out.print(prompt);
        String line = sc.nextLine().trim();
        try {
            return Integer.parseInt(line);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    static double readDouble(String prompt) {
        System.out.print(prompt);
        String line = sc.nextLine().trim();
        try {
            return Double.parseDouble(line);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    static void pause() {
        System.out.print("\nPress Enter to continue...");
        sc.nextLine();
    }

    /* ---------------- Core Features ---------------- */

    static void createAccount() {
        System.out.println("\n--- Create Account ---");
        Account acc = new Account();
        acc.accNo = data.nextAccNo;

        acc.name = readLine("Enter Name: ");

        String pin;
        do {
            pin = readLine("Set a 4-digit PIN: ");
            if (!isValidPinFormat(pin)) {
                System.out.println("Invalid PIN. It must be exactly 4 digits.");
            }
        } while (!isValidPinFormat(pin));
        acc.pin = pin;

        double dep = readDouble("Enter Initial Deposit Amount: ");
        if (dep < 0)
            dep = 0;
        acc.balance = dep;

        acc.mobile = readLine("Enter Mobile Number: ");
        acc.address = readLine("Enter Address: ");
        acc.nomineeName = readLine("Enter Nominee Name (optional, press Enter to skip): ");

        acc.wrongAttempts = 0;
        acc.blocked = false;

        if (dep > 0)
            acc.addTransaction("DEPOSIT", dep);

        data.accounts.add(acc);
        data.nextAccNo++;
        saveData();

        System.out.println("\nAccount created successfully!");
        System.out.println("Your Account Number is: " + acc.accNo + " (please note this down)");
    }

    static Account login() {
        System.out.println("\n--- Login ---");
        int accNo = readInt("Enter Account Number: ");

        Account acc = findAccount(accNo);
        if (acc == null) {
            System.out.println("Account not found.");
            return null;
        }

        if (acc.blocked) {
            System.out.println("This account is BLOCKED due to too many wrong PIN attempts.");
            System.out.println("Please contact bank support.");
            return null;
        }

        for (int attempt = 1; attempt <= MAX_WRONG_PIN; attempt++) {
            String pin = readLine("Enter PIN: ");

            if (pin.equals(acc.pin)) {
                acc.wrongAttempts = 0;
                saveData();
                System.out.println("Login successful. Welcome, " + acc.name + "!");
                return acc;
            }

            else {
                acc.wrongAttempts++;
                int remaining = MAX_WRONG_PIN - acc.wrongAttempts;
                if (acc.wrongAttempts >= MAX_WRONG_PIN) {
                    acc.blocked = true;
                    saveData();
                    System.out.println("Incorrect PIN. Account is now BLOCKED.");
                    return null;
                } else {
                    System.out.println("Incorrect PIN. " + remaining + " attempt(s) remaining.");
                }
            }
        }

        saveData();
        return null;
    }

    static void deposit(Account acc) {
        System.out.println("\n--- Deposit ---");
        double amt = readDouble("Enter amount to deposit: ");

        if (amt <= 0) {
            System.out.println("Invalid amount.");
            return;
        }

        acc.balance += amt;
        acc.addTransaction("DEPOSIT", amt);
        saveData();
        System.out.printf("Deposit successful. New Balance: %.2f%n", acc.balance);
    }

    static void withdraw(Account acc) {
        System.out.println("\n--- Withdraw ---");
        double amt = readDouble("Enter amount to withdraw: ");

        if (amt <= 0) {
            System.out.println("Invalid amount.");
            return;
        }
        if (amt > acc.balance) {
            System.out.printf("Insufficient balance. Current balance: %.2f%n", acc.balance);
            return;
        }

        acc.balance -= amt;
        acc.addTransaction("WITHDRAW", amt);
        saveData();
        System.out.printf("Withdrawal successful. New Balance: %.2f%n", acc.balance);
    }

    static void transferMoney(Account acc) {
        System.out.println("\n--- Transfer ---");
        int destNo = readInt("Enter recipient Account Number: ");

        if (destNo == acc.accNo) {
            System.out.println("You cannot transfer to your own account.");
            return;
        }

        Account dest = findAccount(destNo);
        if (dest == null) {
            System.out.println("Recipient account not found.");
            return;
        }
        if (dest.blocked) {
            System.out.println("Recipient account is blocked. Transfer cancelled.");
            return;
        }

        double amt = readDouble("Enter amount to transfer: ");

        if (amt <= 0) {
            System.out.println("Invalid amount.");
            return;
        }
        if (amt > acc.balance) {
            System.out.printf("Insufficient balance. Current balance: %.2f%n", acc.balance);
            return;
        }

        acc.balance -= amt;
        dest.balance += amt;

        acc.addTransaction("TRANSFER-OUT", amt);
        dest.addTransaction("TRANSFER-IN", amt);

        saveData();
        System.out.printf("Transfer successful. New Balance: %.2f%n", acc.balance);
    }

    static void checkBalance(Account acc) {
        System.out.println("\n--- Balance ---");
        System.out.println("Account Holder: " + acc.name);
        System.out.println("Account Number: " + acc.accNo);
        System.out.printf("Current Balance: %.2f%n", acc.balance);
    }

    static void changePin(Account acc) {
        System.out.println("\n--- Change PIN ---");
        String oldPin = readLine("Enter current PIN: ");

        if (!oldPin.equals(acc.pin)) {
            System.out.println("Incorrect current PIN.");
            return;
        }

        String newPin;
        do {
            newPin = readLine("Enter new 4-digit PIN: ");
            if (!isValidPinFormat(newPin)) {
                System.out.println("Invalid PIN. It must be exactly 4 digits.");
            }
        } while (!isValidPinFormat(newPin));

        acc.pin = newPin;
        saveData();
        System.out.println("PIN changed successfully.");
    }

    static void updateMobile(Account acc) {
        System.out.println("\n--- Update Mobile Number ---");
        System.out.println("Current Mobile: " + acc.mobile);
        acc.mobile = readLine("Enter new Mobile Number: ");
        saveData();
        System.out.println("Mobile number updated successfully.");
    }

    static void updateAddress(Account acc) {
        System.out.println("\n--- Update Address ---");
        System.out.println("Current Address: " + acc.address);
        acc.address = readLine("Enter new Address: ");
        saveData();
        System.out.println("Address updated successfully.");
    }

    static void addNominee(Account acc) {
        System.out.println("\n--- Add / Update Nominee ---");
        if (acc.nomineeName != null && !acc.nomineeName.isEmpty()) {
            System.out.println("Current Nominee: " + acc.nomineeName);
        }
        acc.nomineeName = readLine("Enter Nominee Name: ");
        saveData();
        System.out.println("Nominee updated successfully.");
    }

    static void miniStatement(Account acc) {
        System.out.println("\n--- Mini Statement (last " + Account.MAX_TRANS + " transactions) ---");
        List<Transaction> list = acc.lastTransactions();
        if (list.isEmpty()) {
            System.out.println("No transactions yet.");
            return;
        }

        System.out.printf("%-15s %-12s %-12s%n", "TYPE", "AMOUNT", "BALANCE");
        System.out.println("--------------------------------------------");
        for (Transaction t : list) {
            System.out.printf("%-15s %-12.2f %-12.2f%n", t.type, t.amount, t.balanceAfter);
        }
    }

    /* ---------------- Menus ---------------- */

    static void accountMenu(Account acc) {
        int choice;
        do {
            System.out.println("\n       Account Menu (Acc No: " + acc.accNo + ")     ");
            System.out.println("1. Deposit");
            System.out.println("2. Withdraw");
            System.out.println("3. Transfer");
            System.out.println("4. Check Balance");
            System.out.println("5. Change PIN");
            System.out.println("6. Update Mobile");
            System.out.println("7. Update Address");
            System.out.println("8. Add Nominee");
            System.out.println("9. Mini Statement");
            System.out.println("0. Logout");
            choice = readInt("Enter choice: ");

            switch (choice) {
                case 1:
                    deposit(acc);
                    break;
                case 2:
                    withdraw(acc);
                    break;
                case 3:
                    transferMoney(acc);
                    break;
                case 4:
                    checkBalance(acc);
                    break;
                case 5:
                    changePin(acc);
                    break;
                case 6:
                    updateMobile(acc);
                    break;
                case 7:
                    updateAddress(acc);
                    break;
                case 8:
                    addNominee(acc);
                    break;
                case 9:
                    miniStatement(acc);
                    break;
                case 0:
                    System.out.println("Logging out...");
                    break;
                default:
                    System.out.println("Invalid choice. Try again.");
            }

            if (choice != 0)
                pause();

        } while (choice != 0);
    }

    static void mainMenu() {
        int choice;
        do {
            System.out.println("\n      BANK MANAGEMENT SYSTEM    ");
            System.out.println("1. Create Account");
            System.out.println("2. Login");
            System.out.println("0. Exit");
            choice = readInt("Enter choice: ");

            switch (choice) {
                case 1:
                    createAccount();
                    pause();
                    break;
                case 2:
                    Account acc = login();
                    if (acc != null) {
                        accountMenu(acc);
                    } else {
                        pause();
                    }
                    break;
                case 0:
                    System.out.println("Saving data and exiting. Goodbye!");
                    saveData();
                    break;
                default:
                    System.out.println("Invalid choice. Try again.");
                    pause();
            }
        } while (choice != 0);
    }
}