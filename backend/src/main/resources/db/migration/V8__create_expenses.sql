-- =============================================================================
-- V8: Business Expenses
-- =============================================================================
-- Expenses reduce business profit.
-- Categories are stored in a separate table to allow future additions.
-- Expenses can be optionally linked to a field or contract for cost allocation.
-- =============================================================================

CREATE TABLE expense_categories (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    is_system   BOOLEAN      NOT NULL DEFAULT FALSE,   -- system categories cannot be deleted
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at  DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    INDEX idx_expense_cat_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Seed standard expense categories (is_system = TRUE means they can't be deleted)
INSERT INTO expense_categories (name, description, is_system) VALUES
    ('WORKER',          'Worker wages, advances, and labour costs',  TRUE),
    ('TRANSPORT',       'Transportation of workers or produce',       TRUE),
    ('FUEL',            'Fuel for vehicles or equipment',             TRUE),
    ('FOOD',            'Meals and food for workers',                 TRUE),
    ('ACCOMMODATION',   'Lodging for workers on remote fields',       TRUE),
    ('EQUIPMENT',       'Equipment purchase or rental',               TRUE),
    ('MACHINE_REPAIR',  'Machinery maintenance and repair',           TRUE),
    ('VEHICLE_REPAIR',  'Vehicle maintenance and repair',             TRUE),
    ('COMMUNICATION',   'Phone calls, internet',                      TRUE),
    ('OTHER',           'Miscellaneous expenses',                     TRUE);

CREATE TABLE expenses (
    id                  BIGINT         NOT NULL AUTO_INCREMENT,
    expense_date        DATE           NOT NULL,
    category_id         BIGINT         NOT NULL,
    amount              DECIMAL(12, 2) NOT NULL,
    payment_method      ENUM('CASH','UPI','BANK_TRANSFER','OTHER') NOT NULL DEFAULT 'CASH',
    description         TEXT           NOT NULL,
    -- Optional cost allocation to field/contract
    field_id            BIGINT,
    contract_id         BIGINT,
    vehicle_id          BIGINT,                     -- FK added in V9
    receipt_url         VARCHAR(500),               -- cloud storage path for receipt image
    recorded_by         BIGINT,
    created_at          DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at          DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    created_by          VARCHAR(100),
    updated_by          VARCHAR(100),
    PRIMARY KEY (id),
    CONSTRAINT fk_exp_category   FOREIGN KEY (category_id)  REFERENCES expense_categories    (id) ON DELETE RESTRICT,
    CONSTRAINT fk_exp_field      FOREIGN KEY (field_id)     REFERENCES fields                (id) ON DELETE SET NULL,
    CONSTRAINT fk_exp_contract   FOREIGN KEY (contract_id)  REFERENCES harvesting_contracts  (id) ON DELETE SET NULL,
    CONSTRAINT fk_exp_recorded_by FOREIGN KEY (recorded_by) REFERENCES users                 (id) ON DELETE SET NULL,
    CONSTRAINT chk_exp_amount CHECK (amount > 0),
    INDEX idx_exp_date           (expense_date),
    INDEX idx_exp_category       (category_id),
    INDEX idx_exp_field          (field_id),
    INDEX idx_exp_payment_method (payment_method)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
