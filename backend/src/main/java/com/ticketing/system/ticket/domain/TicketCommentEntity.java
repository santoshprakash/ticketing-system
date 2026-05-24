package com.ticketing.system.ticket.domain;

import com.ticketing.system.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "ticket_comments")
public class TicketCommentEntity extends BaseEntity {

    @Column(name = "ticket_id", nullable = false)
    private UUID ticketId;

    @Column(name = "author_id", nullable = false)
    private UUID authorId;

    @Column(nullable = false, columnDefinition = "text")
    private String message;

    @Column(nullable = false)
    private boolean internal;

    protected TicketCommentEntity() {
    }

    public TicketCommentEntity(UUID ticketId, UUID authorId, String message, boolean internal) {
        this.ticketId = ticketId;
        this.authorId = authorId;
        this.message = message;
        this.internal = internal;
    }

    public UUID getTicketId() { return ticketId; }
    public UUID getAuthorId() { return authorId; }
    public String getMessage() { return message; }
    public boolean isInternal() { return internal; }
}
