package com.ticketing.system.ticket.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.ticketing.system.ticket.domain.TicketAttachmentEntity;
import com.ticketing.system.ticket.domain.TicketCommentEntity;
import com.ticketing.system.ticket.domain.TicketEntity;
import com.ticketing.system.ticket.domain.TicketHistoryEntity;
import com.ticketing.system.ticket.domain.TicketHistoryEventType;
import com.ticketing.system.ticket.domain.TicketPriority;
import com.ticketing.system.ticket.dto.TicketAttachmentResponse;
import com.ticketing.system.ticket.dto.TicketCommentResponse;
import com.ticketing.system.ticket.dto.TicketHistoryResponse;
import com.ticketing.system.ticket.dto.TicketResponse;
import com.ticketing.system.testsupport.TestEntityFactory;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TicketMapperTest {

    private final TicketMapper mapper = new TicketMapper();

    @Test
    void toResponseShouldMapTicketFields() {
        UUID createdBy = UUID.randomUUID();
        UUID ticketId = UUID.randomUUID();
        TicketEntity ticket = new TicketEntity("TCK-2026-000001", "Portal issue", "Customer cannot access the service portal.", TicketPriority.HIGH, "ACCESS", createdBy);
        TestEntityFactory.assignBaseEntity(ticket, ticketId);

        TicketResponse response = mapper.toResponse(ticket);

        assertThat(response.id()).isEqualTo(ticketId);
        assertThat(response.ticketNumber()).isEqualTo("TCK-2026-000001");
        assertThat(response.createdBy()).isEqualTo(createdBy);
        assertThat(response.status().name()).isEqualTo("OPEN");
    }

    @Test
    void toCommentResponseShouldMapCommentFields() {
        UUID ticketId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        TicketCommentEntity comment = new TicketCommentEntity(ticketId, authorId, "Investigating", false);
        TestEntityFactory.assignBaseEntity(comment, UUID.randomUUID());

        TicketCommentResponse response = mapper.toCommentResponse(comment);

        assertThat(response.ticketId()).isEqualTo(ticketId);
        assertThat(response.authorId()).isEqualTo(authorId);
        assertThat(response.message()).isEqualTo("Investigating");
        assertThat(response.internal()).isFalse();
    }

    @Test
    void toAttachmentResponseShouldMapAttachmentFields() {
        UUID ticketId = UUID.randomUUID();
        UUID uploadedBy = UUID.randomUUID();
        TicketAttachmentEntity attachment = new TicketAttachmentEntity(
                ticketId,
                uploadedBy,
                "error.png",
                "image/png",
                1024,
                "tickets/error.png",
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        );
        TestEntityFactory.assignBaseEntity(attachment, UUID.randomUUID());

        TicketAttachmentResponse response = mapper.toAttachmentResponse(attachment);

        assertThat(response.ticketId()).isEqualTo(ticketId);
        assertThat(response.uploadedBy()).isEqualTo(uploadedBy);
        assertThat(response.fileName()).isEqualTo("error.png");
        assertThat(response.fileSizeBytes()).isEqualTo(1024);
    }

    @Test
    void toHistoryResponseShouldMapHistoryFields() {
        UUID ticketId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        TicketHistoryEntity history = new TicketHistoryEntity(ticketId, actorId, TicketHistoryEventType.CREATED, null, "{\"status\":\"OPEN\"}", "Ticket created");

        TicketHistoryResponse response = mapper.toHistoryResponse(history);

        assertThat(response.ticketId()).isEqualTo(ticketId);
        assertThat(response.actorId()).isEqualTo(actorId);
        assertThat(response.eventType()).isEqualTo(TicketHistoryEventType.CREATED);
        assertThat(response.description()).isEqualTo("Ticket created");
    }
}
