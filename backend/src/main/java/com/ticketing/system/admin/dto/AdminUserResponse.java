package com.ticketing.system.admin.dto;

import com.ticketing.system.admin.domain.ModuleCode;
import com.ticketing.system.auth.domain.RoleCode;
import com.ticketing.system.auth.domain.UserStatus;
import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

public record AdminUserResponse(
        UUID id,
        String email,
        String fullName,
        String phoneNumber,
        RoleCode role,
        UserStatus status,
        boolean superAdmin,
        Set<ModuleCode> moduleAccess,
        OffsetDateTime createdAt
) {
}
