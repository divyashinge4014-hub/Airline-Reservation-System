import java.util.*;
import java.sql.*;
import java.time.LocalDateTime;

public class AirlineReservationSystem {
    // DB URL (file-based SQLite)
    private static final String DB_URL = "jdbc:sqlite:ars.db";
    private static final Scanner sc = new Scanner(System.in);
    // Currently logged-in user
    private static User currentUser = null;

    public static void main(String[] args) {
        try {
            initDatabase();
            seedIfEmpty();
            showWelcome();
            mainMenuLoop();
        } catch (Exception e) {
            System.out.println("Fatal error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // --------- Models ----------
    static class User {
        int id;
        String name;
        String email;
        String password;
        String role; // "admin" or "customer"

        User(int id, String name, String email, String password, String role) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.password = password;
            this.role = role;
        }
    }

    static class Flight {
        int id;
        String flightNumber;
        String source;
        String destination;
        String departure; // ISO datetime string
        String arrival;
        double price;
        int seatsAvailable;
        int totalSeats;

        Flight(int id, String flightNumber, String source, String destination,
                String departure, String arrival, double price, int seatsAvailable, int totalSeats) {
            this.id = id;
            this.flightNumber = flightNumber;
            this.source = source;
            this.destination = destination;
            this.departure = departure;
            this.arrival = arrival;
            this.price = price;
            this.seatsAvailable = seatsAvailable;
            this.totalSeats = totalSeats;
        }
    }

    // --------- DB Init & Seed ----------
    private static void initDatabase() throws SQLException {
        try {
        Class.forName("org.sqlite.JDBC");   // ✅ load driver
    } catch (ClassNotFoundException e) {
        throw new SQLException("SQLite JDBC driver not found", e);
    }
        try (
            
            
            Connection conn = DriverManager.getConnection(DB_URL)) {
            Statement st = conn.createStatement();
            // Users
            st.executeUpdate("CREATE TABLE IF NOT EXISTS users (" +
                    "user_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT NOT NULL, email TEXT UNIQUE NOT NULL, password TEXT NOT NULL, role TEXT NOT NULL)");

            // Flights
            st.executeUpdate("CREATE TABLE IF NOT EXISTS flights (" +
                    "flight_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "flight_number TEXT NOT NULL," +
                    "source TEXT NOT NULL, destination TEXT NOT NULL," +
                    "departure TEXT NOT NULL, arrival TEXT NOT NULL," +
                    "price REAL NOT NULL, seats_available INTEGER NOT NULL, total_seats INTEGER NOT NULL)");

            // Reservations
            st.executeUpdate("CREATE TABLE IF NOT EXISTS reservations (" +
                    "reservation_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_id INTEGER NOT NULL, flight_id INTEGER NOT NULL," +
                    "seat_number TEXT, status TEXT NOT NULL, booking_date TEXT NOT NULL," +
                    "FOREIGN KEY(user_id) REFERENCES users(user_id)," +
                    "FOREIGN KEY(flight_id) REFERENCES flights(flight_id))");

            // Payments
            st.executeUpdate("CREATE TABLE IF NOT EXISTS payments (" +
                    "payment_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "reservation_id INTEGER NOT NULL, amount REAL NOT NULL, payment_status TEXT NOT NULL, payment_date TEXT NOT NULL,"
                    +
                    "FOREIGN KEY(reservation_id) REFERENCES reservations(reservation_id))");
            st.close();
        }
    }

    private static void seedIfEmpty() throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            // If no admin exists, create default admin and some flights
            PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM users");
            ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getInt(1) == 0) {
                PreparedStatement p = conn.prepareStatement(
                        "INSERT INTO users (name,email,password,role) VALUES (?,?,?,?)");
                p.setString(1, "Admin");
                p.setString(2, "admin@ars.com");
                p.setString(3, "admin123"); // in production, hash passwords
                p.setString(4, "admin");
                p.executeUpdate();
                p.close();

                // Add a default customer
                p = conn.prepareStatement("INSERT INTO users (name,email,password,role) VALUES (?,?,?,?)");
                p.setString(1, "Demo User");
                p.setString(2, "demo@ars.com");
                p.setString(3, "demo123");
                p.setString(4, "customer");
                p.executeUpdate();
                p.close();

                // Add sample flights
                PreparedStatement f = conn.prepareStatement(
                        "INSERT INTO flights (flight_number,source,destination,departure,arrival,price,seats_available,total_seats) "
                                +
                                "VALUES (?,?,?,?,?,?,?,?)");
                f.setString(1, "ARS101");
                f.setString(2, "Mumbai");
                f.setString(3, "Delhi");
                f.setString(4, "2025-09-10T08:00");
                f.setString(5, "2025-09-10T10:00");
                f.setDouble(6, 5000);
                f.setInt(7, 10);
                f.setInt(8, 10);
                f.executeUpdate();

                f.setString(1, "ARS202");
                f.setString(2, "Delhi");
                f.setString(3, "Bengaluru");
                f.setString(4, "2025-09-11T14:00");
                f.setString(5, "2025-09-11T16:30");
                f.setDouble(6, 4500);
                f.setInt(7, 15);
                f.setInt(8, 15);
                f.executeUpdate();

                f.setString(1, "ARS303");
                f.setString(2, "Kolkata");
                f.setString(3, "Chennai");
                f.setString(4, "2025-09-12T09:30");
                f.setString(5, "2025-09-12T12:00");
                f.setDouble(6, 5500);
                f.setInt(7, 20);
                f.setInt(8, 20);
                f.executeUpdate();

                f.close();
                System.out.println("Seeded default admin, demo user, and sample flights.");
            }
            rs.close();
            ps.close();
        }
    }

    // --------- Menus ----------
    private static void showWelcome() {
        System.out.println("======================================");
        System.out.println("   Welcome to Simple Airline System   ");
        System.out.println("======================================");
    }

    private static void mainMenuLoop() {
        while (true) {
            System.out.println("\nMain Menu");
            System.out.println("1) Register");
            System.out.println("2) Login");
            System.out.println("3) Search Flights");
            System.out.println("4) Exit");
            System.out.print("Choose: ");
            String choice = sc.nextLine().trim();
            switch (choice) {
                case "1":
                    registerFlow();
                    break;
                case "2":
                    loginFlow();
                    break;
                case "3":
                    try {
                        searchFlightsFlow(null);
                    } catch (SQLException e) {
                        System.out.println("Error searching flights: " + e.getMessage());
                    }
                    break;

                case "4":
                    System.out.println("Goodbye!");
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    // --------- Auth ----------
    private static void registerFlow() {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            System.out.println("\n--- Register ---");
            System.out.print("Name: ");
            String name = sc.nextLine().trim();
            System.out.print("Email: ");
            String email = sc.nextLine().trim();
            System.out.print("Password: ");
            String pwd = sc.nextLine().trim();
            String role = "customer";

            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO users (name,email,password,role) VALUES (?,?,?,?)");
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, pwd);
            ps.setString(4, role);
            ps.executeUpdate();
            ps.close();
            System.out.println("Registered successfully. Please login.");
        } catch (SQLException e) {
            System.out.println("Registration failed: " + e.getMessage());
        }
    }

    private static void loginFlow() {
        System.out.println("\n--- Login ---");
        System.out.print("Email: ");
        String email = sc.nextLine().trim();
        System.out.print("Password: ");
        String pwd = sc.nextLine().trim();

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM users WHERE email = ? AND password = ?");
            ps.setString(1, email);
            ps.setString(2, pwd);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                currentUser = new User(
                        rs.getInt("user_id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getString("role"));
                System.out.println("Welcome, " + currentUser.name + " (" + currentUser.role + ")");
                if ("admin".equals(currentUser.role))
                    adminMenu();
                else
                    customerMenu();
            } else {
                System.out.println("Invalid credentials.");
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            System.out.println("Login failed: " + e.getMessage());
        }
    }

    // --------- Admin Menu ----------
    private static void adminMenu() {
        while (true) {
            System.out.println("\n--- Admin Menu ---");
            System.out.println("1) Add Flight");
            System.out.println("2) Edit Flight (price/seats)");
            System.out.println("3) View All Flights");
            System.out.println("4) View Reports");
            System.out.println("5) Logout");
            System.out.print("Choose: ");
            String c = sc.nextLine().trim();
            try {
                switch (c) {
                    case "1":
                        addFlight();
                        break;
                    case "2":
                        editFlight();
                        break;
                    case "3":
                        listAllFlights();
                        break;
                    case "4":
                        reportsMenu();
                        break;
                    case "5":
                        currentUser = null;
                        return;
                    default:
                        System.out.println("Invalid option.");
                        break;
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private static void addFlight() throws SQLException {
        System.out.println("\n--- Add Flight ---");
        System.out.print("Flight Number: ");
        String fn = sc.nextLine().trim();
        System.out.print("Source: ");
        String src = sc.nextLine().trim();
        System.out.print("Destination: ");
        String dst = sc.nextLine().trim();
        System.out.print("Departure (YYYY-MM-DDTHH:MM): ");
        String dep = sc.nextLine().trim();
        System.out.print("Arrival (YYYY-MM-DDTHH:MM): ");
        String arr = sc.nextLine().trim();
        System.out.print("Price: ");
        double price = Double.parseDouble(sc.nextLine().trim());
        System.out.print("Total Seats: ");
        int seats = Integer.parseInt(sc.nextLine().trim());

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO flights (flight_number,source,destination,departure,arrival,price,seats_available,total_seats) "
                            +
                            "VALUES (?,?,?,?,?,?,?,?)");
            ps.setString(1, fn);
            ps.setString(2, src);
            ps.setString(3, dst);
            ps.setString(4, dep);
            ps.setString(5, arr);
            ps.setDouble(6, price);
            ps.setInt(7, seats);
            ps.setInt(8, seats);
            ps.executeUpdate();
            ps.close();
            System.out.println("Flight added.");
        }
    }

    private static void editFlight() throws SQLException {
        listAllFlights();
        System.out.print("Enter flight ID to edit: ");
        String fidStr = sc.nextLine().trim();
        int fid = Integer.parseInt(fidStr);
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM flights WHERE flight_id = ?");
            ps.setInt(1, fid);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                System.out.println("Flight not found.");
                return;
            }
            System.out.println("Current price: " + rs.getDouble("price") + ", seats_available: " +
                    rs.getInt("seats_available"));
            System.out.print("New price (or blank to skip): ");
            String priceS = sc.nextLine().trim();
            System.out.print("New total seats (or blank to skip): ");
            String seatsS = sc.nextLine().trim();

            if (!priceS.isEmpty()) {
                double p = Double.parseDouble(priceS);
                PreparedStatement up = conn.prepareStatement("UPDATE flights SET price = ? WHERE flight_id = ?");
                up.setDouble(1, p);
                up.setInt(2, fid);
                up.executeUpdate();
                up.close();
            }
            if (!seatsS.isEmpty()) {
                int total = Integer.parseInt(seatsS);
                // Adjust seats_available proportionally: we set seats_available = total -
                // (old_total -
                int old_total = rs.getInt("total_seats");
                int old_available = rs.getInt("seats_available");
                int booked = old_total - old_available;
                int new_available = total - booked;
                if (new_available < 0)
                    new_available = 0;
                PreparedStatement up2 = conn.prepareStatement(
                        "UPDATE flights SET total_seats = ?, seats_available = ? WHERE flight_id = ?");
                up2.setInt(1, total);
                up2.setInt(2, new_available);
                up2.setInt(3, fid);
                up2.executeUpdate();
                up2.close();
            }
            rs.close();
            ps.close();
            System.out.println("Flight updated.");
        }
    }

    private static void listAllFlights() throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM flights");
            ResultSet rs = ps.executeQuery();
            System.out.println("\nFlights:");
            System.out.println("ID | Flight# | From -> To | Dep -> Arr | Price | AvlSeats/Total");
            while (rs.next()) {
                System.out.printf("%d | %s | %s -> %s | %s -> %s | %.2f | %d/%d%n",
                        rs.getInt("flight_id"),
                        rs.getString("flight_number"),
                        rs.getString("source"),
                        rs.getString("destination"),
                        rs.getString("departure"),
                        rs.getString("arrival"),
                        rs.getDouble("price"),
                        rs.getInt("seats_available"),
                        rs.getInt("total_seats"));
            }
            rs.close();
            ps.close();
        }
    }

    // --------- Customer Menu ----------
    private static void customerMenu() {
        while (true) {
            System.out.println("\n--- Customer Menu ---");
            System.out.println("1) Search Flights");
            System.out.println("2) My Reservations");
            System.out.println("3) Logout");
            System.out.print("Choose: ");
            String c = sc.nextLine().trim();
            try {
                switch (c) {
                    case "1":
                        searchFlightsFlow(currentUser);
                        break;
                    case "2":
                        myReservations();
                        break;
                    case "3":
                        currentUser = null;
                        return;
                    default:
                        System.out.println("Invalid option.");
                        break;
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    // --------- Search & Booking ----------
    // If user is null => simple search (no booking).
    private static void searchFlightsFlow(User user) throws SQLException {
        System.out.println("\n--- Search Flights ---");
        System.out.print("From (leave blank for all): ");
        String from = sc.nextLine().trim();
        System.out.print("To (leave blank for all): ");
        String to = sc.nextLine().trim();
        // optional date filter omitted for simplicity
        String sql = "SELECT * FROM flights WHERE 1=1";
        List<String> params = new ArrayList<>();
        if (!from.isEmpty()) {
            sql += " AND LOWER(source) = LOWER(?)";
            params.add(from);
        }
        if (!to.isEmpty()) {
            sql += " AND LOWER(destination) = LOWER(?)";
            params.add(to);
        }

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            PreparedStatement ps = conn.prepareStatement(sql);
            for (int i = 0; i < params.size(); i++)
                ps.setString(i + 1, params.get(i));
            ResultSet rs = ps.executeQuery();
            List<Flight> results = new ArrayList<>();
            System.out.println("ID | Flight# | From->To | Dep | Arr | Price | Avl");
            while (rs.next()) {
                Flight f = new Flight(
                        rs.getInt("flight_id"),
                        rs.getString("flight_number"),
                        rs.getString("source"),
                        rs.getString("destination"),
                        rs.getString("departure"),
                        rs.getString("arrival"),
                        rs.getDouble("price"),
                        rs.getInt("seats_available"),
                        rs.getInt("total_seats"));
                results.add(f);
                System.out.printf("%d | %s | %s->%s | %s | %s | %.2f | %d%n",
                        f.id, f.flightNumber, f.source, f.destination, f.departure, f.arrival, f.price,
                        f.seatsAvailable);
            }
            rs.close();
            ps.close();

            if (user != null && !results.isEmpty()) {
                System.out.print("Enter flight ID to book or blank to return: ");
                String s = sc.nextLine().trim();
                if (!s.isEmpty()) {
                    int fid = Integer.parseInt(s);
                    Optional<Flight> of = results.stream().filter(x -> x.id == fid).findFirst();
                    if (of.isPresent()) {
                        bookFlight(user, of.get());
                    } else {
                        System.out.println("Invalid flight id.");
                    }
                }
            }
        }
    }

    private static void bookFlight(User user, Flight flight) throws SQLException {
        if (flight.seatsAvailable <= 0) {
            System.out.println("No seats available.");
            return;
        }
        System.out.println("\n--- Booking Flight " + flight.flightNumber + " ---");
        System.out.print("Choose seat number (e.g., 01A) or leave blank to auto-assign: ");
        String seat = sc.nextLine().trim();
        if (seat.isEmpty()) {
            // simple auto-assign: choose last available seat number
            seat = "S" + (flight.totalSeats - flight.seatsAvailable + 1);
        }
        System.out.printf("Total price: %.2f. Proceed to payment? (y/n): ", flight.price);
        String p = sc.nextLine().trim();
        if (!p.equalsIgnoreCase("y")) {
            System.out.println("Booking cancelled.");
            return;
        }

        // simulate payment
        boolean paid = processPaymentSimulated(flight.price);
        if (!paid) {
            System.out.println("Payment failed. Booking aborted.");
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            conn.setAutoCommit(false);
            try {
                PreparedStatement ins = conn.prepareStatement(
                        "INSERT INTO reservations (user_id,flight_id,seat_number,status,booking_date) VALUES (?,?,?,?,?)",
                        Statement.RETURN_GENERATED_KEYS);
                ins.setInt(1, user.id);
                ins.setInt(2, flight.id);
                ins.setString(3, seat);
                ins.setString(4, "CONFIRMED");
                ins.setString(5, LocalDateTime.now().toString());
                ins.executeUpdate();
                ResultSet gk = ins.getGeneratedKeys();
                int reservationId = -1;
                if (gk.next())
                    reservationId = gk.getInt(1);
                ins.close();

                // Decrement seats_available
                PreparedStatement up = conn.prepareStatement(
                        "UPDATE flights SET seats_available = seats_available - 1 WHERE flight_id = ?");
                up.setInt(1, flight.id);
                up.executeUpdate();
                up.close();

                // Insert payment record
                PreparedStatement pay = conn.prepareStatement(
                        "INSERT INTO payments (reservation_id,amount,payment_status,payment_date) VALUES (?,?,?,?)");
                pay.setInt(1, reservationId);
                pay.setDouble(2, flight.price);
                pay.setString(3, "PAID");
                pay.setString(4, LocalDateTime.now().toString());
                pay.executeUpdate();
                pay.close();

                conn.commit();
                System.out.println("Booking confirmed! Reservation ID: " + reservationId);
                // Notification (console)
                System.out.println("Notification: Booking confirmation sent to " + user.email);
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    // Very simple simulated payment flow
    private static boolean processPaymentSimulated(double amount) {
        System.out.println("---- Payment Gateway (Simulated) ----");
        System.out.print("Enter card number (dummy): ");
        String card = sc.nextLine().trim();
        System.out.print("Enter expiry (MM/YY): ");
        String exp = sc.nextLine().trim();
        System.out.print("Enter CVV: ");
        String cvv = sc.nextLine().trim();
        // For demo: accept any non-empty values
        if (card.isEmpty() || exp.isEmpty() || cvv.isEmpty())
            return false;
        System.out.println("Processing payment of ₹" + String.format("%.2f", amount) + " ...");
        // pretend processing
        try {
            Thread.sleep(600);
        } catch (InterruptedException ignored) {
        }
        System.out.println("Payment successful.");
        return true;
    }

    // --------- Reservations ----------
    private static void myReservations() throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT r.reservation_id, r.seat_number, r.status, r.booking_date, f.flight_number, f.source, f.destination, f.departure, f.arrival "
                            +
                            "FROM reservations r JOIN flights f ON r.flight_id = f.flight_id WHERE r.user_id = ?");
            ps.setInt(1, currentUser.id);
            ResultSet rs = ps.executeQuery();
            System.out.println("\nYour Reservations:");
            System.out.println("ResID | Flight# | Seat | Status | BookingDate | Route | Dep->Arr");
            List<Integer> resIds = new ArrayList<>();
            while (rs.next()) {
                int rid = rs.getInt("reservation_id");
                resIds.add(rid);
                System.out.printf("%d | %s | %s | %s | %s | %s->%s | %s->%s%n",
                        rid,
                        rs.getString("flight_number"),
                        rs.getString("seat_number"),
                        rs.getString("status"),
                        rs.getString("booking_date"),
                        rs.getString("source"),
                        rs.getString("destination"),
                        rs.getString("departure"),
                        rs.getString("arrival"));
            }
            rs.close();
            ps.close();

            System.out.print("Enter Reservation ID to cancel or blank to return: ");
            String s = sc.nextLine().trim();
            if (!s.isEmpty()) {
                int rid = Integer.parseInt(s);
                if (!resIds.contains(rid)) {
                    System.out.println("Invalid reservation id.");
                    return;
                }
                cancelReservation(rid);
            }
        }
    }

    private static void cancelReservation(int reservationId) throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            conn.setAutoCommit(false);
            try {
                // Update reservation status
                PreparedStatement up = conn
                        .prepareStatement("UPDATE reservations SET status = ? WHERE reservation_id = ?");
                up.setString(1, "CANCELLED");
                up.setInt(2, reservationId);
                up.executeUpdate();
                up.close();

                // Refund: mark payment as REFUNDED (simplified)
                PreparedStatement pay = conn
                        .prepareStatement("UPDATE payments SET payment_status = ? WHERE reservation_id = ?");
                pay.setString(1, "REFUNDED");
                pay.setInt(2, reservationId);
                pay.executeUpdate();
                pay.close();

                // Increment seats_available for the flight
                PreparedStatement getFlight = conn
                        .prepareStatement("SELECT flight_id FROM reservations WHERE reservation_id = ?");
                getFlight.setInt(1, reservationId);
                ResultSet rs = getFlight.executeQuery();
                int fid = -1;
                if (rs.next())
                    fid = rs.getInt("flight_id");
                rs.close();
                getFlight.close();

                if (fid != -1) {
                    PreparedStatement inc = conn.prepareStatement(
                            "UPDATE flights SET seats_available = seats_available + 1 WHERE flight_id = ?");
                    inc.setInt(1, fid);
                    inc.executeUpdate();
                    inc.close();
                }
                conn.commit();
                System.out.println("Reservation cancelled and refund initiated (simulated).");
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    // --------- Reports ----------
    private static void reportsMenu() throws SQLException {
        while (true) {
            System.out.println("\n--- Reports ---");
            System.out.println("1) Revenue Report (by flight)");
            System.out.println("2) Occupancy Report (by flight)");
            System.out.println("3) Back");
            System.out.print("Choose: ");
            String c = sc.nextLine().trim();
            switch (c) {
                case "1":
                    revenueReport();
                    break;
                case "2":
                    occupancyReport();
                    break;
                case "3":
                    return;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    private static void revenueReport() throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String sql = "SELECT f.flight_id, f.flight_number, SUM(p.amount) as revenue " +
                    "FROM payments p JOIN reservations r ON p.reservation_id = r.reservation_id " +
                    "JOIN flights f ON r.flight_id = f.flight_id " +
                    "WHERE p.payment_status = 'PAID' GROUP BY f.flight_id, f.flight_number";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            System.out.println("\nRevenue Report:");
            System.out.println("FlightID | Flight# | Revenue");
            while (rs.next()) {
                System.out.printf("%d | %s | %.2f%n", rs.getInt("flight_id"), rs.getString("flight_number"),
                        rs.getDouble("revenue"));
            }
            rs.close();
            ps.close();
        }
    }

    private static void occupancyReport() throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String sql = "SELECT f.flight_id, f.flight_number, f.total_seats, (f.total_seats - f.seats_available) as booked "
                    +
                    "FROM flights f";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            System.out.println("\nOccupancy Report:");
            System.out.println("FlightID | Flight# | Booked | Total | Occupancy%");
            while (rs.next()) {
                int total = rs.getInt("total_seats");
                int booked = rs.getInt("booked");
                double occ = total == 0 ? 0.0 : (booked * 100.0 / total);
                System.out.printf("%d | %s | %d | %d | %.2f%%%n",
                        rs.getInt("flight_id"), rs.getString("flight_number"), booked, total, occ);
            }
            rs.close();
            ps.close();
        }
    }
}