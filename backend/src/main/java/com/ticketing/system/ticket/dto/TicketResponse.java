package com.ticketing.system.ticket.dto;

import com.ticketing.system.ticket.domain.TicketPriority;
import com.ticketing.system.ticket.domain.TicketStatus;
import java.time.OffsetDateTime;
import java.util.UUID;

public record TicketResponse(
        UUID id,
        String ticketNumber,
        String title,
        String description,
        TicketStatus status,
        TicketPriority priority,
        String category,
        UUID createdBy,
        UUID assignedTo,
        OffsetDateTime firstResponseDueAt,
        OffsetDateTime firstRespondedAt,
        OffsetDateTime resolutionDueAt,
        OffsetDateTime resolvedAt,
        OffsetDateTime closedAt,
        boolean slaBreached,
        short escalationLevel,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
