package com.ticketing.system.ticket.infrastructure;

import com.ticketing.system.ticket.domain.TicketEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TicketRepository extends JpaRepository<TicketEntity, UUID>, JpaSpecificationExecutor<TicketEntity> {

    Optional<TicketEntity> findByIdAndDeletedAtIsNull(UUID id);
}
