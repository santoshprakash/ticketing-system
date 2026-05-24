package com.ticketing.system.ticket.mapper;

import com.ticketing.system.ticket.domain.TicketAttachmentEntity;
import com.ticketing.system.ticket.domain.TicketCommentEntity;
import com.ticketing.system.ticket.domain.TicketEntity;
import com.ticketing.system.ticket.domain.TicketHistoryEntity;
import com.ticketing.system.ticket.dto.TicketAttachmentResponse;
import com.ticketing.system.ticket.dto.TicketCommentResponse;
import com.ticketing.system.ticket.dto.TicketHistoryResponse;
import com.ticketing.system.ticket.dto.TicketResponse;
import org.springframework.stereotype.Component;

@Component
public class TicketMapper {

    public TicketResponse toResponse(TicketEntity ticket) {
        return new TicketResponse(
                ticket.getId(),
                ticket.getTicketNumber(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getStatus(),
                ticket.getPriority(),
                ticket.getCategory(),
                ticket.getCreatedById(),
                ticket.getAssignedToId(),
                ticket.getFirstResponseDueAt(),
                ticket.getFirstRespondedAt(),
                ticket.getResolutionDueAt(),
                ticket.getResolvedAt(),
                ticket.getClosedAt(),
                ticket.isSlaBreached(),
                ticket.getEscalationLevel(),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt()
        );
    }

    public TicketCommentResponse toCommentResponse(TicketCommentEntity comment) {
        return new TicketCommentResponse(comment.getId(), comment.getTicketId(), comment.getAuthorId(), comment.getMessage(), comment.isInternal(), comment.getCreatedAt());
    }

    public TicketAttachmentResponse toAttachmentResponse(TicketAttachmentEntity attachment) {
        return new TicketAttachmentResponse(
                attachment.getId(),
                attachment.getTicketId(),
                attachment.getUploadedBy(),
                attachment.getFileName(),
                attachment.getContentType(),
                attachment.getFileSizeBytes(),
                attachment.getStorageKey(),
                attachment.getChecksumSha256(),
                attachment.getCreatedAt()
        );
    }

    public TicketHistoryResponse toHistoryResponse(TicketHistoryEntity history) {
        return new TicketHistoryResponse(
                history.getId(),
                history.getTicketId(),
                history.getActorId(),
                history.getEventType(),
                history.getPreviousValue(),
                history.getNewValue(),
                history.getDescription(),
                history.getCreatedAt()
        );
    }
}
