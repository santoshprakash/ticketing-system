package com.ticketing.system.auth.dto;

import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Refresh access token request")
public record RefreshTokenRequest(
        @Schema(example = "paste-refresh-token-from-login-response")
        @NotBlank(message = "Refresh token is required")
        String refreshToken
) {
}
