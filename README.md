# Ledger — Personal Finance Tracker

A full-stack, multi-user personal finance tracker built with **Spring Boot**, **Spring Security (JWT)**, and **PostgreSQL**, with a glassmorphism **HTML/CSS/JavaScript** frontend. Register an account, log in, and track income/expenses and a monthly budget on your own private dashboard — no frontend framework, no build step for the UI.

## Features

- **Multi-user accounts** — register/login with JWT-based authentication; every user sees only their own data
- Add, edit, and delete income/expense transactions (full CRUD), tracked daily and viewable by month
- Set an overall monthly spending limit and track progress with a live budget bar
- Per-account dashboard with automatic income/expense/balance totals, updated in real time
- Glassmorphism UI — frosted glass panels, blurred gradient background, no page reloads
- REST API backing everything, so the frontend is just one of many possible clients

## Tech Stack

| Layer      | Technology                                             |
|------------|----------------------------------------------------------|
| Backend    | Java 17, Spring Boot 3.3, Spring Data JPA, Spring Security |
| Auth       | JWT (JJWT), BCrypt password hashing                      |
| Database   | PostgreSQL                                                |
| Frontend   | HTML, CSS, vanilla JavaScript (served as static files by Spring Boot) |
| Build tool | Maven                                                      |
| Containers | Docker (multi-stage build), Docker Compose (app + db orchestration) |

## Project Structure

```
finance-tracker/
├── Dockerfile
├── docker-compose.yml
├── .dockerignore
├── .env.example
├── pom.xml
├── src/main/java/com/financetracker/
│   ├── FinanceTrackerApplication.java
│   ├── model/          # User, Transaction, Budget, TransactionType
│   ├── repository/     # Spring Data JPA repositories (all user-scoped)
│   ├── service/         # Business logic
│   ├── security/        # JWT filter/util, UserDetailsService, SecurityConfig
│   ├── controller/      # REST controllers + global exception handler + AuthController
│   └── dto/              # Auth/Login/Register requests, SummaryResponse
└── src/main/resources/
    ├── application.properties
    └── static/
        ├── index.html    # dashboard (requires login)
        ├── login.html    # login / register
        ├── css/style.css
        └── js/
            ├── app.js     # dashboard logic + auth guard
            └── auth.js    # login/register logic
```

## Prerequisites

**Either:**
- Java 17+, Maven 3.6+, and PostgreSQL 13+ running locally (Options A/B below)

**Or:**
- Docker + Docker Compose only — no Java/Maven/Postgres install needed (Option C below)

## Setup

Database and JWT config are read from environment variables (`PGHOST`, `PGPORT`, `PGUSER`, `PGPASSWORD`, `PGDATABASE`, `JWT_SECRET`), each with a local-dev fallback baked in — so you can either export env vars or just edit `application.properties` directly. Pick whichever path matches what you're doing.

### Option A — Running locally against your own Postgres (pgAdmin)

**1. Create the database** (in pgAdmin, or via `psql`)

```sql
CREATE DATABASE finance_tracker;
```

**2. Point the app at it** — easiest is to edit the fallback values directly in `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://${PGHOST:localhost}:${PGPORT:5432}/${PGDATABASE:finance_tracker}
spring.datasource.username=${PGUSER:postgres}
spring.datasource.password=${PGPASSWORD:postgres}
```

Just change the part after the `:` inside each `${...:default}` — e.g. `${PGPASSWORD:your_actual_password}` — to match what's in pgAdmin (**Login/Group Roles → postgres → Properties**).

**3. Run it**

```bash
mvn spring-boot:run
```

**4. Open it** — visit **http://localhost:8080**, register an account, and you're in.

### Option B — Deploying to Railway

Don't edit the properties file for this — set environment variables in Railway's dashboard instead, and the fallbacks are simply ignored.

1. In your Railway project, add a **PostgreSQL** plugin — Railway auto-creates `PGHOST`, `PGPORT`, `PGUSER`, `PGPASSWORD`, `PGDATABASE` and injects them into your app's environment automatically. You don't need to copy these anywhere yourself.
2. In your app service's **Variables** tab, add one more:
   - `JWT_SECRET` → generate one locally with `openssl rand -base64 32` and paste the result in
3. Deploy. Railway also injects `PORT` automatically, which `application.properties` already reads (`server.port=${PORT:8080}`).
4. Visit the URL Railway gives your service — same login/register flow as local.

Either way, Spring Boot creates all the tables automatically (`ddl-auto=update`) on first run, including the `app_users` table — there's no manual schema step.

### Option C — Docker (recommended if you want to show containerization)

The project ships with a multi-stage `Dockerfile` and a `docker-compose.yml` that runs **two containers**: the app and a PostgreSQL database.

> **Why two containers, not three?** This app is a Spring Boot monolith — the backend serves the frontend directly as static files from the same JAR, on the same port. There's no separate frontend process to containerize; splitting it into its own nginx container would mean adding CORS handling and a second web server for a UI that already works fine served by Spring Boot. Two containers (app + database) is the architecturally honest setup for *this* project — a genuinely micro-frontend or SPA-with-separate-build project would warrant three.

**1. Copy the env template and fill in real values**

```bash
cp .env.example .env
```

Edit `.env` — at minimum set a real `PGPASSWORD` and a real `JWT_SECRET` (`openssl rand -base64 32`).

**2. Build and run**

```bash
docker compose up --build
```

This builds the app image (Maven compiles the jar inside the build stage, so you don't need Java or Maven installed locally at all), starts Postgres, waits for it to be healthy, then starts the app.

**3. Open it** — visit **http://localhost:8080**.

**4. Stop it**

```bash
docker compose down          # stop containers, keep data
docker compose down -v       # stop containers and wipe the database volume
```

Data persists in a named Docker volume (`ledger-db-data`) between restarts.

## Google Sign-In (optional)

Adds a "Continue with Google" button. Without credentials, the app runs normally and the button stays hidden.

1. [Google Cloud Console](https://console.cloud.google.com) → create a project → **APIs & Services → OAuth consent screen** (External, add yourself as a test user)
2. **Credentials → Create Credentials → OAuth client ID → Web application**
3. Add **Authorized redirect URIs**:
   - `http://localhost:8080/login/oauth2/code/google`
   - `https://<your-railway-domain>/login/oauth2/code/google` (when deployed)
4. Set two environment variables, then restart:
   - `APP_OAUTH2_GOOGLE_CLIENT_ID`
   - `APP_OAUTH2_GOOGLE_CLIENT_SECRET`

On first Google login an account is created automatically (email = username, no password). The server then issues the same JWT as normal login, so the rest of the app is unchanged.

## How auth works

- `POST /api/auth/register` and `POST /api/auth/login` are the only public API endpoints — everything else under `/api/**` requires a valid JWT
- On login/register, the server returns a signed JWT; the frontend stores it in `localStorage` and sends it as `Authorization: Bearer <token>` on every subsequent request
- A Spring Security filter (`JwtAuthFilter`) validates the token on each request and resolves the current user
- Every transaction and budget is tied to the user who created it at the database level — the API won't return or let you modify another user's records, even if you guess their ID
- Tokens expire after 24h by default (`app.jwt.expiration-ms`); the frontend auto-redirects to `login.html` if a request comes back `401`

## API Reference

| Method | Endpoint                          | Auth required | Description                          |
|--------|------------------------------------|:---:|---------------------------------------|
| POST   | `/api/auth/register`               | No  | Create an account, returns a JWT      |
| POST   | `/api/auth/login`                  | No  | Log in, returns a JWT                 |
| GET    | `/api/transactions?month=YYYY-MM` | Yes | List your transactions (optionally by month) |
| POST   | `/api/transactions`                | Yes | Create a transaction                  |
| PUT    | `/api/transactions/{id}`          | Yes | Update a transaction                  |
| DELETE | `/api/transactions/{id}`          | Yes | Delete a transaction                  |
| GET    | `/api/budgets`                     | Yes | List your budgets                     |
| POST   | `/api/budgets`                     | Yes | Set (or update) your budget for a month |
| PUT    | `/api/budgets/{id}`                | Yes | Update a budget                       |
| DELETE | `/api/budgets/{id}`                | Yes | Delete a budget                       |
| GET    | `/api/summary?month=YYYY-MM`      | Yes | Your monthly totals + budget progress |

## Example: register, then create a transaction

```bash
# 1. Register and grab the token
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"alex","email":"alex@example.com","password":"secret123"}'
# -> { "token": "eyJ...", "username": "alex", "email": "alex@example.com" }

# 2. Use the token for everything else
curl -X POST http://localhost:8080/api/transactions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJ..." \
  -d '{
    "description": "Weekly groceries",
    "amount": 45.50,
    "type": "EXPENSE",
    "date": "2026-07-15"
  }'
```

## Possible Next Steps

- Refresh tokens / "remember me" (current tokens are short-lived and single-use until expiry)
- Export transactions to CSV
- Recurring transactions
- Re-introduce categories/tags for more granular budget tracking
- Charts (spending trend over time)
- Password reset flow

## License

MIT — free to use for learning, portfolio, or resume projects.
