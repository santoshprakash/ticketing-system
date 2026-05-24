package com.ticketing.system.auth.dto;

import com.ticketing.system.auth.domain.RoleCode;
import java.util.UUID;

public record AuthUserResponse(
        UUID id,
        String email,
        String fullName,
        RoleCode role
) {
}
