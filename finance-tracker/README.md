# Ledger — Personal Finance Tracker

A full-stack personal finance tracker built with **Spring Boot**, **PostgreSQL**, and a plain **HTML/CSS/JavaScript** frontend. Track income and expenses, set monthly category budgets, and see your balance update in real time — no frontend framework, no build step for the UI.

## Features

- Add, edit, and delete income/expense transactions (full CRUD)
- Categorize transactions (Salary, Groceries, Rent, etc. — fully customizable)
- Set a monthly spending limit per category and track progress with a live budget bar
- Month-by-month view with automatic income/expense/balance totals
- REST API backing everything, so the frontend is just one of many possible clients

## Tech Stack

| Layer      | Technology                                  |
|------------|----------------------------------------------|
| Backend    | Java 17, Spring Boot 3.3, Spring Data JPA    |
| Database   | PostgreSQL                                   |
| Frontend   | HTML, CSS, vanilla JavaScript (served as static files by Spring Boot) |
| Build tool | Maven                                        |

## Project Structure

```
finance-tracker/
├── pom.xml
├── src/main/java/com/financetracker/
│   ├── FinanceTrackerApplication.java
│   ├── model/          # Category, Transaction, Budget, TransactionType
│   ├── repository/     # Spring Data JPA repositories
│   ├── service/         # Business logic
│   ├── controller/      # REST controllers + global exception handler
│   └── dto/              # SummaryResponse (dashboard totals)
└── src/main/resources/
    ├── application.properties
    ├── data.sql          # seeds default categories on first run
    └── static/           # index.html, css/style.css, js/app.js
```

## Prerequisites

- Java 17+
- Maven 3.6+
- PostgreSQL 13+ running locally (or a connection string to one)

## Setup

**1. Create the database**

```sql
CREATE DATABASE finance_tracker;
```

**2. Configure the connection**

Edit `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/finance_tracker
spring.datasource.username=postgres
spring.datasource.password=your_password
```

**3. Run the app**

```bash
mvn spring-boot:run
```

Spring Boot will create the tables automatically (`ddl-auto=update`) and seed a starter set of categories (Salary, Groceries, Rent, etc.) from `data.sql`.

**4. Open it**

Visit **http://localhost:8080** — the frontend is served directly by Spring Boot, so there's nothing separate to start.

## API Reference

| Method | Endpoint                          | Description                          |
|--------|------------------------------------|---------------------------------------|
| GET    | `/api/transactions?month=YYYY-MM` | List transactions (optionally by month) |
| POST   | `/api/transactions`                | Create a transaction                  |
| PUT    | `/api/transactions/{id}`          | Update a transaction                  |
| DELETE | `/api/transactions/{id}`          | Delete a transaction                  |
| GET    | `/api/categories`                  | List categories                       |
| POST   | `/api/categories`                  | Create a category                     |
| PUT    | `/api/categories/{id}`             | Update a category                     |
| DELETE | `/api/categories/{id}`             | Delete a category                     |
| GET    | `/api/budgets?month=YYYY-MM`      | List budgets (optionally by month)    |
| POST   | `/api/budgets`                     | Set a budget                          |
| PUT    | `/api/budgets/{id}`                | Update a budget                       |
| DELETE | `/api/budgets/{id}`                | Delete a budget                       |
| GET    | `/api/summary?month=YYYY-MM`      | Monthly totals + budget progress      |

## Example: create a transaction

```bash
curl -X POST http://localhost:8080/api/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "description": "Weekly groceries",
    "amount": 45.50,
    "type": "EXPENSE",
    "date": "2026-07-15",
    "category": { "id": 4 }
  }'
```

## Possible Next Steps

- User authentication (Spring Security + JWT) for multi-user support
- Export transactions to CSV
- Recurring transactions
- Charts (spending by category over time)

## License

MIT — free to use for learning, portfolio, or resume projects.
