import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

// Handles everything about connecting to the SQLite database file.
public class DBHelper {
    // "expenses.db" will be created automatically in the folder you run the server from.
    private static final String DB_URL = "jdbc:sqlite:expenses.db";

    public static Connection connect() throws SQLException {
        try {
            // Explicitly load the driver. Usually not needed (JDBC 4+ auto-registers
            // drivers found on the classpath), but forcing it here means that if the
            // sqlite-jdbc jar is missing from the classpath, you get a clear
            // "ClassNotFoundException: org.sqlite.JDBC" instead of a vague
            // "No suitable driver found" error.
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException("sqlite-jdbc jar not found on classpath. " +
                    "Check that it's in backend/ and included in your -cp flag.", e);
        }
        return DriverManager.getConnection(DB_URL);
    }

    // Creates the expenses table the first time the app runs. Safe to call every time.
    public static void initDB() {
        String sql = "CREATE TABLE IF NOT EXISTS expenses (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "amount REAL NOT NULL," +
                "category TEXT NOT NULL," +
                "expense_date TEXT NOT NULL," +
                "note TEXT" +
                ")";
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Database ready: expenses.db");
        } catch (SQLException e) {
            System.out.println("DB init error: " + e.getMessage());
        }
    }
}