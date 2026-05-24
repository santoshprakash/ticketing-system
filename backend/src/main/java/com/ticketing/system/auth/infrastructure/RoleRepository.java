package com.ticketing.system.auth.infrastructure;

import com.ticketing.system.auth.domain.RoleCode;
import com.ticketing.system.auth.domain.RoleEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<RoleEntity, UUID> {

    Optional<RoleEntity> findByCodeAndDeletedAtIsNull(RoleCode code);
}
