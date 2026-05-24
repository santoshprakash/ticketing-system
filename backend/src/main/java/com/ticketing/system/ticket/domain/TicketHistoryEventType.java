package com.ticketing.system.ticket.domain;

public enum TicketHistoryEventType {
    CREATED,
    ASSIGNED,
    STATUS_CHANGED,
    COMMENTED,
    ATTACHMENT_ADDED,
    SLA_BREACHED,
    ESCALATED,
    RESOLVED,
    CLOSED,
    REOPENED
}
