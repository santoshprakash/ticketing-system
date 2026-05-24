package com.ticketing.system.admin.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ticketing.system.admin.application.AdminService;
import com.ticketing.system.admin.domain.ModuleCode;
import com.ticketing.system.admin.dto.AdminResetPasswordRequest;
import com.ticketing.system.admin.dto.AdminUserResponse;
import com.ticketing.system.admin.dto.CreateAdminUserRequest;
import com.ticketing.system.admin.dto.CreateTicketTypeRequest;
import com.ticketing.system.admin.dto.ModuleAccessResponse;
import com.ticketing.system.admin.dto.TicketTypeResponse;
import com.ticketing.system.admin.dto.UpdateModuleAccessRequest;
import com.ticketing.system.auth.domain.RoleCode;
import com.ticketing.system.auth.domain.UserStatus;
import com.ticketing.system.exception.GlobalExceptionHandler;
import com.ticketing.system.security.JwtPrincipal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

class AdminControllerIntegrationTest {

    private AdminService adminService;
    private MockMvc mockMvc;
    private UUID actorId;
    private UsernamePasswordAuthenticationToken authentication;

    @BeforeEach
    void setUp() {
        adminService = mock(AdminService.class);
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders
                .standaloneSetup(new AdminController(adminService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
        actorId = UUID.randomUUID();
        authentication = new UsernamePasswordAuthenticationToken(new JwtPrincipal(actorId.toString(), Set.of("ROLE_ADMIN")), null);
    }

    @Test
    void currentAccessShouldReturnModules() throws Exception {
        when(adminService.currentAccess(actorId)).thenReturn(new ModuleAccessResponse(true, Set.of(ModuleCode.ADMIN, ModuleCode.TICKETS)));

        mockMvc.perform(post("/api/v1/admin/access/me").principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.superAdmin").value(true))
                .andExpect(jsonPath("$.modules.length()").value(2));
    }

    @Test
    void findUsersShouldReturnAllUsers() throws Exception {
        UUID userId = UUID.randomUUID();
        when(adminService.findUsers(actorId)).thenReturn(List.of(new AdminUserResponse(
                userId,
                "admin@example.com",
                "Admin User",
                null,
                RoleCode.ADMIN,
                UserStatus.ACTIVE,
                true,
                Set.of(ModuleCode.ADMIN),
                OffsetDateTime.now()
        )));

        mockMvc.perform(post("/api/v1/admin/users/search").principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("admin@example.com"))
                .andExpect(jsonPath("$[0].superAdmin").value(true));
    }

    @Test
    void createUserShouldValidateAndCreate() throws Exception {
        UUID userId = UUID.randomUUID();
        when(adminService.createUser(any(CreateAdminUserRequest.class), eq(actorId))).thenReturn(new AdminUserResponse(
                userId,
                "manager@example.com",
                "Manager User",
                null,
                RoleCode.SERVICE_MANAGER,
                UserStatus.ACTIVE,
                false,
                Set.of(ModuleCode.SERVICE_MANAGER),
                OffsetDateTime.now()
        ));

        mockMvc.perform(post("/api/v1/admin/users")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fullName": "Manager User",
                                  "email": "manager@example.com",
                                  "password": "Str0ngPassword!",
                                  "role": "SERVICE_MANAGER"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("SERVICE_MANAGER"));
    }

    @Test
    void resetPasswordShouldReturnNoContent() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/admin/users/{userId}/reset-password", userId)
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "newPassword": "NewPassword123!"
                                }
                                """))
                .andExpect(status().isNoContent());

        verify(adminService).resetUserPassword(eq(userId), any(AdminResetPasswordRequest.class), eq(actorId));
    }

    @Test
    void updateModuleAccessShouldReturnUpdatedUser() throws Exception {
        UUID userId = UUID.randomUUID();
        when(adminService.updateModuleAccess(eq(userId), any(UpdateModuleAccessRequest.class), eq(actorId))).thenReturn(new AdminUserResponse(
                userId,
                "admin@example.com",
                "Admin User",
                null,
                RoleCode.ADMIN,
                UserStatus.ACTIVE,
                false,
                Set.of(ModuleCode.ADMIN, ModuleCode.TICKETS),
                OffsetDateTime.now()
        ));

        mockMvc.perform(post("/api/v1/admin/users/{userId}/module-access", userId)
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "moduleCode": "TICKETS",
                                  "enabled": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.moduleAccess.length()").value(2));
    }

    @Test
    void ticketTypesShouldCreateAndListCategories() throws Exception {
        UUID categoryId = UUID.randomUUID();
        when(adminService.findTicketTypes(actorId)).thenReturn(List.of(new TicketTypeResponse(categoryId, "SECURITY", "Security incident", "Security requests", true)));
        when(adminService.createTicketType(any(CreateTicketTypeRequest.class), eq(actorId)))
                .thenReturn(new TicketTypeResponse(categoryId, "SECURITY", "Security incident", "Security requests", true));

        mockMvc.perform(post("/api/v1/admin/ticket-types/search").principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("SECURITY"));

        mockMvc.perform(post("/api/v1/admin/ticket-types")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code": "SECURITY",
                                  "name": "Security incident",
                                  "description": "Security requests"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Security incident"));
    }
}
