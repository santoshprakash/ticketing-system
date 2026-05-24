package com.ticketing.system.ticket.infrastructure;

import com.ticketing.system.ticket.domain.TicketCommentEntity;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketCommentRepository extends JpaRepository<TicketCommentEntity, UUID> {

    Page<TicketCommentEntity> findByTicketIdAndDeletedAtIsNull(UUID ticketId, Pageable pageable);
}
