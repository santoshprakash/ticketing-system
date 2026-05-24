package com.ticketing.system;

import static org.assertj.core.api.Assertions.assertThat;

import com.ticketing.system.admin.infrastructure.TicketTypeRepository;
import com.ticketing.system.admin.infrastructure.UserModuleAccessRepository;
import com.ticketing.system.auth.infrastructure.PasswordResetTokenRepository;
import com.ticketing.system.auth.infrastructure.RefreshTokenRepository;
import com.ticketing.system.auth.infrastructure.RoleRepository;
import com.ticketing.system.auth.infrastructure.UserRepository;
import com.ticketing.system.ticket.infrastructure.AssignmentRepository;
import com.ticketing.system.ticket.infrastructure.TicketAttachmentRepository;
import com.ticketing.system.ticket.infrastructure.TicketCommentRepository;
import com.ticketing.system.ticket.infrastructure.TicketHistoryRepository;
import com.ticketing.system.ticket.infrastructure.TicketRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration",
                "security.jwt.secret=test-secret-value-with-at-least-thirty-two-bytes"
        }
)
class HealthCheckIntegrationTest {

    @LocalServerPort
    private int port;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private RoleRepository roleRepository;

    @MockBean
    private RefreshTokenRepository refreshTokenRepository;

    @MockBean
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @MockBean
    private TicketRepository ticketRepository;

    @MockBean
    private TicketCommentRepository ticketCommentRepository;

    @MockBean
    private TicketHistoryRepository ticketHistoryRepository;

    @MockBean
    private TicketAttachmentRepository ticketAttachmentRepository;

    @MockBean
    private AssignmentRepository assignmentRepository;

    @MockBean
    private UserModuleAccessRepository userModuleAccessRepository;

    @MockBean
    private TicketTypeRepository ticketTypeRepository;

    private final TestRestTemplate restTemplate = new TestRestTemplate();

    @Test
    void healthEndpointShouldBePubliclyAccessible() {
        var response = restTemplate.getForEntity("http://localhost:" + port + "/actuator/health", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"status\":\"UP\"");
    }
}
