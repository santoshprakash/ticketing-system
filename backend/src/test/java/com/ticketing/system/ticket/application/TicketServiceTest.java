package com.ticketing.system.ticket.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ticketing.system.auth.application.AuditLoggingService;
import com.ticketing.system.auth.domain.RoleCode;
import com.ticketing.system.auth.domain.UserEntity;
import com.ticketing.system.auth.infrastructure.UserRepository;
import com.ticketing.system.exception.BusinessException;
import com.ticketing.system.exception.ResourceNotFoundException;
import com.ticketing.system.ticket.domain.AssignmentEntity;
import com.ticketing.system.ticket.domain.TicketEntity;
import com.ticketing.system.ticket.domain.TicketAttachmentEntity;
import com.ticketing.system.ticket.domain.TicketCommentEntity;
import com.ticketing.system.ticket.domain.TicketHistoryEntity;
import com.ticketing.system.ticket.domain.TicketPriority;
import com.ticketing.system.ticket.domain.TicketStatus;
import com.ticketing.system.ticket.dto.AddTicketAttachmentRequest;
import com.ticketing.system.ticket.dto.AddTicketCommentRequest;
import com.ticketing.system.ticket.dto.AssignTicketRequest;
import com.ticketing.system.ticket.dto.ChangeTicketStatusRequest;
import com.ticketing.system.ticket.dto.CreateTicketRequest;
import com.ticketing.system.ticket.dto.UpdateTicketRequest;
import com.ticketing.system.ticket.infrastructure.AssignmentRepository;
import com.ticketing.system.ticket.infrastructure.TicketAttachmentRepository;
import com.ticketing.system.ticket.infrastructure.TicketCommentRepository;
import com.ticketing.system.ticket.infrastructure.TicketHistoryRepository;
import com.ticketing.system.ticket.infrastructure.TicketRepository;
import com.ticketing.system.testsupport.TestEntityFactory;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

class TicketServiceTest {

    private TicketRepository ticketRepository;
    private TicketCommentRepository commentRepository;
    private TicketHistoryRepository historyRepository;
    private TicketAttachmentRepository attachmentRepository;
    private AssignmentRepository assignmentRepository;
    private UserRepository userRepository;
    private AuditLoggingService auditLoggingService;
    private TicketService ticketService;

    @BeforeEach
    void setUp() {
        ticketRepository = mock(TicketRepository.class);
        commentRepository = mock(TicketCommentRepository.class);
        historyRepository = mock(TicketHistoryRepository.class);
        attachmentRepository = mock(TicketAttachmentRepository.class);
        assignmentRepository = mock(AssignmentRepository.class);
        userRepository = mock(UserRepository.class);
        auditLoggingService = mock(AuditLoggingService.class);
        TicketNumberGenerator ticketNumberGenerator = mock(TicketNumberGenerator.class);
        when(ticketNumberGenerator.nextTicketNumber()).thenReturn("TCK-2026-TEST");
        ticketService = new TicketService(
                ticketRepository,
                commentRepository,
                historyRepository,
                attachmentRepository,
                assignmentRepository,
                userRepository,
                ticketNumberGenerator,
                new SlaPolicyService(),
                auditLoggingService
        );
    }

    @Test
    void createShouldPersistTicketWithSlaAndHistory() {
        UUID actorId = UUID.randomUUID();
        when(ticketRepository.save(any(TicketEntity.class))).thenAnswer(invocation -> {
            TicketEntity ticket = invocation.getArgument(0);
            TestEntityFactory.assignBaseEntity(ticket, UUID.randomUUID());
            return ticket;
        });
        when(historyRepository.save(any(TicketHistoryEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TicketEntity ticket = ticketService.create(new CreateTicketRequest(
                "Portal login issue",
                "Customer cannot access the service portal after login.",
                TicketPriority.HIGH,
                "access"
        ), actorId);

        assertThat(ticket.getTicketNumber()).isEqualTo("TCK-2026-TEST");
        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.OPEN);
        assertThat(ticket.getResolutionDueAt()).isNotNull();
        verify(historyRepository).save(any(TicketHistoryEntity.class));
        verify(auditLoggingService).record(actorId, "TICKET_CREATED", "TICKET", ticket.getId());
    }

    @Test
    void getByIdShouldThrowWhenTicketDoesNotExist() {
        UUID ticketId = UUID.randomUUID();
        when(ticketRepository.findByIdAndDeletedAtIsNull(ticketId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketService.getById(ticketId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Ticket not found");
    }

    @Test
    void updateShouldTrimNormalizeCategoryAndRecalculateSla() {
        UUID ticketId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        TicketEntity ticket = TestEntityFactory.ticket(TicketStatus.OPEN);
        when(ticketRepository.findByIdAndDeletedAtIsNull(ticketId)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(ticket)).thenReturn(ticket);

        TicketEntity updated = ticketService.update(ticketId, new UpdateTicketRequest(
                " Updated portal login issue ",
                " Customer cannot access the service portal after MFA reset. ",
                TicketPriority.CRITICAL,
                " security "
        ), actorId);

        assertThat(updated.getTitle()).isEqualTo("Updated portal login issue");
        assertThat(updated.getCategory()).isEqualTo("SECURITY");
        assertThat(updated.getPriority()).isEqualTo(TicketPriority.CRITICAL);
        assertThat(updated.getResolutionDueAt()).isNotNull();
        verify(historyRepository).save(any(TicketHistoryEntity.class));
        verify(auditLoggingService).record(actorId, "TICKET_UPDATED", "TICKET", ticketId);
    }

    @Test
    void updateShouldRejectClosedTicket() {
        UUID ticketId = UUID.randomUUID();
        TicketEntity ticket = TestEntityFactory.ticket(TicketStatus.CLOSED);
        when(ticketRepository.findByIdAndDeletedAtIsNull(ticketId)).thenReturn(Optional.of(ticket));

        assertThatThrownBy(() -> ticketService.update(ticketId, new UpdateTicketRequest(
                "Updated portal login issue",
                "Customer cannot access the service portal after MFA reset.",
                TicketPriority.HIGH,
                "ACCESS"
        ), UUID.randomUUID()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Closed tickets cannot be updated");

        verify(ticketRepository, never()).save(any(TicketEntity.class));
    }

    @Test
    void assignShouldAssignActiveServiceManagerAndReleaseExistingAssignments() {
        UUID ticketId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        TicketEntity ticket = TestEntityFactory.ticket(TicketStatus.OPEN);
        UserEntity serviceManager = TestEntityFactory.user(RoleCode.SERVICE_MANAGER);
        AssignmentEntity activeAssignment = new AssignmentEntity(ticketId, UUID.randomUUID(), actorId, com.ticketing.system.ticket.domain.AssignmentType.MANUAL);

        when(ticketRepository.findByIdAndDeletedAtIsNull(ticketId)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(serviceManager.getId())).thenReturn(Optional.of(serviceManager));
        when(assignmentRepository.findByTicketIdAndActiveTrueAndDeletedAtIsNull(ticketId)).thenReturn(java.util.List.of(activeAssignment));
        when(ticketRepository.save(ticket)).thenReturn(ticket);
        when(assignmentRepository.save(any(AssignmentEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TicketEntity assigned = ticketService.assign(ticketId, new AssignTicketRequest(serviceManager.getId()), actorId);

        assertThat(assigned.getStatus()).isEqualTo(TicketStatus.ASSIGNED);
        assertThat(assigned.getAssignedToId()).isEqualTo(serviceManager.getId());
        verify(assignmentRepository).save(any(AssignmentEntity.class));
        verify(historyRepository).save(any(TicketHistoryEntity.class));
        verify(auditLoggingService).record(actorId, "TICKET_ASSIGNED", "TICKET", ticketId);
    }

    @Test
    void assignShouldRejectNonServiceManagerAssignee() {
        UUID ticketId = UUID.randomUUID();
        TicketEntity ticket = TestEntityFactory.ticket(TicketStatus.OPEN);
        UserEntity customer = TestEntityFactory.user(RoleCode.CUSTOMER);
        when(ticketRepository.findByIdAndDeletedAtIsNull(ticketId)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(customer.getId())).thenReturn(Optional.of(customer));

        assertThatThrownBy(() -> ticketService.assign(ticketId, new AssignTicketRequest(customer.getId()), UUID.randomUUID()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Assignee must be an active service manager");
    }

    @Test
    void changeStatusShouldRejectInvalidTransition() {
        UUID ticketId = UUID.randomUUID();
        TicketEntity ticket = TestEntityFactory.ticket(TicketStatus.OPEN);
        when(ticketRepository.findByIdAndDeletedAtIsNull(ticketId)).thenReturn(Optional.of(ticket));

        assertThatThrownBy(() -> ticketService.changeStatus(ticketId, new ChangeTicketStatusRequest(TicketStatus.REOPENED, "Invalid"), UUID.randomUUID()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Invalid ticket status transition");
    }

    @Test
    void changeStatusShouldResolveTicketAndRecordAudit() {
        UUID ticketId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        TicketEntity ticket = TestEntityFactory.ticket(TicketStatus.IN_PROGRESS);
        when(ticketRepository.findByIdAndDeletedAtIsNull(ticketId)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(ticket)).thenReturn(ticket);

        TicketEntity resolved = ticketService.changeStatus(ticketId, new ChangeTicketStatusRequest(TicketStatus.RESOLVED, "Fixed"), actorId);

        assertThat(resolved.getStatus()).isEqualTo(TicketStatus.RESOLVED);
        assertThat(resolved.getResolvedAt()).isNotNull();
        verify(historyRepository).save(any(TicketHistoryEntity.class));
        verify(auditLoggingService).record(actorId, "TICKET_STATUS_CHANGED", "TICKET", ticketId);
    }

    @Test
    void addCommentShouldTrimMessageAndRecordHistory() {
        UUID ticketId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        when(ticketRepository.findByIdAndDeletedAtIsNull(ticketId)).thenReturn(Optional.of(TestEntityFactory.ticket(TicketStatus.OPEN)));
        when(commentRepository.save(any(TicketCommentEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TicketCommentEntity comment = ticketService.addComment(ticketId, new AddTicketCommentRequest(" Investigating issue ", false), actorId);

        assertThat(comment.getMessage()).isEqualTo("Investigating issue");
        assertThat(comment.getAuthorId()).isEqualTo(actorId);
        verify(historyRepository).save(any(TicketHistoryEntity.class));
        verify(auditLoggingService).record(actorId, "TICKET_COMMENT_ADDED", "TICKET", ticketId);
    }

    @Test
    void addAttachmentShouldTrimMetadataAndRecordHistory() {
        UUID ticketId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        when(ticketRepository.findByIdAndDeletedAtIsNull(ticketId)).thenReturn(Optional.of(TestEntityFactory.ticket(TicketStatus.OPEN)));
        when(attachmentRepository.save(any(TicketAttachmentEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TicketAttachmentEntity attachment = ticketService.addAttachment(ticketId, new AddTicketAttachmentRequest(
                " error.png ",
                " image/png ",
                1024,
                " tickets/error.png ",
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        ), actorId);

        assertThat(attachment.getFileName()).isEqualTo("error.png");
        assertThat(attachment.getContentType()).isEqualTo("image/png");
        assertThat(attachment.getStorageKey()).isEqualTo("tickets/error.png");
        verify(historyRepository).save(any(TicketHistoryEntity.class));
        verify(auditLoggingService).record(actorId, "TICKET_ATTACHMENT_ADDED", "TICKET", ticketId);
    }

    @Test
    void findCommentsShouldVerifyTicketExistsBeforeQueryingComments() {
        UUID ticketId = UUID.randomUUID();
        when(ticketRepository.findByIdAndDeletedAtIsNull(ticketId)).thenReturn(Optional.of(TestEntityFactory.ticket(TicketStatus.OPEN)));
        when(commentRepository.findByTicketIdAndDeletedAtIsNull(ticketId, PageRequest.of(0, 20))).thenReturn(Page.empty());

        Page<TicketCommentEntity> comments = ticketService.findComments(ticketId, PageRequest.of(0, 20));

        assertThat(comments).isEmpty();
        verify(commentRepository).findByTicketIdAndDeletedAtIsNull(ticketId, PageRequest.of(0, 20));
    }
}
