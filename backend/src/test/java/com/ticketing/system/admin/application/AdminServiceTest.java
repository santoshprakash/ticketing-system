package com.ticketing.system.admin.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ticketing.system.admin.domain.ModuleCode;
import com.ticketing.system.admin.domain.TicketTypeEntity;
import com.ticketing.system.admin.domain.UserModuleAccessEntity;
import com.ticketing.system.admin.dto.AdminResetPasswordRequest;
import com.ticketing.system.admin.dto.CreateAdminUserRequest;
import com.ticketing.system.admin.dto.CreateTicketTypeRequest;
import com.ticketing.system.admin.dto.UpdateModuleAccessRequest;
import com.ticketing.system.admin.infrastructure.TicketTypeRepository;
import com.ticketing.system.admin.infrastructure.UserModuleAccessRepository;
import com.ticketing.system.auth.application.AuditLoggingService;
import com.ticketing.system.auth.domain.RoleCode;
import com.ticketing.system.auth.domain.RoleEntity;
import com.ticketing.system.auth.domain.UserEntity;
import com.ticketing.system.auth.infrastructure.RoleRepository;
import com.ticketing.system.auth.infrastructure.UserRepository;
import com.ticketing.system.exception.BusinessException;
import com.ticketing.system.testsupport.TestEntityFactory;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

class AdminServiceTest {

    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private UserModuleAccessRepository accessRepository;
    private TicketTypeRepository ticketTypeRepository;
    private PasswordEncoder passwordEncoder;
    private AuditLoggingService auditLoggingService;
    private AdminService adminService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        roleRepository = mock(RoleRepository.class);
        accessRepository = mock(UserModuleAccessRepository.class);
        ticketTypeRepository = mock(TicketTypeRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        auditLoggingService = mock(AuditLoggingService.class);
        adminService = new AdminService(
                userRepository,
                roleRepository,
                accessRepository,
                ticketTypeRepository,
                passwordEncoder,
                auditLoggingService
        );
    }

    @Test
    void currentAccessShouldReturnAllModulesForSuperAdmin() {
        UserEntity superAdmin = TestEntityFactory.user(RoleCode.ADMIN);
        superAdmin.markSuperAdmin();
        when(userRepository.findByIdAndDeletedAtIsNull(superAdmin.getId())).thenReturn(Optional.of(superAdmin));

        var response = adminService.currentAccess(superAdmin.getId());

        assertThat(response.superAdmin()).isTrue();
        assertThat(response.modules()).containsExactlyInAnyOrder(ModuleCode.values());
    }

    @Test
    void findUsersShouldRequireAdminModuleAndMapUsers() {
        UserEntity actor = TestEntityFactory.user(RoleCode.ADMIN);
        UserEntity customer = TestEntityFactory.user(RoleCode.CUSTOMER);
        when(userRepository.findByIdAndDeletedAtIsNull(actor.getId())).thenReturn(Optional.of(actor));
        when(accessRepository.findByUserIdAndDeletedAtIsNull(actor.getId()))
                .thenReturn(List.of(new UserModuleAccessEntity(actor, ModuleCode.ADMIN, true)));
        when(userRepository.findByDeletedAtIsNullOrderByCreatedAtDesc()).thenReturn(List.of(customer));
        when(accessRepository.findByUserIdAndDeletedAtIsNull(customer.getId())).thenReturn(List.of());

        var users = adminService.findUsers(actor.getId());

        assertThat(users).hasSize(1);
        assertThat(users.getFirst().role()).isEqualTo(RoleCode.CUSTOMER);
        assertThat(users.getFirst().moduleAccess()).containsExactlyInAnyOrder(ModuleCode.DASHBOARD, ModuleCode.TICKETS);
    }

    @Test
    void createUserShouldRejectDuplicateEmail() {
        UserEntity actor = TestEntityFactory.user(RoleCode.ADMIN);
        when(userRepository.findByIdAndDeletedAtIsNull(actor.getId())).thenReturn(Optional.of(actor));
        when(accessRepository.findByUserIdAndDeletedAtIsNull(actor.getId()))
                .thenReturn(List.of(new UserModuleAccessEntity(actor, ModuleCode.ADMIN, true)));
        when(userRepository.existsByEmailIgnoreCaseAndDeletedAtIsNull("new.admin@example.com")).thenReturn(true);

        assertThatThrownBy(() -> adminService.createUser(createUserRequest(RoleCode.ADMIN), actor.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Email is already registered");

        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void createUserShouldPersistUserWithDefaultModuleAccess() {
        UserEntity actor = TestEntityFactory.user(RoleCode.ADMIN);
        RoleEntity role = TestEntityFactory.role(RoleCode.SERVICE_MANAGER);
        when(userRepository.findByIdAndDeletedAtIsNull(actor.getId())).thenReturn(Optional.of(actor));
        when(accessRepository.findByUserIdAndDeletedAtIsNull(actor.getId()))
                .thenReturn(List.of(new UserModuleAccessEntity(actor, ModuleCode.ADMIN, true)));
        when(userRepository.existsByEmailIgnoreCaseAndDeletedAtIsNull("new.admin@example.com")).thenReturn(false);
        when(roleRepository.findByCodeAndDeletedAtIsNull(RoleCode.SERVICE_MANAGER)).thenReturn(Optional.of(role));
        when(passwordEncoder.encode("Str0ngPassword!")).thenReturn("encoded-password");
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity user = invocation.getArgument(0);
            TestEntityFactory.assignBaseEntity(user, UUID.randomUUID());
            return user;
        });
        when(accessRepository.save(any(UserModuleAccessEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = adminService.createUser(createUserRequest(RoleCode.SERVICE_MANAGER), actor.getId());

        assertThat(response.email()).isEqualTo("new.admin@example.com");
        assertThat(response.role()).isEqualTo(RoleCode.SERVICE_MANAGER);
        assertThat(response.moduleAccess()).containsExactlyInAnyOrder(ModuleCode.DASHBOARD, ModuleCode.TICKETS, ModuleCode.SERVICE_MANAGER);
        verify(auditLoggingService).record(actor.getId(), "ADMIN_USER_CREATED", "USER", response.id());
    }

    @Test
    void updateModuleAccessShouldAllowOnlySuperAdmin() {
        UserEntity actor = TestEntityFactory.user(RoleCode.ADMIN);
        UserEntity target = TestEntityFactory.user(RoleCode.ADMIN);
        when(userRepository.findByIdAndDeletedAtIsNull(actor.getId())).thenReturn(Optional.of(actor));

        assertThatThrownBy(() -> adminService.updateModuleAccess(target.getId(), new UpdateModuleAccessRequest(ModuleCode.ADMIN, true), actor.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Only the super admin");

        verify(accessRepository, never()).save(any(UserModuleAccessEntity.class));
    }

    @Test
    void updateModuleAccessShouldRejectAdminModuleForNonAdminRole() {
        UserEntity superAdmin = TestEntityFactory.user(RoleCode.ADMIN);
        superAdmin.markSuperAdmin();
        UserEntity target = TestEntityFactory.user(RoleCode.CUSTOMER);
        when(userRepository.findByIdAndDeletedAtIsNull(superAdmin.getId())).thenReturn(Optional.of(superAdmin));
        when(userRepository.findByIdAndDeletedAtIsNull(target.getId())).thenReturn(Optional.of(target));

        assertThatThrownBy(() -> adminService.updateModuleAccess(target.getId(), new UpdateModuleAccessRequest(ModuleCode.ADMIN, true), superAdmin.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Admin module requires ADMIN role");
    }

    @Test
    void updateModuleAccessShouldGrantModuleForSuperAdminActor() {
        UserEntity superAdmin = TestEntityFactory.user(RoleCode.ADMIN);
        superAdmin.markSuperAdmin();
        UserEntity target = TestEntityFactory.user(RoleCode.ADMIN);
        when(userRepository.findByIdAndDeletedAtIsNull(superAdmin.getId())).thenReturn(Optional.of(superAdmin));
        when(userRepository.findByIdAndDeletedAtIsNull(target.getId())).thenReturn(Optional.of(target));
        when(accessRepository.findByUserIdAndModuleCodeAndDeletedAtIsNull(target.getId(), ModuleCode.SERVICE_MANAGER))
                .thenReturn(Optional.empty());
        when(accessRepository.findByUserIdAndDeletedAtIsNull(target.getId()))
                .thenReturn(List.of(new UserModuleAccessEntity(target, ModuleCode.ADMIN, true)));

        var response = adminService.updateModuleAccess(target.getId(), new UpdateModuleAccessRequest(ModuleCode.SERVICE_MANAGER, true), superAdmin.getId());

        assertThat(response.id()).isEqualTo(target.getId());
        verify(accessRepository).save(any(UserModuleAccessEntity.class));
        verify(auditLoggingService).record(superAdmin.getId(), "MODULE_ACCESS_GRANTED", "USER", target.getId());
    }

    @Test
    void deleteUserShouldRejectSelfDeleteAndSuperAdminDelete() {
        UserEntity actor = TestEntityFactory.user(RoleCode.ADMIN);
        actor.markSuperAdmin();
        UserEntity target = TestEntityFactory.user(RoleCode.ADMIN);
        target.markSuperAdmin();
        when(userRepository.findByIdAndDeletedAtIsNull(actor.getId())).thenReturn(Optional.of(actor));
        when(userRepository.findByIdAndDeletedAtIsNull(target.getId())).thenReturn(Optional.of(target));

        assertThatThrownBy(() -> adminService.deleteUser(actor.getId(), actor.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("own account");
        assertThatThrownBy(() -> adminService.deleteUser(target.getId(), actor.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Super admin account cannot be deleted");
    }

    @Test
    void deleteUserShouldSoftDeleteUser() {
        UserEntity actor = TestEntityFactory.user(RoleCode.ADMIN);
        actor.markSuperAdmin();
        UserEntity target = TestEntityFactory.user(RoleCode.CUSTOMER);
        when(userRepository.findByIdAndDeletedAtIsNull(actor.getId())).thenReturn(Optional.of(actor));
        when(userRepository.findByIdAndDeletedAtIsNull(target.getId())).thenReturn(Optional.of(target));

        adminService.deleteUser(target.getId(), actor.getId());

        assertThat(target.getDeletedAt()).isNotNull();
        verify(userRepository).save(target);
        verify(auditLoggingService).record(actor.getId(), "ADMIN_USER_DELETED", "USER", target.getId());
    }

    @Test
    void resetUserPasswordShouldRequireSuperAdmin() {
        UserEntity actor = TestEntityFactory.user(RoleCode.ADMIN);
        UserEntity target = TestEntityFactory.user(RoleCode.CUSTOMER, "old-password");
        when(userRepository.findByIdAndDeletedAtIsNull(actor.getId())).thenReturn(Optional.of(actor));

        assertThatThrownBy(() -> adminService.resetUserPassword(target.getId(), new AdminResetPasswordRequest("NewPassword123!"), actor.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Only the super admin");

        verify(userRepository, never()).save(target);
    }

    @Test
    void resetUserPasswordShouldAllowSuperAdminToResetAnyUserIncludingSelf() {
        UserEntity superAdmin = TestEntityFactory.user(RoleCode.ADMIN, "old-password");
        superAdmin.markSuperAdmin();
        when(userRepository.findByIdAndDeletedAtIsNull(superAdmin.getId())).thenReturn(Optional.of(superAdmin));
        when(passwordEncoder.encode("NewPassword123!")).thenReturn("encoded-new-password");

        adminService.resetUserPassword(superAdmin.getId(), new AdminResetPasswordRequest("NewPassword123!"), superAdmin.getId());

        assertThat(superAdmin.getPasswordHash()).isEqualTo("encoded-new-password");
        verify(userRepository).save(superAdmin);
        verify(auditLoggingService).record(superAdmin.getId(), "ADMIN_PASSWORD_RESET", "USER", superAdmin.getId());
    }

    @Test
    void createTicketTypeShouldRejectDuplicatesAndCreateNormalizedCode() {
        UserEntity actor = TestEntityFactory.user(RoleCode.ADMIN);
        actor.markSuperAdmin();
        when(userRepository.findByIdAndDeletedAtIsNull(actor.getId())).thenReturn(Optional.of(actor));
        when(ticketTypeRepository.existsByCodeIgnoreCaseAndDeletedAtIsNull("SECURITY_INCIDENT")).thenReturn(false);
        when(ticketTypeRepository.save(any(TicketTypeEntity.class))).thenAnswer(invocation -> {
            TicketTypeEntity ticketType = invocation.getArgument(0);
            TestEntityFactory.assignBaseEntity(ticketType, UUID.randomUUID());
            return ticketType;
        });

        var response = adminService.createTicketType(new CreateTicketTypeRequest("security incident", "Security incident", "Urgent security request"), actor.getId());

        assertThat(response.code()).isEqualTo("SECURITY_INCIDENT");
        verify(auditLoggingService).record(actor.getId(), "TICKET_TYPE_CREATED", "TICKET_TYPE", response.id());

        when(ticketTypeRepository.existsByCodeIgnoreCaseAndDeletedAtIsNull("SECURITY_INCIDENT")).thenReturn(true);
        assertThatThrownBy(() -> adminService.createTicketType(new CreateTicketTypeRequest("security incident", "Security incident", null), actor.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Ticket type code already exists");
    }

    @Test
    void deleteTicketTypeShouldSoftDelete() {
        UserEntity actor = TestEntityFactory.user(RoleCode.ADMIN);
        actor.markSuperAdmin();
        TicketTypeEntity ticketType = new TicketTypeEntity("ACCESS", "Access", "Access requests");
        TestEntityFactory.assignBaseEntity(ticketType, UUID.randomUUID());
        when(userRepository.findByIdAndDeletedAtIsNull(actor.getId())).thenReturn(Optional.of(actor));
        when(ticketTypeRepository.findByIdAndDeletedAtIsNull(ticketType.getId())).thenReturn(Optional.of(ticketType));

        adminService.deleteTicketType(ticketType.getId(), actor.getId());

        assertThat(ticketType.getDeletedAt()).isNotNull();
        verify(ticketTypeRepository).save(ticketType);
        verify(auditLoggingService).record(actor.getId(), "TICKET_TYPE_DELETED", "TICKET_TYPE", ticketType.getId());
    }

    private static CreateAdminUserRequest createUserRequest(RoleCode role) {
        return new CreateAdminUserRequest(
                "New Admin",
                " New.Admin@Example.com ",
                "Str0ngPassword!",
                "+10000000000",
                role
        );
    }
}
