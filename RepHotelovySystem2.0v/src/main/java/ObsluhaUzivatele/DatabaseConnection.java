package ObsluhaUzivatele;

import org.mindrot.jbcrypt.BCrypt;
import ObsluhaUzivatele.UserDAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class DatabaseConnection {
    private UserDAO userDAO;
    private Scanner scanner;

    public DatabaseConnection(String url, String username, String password) {
        this.userDAO = new UserDAO(url, username, password);
        this.scanner = new Scanner(System.in);
    }

    public void run() {
        boolean isRunning = true;
        while (isRunning) {
            if (Main.loggedInUserEmail == null) {
                System.out.println("Vyberte možnost:");
                System.out.println("1 - Přihlášení");
                System.out.println("2 - Registrace");

                int choice = scanner.nextInt();
                scanner.nextLine();  // Consume newline

                switch (choice) {
                    case 1:
                        loginUser();
                        break;
                    case 2:
                        registerUser();
                        break;
                    default:
                        System.out.println("Neplatná volba.");
                        break;
                }
            } else {
                System.out.println("Vyberte možnost:");
                System.out.println("3 - Odhlášení");
                System.out.println("4 - Změna hesla");
                System.out.println("5 - Smazání účtu");
                System.out.println("6 - Provest rezervaci");
                System.out.println("7 - Zobrazit dostupne pokoje");
                System.out.println("8 - Zobraz detaily pokoje");
                System.out.println("9 - Zobrazit rezervace");
                System.out.println("10 - Ukončit");

                int choice = scanner.nextInt();
                scanner.nextLine();  // Consume newline

                switch (choice) {
                    case 3:
                        logoutUser();
                        break;
                    case 4:
                        changePassword();
                        break;
                    case 5:
                        deleteUser();
                        break;
                    case 6:
                        bookRoom();
                        break;
                    case 7:
                        viewAvailableRooms();
                        break;
                    case 8:
                        viewRoomDetails();
                        break;
                    case 9:
                        showUserBookings();
                        break;
                    case 10:
                        isRunning = false;
                        System.out.println("Program byl ukončen.");
                        break;
                    default:
                        System.out.println("Neplatná volba.");
                        break;
                }
            }
        }
    }

    private void loginUser() {
        System.out.print("Zadejte email: ");
        String email = scanner.nextLine();
        System.out.print("Zadejte heslo: ");
        String heslo = scanner.nextLine();

        String storedHashedPassword = userDAO.getHashedPasswordByEmail(email);

        if (storedHashedPassword != null && BCrypt.checkpw(heslo, storedHashedPassword)) {
            Main.loggedInUserEmail = email;
            System.out.println("Přihlášení bylo úspěšné!");
            System.out.println("Přihlášený uživatel: " + Main.loggedInUserEmail);
        } else {
            System.out.println("Neplatný email nebo heslo.");
        }
    }

    private void registerUser() {
        System.out.println("Vítejte v registraci, bude potřeba zadat následující: jméno, přijmení, email, heslo, telefonní číslo");
        System.out.print("Přejete si pokračovat? (y/n): ");
        String pokracovat = scanner.nextLine().toLowerCase().trim();

        if (pokracovat.equals("y")) {
            System.out.print("Zadejte jméno: ");
            String jmeno = scanner.nextLine();
            System.out.print("Zadejte přijmení: ");
            String prijmeni = scanner.nextLine();
            String email;
            do {
                System.out.print("Zadejte email: ");
                email = scanner.nextLine();
                if (!isValidEmail(email)) {
                    System.out.println("Neplatný formát emailu. Zadejte platný email.");
                }
            } while (!isValidEmail(email));

            System.out.print("Zadejte heslo: ");
            String heslo = scanner.nextLine();
            String hashedPassword = BCrypt.hashpw(heslo, BCrypt.gensalt());

            System.out.print("Zadejte telefonní číslo: ");
            String telefonniCislo = scanner.nextLine();

            User user = new User(jmeno, prijmeni, email, hashedPassword, telefonniCislo);
            userDAO.addUser(user);
            System.out.println("Registrace byla úspěšná!");
        } else {
            System.out.println("Registrace byla zrušena.");
        }
    }

    private void logoutUser() {
        if (Main.loggedInUserEmail != null) {
            Main.loggedInUserEmail = null;
            System.out.println("Byli jste úspěšně odhlášeni.");
        } else {
            System.out.println("Žádný uživatel není přihlášen.");
        }
    }

    private void changePassword() {
        if (Main.loggedInUserEmail != null) {
            System.out.print("Zadejte nové heslo: ");
            String newPassword = scanner.nextLine();
            String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
            userDAO.changePassword(Main.loggedInUserEmail, hashedPassword);
            System.out.println("Heslo bylo úspěšně změněno.");
        } else {
            System.out.println("Nejste přihlášený.");
        }
    }

    private void deleteUser() {
        if (Main.loggedInUserEmail != null) {
            System.out.print("Opravdu chcete smazat svůj účet? (y/n): ");
            String confirm = scanner.nextLine().toLowerCase().trim();
            if (confirm.equals("y")) {
                userDAO.deleteUser(Main.loggedInUserEmail);
                Main.loggedInUserEmail = null;
                System.out.println("Váš účet byl úspěšně smazán.");
            } else {
                System.out.println("Smazání účtu bylo zrušeno.");
            }
        } else {
            System.out.println("Nejste přihlášený.");
        }
    }

   private void bookRoom() {
    try (Connection conn = DriverManager.getConnection(UserDAO.URL, UserDAO.USER, UserDAO.PASSWORD)) {
        // Získání ID uživatele podle emailu
        int userId = userDAO.getUserIdByEmail(Main.loggedInUserEmail);

        System.out.print("Zadejte ID pokoje pro rezervaci: ");
        int roomId = scanner.nextInt();
        scanner.nextLine();  // Consume newline

        String checkAvailability = "SELECT is_available FROM rooms WHERE id = ?";
        PreparedStatement checkStmt = conn.prepareStatement(checkAvailability);
        checkStmt.setInt(1, roomId);
        ResultSet rs = checkStmt.executeQuery();

        if (rs.next() && rs.getBoolean("is_available")) {
            String bookRoom = "INSERT INTO bookings (user_id, room_id, booking_date) VALUES (?, ?, ?)";
            PreparedStatement bookStmt = conn.prepareStatement(bookRoom);
            bookStmt.setInt(1, userId);
            bookStmt.setInt(2, roomId);
            bookStmt.setDate(3, new java.sql.Date(System.currentTimeMillis()));
            bookStmt.executeUpdate();

            String updateRoom = "UPDATE rooms SET is_available = FALSE WHERE id = ?";
            PreparedStatement updateStmt = conn.prepareStatement(updateRoom);
            updateStmt.setInt(1, roomId);
            updateStmt.executeUpdate();

            System.out.println("Pokoj byl úspěšně zarezervován!");
        } else {
            System.out.println("Pokoj není k dispozici.");
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
}


    private void viewAvailableRooms() {
        try (Connection conn = DriverManager.getConnection(UserDAO.URL, UserDAO.USER, UserDAO.PASSWORD)) {
            String query = "SELECT rooms.id, room_types.name FROM rooms " +
                           "JOIN room_types ON rooms.type_id = room_types.id " +
                           "WHERE rooms.is_available = TRUE";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            System.out.println("Dostupné pokoje:");
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                System.out.println("ID pokoje: " + id + ", Typ: " + name);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void viewRoomDetails() {
        try (Connection conn = DriverManager.getConnection(UserDAO.URL, UserDAO.USER, UserDAO.PASSWORD)) {
            System.out.println("Dostupné typy pokojů:");
            String roomTypeQuery = "SELECT id, name FROM room_types";
            PreparedStatement roomTypeStmt = conn.prepareStatement(roomTypeQuery);
            ResultSet roomTypeRs = roomTypeStmt.executeQuery();

            List<String> roomTypes = new ArrayList<>();
            int index = 1;
            while (roomTypeRs.next()) {
                String typeName = roomTypeRs.getString("name");
                roomTypes.add(typeName);
                System.out.println(index + ". " + typeName);
                index++;
            }

            System.out.print("Zadejte číslo typu pokoje pro zobrazení detailů: ");
            int typeIndex = scanner.nextInt();
            scanner.nextLine();  // Consume newline

            if (typeIndex > 0 && typeIndex <= roomTypes.size()) {
                String selectedTypeName = roomTypes.get(typeIndex - 1);

                String query = "SELECT room_types.name, room_details.description, room_details.price " +
                               "FROM room_types " +
                               "JOIN room_details ON room_types.id = room_details.room_type_id " +
                               "WHERE room_types.name = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, selectedTypeName);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    String name = rs.getString("name");
                    String description = rs.getString("description");
                    double price = rs.getDouble("price");
                    System.out.println("Typ: " + name);
                    System.out.println("Popis: " + description);
                    System.out.println("Cena: " + price);
                } else {
                    System.out.println("Typ pokoje nebyl nalezen.");
                }
            } else {
                System.out.println("Neplatné číslo typu pokoje.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showUserBookings() {
        try (Connection conn = DriverManager.getConnection(UserDAO.URL, UserDAO.USER, UserDAO.PASSWORD)) {
            String query = "SELECT bookings.id, rooms.id AS room_id, room_types.name AS room_type, bookings.booking_date " +
                           "FROM bookings " +
                           "JOIN rooms ON bookings.room_id = rooms.id " +
                           "JOIN room_types ON rooms.type_id = room_types.id " +
                           "WHERE bookings.user_id = (SELECT id FROM users WHERE email = ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, Main.loggedInUserEmail);
            ResultSet rs = stmt.executeQuery();

            System.out.println("Vaše rezervace:");
            while (rs.next()) {
                int bookingId = rs.getInt("id");
                int roomId = rs.getInt("room_id");
                String roomType = rs.getString("room_type");
                java.util.Date bookingDate = rs.getDate("booking_date");
                System.out.println("ID rezervace: " + bookingId + ", ID pokoje: " + roomId + ", Typ pokoje: " + roomType + ", Datum rezervace: " + bookingDate);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean isValidEmail(String email) {
        String regex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*$";
        return email.matches(regex);
    }
}
