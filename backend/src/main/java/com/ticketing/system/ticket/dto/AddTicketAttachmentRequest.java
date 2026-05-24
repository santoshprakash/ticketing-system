package com.ticketing.system.ticket.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Register ticket attachment metadata request")
public record AddTicketAttachmentRequest(
        @Schema(example = "login-error.png")
        @NotBlank(message = "File name is required")
        @Size(max = 255, message = "File name must not exceed 255 characters")
        String fileName,

        @Schema(example = "image/png")
        @NotBlank(message = "Content type is required")
        @Size(max = 120, message = "Content type must not exceed 120 characters")
        String contentType,

        @Schema(example = "204800")
        @Min(value = 1, message = "File size must be greater than zero")
        @Max(value = 26214400, message = "File size must not exceed 25 MB")
        long fileSizeBytes,

        @Schema(example = "tickets/2026/TCK-2026-000001/login-error.png")
        @NotBlank(message = "Storage key is required")
        @Size(max = 500, message = "Storage key must not exceed 500 characters")
        String storageKey,

        @Schema(example = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")
        @Pattern(regexp = "^[a-fA-F0-9]{64}$", message = "Checksum must be a SHA-256 hex value")
        String checksumSha256
) {
}
