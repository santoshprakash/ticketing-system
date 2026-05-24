package com.ticketing.system.ticket.domain;

import com.ticketing.system.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "tickets")
public class TicketEntity extends BaseEntity {

    @Column(name = "ticket_number", nullable = false, unique = true, length = 40)
    private String ticketNumber;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TicketStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketPriority priority;

    @Column(nullable = false, length = 80)
    private String category;

    @Column(name = "assigned_to")
    private java.util.UUID assignedToId;

    @Column(name = "assigned_at")
    private OffsetDateTime assignedAt;

    @Column(name = "first_response_due_at")
    private OffsetDateTime firstResponseDueAt;

    @Column(name = "first_responded_at")
    private OffsetDateTime firstRespondedAt;

    @Column(name = "resolution_due_at")
    private OffsetDateTime resolutionDueAt;

    @Column(name = "resolved_at")
    private OffsetDateTime resolvedAt;

    @Column(name = "closed_at")
    private OffsetDateTime closedAt;

    @Column(name = "sla_breached", nullable = false)
    private boolean slaBreached;

    @Column(name = "escalation_level", nullable = false)
    private short escalationLevel;

    protected TicketEntity() {
    }

    public TicketEntity(String ticketNumber, String title, String description, TicketPriority priority, String category, java.util.UUID createdById) {
        this.ticketNumber = ticketNumber;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.category = category;
        setCreatedBy(createdById);
        this.status = TicketStatus.OPEN;
        this.slaBreached = false;
        this.escalationLevel = 0;
    }

    public void updateDetails(String title, String description, TicketPriority priority, String category) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.category = category;
    }

    public void assignTo(java.util.UUID assigneeId) {
        this.assignedToId = assigneeId;
        this.assignedAt = OffsetDateTime.now();
        this.status = TicketStatus.ASSIGNED;
    }

    public void changeStatus(TicketStatus newStatus) {
        this.status = newStatus;
        if (newStatus == TicketStatus.IN_PROGRESS && firstRespondedAt == null) {
            firstRespondedAt = OffsetDateTime.now();
        }
        if (newStatus == TicketStatus.RESOLVED) {
            resolvedAt = OffsetDateTime.now();
        }
        if (newStatus == TicketStatus.CLOSED) {
            closedAt = OffsetDateTime.now();
        }
        if (newStatus == TicketStatus.REOPENED) {
            resolvedAt = null;
            closedAt = null;
        }
    }

    public void setSlaDueTimes(OffsetDateTime firstResponseDueAt, OffsetDateTime resolutionDueAt) {
        this.firstResponseDueAt = firstResponseDueAt;
        this.resolutionDueAt = resolutionDueAt;
    }

    public String getTicketNumber() { return ticketNumber; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public TicketStatus getStatus() { return status; }
    public TicketPriority getPriority() { return priority; }
    public String getCategory() { return category; }
    public java.util.UUID getCreatedById() { return getCreatedBy(); }
    public java.util.UUID getAssignedToId() { return assignedToId; }
    public OffsetDateTime getAssignedAt() { return assignedAt; }
    public OffsetDateTime getFirstResponseDueAt() { return firstResponseDueAt; }
    public OffsetDateTime getFirstRespondedAt() { return firstRespondedAt; }
    public OffsetDateTime getResolutionDueAt() { return resolutionDueAt; }
    public OffsetDateTime getResolvedAt() { return resolvedAt; }
    public OffsetDateTime getClosedAt() { return closedAt; }
    public boolean isSlaBreached() { return slaBreached; }
    public short getEscalationLevel() { return escalationLevel; }
}
