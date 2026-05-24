package com.ticketing.system.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.ticketing.system.auth.domain.RoleCode;
import com.ticketing.system.auth.domain.UserEntity;
import com.ticketing.system.auth.infrastructure.UserRepository;
import com.ticketing.system.testsupport.TestEntityFactory;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

class DatabaseUserDetailsServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final DatabaseUserDetailsService service = new DatabaseUserDetailsService(userRepository);

    @Test
    void loadUserByUsernameShouldReturnSpringSecurityUser() {
        UserEntity user = TestEntityFactory.user(RoleCode.ADMIN);
        when(userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull("admin@example.com")).thenReturn(Optional.of(user));

        var userDetails = service.loadUserByUsername("admin@example.com");

        assertThat(userDetails.getUsername()).isEqualTo("admin@example.com");
        assertThat(userDetails.getPassword()).isEqualTo(user.getPasswordHash());
        assertThat(userDetails.isEnabled()).isTrue();
        assertThat(userDetails.getAuthorities()).extracting("authority").containsExactly("ROLE_ADMIN");
    }

    @Test
    void loadUserByUsernameShouldThrowWhenUserIsMissing() {
        when(userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("missing@example.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found");
    }
}
