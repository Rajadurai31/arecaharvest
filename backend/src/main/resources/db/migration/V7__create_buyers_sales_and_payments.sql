-- =============================================================================
-- V7: Buyers (Nut Takers), Sales, and Buyer Payment Ledger
-- =============================================================================
-- Buyer 1 → N Sales
-- Buyer 1 → N BuyerPayments
-- Sale is linked to Inventory — quantity validation enforced at service layer.
-- =============================================================================

-- ── Buyers ────────────────────────────────────────────────────────────────────
CREATE TABLE buyers (
    id                  BIGINT         NOT NULL AUTO_INCREMENT,
    name                VARCHAR(200)   NOT NULL,
    phone               VARCHAR(20)    NOT NULL,
    alternate_phone     VARCHAR(20),
    company_name        VARCHAR(200),
    address             TEXT,
    village             VARCHAR(200),
    district            VARCHAR(200),
    state               VARCHAR(100)   DEFAULT 'Karnataka',
    payment_terms       VARCHAR(200),               -- e.g. "Net 7 days"
    default_rate_per_kg DECIMAL(10, 4),             -- baseline rate for quick entry
    bank_name           VARCHAR(200),
    bank_account_number VARCHAR(50),
    bank_ifsc           VARCHAR(20),
    upi_id              VARCHAR(100),
    notes               TEXT,
    status              ENUM('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    created_at          DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at          DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    created_by          VARCHAR(100),
    updated_by          VARCHAR(100),
    PRIMARY KEY (id),
    INDEX idx_buyers_name   (name),
    INDEX idx_buyers_phone  (phone),
    INDEX idx_buyers_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── Sales ─────────────────────────────────────────────────────────────────────
CREATE TABLE sales (
    id                BIGINT         NOT NULL AUTO_INCREMENT,
    sale_number       VARCHAR(50)    NOT NULL UNIQUE,
    buyer_id          BIGINT         NOT NULL,
    inventory_id      BIGINT         NOT NULL,
    sale_date         DATE           NOT NULL,
    -- Quantity — unit stored explicitly
    quantity_kg       DECIMAL(12, 3) NOT NULL,
    quantity_unit     VARCHAR(10)    NOT NULL DEFAULT 'KG',
    rate_per_unit     DECIMAL(10, 4) NOT NULL,       -- INR per unit
    gross_amount      DECIMAL(14, 2) NOT NULL,       -- quantity × rate (computed)
    transport_cost    DECIMAL(10, 2) DEFAULT 0,
    loading_cost      DECIMAL(10, 2) DEFAULT 0,
    other_charges     DECIMAL(10, 2) DEFAULT 0,
    other_charges_desc VARCHAR(200),
    -- net_amount = gross_amount - deductible costs (business assumption: costs reduce revenue)
    -- Assumption documented: costs are deducted from gross amount to get net receivable.
    net_amount        DECIMAL(14, 2) NOT NULL,
    payment_status    ENUM('PENDING','PARTIAL','PAID') NOT NULL DEFAULT 'PENDING',
    amount_received   DECIMAL(14, 2) DEFAULT 0,
    notes             TEXT,
    created_at        DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at        DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    created_by        VARCHAR(100),
    updated_by        VARCHAR(100),
    PRIMARY KEY (id),
    CONSTRAINT fk_sales_buyer     FOREIGN KEY (buyer_id)     REFERENCES buyers    (id) ON DELETE RESTRICT,
    CONSTRAINT fk_sales_inventory FOREIGN KEY (inventory_id) REFERENCES inventory (id) ON DELETE RESTRICT,
    CONSTRAINT chk_sale_qty       CHECK (quantity_kg > 0),
    CONSTRAINT chk_sale_rate      CHECK (rate_per_unit > 0),
    INDEX idx_sales_buyer_date    (buyer_id, sale_date),
    INDEX idx_sales_date          (sale_date),
    INDEX idx_sales_payment_status (payment_status),
    INDEX idx_sales_inventory     (inventory_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── Buyer Payment Ledger ──────────────────────────────────────────────────────
-- Accounting-style ledger: every sale and payment creates a transaction row.
CREATE TABLE buyer_payments (
    id                  BIGINT         NOT NULL AUTO_INCREMENT,
    buyer_id            BIGINT         NOT NULL,
    sale_id             BIGINT,                      -- nullable for general advance payments
    payment_date        DATE           NOT NULL,
    transaction_type    ENUM('SALE','PAYMENT','ADJUSTMENT') NOT NULL,
    amount              DECIMAL(14, 2) NOT NULL,
    adjustment_sign     TINYINT        DEFAULT 1 CHECK (adjustment_sign IN (-1, 1)),
    payment_method      ENUM('CASH','UPI','BANK_TRANSFER','CHEQUE','OTHER'),
    reference_number    VARCHAR(100),
    remarks             TEXT,
    recorded_by         BIGINT,
    created_at          DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at          DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    created_by          VARCHAR(100),
    updated_by          VARCHAR(100),
    PRIMARY KEY (id),
    CONSTRAINT fk_bp_buyer       FOREIGN KEY (buyer_id)    REFERENCES buyers (id) ON DELETE RESTRICT,
    CONSTRAINT fk_bp_sale        FOREIGN KEY (sale_id)     REFERENCES sales  (id) ON DELETE SET NULL,
    CONSTRAINT fk_bp_recorded_by FOREIGN KEY (recorded_by) REFERENCES users  (id) ON DELETE SET NULL,
    CONSTRAINT chk_bp_amount CHECK (amount > 0),
    INDEX idx_bp_buyer_date  (buyer_id, payment_date),
    INDEX idx_bp_sale        (sale_id),
    INDEX idx_bp_date        (payment_date),
    INDEX idx_bp_type        (transaction_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
