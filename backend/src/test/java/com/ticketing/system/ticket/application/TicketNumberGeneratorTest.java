package com.ticketing.system.ticket.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Year;
import org.junit.jupiter.api.Test;

class TicketNumberGeneratorTest {

    @Test
    void nextTicketNumberShouldUseExpectedFormat() {
        String ticketNumber = new TicketNumberGenerator().nextTicketNumber();

        assertThat(ticketNumber)
                .startsWith("TCK-" + Year.now().getValue() + "-")
                .matches("TCK-\\d{4}-[A-F0-9]{8}");
    }
}
