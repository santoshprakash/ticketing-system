package com.ticketing.system.admin.infrastructure;

import com.ticketing.system.admin.domain.TicketTypeEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketTypeRepository extends JpaRepository<TicketTypeEntity, UUID> {

    boolean existsByCodeIgnoreCaseAndDeletedAtIsNull(String code);

    Optional<TicketTypeEntity> findByIdAndDeletedAtIsNull(UUID id);

    List<TicketTypeEntity> findByDeletedAtIsNullOrderByNameAsc();
}
