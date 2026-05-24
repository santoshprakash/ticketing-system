package com.ticketing.system.admin.dto;

import java.util.UUID;

public record TicketTypeResponse(
        UUID id,
        String code,
        String name,
        String description,
        boolean active
) {
}
