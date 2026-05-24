package com.ticketing.system.auth.infrastructure;

import com.ticketing.system.auth.domain.RefreshTokenEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, UUID> {

    Optional<RefreshTokenEntity> findByTokenHashAndDeletedAtIsNull(String tokenHash);
}
