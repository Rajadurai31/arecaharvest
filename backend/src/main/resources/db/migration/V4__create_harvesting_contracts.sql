-- =============================================================================
-- V4: Harvesting Contracts
-- =============================================================================
-- A contract formalises the agreement between the contractor and field owner.
-- Contract type determines how revenue is calculated (see ContractCalculationService).
-- One field can have multiple contracts over time (harvest history).
-- =============================================================================

CREATE TABLE harvesting_contracts (
    id                    BIGINT         NOT NULL AUTO_INCREMENT,
    owner_id              BIGINT         NOT NULL,
    field_id              BIGINT         NOT NULL,
    -- Contract reference number (human-readable, e.g. CTR-2026-001)
    contract_number       VARCHAR(50)    NOT NULL UNIQUE,
    contract_type         ENUM('PER_KG','PER_TREE','FIXED_AMOUNT') NOT NULL,
    -- Rate interpretation depends on contract_type:
    --   PER_KG     → rate per KG harvested (INR/KG)
    --   PER_TREE   → rate per tree harvested (INR/tree)
    --   FIXED_AMOUNT → total fixed price for the entire field (INR)
    rate                  DECIMAL(12, 4) NOT NULL,
    fixed_amount          DECIMAL(12, 2),              -- populated when type = FIXED_AMOUNT
    expected_quantity_kg  DECIMAL(12, 3),              -- estimated harvest (for ACTIVE tracking)
    start_date            DATE           NOT NULL,
    expected_end_date     DATE,
    actual_end_date       DATE,
    status                ENUM('DRAFT','ACTIVE','PAUSED','COMPLETED','CANCELLED')
                              NOT NULL DEFAULT 'DRAFT',
    supervisor_id         BIGINT,                      -- assigned supervisor user
    notes                 TEXT,
    created_at            DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at            DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    created_by            VARCHAR(100),
    updated_by            VARCHAR(100),
    PRIMARY KEY (id),
    CONSTRAINT fk_contracts_owner      FOREIGN KEY (owner_id)      REFERENCES owners (id) ON DELETE RESTRICT,
    CONSTRAINT fk_contracts_field      FOREIGN KEY (field_id)      REFERENCES fields (id) ON DELETE RESTRICT,
    CONSTRAINT fk_contracts_supervisor FOREIGN KEY (supervisor_id) REFERENCES users  (id) ON DELETE SET NULL,
    -- Business rule: only one ACTIVE contract per field at a time
    -- Enforced at application layer; DB index aids lookups
    INDEX idx_contracts_field_status  (field_id, status),
    INDEX idx_contracts_owner         (owner_id),
    INDEX idx_contracts_status        (status),
    INDEX idx_contracts_start_date    (start_date),
    CONSTRAINT chk_contract_dates CHECK (
        expected_end_date IS NULL OR expected_end_date >= start_date
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Add FK back-reference from worker_assignments to harvesting_contracts
ALTER TABLE worker_assignments
    ADD CONSTRAINT fk_wa_contract
        FOREIGN KEY (contract_id) REFERENCES harvesting_contracts (id) ON DELETE SET NULL;
