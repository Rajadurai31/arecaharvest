# ArecaHarvest — Entity Relationship Diagram

## Entity Summary

| # | Table | Description | PK |
|---|-------|-------------|-----|
| 1 | `users` | System user accounts | `id` |
| 2 | `roles` | ADMIN, SUPERVISOR, WORKER | `id` |
| 3 | `user_roles` | Many-to-many user↔role | composite |
| 4 | `owners` | Areca nut farm owners | `id` |
| 5 | `fields` | Individual fields per owner | `id` |
| 6 | `harvesting_contracts` | Agreements per field | `id` |
| 7 | `workers` | Harvesting workforce | `id` |
| 8 | `worker_assignments` | Worker→field+date assignments | `id` |
| 9 | `attendance` | Daily per-worker attendance | `id` |
| 10 | `wage_ledger` | Transaction-based wage accounting | `id` |
| 11 | `worker_payments` | Cash/UPI payments to workers | `id` |
| 12 | `harvest_sessions` | Organized daily harvest events | `id` |
| 13 | `session_workers` | N:N session↔worker | composite |
| 14 | `harvest_records` | Immutable harvest quantity records | `id` |
| 15 | `inventory` | Post-harvest stock tracking | `id` |
| 16 | `buyers` | Nut buyers / traders | `id` |
| 17 | `sales` | Sale transactions | `id` |
| 18 | `buyer_payments` | Buyer payment ledger | `id` |
| 19 | `expense_categories` | Expense classification | `id` |
| 20 | `expenses` | Business expense records | `id` |
| 21 | `vehicles` | Fleet management | `id` |
| 22 | `equipment` | Tools and machinery | `id` |
| 23 | `schedules` | Work scheduling / calendar | `id` |
| 24 | `schedule_workers` | N:N schedule↔worker | composite |
| 25 | `notifications` | In-app alerts | `id` |
| 26 | `audit_logs` | Immutable action log | `id` |

---

## Key Relationships

```
users ──< user_roles >── roles

owners ──< fields
  fields ──< harvesting_contracts
    harvesting_contracts ──< harvest_sessions
      harvest_sessions ──< session_workers >── workers
      harvest_sessions ──< harvest_records
        harvest_records ──< inventory
          inventory ──< sales >── buyers
            buyers ──< buyer_payments

workers ──< worker_assignments >── fields
workers ──< attendance
workers ──< wage_ledger
workers ──< worker_payments

fields ──< schedules
  schedules ──< schedule_workers >── workers
  schedules ──── harvest_sessions (planned→actual link)

expenses ──── expense_categories
expenses ──── fields (optional)
expenses ──── harvesting_contracts (optional)
expenses ──── vehicles (optional)

vehicles ──── workers (driver)
equipment ──── workers (assigned)
equipment ──── fields (assigned)

notifications ──── users
audit_logs ──── users
```

---

## Cardinalities

| Relationship | Type | Constraint |
|---|---|---|
| Owner → Fields | 1:N | owner deleted → RESTRICT |
| Field → Contracts | 1:N | only 1 ACTIVE contract per field (app-enforced) |
| Contract → Sessions | 1:N | |
| Session ↔ Workers | N:N | via `session_workers` |
| Worker → Attendance | 1:N | unique per worker+field+date |
| Worker → WageLedger | 1:N | append-only |
| Worker → Payments | 1:N | |
| HarvestRecord → Inventory | 1:1 (typ.) | |
| Inventory → Sales | 1:N | qty_sold ≤ qty_received (enforced) |
| Buyer → Sales | 1:N | |
| Buyer → BuyerPayments | 1:N | |
| Schedule → Workers | N:N | via `schedule_workers` |

---

## Critical Business Constraints (enforced at DB + application layer)

1. `attendance.UNIQUE(worker_id, field_id, attendance_date)` — no duplicate attendance
2. `worker_assignments.UNIQUE(worker_id, field_id, assigned_date)` — no duplicate assignments
3. `inventory.available_quantity = quantity_received − quantity_sold` — must stay in sync via transaction
4. `sales.quantity_kg ≤ inventory.available_quantity` — overselling prevented
5. `wage_ledger` — balance computed by summing all rows, never stored as a single field
6. `harvesting_contracts.expected_end_date ≥ start_date` — CHECK constraint
7. `audit_logs` — rows are never updated or deleted

---

## Phase 2 Research Variables (stored for ML dataset)

These fields are specifically stored to support Phase 2 yield/workforce prediction:

| Entity | Field | Phase 2 Use |
|---|---|---|
| `fields` | `acreage`, `tree_count`, `gps_lat/lon` | Yield modelling input features |
| `harvesting_contracts` | `contract_type`, `rate`, `expected_quantity_kg` | Contract profitability prediction |
| `attendance` | `working_hours`, `overtime_hours`, `status` | Productivity time series |
| `harvest_sessions` | `total_quantity`, `worker_count`, `total_working_hours`, `session_date` | Yield & duration prediction |
| `harvest_records` | `quantity`, `bags_count`, `worker_count` | Historical harvest dataset |
| `workers` | `skill_type`, `experience_years` | Workforce optimization |
| `expenses` | `category`, `amount`, `date` | Cost modelling |
| `sales` | `rate_per_unit`, `quantity_kg`, `sale_date` | Revenue forecasting |
