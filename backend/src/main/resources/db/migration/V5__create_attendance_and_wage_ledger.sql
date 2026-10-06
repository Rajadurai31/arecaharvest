-- =============================================================================
-- V5: Attendance, Wage Ledger, and Worker Payments
-- =============================================================================
-- The wage ledger is an accounting-style transaction log, NOT a single "balance" field.
-- Every debit (wage earned, bonus, overtime) and credit (advance, payment)
-- is a separate row. The balance is always computed from the full ledger history.
--
-- This design is intentional for:
--   1. Auditability — every change has a record
--   2. Phase 2 data quality — working-hours time series for productivity modelling
-- =============================================================================

-- ── Attendance ────────────────────────────────────────────────────────────────
CREATE TABLE attendance (
    id                BIGINT         NOT NULL AUTO_INCREMENT,
    worker_id         BIGINT         NOT NULL,
    field_id          BIGINT         NOT NULL,
    contract_id       BIGINT,
    harvest_session_id BIGINT,                         -- FK added in V6
    attendance_date   DATE           NOT NULL,
    status            ENUM('PRESENT','HALF_DAY','ABSENT','LEAVE')
                          NOT NULL DEFAULT 'PRESENT',
    -- Working hours stored for productivity analysis (Phase 2)
    check_in_time     TIME,
    check_out_time    TIME,
    working_hours     DECIMAL(5, 2),                  -- computed or entered
    overtime_hours    DECIMAL(5, 2)  DEFAULT 0,
    -- Wage applied for this day (snapshot — does not change if worker's default wage changes)
    daily_wage_applied DECIMAL(10, 2),
    overtime_rate     DECIMAL(10, 2) DEFAULT 0,       -- INR per overtime hour
    remarks           TEXT,
    marked_by         BIGINT,                         -- user_id who recorded attendance
    created_at        DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at        DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    created_by        VARCHAR(100),
    updated_by        VARCHAR(100),
    PRIMARY KEY (id),
    -- Business rule: one attendance record per worker per field per date
    UNIQUE KEY uq_attendance_worker_field_date (worker_id, field_id, attendance_date),
    CONSTRAINT fk_att_worker   FOREIGN KEY (worker_id)   REFERENCES workers (id) ON DELETE RESTRICT,
    CONSTRAINT fk_att_field    FOREIGN KEY (field_id)    REFERENCES fields  (id) ON DELETE RESTRICT,
    CONSTRAINT fk_att_contract FOREIGN KEY (contract_id) REFERENCES harvesting_contracts (id) ON DELETE SET NULL,
    CONSTRAINT fk_att_marked_by FOREIGN KEY (marked_by)  REFERENCES users  (id) ON DELETE SET NULL,
    INDEX idx_att_worker_date  (worker_id, attendance_date),
    INDEX idx_att_field_date   (field_id, attendance_date),
    INDEX idx_att_date         (attendance_date),
    INDEX idx_att_status       (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── Wage Ledger ───────────────────────────────────────────────────────────────
-- Accounting-style double-entry inspired ledger.
-- DEBIT types  (money owed TO worker):  DAILY_WAGE, OVERTIME, BONUS
-- CREDIT types (money paid BY contractor): ADVANCE, PAYMENT, ADJUSTMENT (either sign)
CREATE TABLE wage_ledger (
    id                BIGINT          NOT NULL AUTO_INCREMENT,
    worker_id         BIGINT          NOT NULL,
    transaction_type  ENUM('DAILY_WAGE','OVERTIME','BONUS','ADVANCE','PAYMENT','ADJUSTMENT')
                          NOT NULL,
    -- amount is always positive; direction determined by transaction_type
    amount            DECIMAL(12, 2)  NOT NULL,
    -- For ADJUSTMENT: +ve = owed more, -ve = owed less
    adjustment_sign   TINYINT         DEFAULT 1 CHECK (adjustment_sign IN (-1, 1)),
    transaction_date  DATE            NOT NULL,
    -- Cross-reference to source records for traceability
    attendance_id     BIGINT,                         -- for DAILY_WAGE / OVERTIME entries
    payment_id        BIGINT,                         -- for PAYMENT / ADVANCE entries (FK in V5b)
    field_id          BIGINT,
    contract_id       BIGINT,
    description       VARCHAR(500),
    recorded_by       BIGINT,                         -- user_id
    created_at        DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at        DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    created_by        VARCHAR(100),
    updated_by        VARCHAR(100),
    PRIMARY KEY (id),
    CONSTRAINT fk_wl_worker     FOREIGN KEY (worker_id)     REFERENCES workers (id) ON DELETE RESTRICT,
    CONSTRAINT fk_wl_attendance FOREIGN KEY (attendance_id) REFERENCES attendance (id) ON DELETE SET NULL,
    CONSTRAINT fk_wl_field      FOREIGN KEY (field_id)      REFERENCES fields (id) ON DELETE SET NULL,
    CONSTRAINT fk_wl_contract   FOREIGN KEY (contract_id)   REFERENCES harvesting_contracts (id) ON DELETE SET NULL,
    CONSTRAINT fk_wl_recorded_by FOREIGN KEY (recorded_by)  REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT chk_wl_amount_positive CHECK (amount > 0),
    INDEX idx_wl_worker_date    (worker_id, transaction_date),
    INDEX idx_wl_type           (transaction_type),
    INDEX idx_wl_date           (transaction_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── Worker Payments ───────────────────────────────────────────────────────────
CREATE TABLE worker_payments (
    id                BIGINT         NOT NULL AUTO_INCREMENT,
    worker_id         BIGINT         NOT NULL,
    payment_date      DATE           NOT NULL,
    amount            DECIMAL(12, 2) NOT NULL,
    payment_type      ENUM('PAYMENT','ADVANCE') NOT NULL DEFAULT 'PAYMENT',
    payment_method    ENUM('CASH','UPI','BANK_TRANSFER','OTHER') NOT NULL DEFAULT 'CASH',
    reference_number  VARCHAR(100),                   -- UPI/bank transaction ref
    remarks           TEXT,
    -- Ledger entry auto-created when payment is recorded
    wage_ledger_id    BIGINT,
    recorded_by       BIGINT,
    created_at        DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at        DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    created_by        VARCHAR(100),
    updated_by        VARCHAR(100),
    PRIMARY KEY (id),
    CONSTRAINT fk_wp_worker        FOREIGN KEY (worker_id)      REFERENCES workers     (id) ON DELETE RESTRICT,
    CONSTRAINT fk_wp_ledger        FOREIGN KEY (wage_ledger_id) REFERENCES wage_ledger (id) ON DELETE SET NULL,
    CONSTRAINT fk_wp_recorded_by   FOREIGN KEY (recorded_by)    REFERENCES users       (id) ON DELETE SET NULL,
    CONSTRAINT chk_wp_amount CHECK (amount > 0),
    INDEX idx_wp_worker_date       (worker_id, payment_date),
    INDEX idx_wp_date              (payment_date),
    INDEX idx_wp_payment_type      (payment_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Back-reference: wage_ledger.payment_id → worker_payments
ALTER TABLE wage_ledger
    ADD CONSTRAINT fk_wl_payment
        FOREIGN KEY (payment_id) REFERENCES worker_payments (id) ON DELETE SET NULL;
