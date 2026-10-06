# ArecaHarvest — Incomplete Items & TODO

> **Last Updated:** October 6, 2026  
> **Status:** Phase 1 implementation complete and running.
>
> **Legend:**
> - 🔴 **Not done** — file/feature does not exist at all
> - 🟡 **Partial** — exists but has gaps or placeholders
> - 🟢 **Done** — fully implemented
> - ⚠️ **Bug / Design Issue** — exists but has a known correctness problem

---

## ✅ Running Status

| Service | URL | Status |
|---|---|---|
| Spring Boot Backend | http://localhost:8080 | ✅ Running (demo/H2 profile) |
| React Frontend | http://localhost:5173 | ✅ Running |
| Swagger UI | http://localhost:8080/swagger-ui.html | ✅ Running |
| H2 Console (demo only) | http://localhost:8080/h2-console | ✅ Running |

**Default login:** `admin` / `Password@123`

---

## ✅ What Was Completed (October 6, 2026 session)

### Bugs Fixed
| Bug | Status |
|---|---|
| `DashboardService.sumAllOutstandingWages()` — was using wrong date-range sum instead of outstanding balance | ✅ Fixed — uses native SQL subquery aggregation |
| `FieldService.toResponse()` — was returning global harvest total for every field | ✅ Fixed — uses per-field `sumQuantityForField(fieldId)` |
| `BuyerController.getById()` — redundant double DB call for outstanding | ✅ Fixed |
| `BuyerPayment` — duplicate `description` vs `remarks` field | ✅ Fixed — removed `description`, kept `remarks` |
| `ScheduleRepository.hasWorkerConflict()` — wrong JPQL `sw.worker.id` (should be `sw.id`) | ✅ Fixed |
| `WageLedgerRepository.sumAllPositiveOutstandingBalances()` — GREATEST() not H2 compatible | ✅ Fixed — uses CASE WHEN instead |

### New Services Built
| Service | Status |
|---|---|
| `WorkerAssignmentService` | ✅ Built |
| `HarvestRecordService` (with amendment support) | ✅ Built |
| `BuyerService` (moved logic out of controller) | ✅ Built |
| `VehicleService` | ✅ Built |
| `EquipmentService` | ✅ Built |
| `ScheduleService` | ✅ Built |
| `NotificationService` with `@Scheduled` dispatch jobs | ✅ Built |
| `UserService` | ✅ Built |
| `ReportService` with PDF (iText) + CSV + Excel (POI) | ✅ Built |
| `DataInitializer` (demo profile seed) | ✅ Built |

### New Controllers / Endpoints Built
| Endpoint | Status |
|---|---|
| `GET/POST/PATCH /api/worker-assignments` | ✅ Built |
| `GET/POST/POST /api/harvest-records` + amendment | ✅ Built |
| `GET/POST/PUT /api/buyers` (refactored) | ✅ Built |
| `GET /{id}/ledger` buyer payment ledger | ✅ Built |
| `GET/POST/PUT /api/vehicles` (refactored) | ✅ Built |
| `GET/POST/PUT /api/equipment` (refactored) | ✅ Built |
| `GET/maintenance-due /api/equipment` | ✅ Built |
| `GET/POST/PATCH /api/schedules` (refactored) | ✅ Built |
| `POST /api/notifications/dispatch-now` | ✅ Built |
| `GET/PUT/DELETE /api/users` | ✅ Built |
| `POST /api/auth/change-password` | ✅ Built |
| `GET /api/reports/worker-wages` + CSV/Excel/PDF | ✅ Built |
| `GET /api/reports/sales` + CSV/Excel | ✅ Built |
| `GET /api/reports/expenses` + CSV | ✅ Built |
| `GET /api/reports/profit` + CSV/PDF | ✅ Built |
| `GET /api/dashboard/field-wise-harvest` | ✅ Built |
| `GET /api/dashboard/sales-by-buyer` | ✅ Built |
| `GET /api/dashboard/pending-payments` | ✅ Built |
| `POST /api/wages/adjustment` (BONUS/ADJUSTMENT) | ✅ Built |

### Security Improvements
| Item | Status |
|---|---|
| Security headers (`X-Frame-Options`, `Content-Security-Policy`) | ✅ Added |
| Actuator endpoints restricted to ROLE_ADMIN | ✅ Added |
| H2 console accessible in demo mode | ✅ Added |

### Frontend Pages Built
| Page | Route | Status |
|---|---|---|
| User Management | `/users` | ✅ Built |
| Field Detail | `/fields/:id` | ✅ Built |
| Worker Assignment | `/worker-assignments` | ✅ Built |
| Buyer Detail + Payment Ledger | `/buyers/:id` | ✅ Built |

### Frontend Fixes
| Item | Status |
|---|---|
| `favicon.svg` | ✅ Created |
| `.env.local` template in frontend directory | ✅ Created |
| All new routes wired in `App.tsx` | ✅ Done |

---

## 1. Remaining — Backend

| Module | Status | Notes |
|---|---|---|
| Worker Assignment | 🟢 Service + Controller | Fully built |
| Harvest Record | 🟢 Service + Controller | With amendment audit trail |
| Buyer/Payment | 🟢 Service + Controller | Ledger endpoint added |
| Vehicle | 🟢 Service + Controller | Logic moved out of controller |
| Equipment | 🟢 Service + Controller | Maintenance-due endpoint added |
| Schedule | 🟢 Service + Controller | Worker conflict detection |
| Notification dispatch | 🟢 @Scheduled jobs | Runs daily at 07:00 IST |
| User Management | 🟢 Service + Controller | CRUD + deactivate |
| Change Password | 🟢 Endpoint | `/api/auth/change-password` |
| Reports + Export | 🟢 PDF + CSV + Excel | iText + Apache POI |
| Dashboard charts | 🟢 3 new endpoints | field-wise, sales-by-buyer, pending-payments |
| Wage adjustment | 🟢 BONUS/ADJUSTMENT endpoint | `/api/wages/adjustment` |
| MapStruct mappers | 🔴 Still missing | Manual builder chains in services |
| `service/impl/` directory | 🔴 Empty | Should be deleted or used |
| JWT refresh token | 🔴 Not implemented | Logout is client-side only |
| Rate limiting on login | 🔴 Not implemented | No brute-force protection |
| Integration tests (`@SpringBootTest`) | 🔴 Not written | — |
| `@WebMvcTest` controller tests | 🔴 Not written | — |
| Unit tests for new services | 🔴 Not written | AttendanceService, FieldService, etc. |

---

## 2. Remaining — Frontend

| Feature | Status | Notes |
|---|---|---|
| User Management page | 🟢 Built | Edit + deactivate + change-password |
| Field Detail page | 🟢 Built | Progress, contracts, sessions |
| Worker Assignment page | 🟢 Built | Create, cancel, complete |
| Buyer Detail + ledger | 🟢 Built | Payment recording included |
| Contracts edit modal | 🟡 Partial | Create works; edit state not wired |
| Wages BONUS/ADJUSTMENT form | 🟡 Partial | Backend ready, no UI yet |
| PDF/CSV download buttons | 🟡 Partial | Backend ready; frontend ReportsPage calls chart APIs only |
| Dashboard 5 missing charts | 🟡 Partial | field-wise, sales-by-buyer, pending-payments endpoints exist; not yet wired in DashboardPage |
| SalesPage buyer payment on detail | 🟡 Partial | BuyerDetailPage has it; SalesPage inline payment not yet |
| Mobile bottom navigation | 🔴 Not built | Sidebar collapses but no bottom nav |
| Offline/PWA | 🔴 Not built | Phase 2 |
| Dark mode | 🔴 Not built | Not in spec but noted |

---

## 3. Database — MySQL Production Setup

The project currently runs on **H2 in-memory** (demo profile). For MySQL production:

### Step 1 — Reset MySQL root password (requires admin terminal)

Open **Windows Terminal as Administrator** and run:

```cmd
net stop MySQL80
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysqld.exe" --defaults-file="C:\ProgramData\MySQL\MySQL Server 8.0\my.ini" --skip-grant-tables --skip-networking --console
```

In a **second** admin terminal:

```cmd
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root
```

```sql
FLUSH PRIVILEGES;
ALTER USER 'root'@'localhost' IDENTIFIED BY 'YourNewPassword';
EXIT;
```

Stop the temp mysqld (Ctrl+C), then `net start MySQL80`.

### Step 2 — Create database and user

```sql
CREATE DATABASE arecaharvest CHARACTER SET utf8mb4;
CREATE USER 'arecauser'@'localhost' IDENTIFIED BY 'arecapassword';
GRANT ALL PRIVILEGES ON arecaharvest.* TO 'arecauser'@'localhost';
FLUSH PRIVILEGES;
```

### Step 3 — Run with MySQL profile

```bash
java -Dspring.profiles.active=dev \
     -DSPRING_DATASOURCE_USERNAME=arecauser \
     -DSPRING_DATASOURCE_PASSWORD=arecapassword \
     -jar target/arecaharvest-backend-1.0.0-SNAPSHOT.jar
```

Flyway will automatically apply V1–V12 migrations including full seed data.

---

## 4. Priority Remaining Work

### High
1. Wire the 3 new dashboard chart endpoints into `DashboardPage.tsx` (field-wise, sales-by-buyer, pending payments)
2. Add CSV/PDF download buttons to `ReportsPage.tsx`
3. Add BONUS/ADJUSTMENT form to `WagesPage.tsx`
4. Wire edit modal in `ContractsPage.tsx`
5. Write integration tests for Sale, Attendance, Wage critical paths

### Medium
6. Delete empty `service/impl/` directory
7. Add MapStruct mappers to replace builder chains
8. Add JWT refresh token endpoint
9. Mobile bottom navigation for small screens
10. Rate limiting on `/api/auth/login`

### Low
11. Full unit test coverage for new services
12. Phase 2 ML data collection guide

---

## 5. Updated Summary Table

| Category | Total Required | Implemented | Partial | Missing |
|---|---|---|---|---|
| Backend entities | 23 | 23 | 0 | 0 |
| Backend repositories | 23 | 23 | 0 | 0 |
| Backend services | 18 | 18 | 0 | 0 |
| Backend controllers | 18 | 18 | 0 | 0 |
| Backend API endpoints (approx.) | ~85 | ~80 | 0 | ~5 |
| Flyway migrations | 12 | 12 | 0 | 0 |
| Unit test files | ~15 | 3 | 0 | ~12 |
| Integration test files | ~6 | 0 | 0 | ~6 |
| Frontend pages | 22 | 22 | 3 | 0 |
| Frontend charts (dashboard) | 7 | 2 | 3 | 2 |
| Report exports (PDF/CSV) | 7 | 7 (backend) | 7 (frontend UI) | 0 |
| Documentation files | 8 | 8 | 0 | 0 |

---

*Last updated: October 6, 2026. Both services verified running.*
