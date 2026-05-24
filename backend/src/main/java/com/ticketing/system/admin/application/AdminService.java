package com.ticketing.system.admin.application;

import com.ticketing.system.admin.domain.ModuleCode;
import com.ticketing.system.admin.domain.TicketTypeEntity;
import com.ticketing.system.admin.domain.UserModuleAccessEntity;
import com.ticketing.system.admin.dto.AdminResetPasswordRequest;
import com.ticketing.system.admin.dto.AdminUserResponse;
import com.ticketing.system.admin.dto.CreateAdminUserRequest;
import com.ticketing.system.admin.dto.CreateTicketTypeRequest;
import com.ticketing.system.admin.dto.ModuleAccessResponse;
import com.ticketing.system.admin.dto.TicketTypeResponse;
import com.ticketing.system.admin.dto.UpdateModuleAccessRequest;
import com.ticketing.system.admin.infrastructure.TicketTypeRepository;
import com.ticketing.system.admin.infrastructure.UserModuleAccessRepository;
import com.ticketing.system.auth.application.AuditLoggingService;
import com.ticketing.system.auth.domain.RoleCode;
import com.ticketing.system.auth.domain.UserEntity;
import com.ticketing.system.auth.infrastructure.RoleRepository;
import com.ticketing.system.auth.infrastructure.UserRepository;
import com.ticketing.system.exception.BusinessException;
import com.ticketing.system.exception.ErrorCode;
import com.ticketing.system.exception.ResourceNotFoundException;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService {

    private static final String SUPER_ADMIN_EMAIL = "customer.acme@ticketing.local";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserModuleAccessRepository accessRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLoggingService auditLoggingService;

    public AdminService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            UserModuleAccessRepository accessRepository,
            TicketTypeRepository ticketTypeRepository,
            PasswordEncoder passwordEncoder,
            AuditLoggingService auditLoggingService
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.accessRepository = accessRepository;
        this.ticketTypeRepository = ticketTypeRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditLoggingService = auditLoggingService;
    }

    @Transactional(readOnly = true)
    public List<AdminUserResponse> findUsers(UUID actorId) {
        requireAdminModule(actorId);
        return userRepository.findByDeletedAtIsNullOrderByCreatedAtDesc().stream()
                .map(this::toUserResponse)
                .toList();
    }

    @Transactional
    public AdminUserResponse createUser(CreateAdminUserRequest request, UUID actorId) {
        requireAdminModule(actorId);
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCaseAndDeletedAtIsNull(email)) {
            throw new BusinessException(ErrorCode.CONFLICT, HttpStatus.CONFLICT, "Email is already registered");
        }

        var role = roleRepository.findByCodeAndDeletedAtIsNull(request.role())
                .orElseThrow(() -> new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION, HttpStatus.BAD_REQUEST, "Role is not configured"));
        var user = new UserEntity(
                role,
                email,
                passwordEncoder.encode(request.password()),
                request.fullName().trim(),
                trimToNull(request.phoneNumber())
        );
        user.setCreatedBy(actorId);
        UserEntity saved = userRepository.save(user);
        applyDefaultAccess(saved, actorId);
        auditLoggingService.record(actorId, "ADMIN_USER_CREATED", "USER", saved.getId());
        return toUserResponse(saved);
    }

    @Transactional
    public void deleteUser(UUID userId, UUID actorId) {
        UserEntity actor = requireAdminModule(actorId);
        UserEntity user = findUser(userId);
        if (user.getId().equals(actor.getId())) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION, HttpStatus.BAD_REQUEST, "You cannot delete your own account");
        }
        if (isSuperAdmin(user)) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION, HttpStatus.BAD_REQUEST, "Super admin account cannot be deleted");
        }
        user.markDeleted(actorId);
        userRepository.save(user);
        auditLoggingService.record(actorId, "ADMIN_USER_DELETED", "USER", userId);
    }

    @Transactional
    public void resetUserPassword(UUID userId, AdminResetPasswordRequest request, UUID actorId) {
        UserEntity actor = findUser(actorId);
        if (!isSuperAdmin(actor)) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION, HttpStatus.FORBIDDEN, "Only the super admin can reset user passwords");
        }

        UserEntity user = findUser(userId);
        user.updatePassword(passwordEncoder.encode(request.newPassword()));
        user.setUpdatedBy(actorId);
        userRepository.save(user);
        auditLoggingService.record(actorId, "ADMIN_PASSWORD_RESET", "USER", userId);
    }

    @Transactional
    public AdminUserResponse updateModuleAccess(UUID userId, UpdateModuleAccessRequest request, UUID actorId) {
        UserEntity actor = findUser(actorId);
        if (!isSuperAdmin(actor)) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION, HttpStatus.FORBIDDEN, "Only the super admin can grant or remove module access");
        }

        UserEntity target = findUser(userId);
        if (isSuperAdmin(target)) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION, HttpStatus.BAD_REQUEST, "Super admin access cannot be modified");
        }
        validateModuleAllowedForRole(target.getRole().getCode(), request.moduleCode());

        UserModuleAccessEntity access = accessRepository
                .findByUserIdAndModuleCodeAndDeletedAtIsNull(userId, request.moduleCode())
                .orElseGet(() -> {
                    var created = new UserModuleAccessEntity(target, request.moduleCode(), request.enabled());
                    created.setCreatedBy(actorId);
                    return created;
                });
        access.setEnabled(request.enabled());
        access.setUpdatedBy(actorId);
        accessRepository.save(access);
        auditLoggingService.record(actorId, request.enabled() ? "MODULE_ACCESS_GRANTED" : "MODULE_ACCESS_REMOVED", "USER", userId);
        return toUserResponse(target);
    }

    @Transactional(readOnly = true)
    public List<TicketTypeResponse> findTicketTypes(UUID actorId) {
        requireAdminModule(actorId);
        return ticketTypeRepository.findByDeletedAtIsNullOrderByNameAsc().stream()
                .map(this::toTicketTypeResponse)
                .toList();
    }

    @Transactional
    public TicketTypeResponse createTicketType(CreateTicketTypeRequest request, UUID actorId) {
        requireAdminModule(actorId);
        String code = normalizeCode(request.code());
        if (ticketTypeRepository.existsByCodeIgnoreCaseAndDeletedAtIsNull(code)) {
            throw new BusinessException(ErrorCode.CONFLICT, HttpStatus.CONFLICT, "Ticket type code already exists");
        }
        var ticketType = new TicketTypeEntity(code, request.name().trim(), trimToNull(request.description()));
        ticketType.setCreatedBy(actorId);
        TicketTypeEntity saved = ticketTypeRepository.save(ticketType);
        auditLoggingService.record(actorId, "TICKET_TYPE_CREATED", "TICKET_TYPE", saved.getId());
        return toTicketTypeResponse(saved);
    }

    @Transactional
    public void deleteTicketType(UUID ticketTypeId, UUID actorId) {
        requireAdminModule(actorId);
        TicketTypeEntity ticketType = ticketTypeRepository.findByIdAndDeletedAtIsNull(ticketTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket type not found"));
        ticketType.markDeleted(actorId);
        ticketTypeRepository.save(ticketType);
        auditLoggingService.record(actorId, "TICKET_TYPE_DELETED", "TICKET_TYPE", ticketTypeId);
    }

    @Transactional(readOnly = true)
    public ModuleAccessResponse currentAccess(UUID userId) {
        UserEntity user = findUser(userId);
        return new ModuleAccessResponse(isSuperAdmin(user), moduleAccess(user));
    }

    private UserEntity requireAdminModule(UUID actorId) {
        UserEntity actor = findUser(actorId);
        if (!isSuperAdmin(actor) && !moduleAccess(actor).contains(ModuleCode.ADMIN)) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION, HttpStatus.FORBIDDEN, "Admin module access is required");
        }
        return actor;
    }

    private void applyDefaultAccess(UserEntity user, UUID actorId) {
        for (ModuleCode moduleCode : defaultAccess(user.getRole().getCode())) {
            var access = new UserModuleAccessEntity(user, moduleCode, true);
            access.setCreatedBy(actorId);
            accessRepository.save(access);
        }
    }

    private AdminUserResponse toUserResponse(UserEntity user) {
        return new AdminUserResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getPhoneNumber(),
                user.getRole().getCode(),
                user.getStatus(),
                isSuperAdmin(user),
                moduleAccess(user),
                user.getCreatedAt()
        );
    }

    private TicketTypeResponse toTicketTypeResponse(TicketTypeEntity ticketType) {
        return new TicketTypeResponse(
                ticketType.getId(),
                ticketType.getCode(),
                ticketType.getName(),
                ticketType.getDescription(),
                ticketType.isActive()
        );
    }

    private UserEntity findUser(UUID userId) {
        return userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private Set<ModuleCode> moduleAccess(UserEntity user) {
        if (isSuperAdmin(user)) {
            return EnumSet.allOf(ModuleCode.class);
        }
        List<UserModuleAccessEntity> configuredAccess = accessRepository.findByUserIdAndDeletedAtIsNull(user.getId());
        if (configuredAccess.isEmpty()) {
            return defaultAccess(user.getRole().getCode());
        }
        return configuredAccess.stream()
                .filter(UserModuleAccessEntity::isEnabled)
                .map(UserModuleAccessEntity::getModuleCode)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(ModuleCode.class)));
    }

    private Set<ModuleCode> defaultAccess(RoleCode role) {
        return switch (role) {
            case CUSTOMER -> EnumSet.of(ModuleCode.DASHBOARD, ModuleCode.TICKETS);
            case SERVICE_MANAGER -> EnumSet.of(ModuleCode.DASHBOARD, ModuleCode.TICKETS, ModuleCode.SERVICE_MANAGER);
            case ADMIN -> EnumSet.of(ModuleCode.DASHBOARD, ModuleCode.TICKETS, ModuleCode.ADMIN);
        };
    }

    private boolean isSuperAdmin(UserEntity user) {
        return user.isSuperAdmin() || SUPER_ADMIN_EMAIL.equalsIgnoreCase(user.getEmail());
    }

    private void validateModuleAllowedForRole(RoleCode role, ModuleCode moduleCode) {
        if (moduleCode == ModuleCode.ADMIN && role != RoleCode.ADMIN) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION, HttpStatus.BAD_REQUEST, "Admin module requires ADMIN role");
        }
        if (moduleCode == ModuleCode.SERVICE_MANAGER && role == RoleCode.CUSTOMER) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION, HttpStatus.BAD_REQUEST, "Service manager module requires ADMIN or SERVICE_MANAGER role");
        }
    }

    private static String normalizeCode(String value) {
        return value.trim().toUpperCase().replaceAll("[^A-Z0-9_]", "_");
    }

    private static String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
