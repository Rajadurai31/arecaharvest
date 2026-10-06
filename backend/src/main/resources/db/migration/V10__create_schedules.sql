-- =============================================================================
-- V10: Work Scheduling
-- =============================================================================
-- Schedules allow the Head Harvester to plan field work in advance.
-- A schedule is the planned version; harvest_session is the executed version.
-- Linking a schedule to a session provides planned-vs-actual comparison.
-- =============================================================================

CREATE TABLE schedules (
    id                    BIGINT         NOT NULL AUTO_INCREMENT,
    schedule_date         DATE           NOT NULL,
    field_id              BIGINT         NOT NULL,
    contract_id           BIGINT,
    supervisor_id         BIGINT,
    vehicle_id            BIGINT,
    planned_start_time    TIME,
    planned_end_time      TIME,
    expected_quantity_kg  DECIMAL(12, 3),
    expected_worker_count INT,
    notes                 TEXT,
    status                ENUM('PLANNED','CONFIRMED','IN_PROGRESS','COMPLETED','CANCELLED')
                              NOT NULL DEFAULT 'PLANNED',
    -- Link to executed harvest session once it occurs
    harvest_session_id    BIGINT,
    created_at            DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at            DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    created_by            VARCHAR(100),
    updated_by            VARCHAR(100),
    PRIMARY KEY (id),
    CONSTRAINT fk_sched_field    FOREIGN KEY (field_id)           REFERENCES fields           (id) ON DELETE RESTRICT,
    CONSTRAINT fk_sched_contract FOREIGN KEY (contract_id)        REFERENCES harvesting_contracts (id) ON DELETE SET NULL,
    CONSTRAINT fk_sched_super    FOREIGN KEY (supervisor_id)      REFERENCES users            (id) ON DELETE SET NULL,
    CONSTRAINT fk_sched_vehicle  FOREIGN KEY (vehicle_id)         REFERENCES vehicles         (id) ON DELETE SET NULL,
    CONSTRAINT fk_sched_session  FOREIGN KEY (harvest_session_id) REFERENCES harvest_sessions (id) ON DELETE SET NULL,
    INDEX idx_sched_date         (schedule_date),
    INDEX idx_sched_field_date   (field_id, schedule_date),
    INDEX idx_sched_status       (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── Schedule Workers (planned worker list) ────────────────────────────────────
CREATE TABLE schedule_workers (
    schedule_id BIGINT NOT NULL,
    worker_id   BIGINT NOT NULL,
    PRIMARY KEY (schedule_id, worker_id),
    CONSTRAINT fk_schw_schedule FOREIGN KEY (schedule_id) REFERENCES schedules (id) ON DELETE CASCADE,
    CONSTRAINT fk_schw_worker   FOREIGN KEY (worker_id)   REFERENCES workers   (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
