package com.ticketing.system.ticket.dto;

import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Assign or reassign ticket request")
public record AssignTicketRequest(
        @Schema(example = "10000000-0000-0000-0000-000000000021")
        @NotNull(message = "Assignee id is required")
        UUID assigneeId
) {
}
