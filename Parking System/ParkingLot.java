import java.io.*;
import java.nio.file.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Scanner;

public class ParkingLot {

    static class Ticket {
        final String plate;
        final int slot;
        final LocalDateTime entryTime;

        Ticket(String plate, int slot, LocalDateTime entryTime) {
            this.plate = plate;
            this.slot = slot;
            this.entryTime = entryTime;
        }
    }

    static class Record {
        final String plate;
        final int slot;
        final LocalDateTime entryTime;
        final LocalDateTime exitTime;
        final long fee;

        Record(String plate, int slot, LocalDateTime entryTime, LocalDateTime exitTime, long fee) {
            this.plate = plate;
            this.slot = slot;
            this.entryTime = entryTime;
            this.exitTime = exitTime;
            this.fee = fee;
        }
    }

    private static final int FIRST_HOUR_RATE = 40;
    private static final int EXTRA_HOUR_RATE = 20;

    private static final String DATA_FILE = "parking.dat";
    private static final int FILE_VERSION = 1;

    private final int capacity;
    private final String dataFile;                                                // null = don't persist
    private final PriorityQueue<Integer> freeSlots = new PriorityQueue<>();       // min-heap
    private final Map<String, Ticket> parked = new HashMap<>();                   // plate -> ticket
    private final List<Record> history = new ArrayList<>();                       // finished visits

    public ParkingLot(int capacity, String dataFile) {
        this.capacity = capacity;
        this.dataFile = dataFile;
        for (int i = 1; i <= capacity; i++) freeSlots.offer(i);
    }

    public Ticket park(String plate) {
        return park(plate, LocalDateTime.now());
    }

    public Ticket park(String plate, LocalDateTime entryTime) {
        if (parked.containsKey(plate)) {
            System.out.println("Vehicle " + plate + " is already parked.");
            return null;
        }
        if (freeSlots.isEmpty()) {
            System.out.println("Parking lot full. Cannot park " + plate);
            return null;
        }
        int slot = freeSlots.poll();                  // O(log n) nearest slot
        Ticket t = new Ticket(plate, slot, entryTime);
        parked.put(plate, t);                         // O(1)
        System.out.println(plate + " parked at slot " + slot);
        save();
        return t;
    }
    public long unpark(String plate) {
        return unpark(plate, LocalDateTime.now());
    }

    public long unpark(String plate, LocalDateTime exitTime) {
        Ticket t = parked.remove(plate);              // O(1)
        if (t == null) {
            System.out.println("Vehicle " + plate + " not found.");
            return -1;
        }
        freeSlots.offer(t.slot);                      // slot becomes available again
        long fee = calculateFee(t.entryTime, exitTime);
        history.add(new Record(plate, t.slot, t.entryTime, exitTime, fee));
        System.out.println(plate + " left slot " + t.slot + ". Fee: Rs." + fee);
        save();
        return fee;
    }

    static long calculateFee(LocalDateTime entry, LocalDateTime exit) {
        long minutes = Duration.between(entry, exit).toMinutes();
        long hours = Math.max(1, (minutes + 59) / 60);
        return FIRST_HOUR_RATE + (hours - 1) * EXTRA_HOUR_RATE;
    }

    public int availableSlots() { return freeSlots.size(); }

    public void status() {
        long revenue = history.stream().mapToLong(r -> r.fee).sum();
        System.out.println("Occupied: " + parked.size() + "/" + capacity
                + " | Free: " + freeSlots.size()
                + " | Next free slot: " + (freeSlots.isEmpty() ? "none" : freeSlots.peek())
                + " | Total collected: Rs." + revenue);
    }

    public void listParked() {
        if (parked.isEmpty()) {
            System.out.println("No vehicles parked.");
            return;
        }
        parked.values().stream()
              .sorted((a, b) -> Integer.compare(a.slot, b.slot))
              .forEach(t -> System.out.println("Slot " + t.slot + " | " + t.plate
                      + " | entered " + t.entryTime.withNano(0)));
    }

    public void showHistory() {
        if (history.isEmpty()) {
            System.out.println("No completed visits yet.");
            return;
        }
        long total = 0;
        for (Record r : history) {
            System.out.println(r.plate + " | slot " + r.slot
                    + " | " + r.entryTime.withNano(0) + " -> " + r.exitTime.withNano(0)
                    + " | Rs." + r.fee);
            total += r.fee;
        }
        System.out.println("Visits: " + history.size() + " | Total collected: Rs." + total);
    }

    private void save() {
        if (dataFile == null) return;
        Path target = Paths.get(dataFile);
        Path tmp = Paths.get(dataFile + ".tmp");
        try (DataOutputStream out = new DataOutputStream(
                new BufferedOutputStream(Files.newOutputStream(tmp)))) {
            out.writeInt(FILE_VERSION);
            out.writeInt(capacity);

            out.writeInt(parked.size());
            for (Ticket t : parked.values()) {
                out.writeUTF(t.plate);
                out.writeInt(t.slot);
                out.writeUTF(t.entryTime.toString());
            }

            out.writeInt(history.size());
            for (Record r : history) {
                out.writeUTF(r.plate);
                out.writeInt(r.slot);
                out.writeUTF(r.entryTime.toString());
                out.writeUTF(r.exitTime.toString());
                out.writeLong(r.fee);
            }
        } catch (IOException e) {
            System.out.println("WARNING: could not save data: " + e.getMessage());
            return;
        }
        try {
            Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.out.println("WARNING: could not save data: " + e.getMessage());
        }
    }

    static ParkingLot load(String dataFile) {
        File f = new File(dataFile);
        if (!f.exists()) return null;
        try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(f)))) {
            int version = in.readInt();
            if (version != FILE_VERSION) throw new IOException("unknown file version " + version);
            int capacity = in.readInt();
            ParkingLot lot = new ParkingLot(capacity, dataFile);

            int parkedCount = in.readInt();
            for (int i = 0; i < parkedCount; i++) {
                String plate = in.readUTF();
                int slot = in.readInt();
                LocalDateTime entry = LocalDateTime.parse(in.readUTF());
                lot.parked.put(plate, new Ticket(plate, slot, entry));
                lot.freeSlots.remove(slot);           // slot is occupied, take it out of the heap
            }

            int historyCount = in.readInt();
            for (int i = 0; i < historyCount; i++) {
                String plate = in.readUTF();
                int slot = in.readInt();
                LocalDateTime entry = LocalDateTime.parse(in.readUTF());
                LocalDateTime exit = LocalDateTime.parse(in.readUTF());
                long fee = in.readLong();
                lot.history.add(new Record(plate, slot, entry, exit, fee));
            }
            return lot;
        } catch (IOException | RuntimeException e) {
            System.out.println("WARNING: could not read " + dataFile + " (" + e.getMessage() + ").");
            return null;
        }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        ParkingLot lot = load(DATA_FILE);
        if (lot != null) {
            System.out.println("Welcome back! Loaded saved data from " + DATA_FILE + ".");
            lot.status();
        } else {
            int capacity = 0;
            while (capacity <= 0) {
                System.out.print("Enter total number of parking slots: ");
                if (!sc.hasNextLine()) return;
                try {
                    capacity = Integer.parseInt(sc.nextLine().trim());
                } catch (NumberFormatException e) {
                    capacity = 0;
                }
                if (capacity <= 0) System.out.println("Please enter a positive number.");
            }
            lot = new ParkingLot(capacity, DATA_FILE);
            lot.save();
        }

        while (true) {
            System.out.println("\n    PARKING LOT MENU  ");
            System.out.println("1. Park a vehicle");
            System.out.println("2. Exit a vehicle (pay fee)");
            System.out.println("3. Show status");
            System.out.println("4. List parked vehicles");
            System.out.println("5. Show history of completed visits");
            System.out.println("6. Run sample demo (not saved)");
            System.out.println("7. Quit");
            System.out.print("Choose an option: ");

            if (!sc.hasNextLine()) break;
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1": {
                    System.out.print("Enter vehicle number: ");
                    String plate = sc.nextLine().trim().toUpperCase();
                    if (plate.isEmpty()) { System.out.println("Vehicle number cannot be empty."); break; }
                    lot.park(plate);
                    break;
                }
                case "2": {
                    System.out.print("Enter vehicle number: ");
                    String plate = sc.nextLine().trim().toUpperCase();
                    Ticket t = lot.parked.get(plate);
                    if (t == null) { lot.unpark(plate); break; }   // prints "not found"
                    System.out.print("Minutes parked (press Enter to use the real clock): ");
                    String m = sc.nextLine().trim();
                    if (m.isEmpty()) {
                        lot.unpark(plate);
                    } else {
                        try {
                            lot.unpark(plate, t.entryTime.plusMinutes(Long.parseLong(m)));
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid minutes. Vehicle not removed.");
                        }
                    }
                    break;
                }
                case "3": lot.status(); break;
                case "4": lot.listParked(); break;
                case "5": lot.showHistory(); break;
                case "6": runDemo(); break;
                case "7": System.out.println("Data saved. Goodbye!"); return;
                default: System.out.println("Invalid option. Enter 1-7.");
            }
        }
    }
    static void runDemo() {
        ParkingLot lot = new ParkingLot(3, null);
        LocalDateTime t0 = LocalDateTime.of(2026, 10, 29, 9, 0);

        lot.park("KA01AB1234", t0);                   // slot 1
        lot.park("MH12XY9999", t0.plusMinutes(5));    // slot 2
        lot.park("DL08CD5555", t0.plusMinutes(10));   // slot 3
        lot.park("TN09ZZ0001", t0.plusMinutes(15));   // full
        lot.status();

        lot.unpark("MH12XY9999", t0.plusMinutes(125)); // 2h -> Rs.60, slot 2 freed
        lot.status();

        lot.park("UP16EF7777", t0.plusMinutes(130));  // reuses slot 2 (nearest free)
        lot.unpark("KA01AB1234", t0.plusMinutes(30)); // <1h -> Rs.40, slot 1 freed
        lot.park("GJ05GH4321", t0.plusMinutes(140));  // gets slot 1
        lot.unpark("XX00XX0000");                     // not found
        lot.status();
    }
}