package com.ticketing.system.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Reset password request")
public record ResetPasswordRequest(
        @Schema(example = "paste-reset-token-from-notification-or-log")
        @NotBlank(message = "Reset token is required")
        String token,

        @Schema(example = "NewPassword123!")
        @NotBlank(message = "Password is required")
        @Size(min = 12, max = 128, message = "Password must be between 12 and 128 characters")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).+$",
                message = "Password must contain uppercase, lowercase, number, and special character"
        )
        String newPassword
) {
}
