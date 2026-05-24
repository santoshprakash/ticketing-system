package com.ticketing.system.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Forgot password request")
public record ForgotPasswordRequest(
        @Schema(example = "customer.acme@ticketing.local")
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email
) {
}
