package com.ticketing.system.auth.controller;

import com.ticketing.system.auth.application.AuthService;
import com.ticketing.system.auth.dto.AuthResponse;
import com.ticketing.system.auth.dto.ChangePasswordRequest;
import com.ticketing.system.auth.dto.ForgotPasswordRequest;
import com.ticketing.system.auth.dto.LoginRequest;
import com.ticketing.system.auth.dto.MessageResponse;
import com.ticketing.system.auth.dto.RefreshTokenRequest;
import com.ticketing.system.auth.dto.RegisterRequest;
import com.ticketing.system.auth.dto.ResetPasswordRequest;
import com.ticketing.system.security.JwtPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Register, login, refresh token, and password recovery APIs")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a customer account", security = {})
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Login and receive JWT access and refresh tokens", security = {})
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Rotate refresh token and issue a new access token", security = {})
    public AuthResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return authService.refresh(request);
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request password reset instructions", security = {})
    public MessageResponse forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return new MessageResponse("If the account exists, password reset instructions have been sent.");
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password using a reset token", security = {})
    public MessageResponse resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return new MessageResponse("Password has been reset successfully.");
    }

    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Change password for the logged-in user")
    public MessageResponse changePassword(@Valid @RequestBody ChangePasswordRequest request, Authentication authentication) {
        authService.changePassword(actorId(authentication), request);
        return new MessageResponse("Password has been changed successfully.");
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
