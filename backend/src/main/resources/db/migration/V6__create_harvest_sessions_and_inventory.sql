-- =============================================================================
-- V6: Harvest Sessions, Harvest Records, and Inventory
-- =============================================================================
-- Workflow: Contract → Harvest Session → Harvest Record → Inventory → Sale
--
-- harvest_sessions   = a single day's organized harvesting event at a field
-- harvest_records    = the actual quantity details recorded for that session
-- inventory          = what's available to sell after harvesting
--
-- Unit is always stored explicitly alongside quantity (Phase 2 data quality).
-- Historical records are NEVER overwritten — amendments create new records.
-- =============================================================================

-- ── Harvest Sessions ─────────────────────────────────────────────────────────
CREATE TABLE harvest_sessions (
    id                    BIGINT         NOT NULL AUTO_INCREMENT,
    session_date          DATE           NOT NULL,
    field_id              BIGINT         NOT NULL,
    contract_id           BIGINT         NOT NULL,
    supervisor_id         BIGINT,                     -- user_id of supervising user
    start_time            TIME,
    end_time              TIME,
    -- Quantities — unit always stored explicitly
    total_bags            INT            DEFAULT 0,
    total_quantity        DECIMAL(12, 3) DEFAULT 0,
    quantity_unit         VARCHAR(10)    NOT NULL DEFAULT 'KG',
    -- Derived/cached counts (recomputed on update)
    worker_count          INT            DEFAULT 0,
    total_working_hours   DECIMAL(8, 2)  DEFAULT 0,
    status                ENUM('PLANNED','IN_PROGRESS','COMPLETED','CANCELLED')
                              NOT NULL DEFAULT 'PLANNED',
    remarks               TEXT,
    -- Phase 2: weather, season, soil moisture can be added here later
    created_at            DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at            DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    created_by            VARCHAR(100),
    updated_by            VARCHAR(100),
    PRIMARY KEY (id),
    CONSTRAINT fk_hs_field      FOREIGN KEY (field_id)    REFERENCES fields                (id) ON DELETE RESTRICT,
    CONSTRAINT fk_hs_contract   FOREIGN KEY (contract_id) REFERENCES harvesting_contracts  (id) ON DELETE RESTRICT,
    CONSTRAINT fk_hs_supervisor FOREIGN KEY (supervisor_id) REFERENCES users               (id) ON DELETE SET NULL,
    INDEX idx_hs_field_date     (field_id, session_date),
    INDEX idx_hs_contract       (contract_id),
    INDEX idx_hs_date           (session_date),
    INDEX idx_hs_status         (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Add FK from attendance.harvest_session_id back to harvest_sessions
ALTER TABLE attendance
    ADD CONSTRAINT fk_att_session
        FOREIGN KEY (harvest_session_id) REFERENCES harvest_sessions (id) ON DELETE SET NULL;

-- ── Session–Worker mapping (N:N) ─────────────────────────────────────────────
CREATE TABLE session_workers (
    session_id  BIGINT NOT NULL,
    worker_id   BIGINT NOT NULL,
    PRIMARY KEY (session_id, worker_id),
    CONSTRAINT fk_sw_session FOREIGN KEY (session_id) REFERENCES harvest_sessions (id) ON DELETE CASCADE,
    CONSTRAINT fk_sw_worker  FOREIGN KEY (worker_id)  REFERENCES workers          (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── Harvest Records ───────────────────────────────────────────────────────────
-- Immutable append-only records of what was actually harvested.
-- Amendments must create a new record with is_amendment=TRUE referencing original.
CREATE TABLE harvest_records (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    harvest_session_id  BIGINT          NOT NULL,
    field_id            BIGINT          NOT NULL,
    contract_id         BIGINT          NOT NULL,
    owner_id            BIGINT          NOT NULL,
    record_date         DATE            NOT NULL,
    -- Quantity fields — unit stored explicitly always
    bags_count          INT             DEFAULT 0,
    quantity            DECIMAL(12, 3)  NOT NULL,
    quantity_unit       VARCHAR(10)     NOT NULL DEFAULT 'KG',
    -- Workforce snapshot at time of harvest (Phase 2 feature variable)
    worker_count        INT,
    start_time          TIME,
    end_time            TIME,
    remarks             TEXT,
    -- Audit trail for amendments
    is_amendment        BOOLEAN         NOT NULL DEFAULT FALSE,
    original_record_id  BIGINT,                         -- references harvest_records.id
    amended_by          BIGINT,
    amendment_reason    TEXT,
    created_at          DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at          DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    created_by          VARCHAR(100),
    updated_by          VARCHAR(100),
    PRIMARY KEY (id),
    CONSTRAINT fk_hr_session   FOREIGN KEY (harvest_session_id) REFERENCES harvest_sessions      (id) ON DELETE RESTRICT,
    CONSTRAINT fk_hr_field     FOREIGN KEY (field_id)           REFERENCES fields                (id) ON DELETE RESTRICT,
    CONSTRAINT fk_hr_contract  FOREIGN KEY (contract_id)        REFERENCES harvesting_contracts  (id) ON DELETE RESTRICT,
    CONSTRAINT fk_hr_owner     FOREIGN KEY (owner_id)           REFERENCES owners                (id) ON DELETE RESTRICT,
    CONSTRAINT fk_hr_original  FOREIGN KEY (original_record_id) REFERENCES harvest_records       (id) ON DELETE SET NULL,
    CONSTRAINT fk_hr_amended_by FOREIGN KEY (amended_by)        REFERENCES users                 (id) ON DELETE SET NULL,
    CONSTRAINT chk_hr_quantity CHECK (quantity >= 0),
    INDEX idx_hr_session       (harvest_session_id),
    INDEX idx_hr_field_date    (field_id, record_date),
    INDEX idx_hr_contract      (contract_id),
    INDEX idx_hr_date          (record_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── Inventory ─────────────────────────────────────────────────────────────────
-- Tracks post-harvest physical stock available for sale.
CREATE TABLE inventory (
    id                  BIGINT         NOT NULL AUTO_INCREMENT,
    harvest_record_id   BIGINT,                        -- source harvest record
    field_id            BIGINT         NOT NULL,
    contract_id         BIGINT,
    -- Quantity always with explicit unit
    quantity_received   DECIMAL(12, 3) NOT NULL,
    quantity_unit       VARCHAR(10)    NOT NULL DEFAULT 'KG',
    quantity_sold       DECIMAL(12, 3) NOT NULL DEFAULT 0,
    -- Computed: available_quantity = quantity_received - quantity_sold
    -- Stored redundantly for quick reads; must stay in sync via transaction
    available_quantity  DECIMAL(12, 3) NOT NULL,
    storage_location    VARCHAR(200),
    received_date       DATE           NOT NULL,
    status              ENUM('AVAILABLE','PARTIALLY_SOLD','SOLD','DAMAGED')
                            NOT NULL DEFAULT 'AVAILABLE',
    notes               TEXT,
    created_at          DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at          DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    created_by          VARCHAR(100),
    updated_by          VARCHAR(100),
    PRIMARY KEY (id),
    CONSTRAINT fk_inv_harvest   FOREIGN KEY (harvest_record_id) REFERENCES harvest_records       (id) ON DELETE SET NULL,
    CONSTRAINT fk_inv_field     FOREIGN KEY (field_id)          REFERENCES fields                (id) ON DELETE RESTRICT,
    CONSTRAINT fk_inv_contract  FOREIGN KEY (contract_id)       REFERENCES harvesting_contracts  (id) ON DELETE SET NULL,
    CONSTRAINT chk_inv_qty      CHECK (quantity_received >= 0 AND quantity_sold >= 0
                                       AND available_quantity >= 0
                                       AND quantity_sold <= quantity_received),
    INDEX idx_inv_field         (field_id),
    INDEX idx_inv_status        (status),
    INDEX idx_inv_received_date (received_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
