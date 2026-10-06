# ArecaHarvest — Architecture Document

## Overview

ArecaHarvest uses a classic three-tier architecture:

```
Browser (React SPA)
    ↕ HTTP/JSON (JWT)
Spring Boot REST API
    ↕ JPA/JDBC
MySQL 8
```

## Backend Architecture

### Layer Responsibilities

```
HTTP Request
    → Controller (input validation, HTTP mapping)
    → Service (business logic, orchestration)
    → Repository (data access, JPQL queries)
    → Entity (JPA mapping)
    → Database
```

**Key principle:** Business logic lives exclusively in the service layer. Controllers are thin. Repositories only do data access.

### Security Flow

```
Request → JwtAuthenticationFilter (extract + validate token)
        → SecurityContextHolder (set authentication)
        → @PreAuthorize (method-level role check)
        → Controller
```

### Financial Transaction Integrity

All multi-step financial operations use `@Transactional`:

```
POST /api/sales
  @Transactional
  1. Validate inventory availability
  2. Create Sale record
  3. Deduct from Inventory
  4. Create BuyerPayment ledger entry
  → COMMIT (all or nothing)
```

Same pattern for:
- Worker payments (create payment + ledger entry)
- Attendance marking (mark attendance + create wage ledger)

### Audit Logging

`AuditLogService.log()` is `@Async` — runs in a background thread to avoid adding latency to user-facing operations. All significant financial and operational actions call this service.

## Frontend Architecture

### State Management

No external state management library (Redux/Zustand). State is kept local to each page component using `useState`. The `AuthContext` provides global authentication state.

### API Layer

All HTTP calls go through `src/api/index.ts` which uses the configured Axios instance. The Axios interceptor handles:
- Attaching JWT to every request
- Redirecting to login on 401
- Showing error toasts on failures

### Route Protection

`ProtectedRoute` component checks `isAuthenticated` before rendering. Role-specific routes use the `requiredRole` prop.

## Data Flow — Daily Workflow

```
1. Mark Attendance (POST /api/attendance)
   Service:
     - Creates Attendance record
     - Computes wage (PRESENT=full, HALF_DAY=50%, ABSENT/LEAVE=0)
     - Creates WageLedger entry (DAILY_WAGE type)
     - If overtime > 0: creates separate WageLedger entry (OVERTIME type)

2. Record Harvest (POST /api/harvest-sessions with status=COMPLETED)
   Service:
     - Creates HarvestSession
     - If COMPLETED: auto-creates Inventory record

3. Pay Worker (POST /api/wages/payments)
   Service:
     - Validates amount ≤ outstanding balance (for PAYMENT type)
     - Creates WorkerPayment record
     - Creates WageLedger entry (PAYMENT or ADVANCE type)
     - Links payment ↔ ledger entry
```

## Database Normalization

The schema is in 3rd Normal Form (3NF):
- No repeating groups
- All attributes depend on the whole key
- No transitive dependencies

Exceptions made for performance:
- `inventory.available_quantity` is stored (derived from received - sold) but kept in sync via transactions
- `harvest_sessions.worker_count` is a cached count

## Phase 2 Extension Points

To add predictive features without rewriting Phase 1:

1. **Yield Prediction**: Query `harvest_sessions` + `fields` (acreage, tree_count, session_date) to train a regression model
2. **Workforce Prediction**: Use `attendance` + `workers` (skill_type, experience) correlated with `harvest_sessions.total_quantity`
3. **Duration Prediction**: `harvest_sessions` (start_time, end_time, worker_count, total_quantity)
4. **Contract Profitability**: `harvesting_contracts` + aggregated harvest + expenses per contract
5. **Intelligent Scheduling**: Feed predictions into a scheduling API that suggests optimal worker allocation and harvest dates
