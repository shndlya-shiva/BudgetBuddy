"""
Bonus feature: run this after exporting expenses.db to a CSV file
(File > Export in a SQLite browser, or a simple SQL query works too).

Expected columns: date, category, amount, note
"""

import csv
from collections import defaultdict


def load_expenses(csv_path):
    expenses = []
    with open(csv_path, newline='') as f:
        reader = csv.DictReader(f)
        for row in reader:
            row["amount"] = float(row["amount"])
            expenses.append(row)
    return expenses


def top_category(expenses):
    totals = defaultdict(float)
    for e in expenses:
        totals[e["category"]] += e["amount"]
    return max(totals.items(), key=lambda x: x[1])


def days_over_budget(expenses, daily_limit):
    by_day = defaultdict(float)
    for e in expenses:
        by_day[e["date"]] += e["amount"]
    return [day for day, total in by_day.items() if total > daily_limit]


if __name__ == "__main__":
    expenses = load_expenses("expenses.csv")

    category, amount = top_category(expenses)
    print(f"Top spending category: {category} (Rs.{amount:.2f})")

    over_budget_days = days_over_budget(expenses, daily_limit=500)
    if over_budget_days:
        print(f"Days you spent over Rs.500: {', '.join(over_budget_days)}")
    else:
        print("No days went over the Rs.500 limit.")
