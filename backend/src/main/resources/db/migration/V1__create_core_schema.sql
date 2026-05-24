CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by UUID,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_by UUID,
    deleted_at TIMESTAMPTZ,
    deleted_by UUID,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_roles_code UNIQUE (code),
    CONSTRAINT chk_roles_code CHECK (code IN ('CUSTOMER', 'ADMIN', 'SERVICE_MANAGER'))
);

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    role_id UUID NOT NULL,
    email VARCHAR(320) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    phone_number VARCHAR(30),
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    last_login_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by UUID,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_by UUID,
    deleted_at TIMESTAMPTZ,
    deleted_by UUID,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles (id),
    CONSTRAINT fk_users_created_by FOREIGN KEY (created_by) REFERENCES users (id),
    CONSTRAINT fk_users_updated_by FOREIGN KEY (updated_by) REFERENCES users (id),
    CONSTRAINT fk_users_deleted_by FOREIGN KEY (deleted_by) REFERENCES users (id),
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT chk_users_status CHECK (status IN ('ACTIVE', 'INVITED', 'LOCKED', 'DISABLED'))
);

ALTER TABLE roles
    ADD CONSTRAINT fk_roles_created_by FOREIGN KEY (created_by) REFERENCES users (id),
    ADD CONSTRAINT fk_roles_updated_by FOREIGN KEY (updated_by) REFERENCES users (id),
    ADD CONSTRAINT fk_roles_deleted_by FOREIGN KEY (deleted_by) REFERENCES users (id);

CREATE TABLE tickets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ticket_number VARCHAR(40) NOT NULL,
    title VARCHAR(150) NOT NULL,
    description TEXT NOT NULL,
    status VARCHAR(40) NOT NULL DEFAULT 'OPEN',
    priority VARCHAR(20) NOT NULL,
    category VARCHAR(80) NOT NULL,
    created_by UUID NOT NULL,
    assigned_to UUID,
    assigned_at TIMESTAMPTZ,
    first_response_due_at TIMESTAMPTZ,
    first_responded_at TIMESTAMPTZ,
    resolution_due_at TIMESTAMPTZ,
    resolved_at TIMESTAMPTZ,
    closed_at TIMESTAMPTZ,
    sla_breached BOOLEAN NOT NULL DEFAULT false,
    escalation_level SMALLINT NOT NULL DEFAULT 0,
    escalated_at TIMESTAMPTZ,
    escalated_to UUID,
    escalation_reason VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_by UUID,
    deleted_at TIMESTAMPTZ,
    deleted_by UUID,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_tickets_created_by FOREIGN KEY (created_by) REFERENCES users (id),
    CONSTRAINT fk_tickets_assigned_to FOREIGN KEY (assigned_to) REFERENCES users (id),
    CONSTRAINT fk_tickets_escalated_to FOREIGN KEY (escalated_to) REFERENCES users (id),
    CONSTRAINT fk_tickets_updated_by FOREIGN KEY (updated_by) REFERENCES users (id),
    CONSTRAINT fk_tickets_deleted_by FOREIGN KEY (deleted_by) REFERENCES users (id),
    CONSTRAINT uk_tickets_ticket_number UNIQUE (ticket_number),
    CONSTRAINT chk_tickets_title_length CHECK (char_length(title) BETWEEN 5 AND 150),
    CONSTRAINT chk_tickets_description_length CHECK (char_length(description) BETWEEN 20 AND 5000),
    CONSTRAINT chk_tickets_status CHECK (status IN ('OPEN', 'ASSIGNED', 'IN_PROGRESS', 'WAITING_FOR_CUSTOMER', 'RESOLVED', 'CLOSED', 'CANCELLED')),
    CONSTRAINT chk_tickets_priority CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    CONSTRAINT chk_tickets_escalation_level CHECK (escalation_level BETWEEN 0 AND 5),
    CONSTRAINT chk_tickets_resolution_order CHECK (resolved_at IS NULL OR resolved_at >= created_at),
    CONSTRAINT chk_tickets_close_order CHECK (closed_at IS NULL OR resolved_at IS NOT NULL)
);

CREATE TABLE ticket_comments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ticket_id UUID NOT NULL,
    author_id UUID NOT NULL,
    message TEXT NOT NULL,
    internal BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by UUID,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_by UUID,
    deleted_at TIMESTAMPTZ,
    deleted_by UUID,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_ticket_comments_ticket FOREIGN KEY (ticket_id) REFERENCES tickets (id),
    CONSTRAINT fk_ticket_comments_author FOREIGN KEY (author_id) REFERENCES users (id),
    CONSTRAINT fk_ticket_comments_created_by FOREIGN KEY (created_by) REFERENCES users (id),
    CONSTRAINT fk_ticket_comments_updated_by FOREIGN KEY (updated_by) REFERENCES users (id),
    CONSTRAINT fk_ticket_comments_deleted_by FOREIGN KEY (deleted_by) REFERENCES users (id),
    CONSTRAINT chk_ticket_comments_message_length CHECK (char_length(message) BETWEEN 1 AND 5000)
);

CREATE TABLE ticket_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ticket_id UUID NOT NULL,
    actor_id UUID,
    event_type VARCHAR(60) NOT NULL,
    previous_value JSONB,
    new_value JSONB,
    description VARCHAR(1000),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_ticket_history_ticket FOREIGN KEY (ticket_id) REFERENCES tickets (id),
    CONSTRAINT fk_ticket_history_actor FOREIGN KEY (actor_id) REFERENCES users (id),
    CONSTRAINT chk_ticket_history_event_type CHECK (event_type IN ('CREATED', 'ASSIGNED', 'STATUS_CHANGED', 'COMMENTED', 'ATTACHMENT_ADDED', 'SLA_BREACHED', 'ESCALATED', 'RESOLVED', 'CLOSED', 'REOPENED'))
);

CREATE TABLE ticket_attachments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ticket_id UUID NOT NULL,
    uploaded_by UUID NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(120) NOT NULL,
    file_size_bytes BIGINT NOT NULL,
    storage_key VARCHAR(500) NOT NULL,
    checksum_sha256 VARCHAR(64),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by UUID,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_by UUID,
    deleted_at TIMESTAMPTZ,
    deleted_by UUID,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_ticket_attachments_ticket FOREIGN KEY (ticket_id) REFERENCES tickets (id),
    CONSTRAINT fk_ticket_attachments_uploaded_by FOREIGN KEY (uploaded_by) REFERENCES users (id),
    CONSTRAINT fk_ticket_attachments_created_by FOREIGN KEY (created_by) REFERENCES users (id),
    CONSTRAINT fk_ticket_attachments_updated_by FOREIGN KEY (updated_by) REFERENCES users (id),
    CONSTRAINT fk_ticket_attachments_deleted_by FOREIGN KEY (deleted_by) REFERENCES users (id),
    CONSTRAINT chk_ticket_attachments_size CHECK (file_size_bytes > 0 AND file_size_bytes <= 26214400),
    CONSTRAINT uk_ticket_attachments_storage_key UNIQUE (storage_key)
);

CREATE TABLE assignments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ticket_id UUID NOT NULL,
    assigned_to UUID NOT NULL,
    assigned_by UUID NOT NULL,
    assignment_type VARCHAR(30) NOT NULL DEFAULT 'MANUAL',
    active BOOLEAN NOT NULL DEFAULT true,
    assigned_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    released_at TIMESTAMPTZ,
    release_reason VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by UUID,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_by UUID,
    deleted_at TIMESTAMPTZ,
    deleted_by UUID,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_assignments_ticket FOREIGN KEY (ticket_id) REFERENCES tickets (id),
    CONSTRAINT fk_assignments_assigned_to FOREIGN KEY (assigned_to) REFERENCES users (id),
    CONSTRAINT fk_assignments_assigned_by FOREIGN KEY (assigned_by) REFERENCES users (id),
    CONSTRAINT fk_assignments_created_by FOREIGN KEY (created_by) REFERENCES users (id),
    CONSTRAINT fk_assignments_updated_by FOREIGN KEY (updated_by) REFERENCES users (id),
    CONSTRAINT fk_assignments_deleted_by FOREIGN KEY (deleted_by) REFERENCES users (id),
    CONSTRAINT chk_assignments_type CHECK (assignment_type IN ('MANUAL', 'AUTO', 'ESCALATION')),
    CONSTRAINT chk_assignments_release_order CHECK (released_at IS NULL OR released_at >= assigned_at)
);

CREATE UNIQUE INDEX ux_assignments_one_active_per_ticket
    ON assignments (ticket_id)
    WHERE active = true AND deleted_at IS NULL;

CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    recipient_id UUID NOT NULL,
    ticket_id UUID,
    channel VARCHAR(30) NOT NULL,
    notification_type VARCHAR(60) NOT NULL,
    subject VARCHAR(200) NOT NULL,
    body TEXT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    retry_count INT NOT NULL DEFAULT 0,
    next_retry_at TIMESTAMPTZ,
    sent_at TIMESTAMPTZ,
    read_at TIMESTAMPTZ,
    failure_reason VARCHAR(1000),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by UUID,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_by UUID,
    deleted_at TIMESTAMPTZ,
    deleted_by UUID,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_notifications_recipient FOREIGN KEY (recipient_id) REFERENCES users (id),
    CONSTRAINT fk_notifications_ticket FOREIGN KEY (ticket_id) REFERENCES tickets (id),
    CONSTRAINT fk_notifications_created_by FOREIGN KEY (created_by) REFERENCES users (id),
    CONSTRAINT fk_notifications_updated_by FOREIGN KEY (updated_by) REFERENCES users (id),
    CONSTRAINT fk_notifications_deleted_by FOREIGN KEY (deleted_by) REFERENCES users (id),
    CONSTRAINT chk_notifications_channel CHECK (channel IN ('IN_APP', 'EMAIL', 'SMS')),
    CONSTRAINT chk_notifications_type CHECK (notification_type IN ('TICKET_CREATED', 'TICKET_ASSIGNED', 'TICKET_COMMENTED', 'TICKET_RESOLVED', 'SLA_WARNING', 'SLA_BREACHED', 'ESCALATION')),
    CONSTRAINT chk_notifications_status CHECK (status IN ('PENDING', 'SENT', 'FAILED', 'READ')),
    CONSTRAINT chk_notifications_retry_count CHECK (retry_count >= 0)
);

CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    actor_id UUID,
    action VARCHAR(100) NOT NULL,
    resource_type VARCHAR(80) NOT NULL,
    resource_id UUID,
    ip_address INET,
    user_agent VARCHAR(500),
    trace_id VARCHAR(100),
    metadata JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_audit_logs_actor FOREIGN KEY (actor_id) REFERENCES users (id)
);

CREATE INDEX ix_roles_active_code ON roles (code) WHERE deleted_at IS NULL;
CREATE INDEX ix_users_role_id ON users (role_id) WHERE deleted_at IS NULL;
CREATE INDEX ix_users_status ON users (status) WHERE deleted_at IS NULL;
CREATE INDEX ix_users_email_lower ON users (lower(email)) WHERE deleted_at IS NULL;
CREATE INDEX ix_tickets_created_by_created_at ON tickets (created_by, created_at DESC) WHERE deleted_at IS NULL;
CREATE INDEX ix_tickets_assigned_to_status ON tickets (assigned_to, status) WHERE deleted_at IS NULL;
CREATE INDEX ix_tickets_status_priority ON tickets (status, priority) WHERE deleted_at IS NULL;
CREATE INDEX ix_tickets_sla_due_open ON tickets (resolution_due_at, priority) WHERE deleted_at IS NULL AND status NOT IN ('RESOLVED', 'CLOSED', 'CANCELLED');
CREATE INDEX ix_tickets_escalation ON tickets (escalation_level, escalated_at DESC) WHERE deleted_at IS NULL AND escalation_level > 0;
CREATE INDEX ix_tickets_dashboard ON tickets (status, created_at DESC, priority) WHERE deleted_at IS NULL;
CREATE INDEX ix_ticket_comments_ticket_created ON ticket_comments (ticket_id, created_at DESC) WHERE deleted_at IS NULL;
CREATE INDEX ix_ticket_comments_author ON ticket_comments (author_id, created_at DESC) WHERE deleted_at IS NULL;
CREATE INDEX ix_ticket_history_ticket_created ON ticket_history (ticket_id, created_at DESC);
CREATE INDEX ix_ticket_history_event_created ON ticket_history (event_type, created_at DESC);
CREATE INDEX ix_ticket_history_new_value_gin ON ticket_history USING gin (new_value);
CREATE INDEX ix_ticket_attachments_ticket_created ON ticket_attachments (ticket_id, created_at DESC) WHERE deleted_at IS NULL;
CREATE INDEX ix_assignments_assigned_to_active ON assignments (assigned_to, assigned_at DESC) WHERE active = true AND deleted_at IS NULL;
CREATE INDEX ix_assignments_ticket_created ON assignments (ticket_id, assigned_at DESC) WHERE deleted_at IS NULL;
CREATE INDEX ix_notifications_recipient_status ON notifications (recipient_id, status, created_at DESC) WHERE deleted_at IS NULL;
CREATE INDEX ix_notifications_pending_retry ON notifications (next_retry_at, created_at) WHERE status = 'PENDING' AND deleted_at IS NULL;
CREATE INDEX ix_notifications_ticket ON notifications (ticket_id, created_at DESC) WHERE ticket_id IS NOT NULL AND deleted_at IS NULL;
CREATE INDEX ix_audit_logs_actor_created ON audit_logs (actor_id, created_at DESC);
CREATE INDEX ix_audit_logs_resource ON audit_logs (resource_type, resource_id, created_at DESC);
CREATE INDEX ix_audit_logs_action_created ON audit_logs (action, created_at DESC);
CREATE INDEX ix_audit_logs_metadata_gin ON audit_logs USING gin (metadata);
