package ObsluhaUzivatele;

public class Main {
    public static String loggedInUserEmail = null;

    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/DTuzivatele";
        String username = "root";
        String password = "ZSsazava12";

        DatabaseConnection dbConnection = new DatabaseConnection(url, username, password);
        dbConnection.run();
    }
}
    