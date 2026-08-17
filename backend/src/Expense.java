// A single expense entry: amount spent, category, date, and an optional note.
public class Expense {
    private int id;
    private double amount;
    private String category;
    private String date;   // format: YYYY-MM-DD
    private String note;

    public Expense() {}

    public Expense(int id, double amount, String category, String date, String note) {
        this.id = id;
        this.amount = amount;
        this.category = category;
        this.date = date;
        this.note = note;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    // Turns this expense into a JSON string so the frontend can read it.
    // (No JSON library used on purpose — keeps the project dependency-free.)
    public String toJson() {
        String safeNote = note == null ? "" : note.replace("\"", "'");
        return String.format(
            "{\"id\":%d,\"amount\":%.2f,\"category\":\"%s\",\"date\":\"%s\",\"note\":\"%s\"}",
            id, amount, category, date, safeNote
        );
    }
}
