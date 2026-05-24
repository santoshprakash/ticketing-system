package com.ticketing.system.ticket.infrastructure;

import com.ticketing.system.ticket.domain.TicketEntity;
import com.ticketing.system.ticket.dto.TicketFilter;
import org.springframework.data.jpa.domain.Specification;

public final class TicketSpecifications {

    private TicketSpecifications() {
    }

    public static Specification<TicketEntity> from(TicketFilter filter) {
        return (root, query, cb) -> {
            var predicate = cb.isNull(root.get("deletedAt"));
            if (filter.status() != null) {
                predicate = cb.and(predicate, cb.equal(root.get("status"), filter.status()));
            }
            if (filter.priority() != null) {
                predicate = cb.and(predicate, cb.equal(root.get("priority"), filter.priority()));
            }
            if (filter.category() != null && !filter.category().isBlank()) {
                predicate = cb.and(predicate, cb.equal(cb.lower(root.get("category")), filter.category().toLowerCase()));
            }
            if (filter.createdBy() != null) {
                predicate = cb.and(predicate, cb.equal(root.get("createdById"), filter.createdBy()));
            }
            if (filter.assignedTo() != null) {
                predicate = cb.and(predicate, cb.equal(root.get("assignedToId"), filter.assignedTo()));
            }
            return predicate;
        };
    }
}
