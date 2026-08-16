# Budget Tracker

A simple full-stack expense tracker: Java backend (core JDK, no framework),
SQLite database, HTML/CSS/JS frontend, and a Python bonus script for insights.

## Folder structure
```
expense-tracker/
  backend/
    src/
      Expense.java       - the expense model
      DBHelper.java       - database connection + table setup
      ExpenseDAO.java      - all SQL: add, list, delete, totals
      Server.java          - REST API + serves the frontend, port 8080
  frontend/
    index.html
    style.css
    script.js
  scripts/
    insights.py
    expenses.csv          - sample data for the script
```

## One-time setup

1. **Download the SQLite JDBC driver jar** (the only external dependency):
   https://github.com/xerial/sqlite-jdbc/releases
   Grab the latest `sqlite-jdbc-x.x.x.jar` and place it in `backend/`.

2. **Compile the backend** (from inside `backend/src`):
   ```
   javac -cp ../sqlite-jdbc-x.x.x.jar -d ../out *.java
   ```

3. **Run the server** (from inside `backend/`):
   ```
   java -cp "out:sqlite-jdbc-x.x.x.jar" Server
   ```
   On Windows use `;` instead of `:` in the classpath:
   ```
   java -cp "out;sqlite-jdbc-x.x.x.jar" Server
   ```

4. Open **http://localhost:8080** in your browser. That's it — `expenses.db`
   is created automatically the first time you run it.

## Running the Python bonus script

```
cd scripts
python insights.py
```
It reads `expenses.csv` and prints the top spending category and any days
that went over a ₹500 daily limit. Swap in your own exported data anytime.

## API endpoints (for reference)

| Method | Endpoint              | Purpose                     |
|--------|------------------------|------------------------------|
| GET    | /api/expenses           | list all expenses           |
| POST   | /api/expenses           | add an expense (JSON body)  |
| DELETE | /api/expenses?id=5      | delete expense with id 5    |
| GET    | /api/summary            | category totals + month total |
