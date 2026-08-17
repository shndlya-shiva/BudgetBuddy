import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// The entire backend server. Uses com.sun.net.httpserver (built into the JDK)
// instead of Spring Boot, so there is nothing extra to install for the server itself.
public class Server {
    static ExpenseDAO dao = new ExpenseDAO();

    public static void main(String[] args) throws IOException {
        DBHelper.initDB();

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/api/expenses", new ExpensesHandler());
        server.createContext("/api/summary", new SummaryHandler());
        server.createContext("/", new StaticFileHandler());
        server.setExecutor(null); // default single-threaded executor, fine for a demo
        server.start();

        System.out.println("Server running at http://localhost:8080");
    }

    // GET  /api/expenses      -> list all expenses
    // POST /api/expenses      -> add a new expense (JSON body)
    // DEL  /api/expenses?id=5 -> delete expense with id 5
    static class ExpensesHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            String method = exchange.getRequestMethod();

            if (method.equals("GET")) {
                List<Expense> expenses = dao.getAllExpenses();
                String json = "[" + expenses.stream().map(Expense::toJson).collect(Collectors.joining(",")) + "]";
                sendResponse(exchange, 200, json);

            } else if (method.equals("POST")) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Expense e = parseExpenseJson(body);
                int id = dao.addExpense(e);
                sendResponse(exchange, 201, "{\"id\":" + id + "}");

            } else if (method.equals("DELETE")) {
                String query = exchange.getRequestURI().getQuery(); // e.g. "id=5"
                int id = Integer.parseInt(query.split("=")[1]);
                boolean ok = dao.deleteExpense(id);
                sendResponse(exchange, ok ? 200 : 404, "{\"deleted\":" + ok + "}");

            } else if (method.equals("OPTIONS")) {
                sendResponse(exchange, 204, "");

            } else {
                sendResponse(exchange, 405, "{\"error\":\"method not allowed\"}");
            }
        }
    }

    // GET /api/summary -> category totals + current month's total
    static class SummaryHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            Map<String, Double> totals = dao.getCategoryTotals();
            String currentMonth = LocalDate.now().toString().substring(0, 7); // "2026-08"
            double monthTotal = dao.getMonthTotal(currentMonth);

            StringBuilder json = new StringBuilder("{\"categoryTotals\":{");
            int i = 0;
            for (Map.Entry<String, Double> entry : totals.entrySet()) {
                if (i++ > 0) json.append(",");
                json.append("\"").append(entry.getKey()).append("\":").append(entry.getValue());
            }
            json.append("},\"monthTotal\":").append(monthTotal).append("}");

            sendResponse(exchange, 200, json.toString());
        }
    }

    // Serves index.html / style.css / script.js from the ../frontend folder
    static class StaticFileHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/")) path = "/index.html";
            File file = new File("../frontend" + path);

            if (file.exists()) {
                byte[] bytes = Files.readAllBytes(file.toPath());
                exchange.sendResponseHeaders(200, bytes.length);
                OutputStream os = exchange.getResponseBody();
                os.write(bytes);
                os.close();
            } else {
                sendResponse(exchange, 404, "Not found");
            }
        }
    }

    // Very small hand-written parser for flat JSON like:
    // {"amount":250,"category":"Food","date":"2026-08-16","note":"lunch"}
    // A full JSON library was skipped on purpose to keep the project dependency-light.
    static Expense parseExpenseJson(String body) {
        Expense e = new Expense();
        e.setAmount(Double.parseDouble(extractValue(body, "amount").replace("\"", "")));
        e.setCategory(extractValue(body, "category"));
        e.setDate(extractValue(body, "date"));
        e.setNote(extractValue(body, "note"));
        return e;
    }

    static String extractValue(String json, String key) {
        String search = "\"" + key + "\":";
        int start = json.indexOf(search);
        if (start == -1) return "";
        start += search.length();
        char end = json.charAt(start) == '"' ? '"' : ',';
        if (end == '"') start++;
        int stop = json.indexOf(end, start);
        if (stop == -1) stop = json.indexOf("}", start);
        return json.substring(start, stop);
    }

    static void enableCORS(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, DELETE, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
    }

    static void sendResponse(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }
}
