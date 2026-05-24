package com.ticketing.system.admin.dto;

import com.ticketing.system.admin.domain.ModuleCode;
import java.util.Set;

public record ModuleAccessResponse(
        boolean superAdmin,
        Set<ModuleCode> modules
) {
}
