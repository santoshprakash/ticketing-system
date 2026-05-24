package com.ticketing.system.ticket.infrastructure;

import com.ticketing.system.ticket.domain.AssignmentEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssignmentRepository extends JpaRepository<AssignmentEntity, UUID> {

    List<AssignmentEntity> findByTicketIdAndActiveTrueAndDeletedAtIsNull(UUID ticketId);
}
