package com.ticketing.system.ticket.application;

import com.ticketing.system.ticket.domain.TicketPriority;
import java.time.OffsetDateTime;
import org.springframework.stereotype.Service;

@Service
public class SlaPolicyService {

    public SlaWindow calculate(TicketPriority priority) {
        OffsetDateTime now = OffsetDateTime.now();
        return switch (priority) {
            case CRITICAL -> new SlaWindow(now.plusHours(1), now.plusHours(4));
            case HIGH -> new SlaWindow(now.plusHours(4), now.plusDays(1));
            case MEDIUM -> new SlaWindow(now.plusHours(8), now.plusDays(3));
            case LOW -> new SlaWindow(now.plusDays(1), now.plusDays(5));
        };
    }

    public record SlaWindow(OffsetDateTime firstResponseDueAt, OffsetDateTime resolutionDueAt) {
    }
}
