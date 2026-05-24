package com.ticketing.system.auth.infrastructure;

import com.ticketing.system.auth.domain.PasswordResetTokenEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetTokenEntity, UUID> {

    Optional<PasswordResetTokenEntity> findByTokenHashAndDeletedAtIsNull(String tokenHash);
}
