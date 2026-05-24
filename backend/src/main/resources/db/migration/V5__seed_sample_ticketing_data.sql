INSERT INTO users (id, role_id, email, password_hash, full_name, phone_number, status, created_by)
VALUES
    (
        '10000000-0000-0000-0000-000000000011',
        '00000000-0000-0000-0000-000000000002',
        'admin.ops@ticketing.local',
        '$2a$12$BPuznNkyoa40dAe2YhgHpOGhdDokMhX7fJHDELN8cFErsccwakQ7i',
        'Operations Administrator',
        '+10000000011',
        'ACTIVE',
        '10000000-0000-0000-0000-000000000001'
    ),
    (
        '10000000-0000-0000-0000-000000000021',
        '00000000-0000-0000-0000-000000000003',
        'manager.network@ticketing.local',
        '$2a$12$BPuznNkyoa40dAe2YhgHpOGhdDokMhX7fJHDELN8cFErsccwakQ7i',
        'Network Service Manager',
        '+10000000021',
        'ACTIVE',
        '10000000-0000-0000-0000-000000000001'
    ),
    (
        '10000000-0000-0000-0000-000000000031',
        '00000000-0000-0000-0000-000000000001',
        'customer.acme@ticketing.local',
        '$2a$12$BPuznNkyoa40dAe2YhgHpOGhdDokMhX7fJHDELN8cFErsccwakQ7i',
        'Acme Customer',
        '+10000000031',
        'ACTIVE',
        '10000000-0000-0000-0000-000000000001'
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
    first_responded_at,
    resolution_due_at,
    resolved_at,
    sla_breached,
    escalation_level,
    escalated_at,
    escalated_to,
    escalation_reason,
    created_at,
    updated_at,
    updated_by
)
VALUES
    (
        '20000000-0000-0000-0000-000000000002',
        'TCK-2026-000002',
        'Password reset email not received',
        'Customer requested password reset but the email has not arrived after multiple attempts.',
        'OPEN',
        'LOW',
        'ACCOUNT',
        '10000000-0000-0000-0000-000000000031',
        NULL,
        NULL,
        now() + interval '8 hours',
        NULL,
        now() + interval '3 days',
        NULL,
        false,
        0,
        NULL,
        NULL,
        NULL,
        now() - interval '2 hours',
        now() - interval '2 hours',
        NULL
    ),
    (
        '20000000-0000-0000-0000-000000000003',
        'TCK-2026-000003',
        'Production VPN outage for branch office',
        'Branch users cannot connect to the production VPN and critical support workflows are blocked.',
        'IN_PROGRESS',
        'CRITICAL',
        'NETWORK',
        '10000000-0000-0000-0000-000000000003',
        '10000000-0000-0000-0000-000000000021',
        now() - interval '90 minutes',
        now() - interval '30 minutes',
        now() - interval '45 minutes',
        now() + interval '6 hours',
        NULL,
        false,
        1,
        now() - interval '15 minutes',
        '10000000-0000-0000-0000-000000000011',
        'Critical priority ticket requires management visibility.',
        now() - interval '3 hours',
        now() - interval '15 minutes',
        '10000000-0000-0000-0000-000000000021'
    ),
    (
        '20000000-0000-0000-0000-000000000004',
        'TCK-2026-000004',
        'Invoice export displays duplicate rows',
        'Monthly invoice export contains duplicate line items for selected customer accounts.',
        'RESOLVED',
        'MEDIUM',
        'BILLING',
        '10000000-0000-0000-0000-000000000031',
        '10000000-0000-0000-0000-000000000002',
        now() - interval '2 days',
        now() - interval '44 hours',
        now() - interval '43 hours',
        now() - interval '4 hours',
        now() - interval '5 hours',
        false,
        0,
        NULL,
        NULL,
        NULL,
        now() - interval '2 days',
        now() - interval '5 hours',
        '10000000-0000-0000-0000-000000000002'
    )
ON CONFLICT (ticket_number) DO NOTHING;

INSERT INTO assignments (id, ticket_id, assigned_to, assigned_by, assignment_type, active, assigned_at, created_by)
VALUES
    (
        '30000000-0000-0000-0000-000000000002',
        '20000000-0000-0000-0000-000000000003',
        '10000000-0000-0000-0000-000000000021',
        '10000000-0000-0000-0000-000000000011',
        'ESCALATION',
        true,
        now() - interval '90 minutes',
        '10000000-0000-0000-0000-000000000011'
    ),
    (
        '30000000-0000-0000-0000-000000000003',
        '20000000-0000-0000-0000-000000000004',
        '10000000-0000-0000-0000-000000000002',
        '10000000-0000-0000-0000-000000000001',
        'MANUAL',
        true,
        now() - interval '2 days',
        '10000000-0000-0000-0000-000000000001'
    )
ON CONFLICT DO NOTHING;

INSERT INTO ticket_comments (id, ticket_id, author_id, message, internal, created_by)
VALUES
    (
        '40000000-0000-0000-0000-000000000002',
        '20000000-0000-0000-0000-000000000003',
        '10000000-0000-0000-0000-000000000021',
        'Network team is checking firewall tunnel health and upstream route propagation.',
        true,
        '10000000-0000-0000-0000-000000000021'
    ),
    (
        '40000000-0000-0000-0000-000000000003',
        '20000000-0000-0000-0000-000000000004',
        '10000000-0000-0000-0000-000000000031',
        'Export has been verified and duplicate invoice rows are no longer present.',
        false,
        '10000000-0000-0000-0000-000000000031'
    )
ON CONFLICT (id) DO NOTHING;

INSERT INTO ticket_attachments (
    id,
    ticket_id,
    uploaded_by,
    file_name,
    content_type,
    file_size_bytes,
    storage_key,
    checksum_sha256,
    created_by
)
VALUES
    (
        '50000000-0000-0000-0000-000000000001',
        '20000000-0000-0000-0000-000000000001',
        '10000000-0000-0000-0000-000000000003',
        'portal-error.png',
        'image/png',
        245760,
        'tickets/2026/TCK-2026-000001/portal-error.png',
        'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa',
        '10000000-0000-0000-0000-000000000003'
    ),
    (
        '50000000-0000-0000-0000-000000000002',
        '20000000-0000-0000-0000-000000000003',
        '10000000-0000-0000-0000-000000000021',
        'vpn-diagnostics.txt',
        'text/plain',
        32768,
        'tickets/2026/TCK-2026-000003/vpn-diagnostics.txt',
        'bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb',
        '10000000-0000-0000-0000-000000000021'
    ),
    (
        '50000000-0000-0000-0000-000000000003',
        '20000000-0000-0000-0000-000000000004',
        '10000000-0000-0000-0000-000000000002',
        'invoice-export-before-after.csv',
        'text/csv',
        98304,
        'tickets/2026/TCK-2026-000004/invoice-export-before-after.csv',
        'cccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc',
        '10000000-0000-0000-0000-000000000002'
    )
ON CONFLICT (storage_key) DO NOTHING;

INSERT INTO ticket_history (id, ticket_id, actor_id, event_type, previous_value, new_value, description)
VALUES
    (
        '60000000-0000-0000-0000-000000000001',
        '20000000-0000-0000-0000-000000000002',
        '10000000-0000-0000-0000-000000000031',
        'CREATED',
        NULL,
        '{"status":"OPEN","priority":"LOW"}',
        'Password reset ticket created by customer.'
    ),
    (
        '60000000-0000-0000-0000-000000000002',
        '20000000-0000-0000-0000-000000000003',
        '10000000-0000-0000-0000-000000000011',
        'ESCALATED',
        '{"escalationLevel":0}',
        '{"escalationLevel":1,"escalatedTo":"10000000-0000-0000-0000-000000000011"}',
        'Critical VPN outage escalated for management visibility.'
    ),
    (
        '60000000-0000-0000-0000-000000000003',
        '20000000-0000-0000-0000-000000000004',
        '10000000-0000-0000-0000-000000000002',
        'RESOLVED',
        '{"status":"IN_PROGRESS"}',
        '{"status":"RESOLVED"}',
        'Invoice export duplication fixed and marked resolved.'
    )
ON CONFLICT (id) DO NOTHING;

INSERT INTO notifications (
    id,
    recipient_id,
    ticket_id,
    channel,
    notification_type,
    subject,
    body,
    status,
    sent_at,
    read_at,
    created_by
)
VALUES
    (
        '70000000-0000-0000-0000-000000000002',
        '10000000-0000-0000-0000-000000000031',
        '20000000-0000-0000-0000-000000000002',
        'EMAIL',
        'TICKET_CREATED',
        'Ticket created',
        'Ticket TCK-2026-000002 has been created.',
        'SENT',
        now() - interval '2 hours',
        NULL,
        '10000000-0000-0000-0000-000000000031'
    ),
    (
        '70000000-0000-0000-0000-000000000003',
        '10000000-0000-0000-0000-000000000011',
        '20000000-0000-0000-0000-000000000003',
        'IN_APP',
        'ESCALATION',
        'Critical ticket escalated',
        'Ticket TCK-2026-000003 has been escalated.',
        'READ',
        now() - interval '15 minutes',
        now() - interval '10 minutes',
        '10000000-0000-0000-0000-000000000021'
    )
ON CONFLICT (id) DO NOTHING;

INSERT INTO audit_logs (id, actor_id, action, resource_type, resource_id, trace_id, metadata)
VALUES
    (
        '80000000-0000-0000-0000-000000000002',
        '10000000-0000-0000-0000-000000000031',
        'TICKET_CREATED',
        'TICKET',
        '20000000-0000-0000-0000-000000000002',
        'seed-data',
        '{"source":"flyway-seed","ticketNumber":"TCK-2026-000002"}'
    ),
    (
        '80000000-0000-0000-0000-000000000003',
        '10000000-0000-0000-0000-000000000021',
        'TICKET_ESCALATED',
        'TICKET',
        '20000000-0000-0000-0000-000000000003',
        'seed-data',
        '{"source":"flyway-seed","ticketNumber":"TCK-2026-000003"}'
    )
ON CONFLICT (id) DO NOTHING;

INSERT INTO refresh_tokens (
    id,
    user_id,
    token_hash,
    expires_at,
    created_by
)
VALUES
    (
        '90000000-0000-0000-0000-000000000001',
        '10000000-0000-0000-0000-000000000031',
        '1111111111111111111111111111111111111111111111111111111111111111',
        now() + interval '7 days',
        '10000000-0000-0000-0000-000000000031'
    ),
    (
        '90000000-0000-0000-0000-000000000002',
        '10000000-0000-0000-0000-000000000021',
        '2222222222222222222222222222222222222222222222222222222222222222',
        now() + interval '7 days',
        '10000000-0000-0000-0000-000000000021'
    )
ON CONFLICT (token_hash) DO NOTHING;

INSERT INTO password_reset_tokens (
    id,
    user_id,
    token_hash,
    expires_at,
    created_by
)
VALUES
    (
        '91000000-0000-0000-0000-000000000001',
        '10000000-0000-0000-0000-000000000031',
        '3333333333333333333333333333333333333333333333333333333333333333',
        now() + interval '30 minutes',
        '10000000-0000-0000-0000-000000000031'
    ),
    (
        '91000000-0000-0000-0000-000000000002',
        '10000000-0000-0000-0000-000000000003',
        '4444444444444444444444444444444444444444444444444444444444444444',
        now() + interval '30 minutes',
        '10000000-0000-0000-0000-000000000003'
    )
ON CONFLICT (token_hash) DO NOTHING;
