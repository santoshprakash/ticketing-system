package com.ticketing.system.ticket.dto;

import com.ticketing.system.ticket.domain.TicketPriority;
import com.ticketing.system.ticket.domain.TicketStatus;
import java.util.UUID;

public record TicketFilter(
        TicketStatus status,
        TicketPriority priority,
        String category,
        UUID createdBy,
        UUID assignedTo
) {
}
