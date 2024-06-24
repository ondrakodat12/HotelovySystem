package ObsluhaUzivatele;

import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {
    private String url;
    private String username;
    private String password;

    public UserDAO(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }
    
    
    // Konstanty pro připojení k databázi
    static final String URL = "jdbc:mysql://localhost:3306/DTuzivatele";
    static final String USER = "root";
    static final String PASSWORD = "ZSsazava12";

    // Metoda pro ověření přihlášení uživatele
    public boolean loginUser(String email, String heslo) {
        String hashedPassword = getHashedPasswordByEmail(email);
        if (hashedPassword != null && BCrypt.checkpw(heslo, hashedPassword)) {
            return true;
        }
        return false;
    }

    // Metoda pro získání hashovaného hesla podle emailu
    public String getHashedPasswordByEmail(String email) {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
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
    
    
    // Metoda pro získání ID uživatele podle emailu
public int getUserIdByEmail(String email) {
    int userId = -1; // Pokud se nepodaří najít uživatele, vrátíme -1
    
    try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
        String query = "SELECT id FROM users WHERE email = ?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, email);
        ResultSet resultSet = statement.executeQuery();
        
        if (resultSet.next()) {
            userId = resultSet.getInt("id");
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    
    return userId;
}


    // Metoda pro přidání nového uživatele
    public void addUser(User user) {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String hashedPassword = BCrypt.hashpw(user.getHeslo(), BCrypt.gensalt());
            String query = "INSERT INTO users (jmeno, prijmeni, email, heslo, telefonni_cislo) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, user.getJmeno());
            statement.setString(2, user.getPrijmeni());
            statement.setString(3, user.getEmail());
            statement.setString(4, hashedPassword); // Ukládání hashovaného hesla
            statement.setString(5, user.getTelefonniCislo());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Metoda pro změnu hesla uživatele
    public void changePassword(String email, String newPassword) {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
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
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String query = "DELETE FROM users WHERE email = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, email);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Metoda pro kontrolu, zda uživatel s daným emailem již existuje
    public boolean checkIfUserExists(String email) {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
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
}