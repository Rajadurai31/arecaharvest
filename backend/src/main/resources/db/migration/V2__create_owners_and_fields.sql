-- =============================================================================
-- V2: Areca Nut Owners and their Fields
-- =============================================================================
-- Relationship: Owner 1 → N Fields
-- Field status tracks the harvesting lifecycle.
-- GPS coordinates stored for future Phase 2 geospatial analysis.
-- =============================================================================

CREATE TABLE owners (
    id                  BIGINT       NOT NULL AUTO_INCREMENT,
    name                VARCHAR(200) NOT NULL,
    phone               VARCHAR(20)  NOT NULL,
    alternate_phone     VARCHAR(20),
    email               VARCHAR(255),
    address             TEXT,
    village             VARCHAR(200),
    district            VARCHAR(200),
    state               VARCHAR(100) DEFAULT 'Karnataka',
    pincode             VARCHAR(10),
    payment_preference  ENUM('CASH','UPI','BANK_TRANSFER','CHEQUE') DEFAULT 'CASH',
    bank_name           VARCHAR(200),
    bank_account_number VARCHAR(50),
    bank_ifsc           VARCHAR(20),
    upi_id              VARCHAR(100),
    notes               TEXT,
    active              BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at          DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at          DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    created_by          VARCHAR(100),
    updated_by          VARCHAR(100),
    PRIMARY KEY (id),
    INDEX idx_owners_name    (name),
    INDEX idx_owners_phone   (phone),
    INDEX idx_owners_village (village),
    INDEX idx_owners_active  (active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE fields (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    owner_id            BIGINT          NOT NULL,
    field_name          VARCHAR(200)    NOT NULL,
    address             TEXT,
    village             VARCHAR(200),
    district            VARCHAR(200),
    state               VARCHAR(100)    DEFAULT 'Karnataka',
    -- Area stored explicitly (Phase 2: yield modelling uses acreage)
    acreage             DECIMAL(10, 4),                 -- in acres
    tree_count          INT,                            -- total areca palm count
    gps_latitude        DECIMAL(10, 7),                 -- WGS84
    gps_longitude       DECIMAL(10, 7),
    expected_yield_kg   DECIMAL(12, 3),                 -- kg per harvest cycle
    -- Status lifecycle: PENDING → ACTIVE → HARVESTING → COMPLETED / INACTIVE
    status              ENUM('PENDING','ACTIVE','HARVESTING','COMPLETED','INACTIVE')
                            NOT NULL DEFAULT 'ACTIVE',
    notes               TEXT,
    created_at          DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at          DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    created_by          VARCHAR(100),
    updated_by          VARCHAR(100),
    PRIMARY KEY (id),
    CONSTRAINT fk_fields_owner FOREIGN KEY (owner_id) REFERENCES owners (id) ON DELETE RESTRICT,
    INDEX idx_fields_owner   (owner_id),
    INDEX idx_fields_status  (status),
    INDEX idx_fields_village (village)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
