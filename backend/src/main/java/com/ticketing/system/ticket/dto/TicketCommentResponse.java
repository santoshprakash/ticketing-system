package com.ticketing.system.ticket.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record TicketCommentResponse(
        UUID id,
        UUID ticketId,
        UUID authorId,
        String message,
        boolean internal,
        OffsetDateTime createdAt
) {
}
