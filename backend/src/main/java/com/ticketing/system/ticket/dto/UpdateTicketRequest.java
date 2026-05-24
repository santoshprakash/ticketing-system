package com.ticketing.system.ticket.dto;

import com.ticketing.system.ticket.domain.TicketPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Update ticket summary fields request")
public record UpdateTicketRequest(
        @Schema(example = "Portal login issue after MFA reset")
        @NotBlank(message = "Title is required")
        @Size(min = 5, max = 150, message = "Title must be between 5 and 150 characters")
        String title,

        @Schema(example = "Customer cannot access the service portal after password and MFA reset.")
        @NotBlank(message = "Description is required")
        @Size(min = 20, max = 5000, message = "Description must be between 20 and 5000 characters")
        String description,

        @Schema(example = "HIGH")
        @NotNull(message = "Priority is required")
        TicketPriority priority,

        @Schema(example = "ACCESS")
        @NotBlank(message = "Category is required")
        @Size(max = 80, message = "Category must not exceed 80 characters")
        String category
) {
}
