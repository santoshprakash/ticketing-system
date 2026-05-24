package com.ticketing.system.auth.application;

import com.ticketing.system.auth.domain.PasswordResetTokenEntity;
import com.ticketing.system.auth.domain.RefreshTokenEntity;
import com.ticketing.system.auth.domain.RoleCode;
import com.ticketing.system.auth.domain.UserEntity;
import com.ticketing.system.auth.dto.AuthResponse;
import com.ticketing.system.auth.dto.AuthUserResponse;
import com.ticketing.system.auth.dto.ChangePasswordRequest;
import com.ticketing.system.auth.dto.ForgotPasswordRequest;
import com.ticketing.system.auth.dto.LoginRequest;
import com.ticketing.system.auth.dto.RefreshTokenRequest;
import com.ticketing.system.auth.dto.RegisterRequest;
import com.ticketing.system.auth.dto.ResetPasswordRequest;
import com.ticketing.system.auth.infrastructure.PasswordResetTokenRepository;
import com.ticketing.system.auth.infrastructure.RefreshTokenRepository;
import com.ticketing.system.auth.infrastructure.RoleRepository;
import com.ticketing.system.auth.infrastructure.UserRepository;
import com.ticketing.system.exception.BusinessException;
import com.ticketing.system.exception.ErrorCode;
import com.ticketing.system.security.JwtProperties;
import com.ticketing.system.security.JwtService;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final long REFRESH_TOKEN_DAYS = 7;
    private static final long PASSWORD_RESET_MINUTES = 30;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final SecureTokenService secureTokenService;
    private final AuditLoggingService auditLoggingService;

    public AuthService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            JwtProperties jwtProperties,
            SecureTokenService secureTokenService,
            AuditLoggingService auditLoggingService
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
        this.secureTokenService = secureTokenService;
        this.auditLoggingService = auditLoggingService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmailIgnoreCaseAndDeletedAtIsNull(email)) {
            throw new BusinessException(ErrorCode.CONFLICT, HttpStatus.CONFLICT, "Email is already registered.");
        }

        var role = roleRepository.findByCodeAndDeletedAtIsNull(RoleCode.CUSTOMER)
                .orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, "Default customer role is not configured."));

        UserEntity user = new UserEntity(
                role,
                email,
                passwordEncoder.encode(request.password()),
                request.fullName().trim(),
                request.phoneNumber()
        );
        UserEntity savedUser = userRepository.save(user);
        auditLoggingService.record(savedUser.getId(), "USER_REGISTERED", "USER", savedUser.getId());
        return issueTokens(savedUser);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        UserEntity user = userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull(normalizeEmail(request.email()))
                .orElseThrow(this::badCredentials);

        if (!user.isActive() || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            auditLoggingService.record(user.getId(), "LOGIN_FAILED", "USER", user.getId());
            throw badCredentials();
        }

        user.recordLogin();
        auditLoggingService.record(user.getId(), "LOGIN_SUCCESS", "USER", user.getId());
        return issueTokens(user);
    }

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        String currentHash = secureTokenService.hashToken(request.refreshToken());
        RefreshTokenEntity currentToken = refreshTokenRepository.findByTokenHashAndDeletedAtIsNull(currentHash)
                .orElseThrow(() -> invalidToken("Refresh token is invalid."));

        if (!currentToken.isUsable() || !currentToken.getUser().isActive()) {
            throw invalidToken("Refresh token is expired or revoked.");
        }

        String replacementToken = secureTokenService.generateToken();
        String replacementHash = secureTokenService.hashToken(replacementToken);
        currentToken.revoke(replacementHash);
        refreshTokenRepository.save(currentToken);

        UserEntity user = currentToken.getUser();
        RefreshTokenEntity replacement = new RefreshTokenEntity(
                user,
                replacementHash,
                OffsetDateTime.now().plusDays(REFRESH_TOKEN_DAYS)
        );
        refreshTokenRepository.save(replacement);
        auditLoggingService.record(user.getId(), "TOKEN_REFRESHED", "USER", user.getId());

        return authResponse(user, replacementToken);
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull(normalizeEmail(request.email()))
                .filter(UserEntity::isActive)
                .ifPresent(user -> {
                    String token = secureTokenService.generateToken();
                    PasswordResetTokenEntity resetToken = new PasswordResetTokenEntity(
                            user,
                            secureTokenService.hashToken(token),
                            OffsetDateTime.now().plusMinutes(PASSWORD_RESET_MINUTES)
                    );
                    passwordResetTokenRepository.save(resetToken);
                    auditLoggingService.record(user.getId(), "PASSWORD_RESET_REQUESTED", "USER", user.getId());
                    // Notification/email delivery will consume the raw token in the notification module.
                });
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetTokenEntity token = passwordResetTokenRepository
                .findByTokenHashAndDeletedAtIsNull(secureTokenService.hashToken(request.token()))
                .orElseThrow(() -> invalidToken("Password reset token is invalid."));

        if (!token.isUsable() || !token.getUser().isActive()) {
            throw invalidToken("Password reset token is expired or already used.");
        }

        UserEntity user = token.getUser();
        user.updatePassword(passwordEncoder.encode(request.newPassword()));
        token.markUsed();
        userRepository.save(user);
        passwordResetTokenRepository.save(token);
        auditLoggingService.record(user.getId(), "PASSWORD_RESET_COMPLETED", "USER", user.getId());
    }

    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        UserEntity user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND, "User not found."));

        if (!user.isActive() || !passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            auditLoggingService.record(user.getId(), "PASSWORD_CHANGE_FAILED", "USER", user.getId());
            throw new BusinessException(ErrorCode.AUTHENTICATION_FAILED, HttpStatus.UNAUTHORIZED, "Current password is incorrect.");
        }

        user.updatePassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        auditLoggingService.record(user.getId(), "PASSWORD_CHANGED", "USER", user.getId());
    }

    private AuthResponse issueTokens(UserEntity user) {
        String refreshToken = secureTokenService.generateToken();
        RefreshTokenEntity entity = new RefreshTokenEntity(
                user,
                secureTokenService.hashToken(refreshToken),
                OffsetDateTime.now().plusDays(REFRESH_TOKEN_DAYS)
        );
        refreshTokenRepository.save(entity);
        return authResponse(user, refreshToken);
    }

    private AuthResponse authResponse(UserEntity user, String refreshToken) {
        String role = user.getRole().getCode().name();
        String accessToken = jwtService.generateAccessToken(user.getId().toString(), List.of(role));
        return new AuthResponse(
                accessToken,
                refreshToken,
                "Bearer",
                jwtProperties.accessTokenExpirationMinutes() * 60,
                new AuthUserResponse(user.getId(), user.getEmail(), user.getFullName(), user.getRole().getCode())
        );
    }

    private BusinessException badCredentials() {
        return new BusinessException(ErrorCode.AUTHENTICATION_FAILED, HttpStatus.UNAUTHORIZED, "Invalid email or password.");
    }

    private BusinessException invalidToken(String message) {
        return new BusinessException(ErrorCode.INVALID_TOKEN, HttpStatus.UNAUTHORIZED, message);
    }

    private static String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }
}
