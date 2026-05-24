package com.ticketing.system.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private static final String SECRET = "test-secret-value-with-at-least-thirty-two-bytes";

    @Test
    void generateAccessTokenShouldIncludeSubjectAndRoles() {
        JwtService jwtService = new JwtService(new JwtProperties("ticketing-test", SECRET, 15));

        String token = jwtService.generateAccessToken("user-123", List.of("ADMIN", "SERVICE_MANAGER"));

        JwtPrincipal principal = jwtService.parseToken(token);
        assertThat(principal.subject()).isEqualTo("user-123");
        assertThat(principal.roles()).containsExactlyInAnyOrder("ADMIN", "SERVICE_MANAGER");
    }

    @Test
    void constructorShouldRejectWeakSecret() {
        JwtProperties properties = new JwtProperties("ticketing-test", "short-secret", 15);

        assertThatThrownBy(() -> new JwtService(properties))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("JWT secret");
    }
}
