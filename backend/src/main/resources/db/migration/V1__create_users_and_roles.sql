-- =============================================================================
-- V1: Users, Roles, and User-Role mapping
-- =============================================================================
-- Assumption: Role is a named constant (ADMIN/HEAD_HARVESTER, SUPERVISOR, WORKER).
--             A user may have multiple roles (e.g., an admin who also works as supervisor).
-- =============================================================================

CREATE TABLE roles (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(50)  NOT NULL UNIQUE,        -- e.g. ROLE_ADMIN, ROLE_SUPERVISOR
    description VARCHAR(255),
    created_at  DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at  DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE users (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    username       VARCHAR(100) NOT NULL UNIQUE,
    email          VARCHAR(255) UNIQUE,
    password_hash  VARCHAR(255) NOT NULL,             -- BCrypt hash; NEVER plain text
    full_name      VARCHAR(200) NOT NULL,
    phone          VARCHAR(20),
    active         BOOLEAN      NOT NULL DEFAULT TRUE,
    last_login_at  DATETIME(6),
    created_at     DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at     DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    created_by     VARCHAR(100),
    updated_by     VARCHAR(100),
    PRIMARY KEY (id),
    INDEX idx_users_email    (email),
    INDEX idx_users_username (username),
    INDEX idx_users_active   (active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Seed the three application roles
INSERT INTO roles (name, description) VALUES
    ('ROLE_ADMIN',      'Head Harvester / Contractor — full system access'),
    ('ROLE_SUPERVISOR', 'Field Supervisor — operational access, limited financial'),
    ('ROLE_WORKER',     'Harvesting Worker — view own profile, wages, attendance');
