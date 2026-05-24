INSERT INTO roles (id, code, name, description)
VALUES
    ('00000000-0000-0000-0000-000000000001', 'CUSTOMER', 'Customer', 'Customer portal user who creates and tracks service tickets'),
    ('00000000-0000-0000-0000-000000000002', 'ADMIN', 'Administrator', 'Administrative user with user, SLA, and platform management privileges'),
    ('00000000-0000-0000-0000-000000000003', 'SERVICE_MANAGER', 'Service Manager', 'Service team user who assigns, services, escalates, and resolves tickets')
ON CONFLICT (code) DO NOTHING;

INSERT INTO users (id, role_id, email, password_hash, full_name, phone_number, status)
VALUES
    (
        '10000000-0000-0000-0000-000000000001',
        '00000000-0000-0000-0000-000000000002',
        'admin@ticketing.local',
        '$2a$12$replace.with.real.bcrypt.hash.before.use',
        'System Administrator',
        '+10000000001',
        'ACTIVE'
    ),
    (
        '10000000-0000-0000-0000-000000000002',
        '00000000-0000-0000-0000-000000000003',
        'manager@ticketing.local',
        '$2a$12$replace.with.real.bcrypt.hash.before.use',
        'Service Manager',
        '+10000000002',
        'ACTIVE'
    ),
    (
        '10000000-0000-0000-0000-000000000003',
        '00000000-0000-0000-0000-000000000001',
        'customer@ticketing.local',
        '$2a$12$replace.with.real.bcrypt.hash.before.use',
        'Demo Customer',
        '+10000000003',
        'ACTIVE'
    )
ON CONFLICT (email) DO NOTHING;

INSERT INTO tickets (
    id,
    ticket_number,
    title,
    description,
    status,
    priority,
    category,
    created_by,
    assigned_to,
    assigned_at,
    first_response_due_at,
    resolution_due_at,
    created_at,
    updated_at
)
VALUES (
    '20000000-0000-0000-0000-000000000001',
    'TCK-2026-000001',
    'Cannot access service portal',
    'Login succeeds but the dashboard page does not load after authentication.',
    'ASSIGNED',
    'HIGH',
    'ACCESS',
    '10000000-0000-0000-0000-000000000003',
    '10000000-0000-0000-0000-000000000002',
    now(),
    now() + interval '4 hours',
    now() + interval '1 day',
    now(),
    now()
)
ON CONFLICT (ticket_number) DO NOTHING;

INSERT INTO assignments (
    id,
    ticket_id,
    assigned_to,
    assigned_by,
    assignment_type,
    active,
    created_by
)
VALUES (
    '30000000-0000-0000-0000-000000000001',
    '20000000-0000-0000-0000-000000000001',
    '10000000-0000-0000-0000-000000000002',
    '10000000-0000-0000-0000-000000000001',
    'MANUAL',
    true,
    '10000000-0000-0000-0000-000000000001'
)
ON CONFLICT DO NOTHING;

INSERT INTO ticket_history (ticket_id, actor_id, event_type, previous_value, new_value, description)
VALUES
    (
        '20000000-0000-0000-0000-000000000001',
        '10000000-0000-0000-0000-000000000003',
        'CREATED',
        NULL,
        '{"status":"OPEN","priority":"HIGH"}',
        'Ticket created by customer'
    ),
    (
        '20000000-0000-0000-0000-000000000001',
        '10000000-0000-0000-0000-000000000001',
        'ASSIGNED',
        '{"assignedTo":null,"status":"OPEN"}',
        '{"assignedTo":"10000000-0000-0000-0000-000000000002","status":"ASSIGNED"}',
        'Ticket assigned to service manager'
    );

INSERT INTO ticket_comments (ticket_id, author_id, message, internal, created_by)
VALUES (
    '20000000-0000-0000-0000-000000000001',
    '10000000-0000-0000-0000-000000000003',
    'I can reproduce this issue in Chrome and Edge.',
    false,
    '10000000-0000-0000-0000-000000000003'
);

INSERT INTO notifications (
    recipient_id,
    ticket_id,
    channel,
    notification_type,
    subject,
    body,
    status
)
VALUES (
    '10000000-0000-0000-0000-000000000002',
    '20000000-0000-0000-0000-000000000001',
    'IN_APP',
    'TICKET_ASSIGNED',
    'Ticket assigned',
    'Ticket TCK-2026-000001 has been assigned to you.',
    'PENDING'
);

INSERT INTO audit_logs (actor_id, action, resource_type, resource_id, trace_id, metadata)
VALUES (
    '10000000-0000-0000-0000-000000000001',
    'TICKET_ASSIGNED',
    'TICKET',
    '20000000-0000-0000-0000-000000000001',
    'seed-data',
    '{"source":"flyway-seed"}'
);
