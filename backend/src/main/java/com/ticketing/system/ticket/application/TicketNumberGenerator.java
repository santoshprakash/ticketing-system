package com.ticketing.system.ticket.application;

import java.time.Year;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class TicketNumberGenerator {

    public String nextTicketNumber() {
        String suffix = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "TCK-" + Year.now().getValue() + "-" + suffix;
    }
}
