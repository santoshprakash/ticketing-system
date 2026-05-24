package com.ticketing.system.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ticketing.system.auth.application.AuthService;
import com.ticketing.system.auth.domain.RoleCode;
import com.ticketing.system.auth.dto.AuthResponse;
import com.ticketing.system.auth.dto.AuthUserResponse;
import com.ticketing.system.auth.dto.ChangePasswordRequest;
import com.ticketing.system.auth.dto.LoginRequest;
import com.ticketing.system.auth.dto.RefreshTokenRequest;
import com.ticketing.system.auth.dto.RegisterRequest;
import com.ticketing.system.exception.GlobalExceptionHandler;
import com.ticketing.system.security.JwtPrincipal;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

class AuthControllerIntegrationTest {

    private AuthService authService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        authService = mock(AuthService.class);
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(new AuthController(authService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void registerShouldReturnCreatedAuthResponse() throws Exception {
        UUID userId = UUID.randomUUID();
        when(authService.register(any(RegisterRequest.class))).thenReturn(new AuthResponse(
                "access-token",
                "refresh-token",
                "Bearer",
                900,
                new AuthUserResponse(userId, "customer@example.com", "Customer User", RoleCode.CUSTOMER)
        ));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fullName": "Customer User",
                                  "email": "customer@example.com",
                                  "password": "Str0ngPassword!",
                                  "phoneNumber": "+10000000000"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.role").value("CUSTOMER"));
    }

    @Test
    void registerShouldValidatePasswordStrength() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fullName": "Customer User",
                                  "email": "customer@example.com",
                                  "password": "weak",
                                  "phoneNumber": "+10000000000"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void loginShouldReturnAuthResponse() throws Exception {
        UUID userId = UUID.randomUUID();
        when(authService.login(any(LoginRequest.class))).thenReturn(new AuthResponse(
                "access-token",
                "refresh-token",
                "Bearer",
                900,
                new AuthUserResponse(userId, "admin@example.com", "Admin User", RoleCode.ADMIN)
        ));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "admin@example.com",
                                  "password": "Password123!"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.user.role").value("ADMIN"));
    }

    @Test
    void loginShouldValidateEmail() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "not-email",
                                  "password": "Password123!"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void refreshShouldValidateTokenPresence() throws Exception {
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "refreshToken": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void refreshShouldReturnRotatedTokens() throws Exception {
        UUID userId = UUID.randomUUID();
        when(authService.refresh(any(RefreshTokenRequest.class))).thenReturn(new AuthResponse(
                "new-access-token",
                "new-refresh-token",
                "Bearer",
                900,
                new AuthUserResponse(userId, "customer@example.com", "Customer User", RoleCode.CUSTOMER)
        ));

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "refreshToken": "refresh-token"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.refreshToken").value("new-refresh-token"));
    }

    @Test
    void forgotPasswordShouldReturnPrivacyPreservingMessage() throws Exception {
        mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "customer@example.com"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("If the account exists, password reset instructions have been sent."));

        verify(authService).forgotPassword(any());
    }

    @Test
    void resetPasswordShouldValidateStrongPassword() throws Exception {
        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "token": "reset-token",
                                  "newPassword": "weak"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void changePasswordShouldUseAuthenticatedPrincipal() throws Exception {
        UUID userId = UUID.randomUUID();
        var authentication = new UsernamePasswordAuthenticationToken(new JwtPrincipal(userId.toString(), java.util.Set.of("ROLE_CUSTOMER")), null);

        mockMvc.perform(post("/api/v1/auth/change-password")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "currentPassword": "Password123!",
                                  "newPassword": "NewPassword123!"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password has been changed successfully."));

        verify(authService).changePassword(org.mockito.ArgumentMatchers.eq(userId), any(ChangePasswordRequest.class));
    }
}
