/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ObsluhaUzivatele;

/**
 *
 * @author user
 */
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PasswordResetService {

    // Metoda pro změnu hesla po ověření verifikačního kódu
    public void resetPassword(String email, String verificationCode, String newPassword) {
        String url = "jdbc:mysql://localhost:3306/hotelovy_system";
        String username = "root";
        String password = "ZSsazava12";

        String query = "SELECT * FROM password_reset WHERE email = ? AND verification_code = ?";
        String updateQuery = "UPDATE users SET heslo = ? WHERE email = ?";
        String deleteQuery = "DELETE FROM password_reset WHERE email = ?";

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement selectStatement = connection.prepareStatement(query);
             PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
             PreparedStatement deleteStatement = connection.prepareStatement(deleteQuery)) {

            // Zkontrolujeme, zda verifikační kód odpovídá
            selectStatement.setString(1, email);
            selectStatement.setString(2, verificationCode);
            ResultSet resultSet = selectStatement.executeQuery();

            if (resultSet.next()) {
                // Verifikační kód odpovídá, můžeme změnit heslo
                String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
                updateStatement.setString(1, hashedPassword);
                updateStatement.setString(2, email);
                updateStatement.executeUpdate();

                System.out.println("Heslo bylo úspěšně změněno.");

                // Smazání záznamu o verifikačním kódu
                deleteStatement.setString(1, email);
                deleteStatement.executeUpdate();

                System.out.println("Záznam verifikačního kódu byl smazán.");

            } else {
                System.out.println("Neplatný nebo vypršelý verifikační kód.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
