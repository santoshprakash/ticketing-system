package com.ticketing.system.ticket.infrastructure;

import com.ticketing.system.ticket.domain.TicketHistoryEntity;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketHistoryRepository extends JpaRepository<TicketHistoryEntity, UUID> {

    Page<TicketHistoryEntity> findByTicketId(UUID ticketId, Pageable pageable);
}
