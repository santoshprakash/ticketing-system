ALTER TABLE users
    ADD COLUMN IF NOT EXISTS super_admin BOOLEAN NOT NULL DEFAULT false;

CREATE TABLE IF NOT EXISTS user_module_access (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    module_code VARCHAR(50) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by UUID,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_by UUID,
    deleted_at TIMESTAMPTZ,
    deleted_by UUID,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_user_module_access_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_user_module_access_created_by FOREIGN KEY (created_by) REFERENCES users (id),
    CONSTRAINT fk_user_module_access_updated_by FOREIGN KEY (updated_by) REFERENCES users (id),
    CONSTRAINT fk_user_module_access_deleted_by FOREIGN KEY (deleted_by) REFERENCES users (id),
    CONSTRAINT chk_user_module_access_module CHECK (module_code IN ('DASHBOARD', 'TICKETS', 'ADMIN', 'SERVICE_MANAGER'))
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_user_module_access_user_module
    ON user_module_access (user_id, module_code)
    WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS ix_user_module_access_user_enabled
    ON user_module_access (user_id, enabled)
    WHERE deleted_at IS NULL;

CREATE TABLE IF NOT EXISTS ticket_types (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(80) NOT NULL,
    name VARCHAR(120) NOT NULL,
    description VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by UUID,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_by UUID,
    deleted_at TIMESTAMPTZ,
    deleted_by UUID,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_ticket_types_created_by FOREIGN KEY (created_by) REFERENCES users (id),
    CONSTRAINT fk_ticket_types_updated_by FOREIGN KEY (updated_by) REFERENCES users (id),
    CONSTRAINT fk_ticket_types_deleted_by FOREIGN KEY (deleted_by) REFERENCES users (id)
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_ticket_types_code_active
    ON ticket_types (lower(code))
    WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS ix_ticket_types_active_name
    ON ticket_types (active, name)
    WHERE deleted_at IS NULL;

UPDATE users
SET role_id = (SELECT id FROM roles WHERE code = 'ADMIN'),
    super_admin = true
WHERE lower(email) = 'customer.acme@ticketing.local';

INSERT INTO ticket_types (code, name, description, created_by)
VALUES
    ('ACCESS', 'Access request', 'Login, permission, account, and portal access requests.', '10000000-0000-0000-0000-000000000001'),
    ('ACCOUNT', 'Account support', 'Customer account profile and onboarding support.', '10000000-0000-0000-0000-000000000001'),
    ('NETWORK', 'Network issue', 'Connectivity, VPN, firewall, and routing incidents.', '10000000-0000-0000-0000-000000000001'),
    ('BILLING', 'Billing support', 'Invoice, export, subscription, and payment questions.', '10000000-0000-0000-0000-000000000001'),
    ('HARDWARE', 'Hardware request', 'Device provisioning, repair, and replacement support.', '10000000-0000-0000-0000-000000000001'),
    ('SOFTWARE', 'Software support', 'Application defects, configuration, and access support.', '10000000-0000-0000-0000-000000000001'),
    ('SECURITY', 'Security incident', 'Security review, vulnerability, and incident response.', '10000000-0000-0000-0000-000000000001')
ON CONFLICT DO NOTHING;

INSERT INTO user_module_access (user_id, module_code, enabled, created_by)
SELECT u.id, module_code, true, '10000000-0000-0000-0000-000000000001'
FROM users u
CROSS JOIN (VALUES ('DASHBOARD'), ('TICKETS'), ('ADMIN'), ('SERVICE_MANAGER')) AS modules(module_code)
WHERE u.super_admin = true
ON CONFLICT DO NOTHING;

INSERT INTO user_module_access (user_id, module_code, enabled, created_by)
SELECT u.id, module_code, true, '10000000-0000-0000-0000-000000000001'
FROM users u
CROSS JOIN (VALUES ('DASHBOARD'), ('TICKETS'), ('ADMIN')) AS modules(module_code)
JOIN roles r ON r.id = u.role_id
WHERE r.code = 'ADMIN' AND u.super_admin = false
ON CONFLICT DO NOTHING;

INSERT INTO user_module_access (user_id, module_code, enabled, created_by)
SELECT u.id, module_code, true, '10000000-0000-0000-0000-000000000001'
FROM users u
CROSS JOIN (VALUES ('DASHBOARD'), ('TICKETS'), ('SERVICE_MANAGER')) AS modules(module_code)
JOIN roles r ON r.id = u.role_id
WHERE r.code = 'SERVICE_MANAGER'
ON CONFLICT DO NOTHING;

INSERT INTO user_module_access (user_id, module_code, enabled, created_by)
SELECT u.id, module_code, true, '10000000-0000-0000-0000-000000000001'
FROM users u
CROSS JOIN (VALUES ('DASHBOARD'), ('TICKETS')) AS modules(module_code)
JOIN roles r ON r.id = u.role_id
WHERE r.code = 'CUSTOMER'
ON CONFLICT DO NOTHING;
