package com.ticketing.system.ticket.dto;

import com.ticketing.system.ticket.domain.TicketHistoryEventType;
import java.time.OffsetDateTime;
import java.util.UUID;

public record TicketHistoryResponse(
        UUID id,
        UUID ticketId,
        UUID actorId,
        TicketHistoryEventType eventType,
        String previousValue,
        String newValue,
        String description,
        OffsetDateTime createdAt
) {
}
