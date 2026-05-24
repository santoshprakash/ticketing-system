ALTER TABLE tickets DROP CONSTRAINT chk_tickets_status;

ALTER TABLE tickets
    ADD CONSTRAINT chk_tickets_status
    CHECK (status IN ('OPEN', 'ASSIGNED', 'IN_PROGRESS', 'RESOLVED', 'CLOSED', 'REOPENED'));
