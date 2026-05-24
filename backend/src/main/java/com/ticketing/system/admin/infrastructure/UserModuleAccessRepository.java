package com.ticketing.system.admin.infrastructure;

import com.ticketing.system.admin.domain.ModuleCode;
import com.ticketing.system.admin.domain.UserModuleAccessEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserModuleAccessRepository extends JpaRepository<UserModuleAccessEntity, UUID> {

    List<UserModuleAccessEntity> findByUserIdAndDeletedAtIsNull(UUID userId);

    Optional<UserModuleAccessEntity> findByUserIdAndModuleCodeAndDeletedAtIsNull(UUID userId, ModuleCode moduleCode);
}
