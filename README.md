# ArecaHarvest

**Integrated Areca Nut Harvesting Business Management Platform**

Phase 1 of a two-phase IEEE research project — digitalization and operational management for small-scale areca nut harvesting contractors.

---

## Overview

ArecaHarvest replaces notebooks, spreadsheets, WhatsApp, and phone calls with a single platform that manages:

- Areca nut owners and their fields
- Harvesting contracts (per-KG, per-tree, fixed-amount)
- Worker management and daily attendance
- Automatic wage ledger (accounting-style transaction log)
- Daily harvest recording and inventory tracking
- Buyer management and sales with automatic inventory deduction
- Business expense tracking
- Vehicles and equipment
- Work scheduling with conflict detection
- Dashboard KPIs and charts
- Reports and analytics

---

## Technology Stack

| Layer     | Technology                                              |
|-----------|---------------------------------------------------------|
| Frontend  | React 18, Vite, TypeScript, Tailwind CSS, Recharts      |
| Backend   | Java 21, Spring Boot 3.2, Spring Security, JWT          |
| Database  | MySQL 8+, Flyway migrations                             |
| Auth      | JWT (HS512), BCrypt password hashing                    |
| Docs      | OpenAPI / Swagger UI at `/swagger-ui.html`              |
| Container | Docker + Docker Compose                                 |

---

## Quick Start

### Prerequisites

- Java 21+
- Node.js 20+
- MySQL 8+ (or Docker)
- Maven 3.9+

### Option 1 — Docker Compose (recommended)

```bash
cp .env.example .env
# Edit .env with your secrets
docker-compose up -d
```

- Frontend: http://localhost:3000
- Backend API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html

### Option 2 — Local Development

**Backend:**

```bash
# Create MySQL database
mysql -u root -p -e "CREATE DATABASE arecaharvest CHARACTER SET utf8mb4;"
mysql -u root -p -e "CREATE USER 'arecauser'@'localhost' IDENTIFIED BY 'arecapassword';"
mysql -u root -p -e "GRANT ALL ON arecaharvest.* TO 'arecauser'@'localhost';"

# Run backend
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

**Frontend:**

```bash
cd frontend
npm install
cp .env.example .env.local
# Set VITE_API_BASE_URL=http://localhost:8080/api
npm run dev
```

---

## Default Credentials (Demo)

| Username | Password      | Role       |
|----------|---------------|------------|
| admin    | Password@123  | ADMIN      |
| supervisor1 | Password@123 | SUPERVISOR |
| worker_raju | Password@123 | WORKER   |

> ⚠️ Change all passwords before production use.

---

## Project Structure

```
arecaharvest/
├── backend/                    # Spring Boot application
│   ├── src/main/java/com/arecaharvest/
│   │   ├── ArecaHarvestApplication.java
│   │   ├── audit/              # JPA auditing
│   │   ├── config/security/    # JWT + Spring Security
│   │   ├── controller/         # REST controllers
│   │   ├── dto/                # Request/response DTOs
│   │   ├── entity/             # JPA entities + enums
│   │   ├── exception/          # Custom exceptions + global handler
│   │   ├── repository/         # Spring Data JPA repositories
│   │   ├── service/            # Business logic
│   │   └── util/               # JWT utilities
│   └── src/main/resources/
│       ├── application.yml
│       └── db/migration/       # Flyway V1–V12 SQL scripts
├── frontend/                   # React + Vite application
│   └── src/
│       ├── api/                # Axios client + API functions
│       ├── components/         # Reusable UI components
│       ├── context/            # AuthContext
│       ├── hooks/              # Custom React hooks
│       ├── pages/              # One folder per module
│       ├── routes/             # Route guards
│       ├── types/              # TypeScript types
│       └── utils/              # Formatters + helpers
├── docs/
│   └── ER_DIAGRAM.md           # Entity relationship diagram
├── docker-compose.yml
├── .env.example
└── README.md
```

---

## Database Migrations

Flyway automatically applies migrations on startup in version order:

| Version | Description                            |
|---------|----------------------------------------|
| V1      | Users and roles                        |
| V2      | Owners and fields                      |
| V3      | Workers and assignments                |
| V4      | Harvesting contracts                   |
| V5      | Attendance, wage ledger, payments      |
| V6      | Harvest sessions, records, inventory   |
| V7      | Buyers, sales, buyer payments          |
| V8      | Expenses and categories                |
| V9      | Vehicles and equipment                 |
| V10     | Work schedules                         |
| V11     | Notifications and audit logs           |
| V12     | Development seed data                  |

---

## API Documentation

Full Swagger UI available at: `http://localhost:8080/swagger-ui.html`

Key endpoints:

```
POST   /api/auth/login
GET    /api/dashboard/kpis
GET    /api/owners
POST   /api/owners
GET    /api/fields
POST   /api/attendance          # Auto-creates wage ledger entry
POST   /api/wages/payments      # Validates against outstanding balance
POST   /api/harvest-sessions    # COMPLETED status auto-creates inventory
POST   /api/sales               # Validates inventory, updates stock, creates buyer ledger
GET    /api/dashboard/harvest-trend
GET    /api/dashboard/revenue-vs-expenses
```

---

## Running Tests

```bash
cd backend
mvn test
```

Critical business logic tests:
- `ContractCalculationServiceTest` — revenue calculations for all contract types, daily wage rules
- `WageServiceTest` — payment validation, advance allowance, balance enforcement
- `SaleServiceInventoryTest` — overselling prevention, inventory deduction

---

## Environment Variables

| Variable                   | Description                          | Default          |
|----------------------------|--------------------------------------|------------------|
| `MYSQL_DATABASE`           | Database name                        | arecaharvest     |
| `MYSQL_USER`               | Database user                        | arecauser        |
| `MYSQL_PASSWORD`           | Database password                    | arecapassword    |
| `JWT_SECRET`               | JWT signing secret (≥64 chars)       | — (required)     |
| `JWT_EXPIRATION_MS`        | Token expiry in milliseconds         | 86400000 (24h)   |
| `SPRING_PROFILES_ACTIVE`   | Spring profile (dev/prod)            | dev              |
| `VITE_API_BASE_URL`        | Frontend API base URL                | /api             |

---

## Phase 2 Research Preparation

The database schema intentionally stores research-critical variables:

- `fields.acreage`, `fields.tree_count` — yield modelling inputs
- `attendance.working_hours` — productivity time series
- `harvest_sessions.total_quantity`, `worker_count`, `session_date` — harvest dataset
- `harvesting_contracts.rate`, `contract_type`, `expected_quantity_kg` — contract profitability
- `workers.skill_type`, `experience_years` — workforce optimization

Phase 2 will add: yield prediction, workforce prediction, harvest duration prediction, intelligent scheduling — all using data accumulated by Phase 1.

---

## Design Decisions and Assumptions

1. **Wage ledger is append-only** — balance is always computed from the full transaction history, never stored as a single field. This ensures auditability.

2. **Unpaid leave = zero wage** — LEAVE attendance status generates no wage entry. If paid leave is needed, it should be configured per contract/organization.

3. **Sale deductions reduce revenue** — transport, loading, and other charges are deducted from gross amount to compute net receivable. This can be changed per business practice.

4. **One ACTIVE contract per field** — enforced at the application layer. A field must have its active contract completed or cancelled before starting a new one.

5. **Inventory is auto-created** when a harvest session is marked COMPLETED. This can be overridden by creating inventory entries manually.

6. **Audit logs are async** — written asynchronously to avoid blocking financial transactions. In rare cases, an audit entry may not be written if the application crashes. This is a known and acceptable tradeoff for Phase 1.
