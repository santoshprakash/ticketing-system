package com.ticketing.system.ticket.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "ticket_history")
public class TicketHistoryEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "ticket_id", nullable = false)
    private UUID ticketId;

    @Column(name = "actor_id")
    private UUID actorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 60)
    private TicketHistoryEventType eventType;

    @Column(name = "previous_value", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String previousValue;

    @Column(name = "new_value", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String newValue;

    @Column(length = 1000)
    private String description;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected TicketHistoryEntity() {
    }

    public TicketHistoryEntity(UUID ticketId, UUID actorId, TicketHistoryEventType eventType, String previousValue, String newValue, String description) {
        this.ticketId = ticketId;
        this.actorId = actorId;
        this.eventType = eventType;
        this.previousValue = previousValue;
        this.newValue = newValue;
        this.description = description;
        this.createdAt = OffsetDateTime.now();
    }

    public UUID getId() { return id; }
    public UUID getTicketId() { return ticketId; }
    public UUID getActorId() { return actorId; }
    public TicketHistoryEventType getEventType() { return eventType; }
    public String getPreviousValue() { return previousValue; }
    public String getNewValue() { return newValue; }
    public String getDescription() { return description; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
