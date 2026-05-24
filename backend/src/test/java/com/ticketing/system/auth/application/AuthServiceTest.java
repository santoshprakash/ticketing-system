package com.ticketing.system.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ticketing.system.auth.domain.PasswordResetTokenEntity;
import com.ticketing.system.auth.domain.RefreshTokenEntity;
import com.ticketing.system.auth.domain.RoleCode;
import com.ticketing.system.auth.domain.RoleEntity;
import com.ticketing.system.auth.domain.UserEntity;
import com.ticketing.system.auth.domain.UserStatus;
import com.ticketing.system.auth.dto.ForgotPasswordRequest;
import com.ticketing.system.auth.dto.ChangePasswordRequest;
import com.ticketing.system.auth.dto.LoginRequest;
import com.ticketing.system.auth.dto.RefreshTokenRequest;
import com.ticketing.system.auth.dto.RegisterRequest;
import com.ticketing.system.auth.dto.ResetPasswordRequest;
import com.ticketing.system.auth.infrastructure.PasswordResetTokenRepository;
import com.ticketing.system.auth.infrastructure.RefreshTokenRepository;
import com.ticketing.system.auth.infrastructure.RoleRepository;
import com.ticketing.system.auth.infrastructure.UserRepository;
import com.ticketing.system.exception.BusinessException;
import com.ticketing.system.security.JwtProperties;
import com.ticketing.system.security.JwtService;
import java.time.OffsetDateTime;
import java.util.Optional;
import com.ticketing.system.testsupport.TestEntityFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

class AuthServiceTest {

    private static final String SECRET = "test-secret-value-with-at-least-thirty-two-bytes";

    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private RefreshTokenRepository refreshTokenRepository;
    private PasswordResetTokenRepository passwordResetTokenRepository;
    private PasswordEncoder passwordEncoder;
    private SecureTokenService secureTokenService;
    private AuditLoggingService auditLoggingService;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        roleRepository = mock(RoleRepository.class);
        refreshTokenRepository = mock(RefreshTokenRepository.class);
        passwordResetTokenRepository = mock(PasswordResetTokenRepository.class);
        passwordEncoder = new BCryptPasswordEncoder(4);
        JwtProperties jwtProperties = new JwtProperties("ticketing-test", SECRET, 15);
        secureTokenService = new SecureTokenService();
        auditLoggingService = mock(AuditLoggingService.class);

        authService = new AuthService(
                userRepository,
                roleRepository,
                refreshTokenRepository,
                passwordResetTokenRepository,
                passwordEncoder,
                new JwtService(jwtProperties),
                jwtProperties,
                secureTokenService,
                auditLoggingService
        );
    }

    @Test
    void registerShouldCreateCustomerAndReturnTokens() {
        RoleEntity customerRole = new RoleEntity(RoleCode.CUSTOMER, "Customer", "Customer");
        when(userRepository.existsByEmailIgnoreCaseAndDeletedAtIsNull("new@example.com")).thenReturn(false);
        when(roleRepository.findByCodeAndDeletedAtIsNull(RoleCode.CUSTOMER)).thenReturn(Optional.of(customerRole));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity user = invocation.getArgument(0);
            TestEntityFactory.assignBaseEntity(user, java.util.UUID.randomUUID());
            return user;
        });
        when(refreshTokenRepository.save(any(RefreshTokenEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = authService.register(new RegisterRequest(
                "New Customer",
                "New@Example.com",
                "Str0ngPassword!",
                "+10000000000"
        ));

        assertThat(response.accessToken()).isNotBlank();
        assertThat(response.refreshToken()).isNotBlank();
        assertThat(response.user().email()).isEqualTo("new@example.com");
        assertThat(response.user().role()).isEqualTo(RoleCode.CUSTOMER);
        verify(userRepository).save(any(UserEntity.class));
        verify(refreshTokenRepository).save(any(RefreshTokenEntity.class));
    }

    @Test
    void registerShouldRejectDuplicateEmail() {
        when(userRepository.existsByEmailIgnoreCaseAndDeletedAtIsNull("new@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(new RegisterRequest(
                "New Customer",
                " New@Example.com ",
                "Str0ngPassword!",
                null
        )))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Email is already registered");

        verify(userRepository, never()).save(any(UserEntity.class));
        verifyNoInteractions(refreshTokenRepository);
    }

    @Test
    void registerShouldFailWhenDefaultCustomerRoleIsMissing() {
        when(userRepository.existsByEmailIgnoreCaseAndDeletedAtIsNull("new@example.com")).thenReturn(false);
        when(roleRepository.findByCodeAndDeletedAtIsNull(RoleCode.CUSTOMER)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.register(new RegisterRequest(
                "New Customer",
                "new@example.com",
                "Str0ngPassword!",
                null
        )))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Default customer role is not configured");
    }

    @Test
    void loginShouldReturnTokensForValidCredentialsAndRecordLogin() {
        UserEntity user = TestEntityFactory.user(RoleCode.CUSTOMER, passwordEncoder.encode("CorrectPassword1!"));
        when(userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull("customer@example.com")).thenReturn(Optional.of(user));
        when(refreshTokenRepository.save(any(RefreshTokenEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = authService.login(new LoginRequest(" Customer@Example.com ", "CorrectPassword1!"));

        assertThat(response.accessToken()).isNotBlank();
        assertThat(response.user().email()).isEqualTo("customer@example.com");
        assertThat(user.getLastLoginAt()).isNotNull();
        verify(auditLoggingService).record(user.getId(), "LOGIN_SUCCESS", "USER", user.getId());
    }

    @Test
    void loginShouldRejectInvalidPassword() {
        UserEntity user = TestEntityFactory.user(RoleCode.CUSTOMER, passwordEncoder.encode("CorrectPassword1!"));
        when(userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull("customer@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(new LoginRequest("customer@example.com", "WrongPassword1!")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Invalid email or password");

        verify(auditLoggingService).record(user.getId(), "LOGIN_FAILED", "USER", user.getId());
    }

    @Test
    void loginShouldRejectUnknownEmail() {
        when(userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("missing@example.com", "Password123!")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Invalid email or password");
    }

    @Test
    void refreshShouldRejectUnknownToken() {
        when(refreshTokenRepository.findByTokenHashAndDeletedAtIsNull(secureTokenService.hashToken("missing-token")))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refresh(new RefreshTokenRequest("missing-token")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Refresh token is invalid");
    }

    @Test
    void refreshShouldRotateUsableToken() {
        UserEntity user = TestEntityFactory.user(RoleCode.ADMIN);
        RefreshTokenEntity current = new RefreshTokenEntity(
                user,
                secureTokenService.hashToken("valid-refresh-token"),
                OffsetDateTime.now().plusDays(1)
        );
        when(refreshTokenRepository.findByTokenHashAndDeletedAtIsNull(secureTokenService.hashToken("valid-refresh-token")))
                .thenReturn(Optional.of(current));
        when(refreshTokenRepository.save(any(RefreshTokenEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = authService.refresh(new RefreshTokenRequest("valid-refresh-token"));

        assertThat(response.accessToken()).isNotBlank();
        assertThat(response.refreshToken()).isNotEqualTo("valid-refresh-token");
        verify(refreshTokenRepository).save(current);
        verify(auditLoggingService).record(user.getId(), "TOKEN_REFRESHED", "USER", user.getId());
    }

    @Test
    void refreshShouldRejectExpiredToken() {
        UserEntity user = TestEntityFactory.user(RoleCode.CUSTOMER);
        RefreshTokenEntity expired = new RefreshTokenEntity(
                user,
                secureTokenService.hashToken("expired-refresh-token"),
                OffsetDateTime.now().minusMinutes(1)
        );
        when(refreshTokenRepository.findByTokenHashAndDeletedAtIsNull(secureTokenService.hashToken("expired-refresh-token")))
                .thenReturn(Optional.of(expired));

        assertThatThrownBy(() -> authService.refresh(new RefreshTokenRequest("expired-refresh-token")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Refresh token is expired or revoked");
    }

    @Test
    void forgotPasswordShouldCreateResetTokenForActiveUser() {
        UserEntity user = TestEntityFactory.user(RoleCode.CUSTOMER);
        when(userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull("customer@example.com")).thenReturn(Optional.of(user));

        authService.forgotPassword(new ForgotPasswordRequest(" Customer@Example.com "));

        verify(passwordResetTokenRepository).save(any(PasswordResetTokenEntity.class));
        verify(auditLoggingService).record(user.getId(), "PASSWORD_RESET_REQUESTED", "USER", user.getId());
    }

    @Test
    void forgotPasswordShouldNotRevealUnknownAccount() {
        when(userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull("missing@example.com")).thenReturn(Optional.empty());

        authService.forgotPassword(new ForgotPasswordRequest("missing@example.com"));

        verify(passwordResetTokenRepository, never()).save(any(PasswordResetTokenEntity.class));
    }

    @Test
    void forgotPasswordShouldIgnoreDisabledUser() {
        UserEntity user = TestEntityFactory.user(RoleCode.CUSTOMER);
        TestEntityFactory.setStatus(user, UserStatus.DISABLED);
        when(userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull("customer@example.com")).thenReturn(Optional.of(user));

        authService.forgotPassword(new ForgotPasswordRequest("customer@example.com"));

        verify(passwordResetTokenRepository, never()).save(any(PasswordResetTokenEntity.class));
    }

    @Test
    void resetPasswordShouldUpdatePasswordAndMarkTokenUsed() {
        UserEntity user = TestEntityFactory.user(RoleCode.CUSTOMER, passwordEncoder.encode("OldPassword1!"));
        PasswordResetTokenEntity resetToken = new PasswordResetTokenEntity(
                user,
                secureTokenService.hashToken("reset-token"),
                OffsetDateTime.now().plusMinutes(10)
        );
        when(passwordResetTokenRepository.findByTokenHashAndDeletedAtIsNull(secureTokenService.hashToken("reset-token")))
                .thenReturn(Optional.of(resetToken));

        authService.resetPassword(new ResetPasswordRequest("reset-token", "NewPassword123!"));

        assertThat(passwordEncoder.matches("NewPassword123!", user.getPasswordHash())).isTrue();
        assertThat(resetToken.isUsable()).isFalse();
        verify(userRepository).save(user);
        verify(passwordResetTokenRepository).save(resetToken);
        verify(auditLoggingService).record(user.getId(), "PASSWORD_RESET_COMPLETED", "USER", user.getId());
    }

    @Test
    void resetPasswordShouldRejectExpiredToken() {
        UserEntity user = TestEntityFactory.user(RoleCode.CUSTOMER);
        PasswordResetTokenEntity resetToken = new PasswordResetTokenEntity(
                user,
                secureTokenService.hashToken("expired-token"),
                OffsetDateTime.now().minusMinutes(1)
        );
        when(passwordResetTokenRepository.findByTokenHashAndDeletedAtIsNull(secureTokenService.hashToken("expired-token")))
                .thenReturn(Optional.of(resetToken));

        assertThatThrownBy(() -> authService.resetPassword(new ResetPasswordRequest("expired-token", "NewPassword123!")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Password reset token is expired or already used");
    }

    @Test
    void changePasswordShouldRequireCurrentPassword() {
        UserEntity user = TestEntityFactory.user(RoleCode.CUSTOMER, passwordEncoder.encode("CorrectPassword1!"));
        when(userRepository.findByIdAndDeletedAtIsNull(user.getId())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.changePassword(user.getId(), new ChangePasswordRequest("WrongPassword1!", "NewPassword123!")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Current password is incorrect");

        verify(auditLoggingService).record(user.getId(), "PASSWORD_CHANGE_FAILED", "USER", user.getId());
        verify(userRepository, never()).save(user);
    }

    @Test
    void changePasswordShouldUpdatePasswordForLoggedInUser() {
        UserEntity user = TestEntityFactory.user(RoleCode.CUSTOMER, passwordEncoder.encode("CorrectPassword1!"));
        when(userRepository.findByIdAndDeletedAtIsNull(user.getId())).thenReturn(Optional.of(user));

        authService.changePassword(user.getId(), new ChangePasswordRequest("CorrectPassword1!", "NewPassword123!"));

        assertThat(passwordEncoder.matches("NewPassword123!", user.getPasswordHash())).isTrue();
        verify(userRepository).save(user);
        verify(auditLoggingService).record(user.getId(), "PASSWORD_CHANGED", "USER", user.getId());
    }
}
