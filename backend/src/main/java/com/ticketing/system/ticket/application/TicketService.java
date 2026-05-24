package com.ticketing.system.ticket.application;

import com.ticketing.system.auth.application.AuditLoggingService;
import com.ticketing.system.auth.domain.RoleCode;
import com.ticketing.system.auth.domain.UserEntity;
import com.ticketing.system.auth.infrastructure.UserRepository;
import com.ticketing.system.exception.BusinessException;
import com.ticketing.system.exception.ErrorCode;
import com.ticketing.system.exception.ResourceNotFoundException;
import com.ticketing.system.ticket.domain.AssignmentEntity;
import com.ticketing.system.ticket.domain.AssignmentType;
import com.ticketing.system.ticket.domain.TicketAttachmentEntity;
import com.ticketing.system.ticket.domain.TicketCommentEntity;
import com.ticketing.system.ticket.domain.TicketEntity;
import com.ticketing.system.ticket.domain.TicketHistoryEntity;
import com.ticketing.system.ticket.domain.TicketHistoryEventType;
import com.ticketing.system.ticket.domain.TicketStatus;
import com.ticketing.system.ticket.dto.AddTicketAttachmentRequest;
import com.ticketing.system.ticket.dto.AddTicketCommentRequest;
import com.ticketing.system.ticket.dto.AssignTicketRequest;
import com.ticketing.system.ticket.dto.ChangeTicketStatusRequest;
import com.ticketing.system.ticket.dto.CreateTicketRequest;
import com.ticketing.system.ticket.dto.TicketFilter;
import com.ticketing.system.ticket.dto.UpdateTicketRequest;
import com.ticketing.system.ticket.infrastructure.AssignmentRepository;
import com.ticketing.system.ticket.infrastructure.TicketAttachmentRepository;
import com.ticketing.system.ticket.infrastructure.TicketCommentRepository;
import com.ticketing.system.ticket.infrastructure.TicketHistoryRepository;
import com.ticketing.system.ticket.infrastructure.TicketRepository;
import com.ticketing.system.ticket.infrastructure.TicketSpecifications;
import java.util.EnumMap;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TicketService {

    private static final EnumMap<TicketStatus, Set<TicketStatus>> ALLOWED_TRANSITIONS = new EnumMap<>(TicketStatus.class);

    static {
        ALLOWED_TRANSITIONS.put(TicketStatus.OPEN, Set.of(TicketStatus.ASSIGNED, TicketStatus.IN_PROGRESS, TicketStatus.CLOSED));
        ALLOWED_TRANSITIONS.put(TicketStatus.ASSIGNED, Set.of(TicketStatus.IN_PROGRESS, TicketStatus.RESOLVED, TicketStatus.CLOSED));
        ALLOWED_TRANSITIONS.put(TicketStatus.IN_PROGRESS, Set.of(TicketStatus.RESOLVED, TicketStatus.CLOSED));
        ALLOWED_TRANSITIONS.put(TicketStatus.RESOLVED, Set.of(TicketStatus.CLOSED, TicketStatus.REOPENED));
        ALLOWED_TRANSITIONS.put(TicketStatus.CLOSED, Set.of(TicketStatus.REOPENED));
        ALLOWED_TRANSITIONS.put(TicketStatus.REOPENED, Set.of(TicketStatus.ASSIGNED, TicketStatus.IN_PROGRESS, TicketStatus.RESOLVED, TicketStatus.CLOSED));
    }

    private final TicketRepository ticketRepository;
    private final TicketCommentRepository commentRepository;
    private final TicketHistoryRepository historyRepository;
    private final TicketAttachmentRepository attachmentRepository;
    private final AssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final TicketNumberGenerator ticketNumberGenerator;
    private final SlaPolicyService slaPolicyService;
    private final AuditLoggingService auditLoggingService;

    public TicketService(
            TicketRepository ticketRepository,
            TicketCommentRepository commentRepository,
            TicketHistoryRepository historyRepository,
            TicketAttachmentRepository attachmentRepository,
            AssignmentRepository assignmentRepository,
            UserRepository userRepository,
            TicketNumberGenerator ticketNumberGenerator,
            SlaPolicyService slaPolicyService,
            AuditLoggingService auditLoggingService
    ) {
        this.ticketRepository = ticketRepository;
        this.commentRepository = commentRepository;
        this.historyRepository = historyRepository;
        this.attachmentRepository = attachmentRepository;
        this.assignmentRepository = assignmentRepository;
        this.userRepository = userRepository;
        this.ticketNumberGenerator = ticketNumberGenerator;
        this.slaPolicyService = slaPolicyService;
        this.auditLoggingService = auditLoggingService;
    }

    @Transactional
    public TicketEntity create(CreateTicketRequest request, UUID actorId) {
        TicketEntity ticket = new TicketEntity(
                ticketNumberGenerator.nextTicketNumber(),
                request.title().trim(),
                request.description().trim(),
                request.priority(),
                request.category().trim().toUpperCase(),
                actorId
        );
        var sla = slaPolicyService.calculate(request.priority());
        ticket.setSlaDueTimes(sla.firstResponseDueAt(), sla.resolutionDueAt());
        TicketEntity saved = ticketRepository.save(ticket);
        recordHistory(saved.getId(), actorId, TicketHistoryEventType.CREATED, null, "{\"status\":\"OPEN\"}", "Ticket created");
        auditLoggingService.record(actorId, "TICKET_CREATED", "TICKET", saved.getId());
        return saved;
    }

    @Transactional(readOnly = true)
    public Page<TicketEntity> findAll(TicketFilter filter, Pageable pageable) {
        return ticketRepository.findAll(TicketSpecifications.from(filter), pageable);
    }

    @Transactional(readOnly = true)
    public TicketEntity getById(UUID ticketId) {
        return findTicket(ticketId);
    }

    @Transactional
    public TicketEntity update(UUID ticketId, UpdateTicketRequest request, UUID actorId) {
        TicketEntity ticket = findTicket(ticketId);
        ensureMutable(ticket);
        ticket.updateDetails(request.title().trim(), request.description().trim(), request.priority(), request.category().trim().toUpperCase());
        var sla = slaPolicyService.calculate(request.priority());
        ticket.setSlaDueTimes(sla.firstResponseDueAt(), sla.resolutionDueAt());
        TicketEntity saved = ticketRepository.save(ticket);
        recordHistory(ticketId, actorId, TicketHistoryEventType.STATUS_CHANGED, null, "{\"action\":\"UPDATED\"}", "Ticket details updated");
        auditLoggingService.record(actorId, "TICKET_UPDATED", "TICKET", ticketId);
        return saved;
    }

    @Transactional
    public TicketEntity assign(UUID ticketId, AssignTicketRequest request, UUID actorId) {
        TicketEntity ticket = findTicket(ticketId);
        UserEntity assignee = userRepository.findById(request.assigneeId())
                .filter(user -> user.getDeletedAt() == null && user.isActive() && user.getRole().getCode() == RoleCode.SERVICE_MANAGER)
                .orElseThrow(() -> new BusinessException(ErrorCode.VALIDATION_ERROR, HttpStatus.BAD_REQUEST, "Assignee must be an active service manager."));

        assignmentRepository.findByTicketIdAndActiveTrueAndDeletedAtIsNull(ticketId)
                .forEach(active -> active.release("Reassigned"));
        ticket.assignTo(assignee.getId());
        AssignmentEntity assignment = new AssignmentEntity(ticketId, assignee.getId(), actorId, AssignmentType.MANUAL);
        assignmentRepository.save(assignment);
        TicketEntity saved = ticketRepository.save(ticket);
        recordHistory(ticketId, actorId, TicketHistoryEventType.ASSIGNED, null, "{\"assignedTo\":\"" + assignee.getId() + "\"}", "Ticket assigned");
        auditLoggingService.record(actorId, "TICKET_ASSIGNED", "TICKET", ticketId);
        return saved;
    }

    @Transactional
    public TicketEntity changeStatus(UUID ticketId, ChangeTicketStatusRequest request, UUID actorId) {
        TicketEntity ticket = findTicket(ticketId);
        TicketStatus current = ticket.getStatus();
        TicketStatus target = request.status();
        if (!ALLOWED_TRANSITIONS.getOrDefault(current, Set.of()).contains(target)) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION, HttpStatus.CONFLICT, "Invalid ticket status transition.");
        }
        ticket.changeStatus(target);
        TicketEntity saved = ticketRepository.save(ticket);
        TicketHistoryEventType eventType = switch (target) {
            case RESOLVED -> TicketHistoryEventType.RESOLVED;
            case CLOSED -> TicketHistoryEventType.CLOSED;
            case REOPENED -> TicketHistoryEventType.REOPENED;
            default -> TicketHistoryEventType.STATUS_CHANGED;
        };
        recordHistory(ticketId, actorId, eventType, "{\"status\":\"" + current + "\"}", "{\"status\":\"" + target + "\"}", request.reason());
        auditLoggingService.record(actorId, "TICKET_STATUS_CHANGED", "TICKET", ticketId);
        return saved;
    }

    @Transactional
    public TicketCommentEntity addComment(UUID ticketId, AddTicketCommentRequest request, UUID actorId) {
        findTicket(ticketId);
        TicketCommentEntity comment = commentRepository.save(new TicketCommentEntity(ticketId, actorId, request.message().trim(), request.internal()));
        recordHistory(ticketId, actorId, TicketHistoryEventType.COMMENTED, null, "{\"commentId\":\"" + comment.getId() + "\"}", "Ticket commented");
        auditLoggingService.record(actorId, "TICKET_COMMENT_ADDED", "TICKET", ticketId);
        return comment;
    }

    @Transactional
    public TicketAttachmentEntity addAttachment(UUID ticketId, AddTicketAttachmentRequest request, UUID actorId) {
        findTicket(ticketId);
        TicketAttachmentEntity attachment = attachmentRepository.save(new TicketAttachmentEntity(
                ticketId,
                actorId,
                request.fileName().trim(),
                request.contentType().trim(),
                request.fileSizeBytes(),
                request.storageKey().trim(),
                request.checksumSha256()
        ));
        recordHistory(ticketId, actorId, TicketHistoryEventType.ATTACHMENT_ADDED, null, "{\"attachmentId\":\"" + attachment.getId() + "\"}", "Ticket attachment added");
        auditLoggingService.record(actorId, "TICKET_ATTACHMENT_ADDED", "TICKET", ticketId);
        return attachment;
    }

    @Transactional(readOnly = true)
    public Page<TicketCommentEntity> findComments(UUID ticketId, Pageable pageable) {
        findTicket(ticketId);
        return commentRepository.findByTicketIdAndDeletedAtIsNull(ticketId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<TicketHistoryEntity> findHistory(UUID ticketId, Pageable pageable) {
        findTicket(ticketId);
        return historyRepository.findByTicketId(ticketId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<TicketAttachmentEntity> findAttachments(UUID ticketId, Pageable pageable) {
        findTicket(ticketId);
        return attachmentRepository.findByTicketIdAndDeletedAtIsNull(ticketId, pageable);
    }

    private TicketEntity findTicket(UUID ticketId) {
        return ticketRepository.findByIdAndDeletedAtIsNull(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found."));
    }

    private void ensureMutable(TicketEntity ticket) {
        if (ticket.getStatus() == TicketStatus.CLOSED) {
            throw new BusinessException(ErrorCode.CONFLICT, HttpStatus.CONFLICT, "Closed tickets cannot be updated.");
        }
    }

    private void recordHistory(UUID ticketId, UUID actorId, TicketHistoryEventType eventType, String previousValue, String newValue, String description) {
        historyRepository.save(new TicketHistoryEntity(ticketId, actorId, eventType, previousValue, newValue, description));
    }
}
