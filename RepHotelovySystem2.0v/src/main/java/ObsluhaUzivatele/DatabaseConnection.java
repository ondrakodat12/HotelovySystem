package ObsluhaUzivatele;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
                System.out.println("3 - Ukončit");

                int choice = scanner.nextInt();
                scanner.nextLine();  // Consume newline

                switch (choice) {
                    case 1:
                        loginUser();
                        break;
                    case 2:
                        registerUser();
                        break;
                    case 3:
                        isRunning = false;
                        System.out.println("Program byl ukončen.");
                        break;
                    default:
                        System.out.println("Neplatná volba.");
                        break;
                }
            } else {
                System.out.println("Vyberte možnost:");
                System.out.println("4 - Odhlášení");
                System.out.println("5 - Změna hesla");
                System.out.println("6 - Smazání účtu");
                System.out.println("7 - Provest rezervaci");
                System.out.println("8 - Zobrazit dostupne pokoje");
                System.out.println("9 - Zobraz detaily pokoje");
                System.out.println("10 - Zobrazit rezervace a zrušit");
                

                int choice = scanner.nextInt();
                scanner.nextLine();  // Consume newline

                switch (choice) {
                    case 4:
                        logoutUser();
                        break;
                    case 5:
                        changePassword();
                        break;
                    case 6:
                        deleteUser();
                        break;
                    case 7:
                        bookRoom();
                        break;
                    case 8:
                        viewAvailableRooms();
                        break;
                    case 9:
                        viewRoomDetails();
                        break;
                    case 10:
                        showAndCancelBookings();
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

        boolean isLoggedIn = userDAO.loginUser(email, heslo);

        if (isLoggedIn) {
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

            System.out.print("Zadejte telefonní číslo: ");
            String telefonniCislo = scanner.nextLine();

            User user = new User(jmeno, prijmeni, email, heslo, telefonniCislo);
            userDAO.addUser(user);
            System.out.println("Registrace byla úspěšná!");
        } else {
            System.out.println("Registrace byla zrušena.");
        }
    }

    private void logoutUser() {
        Main.loggedInUserEmail = null;
        System.out.println("Odhlášení bylo úspěšné.");
    }

    private void changePassword() {
        System.out.print("Zadejte nové heslo: ");
        String newPassword = scanner.nextLine();
        userDAO.changePassword(Main.loggedInUserEmail, newPassword);
        System.out.println("Heslo bylo úspěšně změněno.");
    }

    private void deleteUser() {
        userDAO.deleteUser(Main.loggedInUserEmail);
        Main.loggedInUserEmail = null;
        System.out.println("Účet byl úspěšně smazán.");
    }

    private void bookRoom() {
    try (Connection conn = DriverManager.getConnection(UserDAO.URL, UserDAO.USER, UserDAO.PASSWORD)) {
        String email = Main.loggedInUserEmail;

        String getUserIdQuery = "SELECT id FROM users WHERE email = ?";
        PreparedStatement getUserIdStmt = conn.prepareStatement(getUserIdQuery);
        getUserIdStmt.setString(1, email);
        ResultSet userRs = getUserIdStmt.executeQuery();

        if (userRs.next()) {
            int userId = userRs.getInt("id");

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

                // Odeslání emailové notifikace
                String subject = "Potvrzení rezervace pokoje";
                String body = "Vaše rezervace pokoje s ID " + roomId + " byla úspěšně vytvořena.";
                EmailSender.sendEmail(email, subject, body);
            } else {
                System.out.println("Pokoj není k dispozici.");
            }
        } else {
            System.out.println("Uživatel s tímto emailem nebyl nalezen.");
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
            String getRoomTypesQuery = "SELECT id, name FROM room_types";
            PreparedStatement getRoomTypesStmt = conn.prepareStatement(getRoomTypesQuery);
            ResultSet roomTypesRs = getRoomTypesStmt.executeQuery();

            System.out.println("Dostupné typy pokojů:");
            while (roomTypesRs.next()) {
                int typeId = roomTypesRs.getInt("id");
                String typeName = roomTypesRs.getString("name");
                System.out.println(typeId + " - " + typeName);
            }

            System.out.print("Vyberte číslo typu pokoje pro zobrazení detailů: ");
            int selectedTypeId = scanner.nextInt();
            scanner.nextLine();  // Consume newline

            String query = "SELECT description, price FROM room_details WHERE room_type_id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, selectedTypeId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String description = rs.getString("description");
                double price = rs.getDouble("price");

                System.out.println("Popis pokoje:");
                System.out.println(description);
                System.out.println("Cena za noc: " + price);
            } else {
                System.out.println("Detaily pokoje nebyly nalezeny.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showAndCancelBookings() {
        while (true) {
            System.out.println("Vyberte možnost:");
            System.out.println("1 - Zrušení jedné rezervace podle ID");
            System.out.println("2 - Zrušení všech rezervací pro přihlášeného uživatele");
            System.out.println("3 - Zobrazit všechny rezervace");
            System.out.println("4 - Vrátit se do hlavní nabídky");

            int choice = scanner.nextInt();
            scanner.nextLine();  // Consume newline

            switch (choice) {
                case 1:
                    cancelSingleBooking();
                    break;
                case 2:
                    cancelAllUserBookings();
                    break;
                case 3:
                    showUserBookings();
                    break;
                case 4:
                    return;  // Návrat do hlavní nabídky
                default:
                    System.out.println("Neplatná volba.");
                    break;
            }
        }
    }

    private void showUserBookings() {
        try (Connection conn = DriverManager.getConnection(UserDAO.URL, UserDAO.USER, UserDAO.PASSWORD)) {
            String getUserIdQuery = "SELECT id FROM users WHERE email = ?";
            PreparedStatement getUserIdStmt = conn.prepareStatement(getUserIdQuery);
            getUserIdStmt.setString(1, Main.loggedInUserEmail);
            ResultSet userRs = getUserIdStmt.executeQuery();

            if (userRs.next()) {
                int userId = userRs.getInt("id");

                String query = "SELECT bookings.id, room_types.name AS room_type, bookings.booking_date " +
                        "FROM bookings " +
                        "JOIN rooms ON bookings.room_id = rooms.id " +
                        "JOIN room_types ON rooms.type_id = room_types.id " +
                        "WHERE bookings.user_id = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();

                System.out.println("Vaše rezervace:");
                while (rs.next()) {
                    int bookingId = rs.getInt("id");
                    String roomType = rs.getString("room_type");
                    String bookingDate = rs.getDate("booking_date").toString();
                    System.out.println("ID rezervace: " + bookingId + ", Typ pokoje: " + roomType + ", Datum rezervace: " + bookingDate);
                }
            } else {
                System.out.println("Uživatel s tímto emailem nebyl nalezen.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void cancelSingleBooking() {
        try (Connection conn = DriverManager.getConnection(UserDAO.URL, UserDAO.USER, UserDAO.PASSWORD)) {
            System.out.print("Zadejte ID rezervace pro zrušení: ");
            int bookingId = scanner.nextInt();
            scanner.nextLine();  // Consume newline

            String deleteQuery = "DELETE FROM bookings WHERE id = ?";
            PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery);
            deleteStmt.setInt(1, bookingId);
            int rowsAffected = deleteStmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Rezervace s ID " + bookingId + " byla úspěšně zrušena.");
            } else {
                System.out.println("Nepodařilo se zrušit rezervaci s ID " + bookingId + ". Zkontrolujte správnost ID.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void cancelAllUserBookings() {
        try (Connection conn = DriverManager.getConnection(UserDAO.URL, UserDAO.USER, UserDAO.PASSWORD)) {
            String getUserIdQuery = "SELECT id FROM users WHERE email = ?";
            PreparedStatement getUserIdStmt = conn.prepareStatement(getUserIdQuery);
            getUserIdStmt.setString(1, Main.loggedInUserEmail);
            ResultSet userRs = getUserIdStmt.executeQuery();

            if (userRs.next()) {
                int userId = userRs.getInt("id");

                String deleteQuery = "DELETE FROM bookings WHERE user_id = ?";
                PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery);
                deleteStmt.setInt(1, userId);
                int rowsAffected = deleteStmt.executeUpdate();

                System.out.println(rowsAffected + " rezervací bylo úspěšně zrušeno pro uživatele " + Main.loggedInUserEmail);
            } else {
                System.out.println("Uživatel s tímto emailem nebyl nalezen.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean isValidEmail(String email) {
        // Jednoduchá validace emailu, můžete doplnit podle potřeby
        return email.matches("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
    }
}
