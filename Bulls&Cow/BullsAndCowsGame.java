import java.io.*;
import java.util.*;

interface ComputerPlayer {
    String generateSecretCode();

    String makeGuess();
}

// EASY COMPUTER
class EasyComputerPlayer implements ComputerPlayer {

    public String generateSecretCode() {
        return generateCode();
    }

    public String makeGuess() {
        return String.format("%04d",
                new Random().nextInt(10000));
    }

    protected String generateCode() {
        Random r = new Random();
        StringBuilder code = new StringBuilder();

        while (code.length() < 4) {
            int n = r.nextInt(10);
            if (code.indexOf("" + n) == -1)
                code.append(n);
        }
        return code.toString();
    }
}

// MEDIUM COMPUTER
class MediumComputerPlayer extends EasyComputerPlayer {

    HashSet<String> guesses = new HashSet<>();

    public String makeGuess() {

        String guess;

        do {
            guess = String.format("%04d",
                    new Random().nextInt(10000));
        } while (guesses.contains(guess));

        guesses.add(guess);

        return guess;
    }
}

// HARD COMPUTER
class HardComputerPlayer extends MediumComputerPlayer {

    public String makeGuess() {

        Random r = new Random();

        StringBuilder s = new StringBuilder();

        while (s.length() < 4) {

            int n = r.nextInt(10);

            if (s.indexOf("" + n) == -1)
                s.append(n);
        }

        String guess = s.toString();

        guesses.add(guess);

        return guess;
    }
}

class Player {

    private String secretCode;

    public void setSecretCode(String code) {
        secretCode = code;
    }

    public String getSecretCode() {
        return secretCode;
    }

    public String makeGuess(Scanner sc) {

        String guess;

        while (true) {

            System.out.print("Enter your guess: ");

            guess = sc.nextLine();

            if (valid(guess))
                return guess;

            System.out.println(
                    "Enter exactly 4 different digits!");
        }
    }

    private boolean valid(String s) {

        return s.matches("\\d{4}")
                &&
                s.chars().distinct().count() == 4;
    }

}

public class BullsAndCowsGame {

    static Scanner sc = new Scanner(System.in);

    StringBuilder result = new StringBuilder();

    public static void main(String args[]) {

        BullsAndCowsGame game = new BullsAndCowsGame();

        while (true) {

            System.out.println("\n==== BULLS AND COWS ====");

            System.out.println("1. Single Player");
            System.out.println("2. Computer vs Player");
            System.out.println("3. Exit");

            System.out.print("Choice : ");

            int ch = Integer.parseInt(sc.nextLine());

            if (ch == 3)
                break;

            if (ch == 1)
                game.singlePlayer();

            else if (ch == 2)
                game.twoPlayer();

            else
                System.out.println("Wrong choice");

        }

    }

    void singlePlayer() {

        Player p = new Player();

        ComputerPlayer cpu = new EasyComputerPlayer();

        String secret = cpu.generateSecretCode();

        play(p, secret);

        save();

    }

    void twoPlayer() {

        Player p = new Player();

        System.out.print(
                "Enter your secret code : ");

        String code = sc.nextLine();

        while (!valid(code)) {

            System.out.println(
                    "Invalid code!");

            code = sc.nextLine();
        }

        p.setSecretCode(code);

        System.out.println(
                "Select Level");

        System.out.println(
                "1 Easy\n2 Medium\n3 Hard");

        String level = sc.nextLine();

        ComputerPlayer cpu;

        if (level.equals("1"))

            cpu = new EasyComputerPlayer();

        else if (level.equals("2"))

            cpu = new MediumComputerPlayer();

        else

            cpu = new HardComputerPlayer();

        String cpuSecret = cpu.generateSecretCode();

        int turn = 0;

        while (turn < 7) {

            String guess = p.makeGuess(sc);

            String r = check(guess, cpuSecret);

            System.out.println(r);

            if (r.equals("4 Bulls 0 Cows")) {

                System.out.println(
                        "You Win!");

                break;
            }

            String cpuGuess = cpu.makeGuess();

            String cr = check(cpuGuess,
                    p.getSecretCode());

            System.out.println(
                    "Computer guessed "
                            + cpuGuess + " : " + cr);

            if (cr.equals("4 Bulls 0 Cows")) {

                System.out.println(
                        "Computer Wins!");

                break;
            }

            turn++;

        }

        save();

    }

    void play(Player p, String secret) {

        int tries = 0;

        while (tries < 7) {

            String guess = p.makeGuess(sc);

            String r = check(guess, secret);

            System.out.println(r);

            result.append(
                    guess + " -> " + r + "\n");

            if (r.equals("4 Bulls 0 Cows")) {

                System.out.println(
                        "Congratulations!");

                return;
            }

            tries++;

        }

        System.out.println(
                "Game Over!");

        System.out.println(
                "Secret was " + secret);

    }

    String check(String guess, String secret) {

        int bulls = 0;
        int cows = 0;

        for (int i = 0; i < 4; i++) {

            if (guess.charAt(i) == secret.charAt(i))

                bulls++;

            else if (secret.contains(
                    "" + guess.charAt(i)))

                cows++;

        }

        return bulls +
                " Bulls " + cows +
                " Cows";

    }

    boolean valid(String s) {

        return s.matches("\\d{4}")
                &&
                s.chars().distinct().count() == 4;

    }

    void save() {

        System.out.print(
                "Save result? yes/no : ");

        String a = sc.nextLine();

        if (a.equalsIgnoreCase("yes")) {

            try {

                FileWriter fw = new FileWriter(
                        "BullsAndCowsResult.txt",
                        true);

                fw.write(
                        "\n--- GAME ---\n");

                fw.write(
                        result.toString());

                fw.close();

                System.out.println(
                        "Saved successfully");

            }

            catch (Exception e) {

                System.out.println(
                        "Saving error");

            }

        }

    }

}