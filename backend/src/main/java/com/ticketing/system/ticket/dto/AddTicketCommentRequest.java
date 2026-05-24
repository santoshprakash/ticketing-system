package com.ticketing.system.ticket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Add ticket comment request")
public record AddTicketCommentRequest(
        @Schema(example = "We verified the account and found the MFA device is out of sync.")
        @NotBlank(message = "Message is required")
        @Size(max = 5000, message = "Message must not exceed 5000 characters")
        String message,
        @Schema(example = "false")
        boolean internal
) {
}
