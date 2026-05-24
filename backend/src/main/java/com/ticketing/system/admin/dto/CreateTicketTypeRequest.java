package com.ticketing.system.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTicketTypeRequest(
        @NotBlank(message = "Code is required")
        @Size(max = 80, message = "Code must not exceed 80 characters")
        String code,

        @NotBlank(message = "Name is required")
        @Size(max = 120, message = "Name must not exceed 120 characters")
        String name,

        @Size(max = 500, message = "Description must not exceed 500 characters")
        String description
) {
}
