package com.ticketing.system.admin.dto;

import com.ticketing.system.admin.domain.ModuleCode;
import jakarta.validation.constraints.NotNull;

public record UpdateModuleAccessRequest(
        @NotNull(message = "Module code is required")
        ModuleCode moduleCode,
        boolean enabled
) {
}
