package com.ticketing.system.ticket.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ticketing.system.exception.GlobalExceptionHandler;
import com.ticketing.system.security.JwtPrincipal;
import com.ticketing.system.ticket.application.TicketService;
import com.ticketing.system.ticket.domain.TicketEntity;
import com.ticketing.system.ticket.domain.TicketAttachmentEntity;
import com.ticketing.system.ticket.domain.TicketCommentEntity;
import com.ticketing.system.ticket.domain.TicketHistoryEntity;
import com.ticketing.system.ticket.domain.TicketHistoryEventType;
import com.ticketing.system.ticket.domain.TicketPriority;
import com.ticketing.system.ticket.domain.TicketStatus;
import com.ticketing.system.ticket.dto.AddTicketAttachmentRequest;
import com.ticketing.system.ticket.dto.AddTicketCommentRequest;
import com.ticketing.system.ticket.dto.ChangeTicketStatusRequest;
import com.ticketing.system.ticket.dto.CreateTicketRequest;
import com.ticketing.system.ticket.dto.UpdateTicketRequest;
import com.ticketing.system.ticket.mapper.TicketMapper;
import com.ticketing.system.testsupport.TestEntityFactory;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

class TicketControllerIntegrationTest {

    private TicketService ticketService;
    private MockMvc mockMvc;
    private UsernamePasswordAuthenticationToken auth;

    @BeforeEach
    void setUp() {
        ticketService = mock(TicketService.class);
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        auth = new UsernamePasswordAuthenticationToken(new JwtPrincipal(UUID.randomUUID().toString(), Set.of("CUSTOMER")), null, Set.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new TicketController(ticketService, new TicketMapper()))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setValidator(validator)
                .build();
    }

    @Test
    void createShouldReturnCreatedTicket() throws Exception {
        TicketEntity ticket = new TicketEntity("TCK-2026-TEST", "Portal login issue", "Customer cannot access the service portal after login.", TicketPriority.HIGH, "ACCESS", UUID.randomUUID());
        assignId(ticket);
        when(ticketService.create(any(CreateTicketRequest.class), any(UUID.class))).thenReturn(ticket);

        mockMvc.perform(post("/api/v1/tickets")
                        .with(authentication(auth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Portal login issue",
                                  "description": "Customer cannot access the service portal after login.",
                                  "priority": "HIGH",
                                  "category": "ACCESS"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ticketNumber").value("TCK-2026-TEST"))
                .andExpect(jsonPath("$.status").value("OPEN"));
    }

    @Test
    void createShouldValidateRequest() throws Exception {
        mockMvc.perform(post("/api/v1/tickets")
                        .with(authentication(auth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Bad",
                                  "description": "Too short",
                                  "priority": "HIGH",
                                  "category": "ACCESS"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void searchShouldUsePostMethod() throws Exception {
        when(ticketService.findAll(any(), any(Pageable.class))).thenReturn(Page.empty(PageRequest.of(0, 20)));

        mockMvc.perform(post("/api/v1/tickets/search")
                        .with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void searchShouldRejectGetMethod() throws Exception {
        mockMvc.perform(get("/api/v1/tickets/search")
                        .with(authentication(auth)))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void getByIdShouldReturnTicketDetails() throws Exception {
        UUID ticketId = UUID.randomUUID();
        TicketEntity ticket = new TicketEntity("TCK-2026-DETAIL", "Portal login issue", "Customer cannot access the service portal after login.", TicketPriority.HIGH, "ACCESS", UUID.randomUUID());
        assignId(ticket);
        when(ticketService.getById(ticketId)).thenReturn(ticket);

        mockMvc.perform(post("/api/v1/tickets/{ticketId}", ticketId)
                        .with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ticketNumber").value("TCK-2026-DETAIL"));
    }

    @Test
    void updateShouldValidateRequest() throws Exception {
        UUID ticketId = UUID.randomUUID();

        mockMvc.perform(put("/api/v1/tickets/{ticketId}", ticketId)
                        .with(authentication(auth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Bad",
                                  "description": "Too short",
                                  "priority": null,
                                  "category": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void updateShouldReturnUpdatedTicket() throws Exception {
        UUID ticketId = UUID.randomUUID();
        TicketEntity ticket = TestEntityFactory.ticket(TicketStatus.OPEN);
        ticket.updateDetails("Updated portal login issue", "Customer cannot access the portal after MFA reset.", TicketPriority.CRITICAL, "SECURITY");
        when(ticketService.update(any(UUID.class), any(UpdateTicketRequest.class), any(UUID.class))).thenReturn(ticket);

        mockMvc.perform(put("/api/v1/tickets/{ticketId}", ticketId)
                        .with(authentication(auth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Updated portal login issue",
                                  "description": "Customer cannot access the portal after MFA reset.",
                                  "priority": "CRITICAL",
                                  "category": "SECURITY"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.priority").value("CRITICAL"))
                .andExpect(jsonPath("$.category").value("SECURITY"));
    }

    @Test
    void assignShouldValidateAssigneeId() throws Exception {
        UUID ticketId = UUID.randomUUID();

        mockMvc.perform(patch("/api/v1/tickets/{ticketId}/assignment", ticketId)
                        .with(authentication(auth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void changeStatusShouldValidateStatus() throws Exception {
        UUID ticketId = UUID.randomUUID();

        mockMvc.perform(patch("/api/v1/tickets/{ticketId}/status", ticketId)
                        .with(authentication(auth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "reason": "Missing status"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void addCommentShouldValidateMessage() throws Exception {
        UUID ticketId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/tickets/{ticketId}/comments", ticketId)
                        .with(authentication(auth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "message": "",
                                  "internal": false
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void addAttachmentShouldValidateChecksumAndSize() throws Exception {
        UUID ticketId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/tickets/{ticketId}/attachments", ticketId)
                        .with(authentication(auth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fileName": "error.png",
                                  "contentType": "image/png",
                                  "fileSizeBytes": 0,
                                  "storageKey": "tickets/error.png",
                                  "checksumSha256": "bad"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void resolveShouldChangeStatusToResolved() throws Exception {
        UUID ticketId = UUID.randomUUID();
        TicketEntity ticket = TestEntityFactory.ticket(TicketStatus.IN_PROGRESS);
        ticket.changeStatus(TicketStatus.RESOLVED);
        when(ticketService.changeStatus(any(UUID.class), any(ChangeTicketStatusRequest.class), any(UUID.class))).thenReturn(ticket);

        mockMvc.perform(patch("/api/v1/tickets/{ticketId}/resolve", ticketId)
                        .with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESOLVED"));
    }

    @Test
    void reopenShouldChangeStatusToReopened() throws Exception {
        UUID ticketId = UUID.randomUUID();
        TicketEntity ticket = TestEntityFactory.ticket(TicketStatus.RESOLVED);
        ticket.changeStatus(TicketStatus.REOPENED);
        when(ticketService.changeStatus(any(UUID.class), any(ChangeTicketStatusRequest.class), any(UUID.class))).thenReturn(ticket);

        mockMvc.perform(patch("/api/v1/tickets/{ticketId}/reopen", ticketId)
                        .with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REOPENED"));
    }

    @Test
    void addCommentShouldReturnCreatedComment() throws Exception {
        UUID ticketId = UUID.randomUUID();
        TicketCommentEntity comment = new TicketCommentEntity(ticketId, UUID.randomUUID(), "Investigating", false);
        TestEntityFactory.assignBaseEntity(comment, UUID.randomUUID());
        when(ticketService.addComment(any(UUID.class), any(AddTicketCommentRequest.class), any(UUID.class))).thenReturn(comment);

        mockMvc.perform(post("/api/v1/tickets/{ticketId}/comments", ticketId)
                        .with(authentication(auth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "message": "Investigating",
                                  "internal": false
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Investigating"));
    }

    @Test
    void findCommentsShouldReturnPagedComments() throws Exception {
        UUID ticketId = UUID.randomUUID();
        TicketCommentEntity comment = new TicketCommentEntity(ticketId, UUID.randomUUID(), "Investigating", false);
        TestEntityFactory.assignBaseEntity(comment, UUID.randomUUID());
        when(ticketService.findComments(any(UUID.class), any(Pageable.class))).thenReturn(new PageImpl<>(java.util.List.of(comment), PageRequest.of(0, 20), 1));

        mockMvc.perform(post("/api/v1/tickets/{ticketId}/comments/search", ticketId)
                        .with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].message").value("Investigating"));
    }

    @Test
    void addAttachmentShouldReturnCreatedAttachment() throws Exception {
        UUID ticketId = UUID.randomUUID();
        TicketAttachmentEntity attachment = new TicketAttachmentEntity(
                ticketId,
                UUID.randomUUID(),
                "error.png",
                "image/png",
                1024,
                "tickets/error.png",
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        );
        TestEntityFactory.assignBaseEntity(attachment, UUID.randomUUID());
        when(ticketService.addAttachment(any(UUID.class), any(AddTicketAttachmentRequest.class), any(UUID.class))).thenReturn(attachment);

        mockMvc.perform(post("/api/v1/tickets/{ticketId}/attachments", ticketId)
                        .with(authentication(auth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fileName": "error.png",
                                  "contentType": "image/png",
                                  "fileSizeBytes": 1024,
                                  "storageKey": "tickets/error.png",
                                  "checksumSha256": "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fileName").value("error.png"));
    }

    @Test
    void findAttachmentsShouldReturnPagedAttachments() throws Exception {
        UUID ticketId = UUID.randomUUID();
        TicketAttachmentEntity attachment = new TicketAttachmentEntity(
                ticketId,
                UUID.randomUUID(),
                "error.png",
                "image/png",
                1024,
                "tickets/error.png",
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        );
        TestEntityFactory.assignBaseEntity(attachment, UUID.randomUUID());
        when(ticketService.findAttachments(any(UUID.class), any(Pageable.class))).thenReturn(new PageImpl<>(java.util.List.of(attachment), PageRequest.of(0, 20), 1));

        mockMvc.perform(post("/api/v1/tickets/{ticketId}/attachments/search", ticketId)
                        .with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].fileName").value("error.png"));
    }

    @Test
    void findHistoryShouldReturnPagedHistory() throws Exception {
        UUID ticketId = UUID.randomUUID();
        TicketHistoryEntity history = new TicketHistoryEntity(ticketId, UUID.randomUUID(), TicketHistoryEventType.CREATED, null, "{\"status\":\"OPEN\"}", "Ticket created");
        when(ticketService.findHistory(any(UUID.class), any(Pageable.class))).thenReturn(new PageImpl<>(java.util.List.of(history), PageRequest.of(0, 20), 1));

        mockMvc.perform(post("/api/v1/tickets/{ticketId}/history/search", ticketId)
                        .with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].eventType").value("CREATED"));
    }

    private static void assignId(TicketEntity ticket) {
        try {
            Method setter = ticket.getClass().getSuperclass().getDeclaredMethod("setId", UUID.class);
            setter.setAccessible(true);
            setter.invoke(ticket, UUID.randomUUID());
            Method onCreate = ticket.getClass().getSuperclass().getDeclaredMethod("onCreate");
            onCreate.setAccessible(true);
            onCreate.invoke(ticket);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
