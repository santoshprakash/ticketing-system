package com.ticketing.system.admin.controller;

import com.ticketing.system.admin.application.AdminService;
import com.ticketing.system.admin.dto.AdminResetPasswordRequest;
import com.ticketing.system.admin.dto.AdminUserResponse;
import com.ticketing.system.admin.dto.CreateAdminUserRequest;
import com.ticketing.system.admin.dto.CreateTicketTypeRequest;
import com.ticketing.system.admin.dto.ModuleAccessResponse;
import com.ticketing.system.admin.dto.TicketTypeResponse;
import com.ticketing.system.admin.dto.UpdateModuleAccessRequest;
import com.ticketing.system.security.JwtPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
@Tag(name = "Admin", description = "User, module access, and ticket type administration APIs")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/access/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Fetch current user's module access")
    public ModuleAccessResponse currentAccess(Authentication authentication) {
        return adminService.currentAccess(actorId(authentication));
    }

    @PostMapping("/users/search")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List active users for administration")
    public List<AdminUserResponse> findUsers(Authentication authentication) {
        return adminService.findUsers(actorId(authentication));
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a user with a role and default module access")
    public AdminUserResponse createUser(@Valid @RequestBody CreateAdminUserRequest request, Authentication authentication) {
        return adminService.createUser(request, actorId(authentication));
    }

    @PostMapping("/users/{userId}/delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Soft delete a user")
    public void deleteUser(@PathVariable UUID userId, Authentication authentication) {
        adminService.deleteUser(userId, actorId(authentication));
    }

    @PostMapping("/users/{userId}/reset-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reset a user's password as super admin")
    public void resetUserPassword(
            @PathVariable UUID userId,
            @Valid @RequestBody AdminResetPasswordRequest request,
            Authentication authentication
    ) {
        adminService.resetUserPassword(userId, request, actorId(authentication));
    }

    @PostMapping("/users/{userId}/module-access")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Grant or remove a module from a user")
    public AdminUserResponse updateModuleAccess(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateModuleAccessRequest request,
            Authentication authentication
    ) {
        return adminService.updateModuleAccess(userId, request, actorId(authentication));
    }

    @PostMapping("/ticket-types/search")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List ticket types")
    public List<TicketTypeResponse> findTicketTypes(Authentication authentication) {
        return adminService.findTicketTypes(actorId(authentication));
    }

    @PostMapping("/ticket-types")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a ticket type")
    public TicketTypeResponse createTicketType(@Valid @RequestBody CreateTicketTypeRequest request, Authentication authentication) {
        return adminService.createTicketType(request, actorId(authentication));
    }

    @PostMapping("/ticket-types/{ticketTypeId}/delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Soft delete a ticket type")
    public void deleteTicketType(@PathVariable UUID ticketTypeId, Authentication authentication) {
        adminService.deleteTicketType(ticketTypeId, actorId(authentication));
    }

    private static UUID actorId(Authentication authentication) {
        if (authentication == null) {
            authentication = SecurityContextHolder.getContext().getAuthentication();
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof JwtPrincipal jwtPrincipal) {
            return UUID.fromString(jwtPrincipal.subject());
        }
        return UUID.fromString(authentication.getName());
    }
}
