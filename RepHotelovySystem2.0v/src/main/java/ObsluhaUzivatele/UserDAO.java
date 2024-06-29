package ObsluhaUzivatele;

import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class UserDAO {

    static final String URL = "jdbc:mysql://localhost:3306/DTuzivatele";
    static final String USER = "root";
    static final String PASSWORD = "ZSsazava12";

    private final String url;
    private final String dbUser;
    private final String dbPassword;

    public UserDAO(String url, String user, String password) {
        this.url = url;
        this.dbUser = user;
        this.dbPassword = password;
    }

    // Metoda pro ověření přihlášení uživatele
     public boolean loginUser(String email, String heslo) {
        if (isAccountBlocked(email)) {
            System.out.println("Účet je zablokován. Zkuste to znovu po 30 minutách.");
            return false;
        }

        String hashedPassword = getHashedPasswordByEmail(email);
        if (hashedPassword != null && BCrypt.checkpw(heslo, hashedPassword)) {
            clearLoginAttempts(email); // Vymažeme neúspěšné pokusy po úspěšném přihlášení
            return true;
        } else {
            recordLoginAttempt(email); // Zaznamenáme neúspěšný pokus
            int attempts = countInvalidLoginAttempts(email);
            if (attempts >= 5) {
                blockAccount(email); // Pokud je 5 neplatných pokusů, zablokujeme účet na 30 minut
            }
            return false;
        }
    }

    // Metoda pro získání hashovaného hesla podle emailu
    public String getHashedPasswordByEmail(String email) {
        try (Connection connection = DriverManager.getConnection(url, dbUser, dbPassword)) {
            String query = "SELECT heslo FROM users WHERE email = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, email);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("heslo");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Metoda pro přidání nového uživatele
    public void addUser(User newUser) {
        try (Connection connection = DriverManager.getConnection(url, dbUser, dbPassword)) {
            if (!checkIfUserExists(newUser.getEmail())) {
                String hashedPassword = BCrypt.hashpw(newUser.getHeslo(), BCrypt.gensalt());
                String query = "INSERT INTO users (jmeno, prijmeni, email, heslo, telefonni_cislo) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, newUser.getJmeno());
                statement.setString(2, newUser.getPrijmeni());
                statement.setString(3, newUser.getEmail());
                statement.setString(4, hashedPassword); // Ukládání hashovaného hesla
                statement.setString(5, newUser.getTelefonniCislo());
                statement.executeUpdate();
                System.out.println("Uživatel byl úspěšně přidán.");
            } else {
                System.out.println("Uživatel s tímto emailem již existuje.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Metoda pro kontrolu, zda uživatel s daným emailem již existuje
    public boolean checkIfUserExists(String email) {
        try (Connection connection = DriverManager.getConnection(url, dbUser, dbPassword)) {
            String query = "SELECT COUNT(*) AS count FROM users WHERE email = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, email);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                int count = resultSet.getInt("count");
                return count > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Metoda pro změnu hesla uživatele
    public void changePassword(String email, String newPassword) {
        try (Connection connection = DriverManager.getConnection(url, dbUser, dbPassword)) {
            String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
            String query = "UPDATE users SET heslo = ? WHERE email = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, hashedPassword);
            statement.setString(2, email);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Metoda pro smazání uživatele podle emailu
    public void deleteUser(String email) {
        try (Connection connection = DriverManager.getConnection(url, dbUser, dbPassword)) {
            String query = "DELETE FROM users WHERE email = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, email);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
     public void recordLoginAttempt(String email) {
        try (Connection connection = DriverManager.getConnection(url, dbUser, dbPassword)) {
            String query = "INSERT INTO login_attempts (email) VALUES (?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, email);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
     
     public int countInvalidLoginAttempts(String email) {
        try (Connection connection = DriverManager.getConnection(url, dbUser, dbPassword)) {
            String query = "SELECT COUNT(*) AS attempts FROM login_attempts WHERE email = ? AND attempt_time >= DATE_SUB(NOW(), INTERVAL 30 MINUTE)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, email);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("attempts");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
     
     public void blockAccount(String email) {
        try (Connection connection = DriverManager.getConnection(url, dbUser, dbPassword)) {
            String query = "UPDATE users SET blocked_until = DATE_ADD(NOW(), INTERVAL 30 MINUTE) WHERE email = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, email);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
     
     // Metoda pro kontrolu, zda je účet zablokován
    private boolean isAccountBlocked(String email) {
        try (Connection connection = DriverManager.getConnection(url, dbUser, dbPassword)) {
            String query = "SELECT blocked_until FROM users WHERE email = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, email);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                Timestamp blockedUntil = resultSet.getTimestamp("blocked_until");
                if (blockedUntil != null && blockedUntil.after(new Timestamp(System.currentTimeMillis()))) {
                    return true; // Účet je zablokován
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Metoda pro vymazání neúspěšných pokusů o přihlášení po úspěšném přihlášení
    private void clearLoginAttempts(String email) {
        try (Connection connection = DriverManager.getConnection(url, dbUser, dbPassword)) {
            String query = "DELETE FROM login_attempts WHERE email = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, email);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
     
    
    
}
