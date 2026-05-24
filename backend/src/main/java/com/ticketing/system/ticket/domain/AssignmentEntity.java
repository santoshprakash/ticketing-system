package com.ticketing.system.ticket.domain;

import com.ticketing.system.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "assignments")
public class AssignmentEntity extends BaseEntity {

    @Column(name = "ticket_id", nullable = false)
    private UUID ticketId;

    @Column(name = "assigned_to", nullable = false)
    private UUID assignedTo;

    @Column(name = "assigned_by", nullable = false)
    private UUID assignedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "assignment_type", nullable = false, length = 30)
    private AssignmentType assignmentType;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "assigned_at", nullable = false)
    private OffsetDateTime assignedAt;

    @Column(name = "released_at")
    private OffsetDateTime releasedAt;

    @Column(name = "release_reason", length = 500)
    private String releaseReason;

    protected AssignmentEntity() {
    }

    public AssignmentEntity(UUID ticketId, UUID assignedTo, UUID assignedBy, AssignmentType assignmentType) {
        this.ticketId = ticketId;
        this.assignedTo = assignedTo;
        this.assignedBy = assignedBy;
        this.assignmentType = assignmentType;
        this.active = true;
        this.assignedAt = OffsetDateTime.now();
    }

    public void release(String reason) {
        this.active = false;
        this.releasedAt = OffsetDateTime.now();
        this.releaseReason = reason;
    }
}
