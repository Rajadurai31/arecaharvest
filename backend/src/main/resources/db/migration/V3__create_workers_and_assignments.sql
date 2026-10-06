-- =============================================================================
-- V3: Workers and Worker Assignments
-- =============================================================================
-- Workers are the primary labour resource.
-- Assignments link workers to a specific field+contract+date.
-- A worker can be assigned to multiple sessions but NOT duplicate active
-- assignments for the same worker on the same date+session.
-- =============================================================================

CREATE TABLE workers (
    id                  BIGINT       NOT NULL AUTO_INCREMENT,
    -- Optional link to a system user account (if worker logs in)
    user_id             BIGINT,
    name                VARCHAR(200) NOT NULL,
    phone               VARCHAR(20)  NOT NULL,
    alternate_phone     VARCHAR(20),
    address             TEXT,
    village             VARCHAR(200),
    district            VARCHAR(200),
    state               VARCHAR(100) DEFAULT 'Karnataka',
    -- Skill type drives wage calculation and Phase 2 workforce prediction
    skill_type          ENUM('CLIMBER','CUTTER','COLLECTOR','LOADER',
                             'HELPER','DRIVER','OTHER') NOT NULL DEFAULT 'HELPER',
    experience_years    DECIMAL(4,1) DEFAULT 0,         -- Phase 2 feature variable
    daily_wage          DECIMAL(10, 2) NOT NULL,        -- default/base daily wage in INR
    joining_date        DATE,
    status              ENUM('ACTIVE','INACTIVE','BLACKLISTED') NOT NULL DEFAULT 'ACTIVE',
    emergency_contact_name  VARCHAR(200),
    emergency_contact_phone VARCHAR(20),
    notes               TEXT,
    created_at          DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at          DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    created_by          VARCHAR(100),
    updated_by          VARCHAR(100),
    PRIMARY KEY (id),
    CONSTRAINT fk_workers_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE SET NULL,
    INDEX idx_workers_name       (name),
    INDEX idx_workers_phone      (phone),
    INDEX idx_workers_skill_type (skill_type),
    INDEX idx_workers_status     (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE worker_assignments (
    id              BIGINT   NOT NULL AUTO_INCREMENT,
    worker_id       BIGINT   NOT NULL,
    field_id        BIGINT   NOT NULL,
    contract_id     BIGINT,              -- nullable until contracts table exists (added FK in V6)
    assigned_date   DATE     NOT NULL,
    -- role in this session: may differ from worker's default skill
    assigned_role   ENUM('CLIMBER','CUTTER','COLLECTOR','LOADER',
                         'HELPER','DRIVER','OTHER'),
    assigned_by     BIGINT,              -- user_id of supervisor/admin who assigned
    notes           TEXT,
    -- ACTIVE = currently assigned; COMPLETED = session over; CANCELLED = removed
    status          ENUM('ACTIVE','COMPLETED','CANCELLED') NOT NULL DEFAULT 'ACTIVE',
    created_at      DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),
    PRIMARY KEY (id),
    -- Business rule: one active assignment per worker per field per date
    UNIQUE KEY uq_assignment_worker_field_date (worker_id, field_id, assigned_date),
    CONSTRAINT fk_wa_worker    FOREIGN KEY (worker_id)   REFERENCES workers (id) ON DELETE RESTRICT,
    CONSTRAINT fk_wa_field     FOREIGN KEY (field_id)    REFERENCES fields  (id) ON DELETE RESTRICT,
    CONSTRAINT fk_wa_assigned_by FOREIGN KEY (assigned_by) REFERENCES users (id) ON DELETE SET NULL,
    INDEX idx_wa_worker_id     (worker_id),
    INDEX idx_wa_field_id      (field_id),
    INDEX idx_wa_date          (assigned_date),
    INDEX idx_wa_status        (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
