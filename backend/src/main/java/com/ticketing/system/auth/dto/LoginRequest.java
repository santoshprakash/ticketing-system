package com.ticketing.system.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Login request using seeded or registered user credentials")
public record LoginRequest(
        @Schema(example = "customer.acme@ticketing.local")
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,

        @Schema(example = "Password123!")
        @NotBlank(message = "Password is required")
        String password
) {
}
