package com.ticketing.system.auth.application;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.JdbcTemplate;

class AuditLoggingServiceTest {

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void recordShouldInsertAuditLogWhenJdbcTemplateIsAvailable() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        @SuppressWarnings("unchecked")
        ObjectProvider<JdbcTemplate> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(jdbcTemplate);
        UUID actorId = UUID.randomUUID();
        UUID resourceId = UUID.randomUUID();
        MDC.put("correlationId", "trace-123");

        new AuditLoggingService(provider).record(actorId, "LOGIN_SUCCESS", "USER", resourceId);

        verify(jdbcTemplate).update(
                org.mockito.ArgumentMatchers.contains("INSERT INTO audit_logs"),
                eq(actorId),
                eq("LOGIN_SUCCESS"),
                eq("USER"),
                eq(resourceId),
                eq("trace-123")
        );
    }

    @Test
    void recordShouldNoopWhenJdbcTemplateIsUnavailable() {
        @SuppressWarnings("unchecked")
        ObjectProvider<JdbcTemplate> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(null);
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);

        new AuditLoggingService(provider).record(UUID.randomUUID(), "ACTION", "USER", UUID.randomUUID());

        verify(jdbcTemplate, never()).update(org.mockito.ArgumentMatchers.anyString());
    }
}
