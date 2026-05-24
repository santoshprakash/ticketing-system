package com.ticketing.system.auth.application;

import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class AuditLoggingService {

    private final JdbcTemplate jdbcTemplate;

    public AuditLoggingService(ObjectProvider<JdbcTemplate> jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate.getIfAvailable();
    }

    public void record(UUID actorId, String action, String resourceType, UUID resourceId) {
        if (jdbcTemplate == null) {
            return;
        }
        jdbcTemplate.update(
                """
                INSERT INTO audit_logs (actor_id, action, resource_type, resource_id, trace_id, metadata)
                VALUES (?, ?, ?, ?, ?, '{}'::jsonb)
                """,
                actorId,
                action,
                resourceType,
                resourceId,
                MDC.get("correlationId")
        );
    }
}
