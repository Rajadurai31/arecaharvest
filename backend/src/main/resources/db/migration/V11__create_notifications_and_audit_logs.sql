-- =============================================================================
-- V11: In-App Notifications and Audit Logs
-- =============================================================================

-- ── Notifications ─────────────────────────────────────────────────────────────
-- Designed to support future external delivery (email/SMS/WhatsApp) without
-- schema changes — just add a 'delivery_channel' enum value.
CREATE TABLE notifications (
    id                BIGINT       NOT NULL AUTO_INCREMENT,
    recipient_user_id BIGINT       NOT NULL,
    type              ENUM(
                          'PENDING_WORKER_WAGES',
                          'BUYER_OUTSTANDING',
                          'CONTRACT_NEARING_COMPLETION',
                          'SCHEDULED_WORK_REMINDER',
                          'EQUIPMENT_MAINTENANCE_DUE',
                          'FIELD_HARVEST_COMPLETE',
                          'PAYMENT_RECEIVED',
                          'SYSTEM'
                      ) NOT NULL,
    title             VARCHAR(300) NOT NULL,
    message           TEXT         NOT NULL,
    -- Optional deep-link to the relevant entity
    entity_type       VARCHAR(100),               -- e.g. "WORKER", "SALE", "CONTRACT"
    entity_id         BIGINT,
    is_read           BOOLEAN      NOT NULL DEFAULT FALSE,
    read_at           DATETIME(6),
    priority          ENUM('LOW','NORMAL','HIGH') NOT NULL DEFAULT 'NORMAL',
    created_at        DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at        DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_notif_user FOREIGN KEY (recipient_user_id) REFERENCES users (id) ON DELETE CASCADE,
    INDEX idx_notif_user_read   (recipient_user_id, is_read),
    INDEX idx_notif_created     (created_at),
    INDEX idx_notif_type        (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── Audit Logs ────────────────────────────────────────────────────────────────
-- Immutable append-only log. Never update or delete rows.
-- previous_value and new_value stored as JSON text for flexibility.
CREATE TABLE audit_logs (
    id              BIGINT        NOT NULL AUTO_INCREMENT,
    performed_by    BIGINT,                           -- user_id (nullable if system action)
    action          VARCHAR(100)  NOT NULL,           -- e.g. CREATE, UPDATE, DELETE, PAYMENT
    entity_type     VARCHAR(100)  NOT NULL,           -- e.g. SALE, WORKER_PAYMENT
    entity_id       BIGINT,
    previous_value  JSON,                             -- state before change
    new_value       JSON,                             -- state after change
    ip_address      VARCHAR(45),                      -- supports IPv6
    user_agent      VARCHAR(500),
    description     TEXT,
    created_at      DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_audit_user FOREIGN KEY (performed_by) REFERENCES users (id) ON DELETE SET NULL,
    INDEX idx_audit_entity     (entity_type, entity_id),
    INDEX idx_audit_user       (performed_by),
    INDEX idx_audit_created    (created_at),
    INDEX idx_audit_action     (action)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
