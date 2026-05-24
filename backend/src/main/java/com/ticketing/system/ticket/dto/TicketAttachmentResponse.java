package com.ticketing.system.ticket.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record TicketAttachmentResponse(
        UUID id,
        UUID ticketId,
        UUID uploadedBy,
        String fileName,
        String contentType,
        long fileSizeBytes,
        String storageKey,
        String checksumSha256,
        OffsetDateTime createdAt
) {
}
