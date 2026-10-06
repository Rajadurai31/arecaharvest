-- =============================================================================
-- V9: Vehicles and Equipment
-- =============================================================================

-- ── Vehicles ──────────────────────────────────────────────────────────────────
CREATE TABLE vehicles (
    id                  BIGINT       NOT NULL AUTO_INCREMENT,
    registration_number VARCHAR(20)  NOT NULL UNIQUE,
    vehicle_type        ENUM('TRUCK','TRACTOR','PICKUP','AUTO','BIKE','OTHER') NOT NULL,
    make                VARCHAR(100),
    model               VARCHAR(100),
    year                INT,
    driver_id           BIGINT,                   -- FK to workers (driver)
    fuel_type           ENUM('PETROL','DIESEL','ELECTRIC','OTHER') DEFAULT 'DIESEL',
    status              ENUM('ACTIVE','INACTIVE','MAINTENANCE') NOT NULL DEFAULT 'ACTIVE',
    insurance_expiry    DATE,
    last_service_date   DATE,
    next_service_date   DATE,
    notes               TEXT,
    created_at          DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at          DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    created_by          VARCHAR(100),
    updated_by          VARCHAR(100),
    PRIMARY KEY (id),
    CONSTRAINT fk_veh_driver FOREIGN KEY (driver_id) REFERENCES workers (id) ON DELETE SET NULL,
    INDEX idx_veh_status (status),
    INDEX idx_veh_type   (vehicle_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Add FK from expenses to vehicles
ALTER TABLE expenses
    ADD CONSTRAINT fk_exp_vehicle
        FOREIGN KEY (vehicle_id) REFERENCES vehicles (id) ON DELETE SET NULL;

-- ── Equipment ─────────────────────────────────────────────────────────────────
CREATE TABLE equipment (
    id                  BIGINT       NOT NULL AUTO_INCREMENT,
    name                VARCHAR(200) NOT NULL,
    category            ENUM('LADDER','ROPE','CUTTER','HARVESTING_MACHINE',
                             'BAGS','SAFETY_EQUIPMENT','TARPAULIN','OTHER') NOT NULL,
    quantity            INT          NOT NULL DEFAULT 1,
    condition_status    ENUM('GOOD','FAIR','POOR','DAMAGED') NOT NULL DEFAULT 'GOOD',
    purchase_date       DATE,
    purchase_cost       DECIMAL(10, 2),
    -- Assigned to a worker or field
    assigned_worker_id  BIGINT,
    assigned_field_id   BIGINT,
    last_maintenance_date DATE,
    next_maintenance_date DATE,
    status              ENUM('AVAILABLE','IN_USE','MAINTENANCE','RETIRED') NOT NULL DEFAULT 'AVAILABLE',
    notes               TEXT,
    created_at          DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at          DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    created_by          VARCHAR(100),
    updated_by          VARCHAR(100),
    PRIMARY KEY (id),
    CONSTRAINT fk_eq_worker FOREIGN KEY (assigned_worker_id) REFERENCES workers (id) ON DELETE SET NULL,
    CONSTRAINT fk_eq_field  FOREIGN KEY (assigned_field_id)  REFERENCES fields  (id) ON DELETE SET NULL,
    INDEX idx_eq_category (category),
    INDEX idx_eq_status   (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
