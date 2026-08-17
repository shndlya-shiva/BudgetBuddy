const API = "http://localhost:8080/api";

const form = document.getElementById("expenseForm");
const table = document.getElementById("expenseTable");
const monthTotalEl = document.getElementById("monthTotal");
const barsEl = document.getElementById("categoryBars");

// Default the date field to today so the user doesn't have to pick it every time
document.getElementById("date").valueAsDate = new Date();

// Handle the "Add expense" form submit
form.addEventListener("submit", async (e) => {
  e.preventDefault();
  const expense = {
    amount: document.getElementById("amount").value,
    category: document.getElementById("category").value,
    date: document.getElementById("date").value,
    note: document.getElementById("note").value
  };

  await fetch(`${API}/expenses`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(expense)
  });

  form.reset();
  document.getElementById("date").valueAsDate = new Date();
  loadEverything();
});

// Called by the ✕ button in each table row
async function deleteExpense(id) {
  await fetch(`${API}/expenses?id=${id}`, { method: "DELETE" });
  loadEverything();
}

// Fetch all expenses and render the table
async function loadExpenses() {
  const res = await fetch(`${API}/expenses`);
  const expenses = await res.json();

  table.innerHTML = expenses.map(exp => `
    <tr>
      <td>${exp.date}</td>
      <td>${exp.category}</td>
      <td>${exp.note || "-"}</td>
      <td>₹${exp.amount.toFixed(2)}</td>
      <td><button onclick="deleteExpense(${exp.id})">✕</button></td>
    </tr>
  `).join("");
}

// Fetch category totals + month total and render the header + bars
async function loadSummary() {
  const res = await fetch(`${API}/summary`);
  const summary = await res.json();

  monthTotalEl.textContent = `₹${summary.monthTotal.toFixed(2)}`;

  const totals = summary.categoryTotals;
  const max = Math.max(...Object.values(totals), 1);

  barsEl.innerHTML = Object.entries(totals).map(([cat, amt]) => `
    <div class="bar-row">
      <span class="bar-label">${cat}</span>
      <div class="bar-track">
        <div class="bar-fill" style="width:${(amt / max) * 100}%"></div>
      </div>
      <span class="bar-amount">₹${amt.toFixed(2)}</span>
    </div>
  `).join("");
}

function loadEverything() {
  loadExpenses();
  loadSummary();
}

loadEverything();
