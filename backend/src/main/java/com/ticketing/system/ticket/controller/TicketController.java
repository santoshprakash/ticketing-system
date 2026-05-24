package com.ticketing.system.ticket.controller;

import com.ticketing.system.security.JwtPrincipal;
import com.ticketing.system.ticket.application.TicketService;
import com.ticketing.system.ticket.domain.TicketPriority;
import com.ticketing.system.ticket.domain.TicketStatus;
import com.ticketing.system.ticket.dto.AddTicketAttachmentRequest;
import com.ticketing.system.ticket.dto.AddTicketCommentRequest;
import com.ticketing.system.ticket.dto.AssignTicketRequest;
import com.ticketing.system.ticket.dto.ChangeTicketStatusRequest;
import com.ticketing.system.ticket.dto.CreateTicketRequest;
import com.ticketing.system.ticket.dto.TicketAttachmentResponse;
import com.ticketing.system.ticket.dto.TicketCommentResponse;
import com.ticketing.system.ticket.dto.TicketFilter;
import com.ticketing.system.ticket.dto.TicketHistoryResponse;
import com.ticketing.system.ticket.dto.TicketResponse;
import com.ticketing.system.ticket.dto.UpdateTicketRequest;
import com.ticketing.system.ticket.mapper.TicketMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tickets")
@Tag(name = "Tickets", description = "Ticket lifecycle, assignment, comments, attachments, and history APIs")
@SecurityRequirement(name = "bearerAuth")
public class TicketController {

    private final TicketService ticketService;
    private final TicketMapper ticketMapper;

    public TicketController(TicketService ticketService, TicketMapper ticketMapper) {
        this.ticketService = ticketService;
        this.ticketMapper = ticketMapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
    @Operation(summary = "Create a ticket")
    public TicketResponse create(@Valid @RequestBody CreateTicketRequest request, Authentication authentication) {
        return ticketMapper.toResponse(ticketService.create(request, actorId(authentication)));
    }

    @PostMapping("/search")
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN','SERVICE_MANAGER')")
    @Operation(summary = "Search tickets with filters, pagination, and sorting")
    public Page<TicketResponse> findAll(
            @Parameter(example = "IN_PROGRESS")
            @RequestParam(required = false) TicketStatus status,
            @Parameter(example = "CRITICAL")
            @RequestParam(required = false) TicketPriority priority,
            @Parameter(example = "NETWORK")
            @RequestParam(required = false) String category,
            @Parameter(example = "10000000-0000-0000-0000-000000000031")
            @RequestParam(required = false) UUID createdBy,
            @Parameter(example = "10000000-0000-0000-0000-000000000021")
            @RequestParam(required = false) UUID assignedTo,
            Pageable pageable
    ) {
        return ticketService.findAll(new TicketFilter(status, priority, category, createdBy, assignedTo), pageable)
                .map(ticketMapper::toResponse);
    }

    @PostMapping("/{ticketId}")
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN','SERVICE_MANAGER')")
    @Operation(summary = "Fetch ticket details")
    public TicketResponse getById(@Parameter(example = "20000000-0000-0000-0000-000000000003") @PathVariable UUID ticketId) {
        return ticketMapper.toResponse(ticketService.getById(ticketId));
    }

    @PutMapping("/{ticketId}")
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
    @Operation(summary = "Update ticket summary fields")
    public TicketResponse update(@Parameter(example = "20000000-0000-0000-0000-000000000003") @PathVariable UUID ticketId, @Valid @RequestBody UpdateTicketRequest request, Authentication authentication) {
        return ticketMapper.toResponse(ticketService.update(ticketId, request, actorId(authentication)));
    }

    @PatchMapping("/{ticketId}/assignment")
    @PreAuthorize("hasAnyRole('ADMIN','SERVICE_MANAGER')")
    @Operation(summary = "Assign or reassign a ticket")
    public TicketResponse assign(@Parameter(example = "20000000-0000-0000-0000-000000000003") @PathVariable UUID ticketId, @Valid @RequestBody AssignTicketRequest request, Authentication authentication) {
        return ticketMapper.toResponse(ticketService.assign(ticketId, request, actorId(authentication)));
    }

    @PatchMapping("/{ticketId}/status")
    @PreAuthorize("hasAnyRole('ADMIN','SERVICE_MANAGER')")
    @Operation(summary = "Change ticket status")
    public TicketResponse changeStatus(@Parameter(example = "20000000-0000-0000-0000-000000000003") @PathVariable UUID ticketId, @Valid @RequestBody ChangeTicketStatusRequest request, Authentication authentication) {
        return ticketMapper.toResponse(ticketService.changeStatus(ticketId, request, actorId(authentication)));
    }

    @PatchMapping("/{ticketId}/resolve")
    @PreAuthorize("hasAnyRole('ADMIN','SERVICE_MANAGER')")
    @Operation(summary = "Resolve a ticket")
    public TicketResponse resolve(@Parameter(example = "20000000-0000-0000-0000-000000000003") @PathVariable UUID ticketId, Authentication authentication) {
        return ticketMapper.toResponse(ticketService.changeStatus(ticketId, new ChangeTicketStatusRequest(TicketStatus.RESOLVED, "Resolved"), actorId(authentication)));
    }

    @PatchMapping("/{ticketId}/reopen")
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN','SERVICE_MANAGER')")
    @Operation(summary = "Reopen a ticket")
    public TicketResponse reopen(@Parameter(example = "20000000-0000-0000-0000-000000000004") @PathVariable UUID ticketId, Authentication authentication) {
        return ticketMapper.toResponse(ticketService.changeStatus(ticketId, new ChangeTicketStatusRequest(TicketStatus.REOPENED, "Reopened"), actorId(authentication)));
    }

    @PostMapping("/{ticketId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN','SERVICE_MANAGER')")
    @Operation(summary = "Add a ticket comment")
    public TicketCommentResponse addComment(@Parameter(example = "20000000-0000-0000-0000-000000000003") @PathVariable UUID ticketId, @Valid @RequestBody AddTicketCommentRequest request, Authentication authentication) {
        return ticketMapper.toCommentResponse(ticketService.addComment(ticketId, request, actorId(authentication)));
    }

    @PostMapping("/{ticketId}/comments/search")
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN','SERVICE_MANAGER')")
    @Operation(summary = "List ticket comments")
    public Page<TicketCommentResponse> findComments(@Parameter(example = "20000000-0000-0000-0000-000000000003") @PathVariable UUID ticketId, Pageable pageable) {
        return ticketService.findComments(ticketId, pageable).map(ticketMapper::toCommentResponse);
    }

    @PostMapping("/{ticketId}/attachments")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN','SERVICE_MANAGER')")
    @Operation(summary = "Register attachment metadata")
    public TicketAttachmentResponse addAttachment(@Parameter(example = "20000000-0000-0000-0000-000000000003") @PathVariable UUID ticketId, @Valid @RequestBody AddTicketAttachmentRequest request, Authentication authentication) {
        return ticketMapper.toAttachmentResponse(ticketService.addAttachment(ticketId, request, actorId(authentication)));
    }

    @PostMapping("/{ticketId}/attachments/search")
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN','SERVICE_MANAGER')")
    @Operation(summary = "List ticket attachments")
    public Page<TicketAttachmentResponse> findAttachments(@Parameter(example = "20000000-0000-0000-0000-000000000003") @PathVariable UUID ticketId, Pageable pageable) {
        return ticketService.findAttachments(ticketId, pageable).map(ticketMapper::toAttachmentResponse);
    }

    @PostMapping("/{ticketId}/history/search")
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN','SERVICE_MANAGER')")
    @Operation(summary = "List ticket history events")
    public Page<TicketHistoryResponse> findHistory(@Parameter(example = "20000000-0000-0000-0000-000000000003") @PathVariable UUID ticketId, Pageable pageable) {
        return ticketService.findHistory(ticketId, pageable).map(ticketMapper::toHistoryResponse);
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
