package com.ticketing.system.ticket.dto;

import com.ticketing.system.ticket.domain.TicketStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ticket status transition request")
public record ChangeTicketStatusRequest(
        @Schema(example = "IN_PROGRESS")
        @NotNull(message = "Status is required")
        TicketStatus status,

        @Schema(example = "Service manager started investigation")
        @Size(max = 500, message = "Reason must not exceed 500 characters")
        String reason
) {
}
