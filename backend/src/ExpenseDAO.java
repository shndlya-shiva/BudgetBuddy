import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// All database operations for expenses: add, list, delete, and totals.
public class ExpenseDAO {

    // Insert a new expense, return the auto-generated id.
    public int addExpense(Expense e) {
        String sql = "INSERT INTO expenses (amount, category, expense_date, note) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBHelper.connect();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setDouble(1, e.getAmount());
            ps.setString(2, e.getCategory());
            ps.setString(3, e.getDate());
            ps.setString(4, e.getNote());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
        } catch (SQLException ex) {
            System.out.println("Insert error: " + ex.getMessage());
        }
        return -1;
    }

    // Return every expense, most recent first.
    public List<Expense> getAllExpenses() {
        List<Expense> list = new ArrayList<>();
        String sql = "SELECT * FROM expenses ORDER BY expense_date DESC, id DESC";
        try (Connection conn = DBHelper.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Expense(
                        rs.getInt("id"),
                        rs.getDouble("amount"),
                        rs.getString("category"),
                        rs.getString("expense_date"),
                        rs.getString("note")
                ));
            }
        } catch (SQLException ex) {
            System.out.println("Fetch error: " + ex.getMessage());
        }
        return list;
    }

    // Delete an expense by id. Returns true if a row was actually removed.
    public boolean deleteExpense(int id) {
        String sql = "DELETE FROM expenses WHERE id = ?";
        try (Connection conn = DBHelper.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            System.out.println("Delete error: " + ex.getMessage());
            return false;
        }
    }

    // Category-wise totals, e.g. {"Food": 450.0, "Travel": 120.0}
    public Map<String, Double> getCategoryTotals() {
        Map<String, Double> totals = new LinkedHashMap<>();
        String sql = "SELECT category, SUM(amount) as total FROM expenses GROUP BY category";
        try (Connection conn = DBHelper.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                totals.put(rs.getString("category"), rs.getDouble("total"));
            }
        } catch (SQLException ex) {
            System.out.println("Summary error: " + ex.getMessage());
        }
        return totals;
    }

    // Total spend for a given month, e.g. yearMonth = "2026-08"
    public double getMonthTotal(String yearMonth) {
        double total = 0;
        String sql = "SELECT SUM(amount) as total FROM expenses WHERE expense_date LIKE ?";
        try (Connection conn = DBHelper.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, yearMonth + "%");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) total = rs.getDouble("total");
        } catch (SQLException ex) {
            System.out.println("Month total error: " + ex.getMessage());
        }
        return total;
    }
}
