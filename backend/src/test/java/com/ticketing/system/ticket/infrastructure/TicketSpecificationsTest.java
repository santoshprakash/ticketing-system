package com.ticketing.system.ticket.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ticketing.system.ticket.domain.TicketEntity;
import com.ticketing.system.ticket.domain.TicketPriority;
import com.ticketing.system.ticket.domain.TicketStatus;
import com.ticketing.system.ticket.dto.TicketFilter;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TicketSpecificationsTest {

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void fromShouldBuildPredicateForEveryFilterField() {
        Root<TicketEntity> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Path path = mock(Path.class);
        Expression<String> lowerCategory = mock(Expression.class);
        Predicate initial = mock(Predicate.class);
        Predicate combined = mock(Predicate.class);
        when(root.get(any(String.class))).thenReturn(path);
        when(cb.isNull(path)).thenReturn(initial);
        when(cb.lower(path)).thenReturn(lowerCategory);
        when(cb.equal(any(), any())).thenReturn(mock(Predicate.class));
        when(cb.and(any(Expression.class), any(Expression.class))).thenReturn(combined);
        UUID createdBy = UUID.randomUUID();
        UUID assignedTo = UUID.randomUUID();

        TicketSpecifications.from(new TicketFilter(
                TicketStatus.IN_PROGRESS,
                TicketPriority.CRITICAL,
                " Network ",
                createdBy,
                assignedTo
        )).toPredicate(root, query, cb);

        verify(root).get("deletedAt");
        verify(root).get("status");
        verify(root).get("priority");
        verify(root).get("category");
        verify(root).get("createdById");
        verify(root).get("assignedToId");
        verify(cb).equal(lowerCategory, " network ");
        verify(cb).equal(path, TicketStatus.IN_PROGRESS);
        verify(cb).equal(path, TicketPriority.CRITICAL);
        verify(cb).equal(path, createdBy);
        verify(cb).equal(path, assignedTo);
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void fromShouldOnlyRequireDeletedAtPredicateWhenFilterIsEmpty() {
        Root<TicketEntity> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Path path = mock(Path.class);
        Predicate initial = mock(Predicate.class);
        when(root.get(any(String.class))).thenReturn(path);
        when(cb.isNull(path)).thenReturn(initial);

        Predicate predicate = TicketSpecifications.from(new TicketFilter(null, null, null, null, null)).toPredicate(root, query, cb);

        assertThat(predicate).isSameAs(initial);
        verify(root).get("deletedAt");
    }
}
