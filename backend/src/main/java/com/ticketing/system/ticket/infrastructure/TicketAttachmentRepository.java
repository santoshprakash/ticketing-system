package com.ticketing.system.ticket.infrastructure;

import com.ticketing.system.ticket.domain.TicketAttachmentEntity;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketAttachmentRepository extends JpaRepository<TicketAttachmentEntity, UUID> {

    Page<TicketAttachmentEntity> findByTicketIdAndDeletedAtIsNull(UUID ticketId, Pageable pageable);
}
