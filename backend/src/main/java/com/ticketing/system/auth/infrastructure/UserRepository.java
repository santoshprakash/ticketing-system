package com.ticketing.system.auth.infrastructure;

import com.ticketing.system.auth.domain.UserEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {

    boolean existsByEmailIgnoreCaseAndDeletedAtIsNull(String email);

    Optional<UserEntity> findByEmailIgnoreCaseAndDeletedAtIsNull(String email);

    Optional<UserEntity> findByIdAndDeletedAtIsNull(UUID id);

    List<UserEntity> findByDeletedAtIsNullOrderByCreatedAtDesc();
}
