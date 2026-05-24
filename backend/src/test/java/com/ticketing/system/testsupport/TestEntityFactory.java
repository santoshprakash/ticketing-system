package com.ticketing.system.testsupport;

import com.ticketing.system.auth.domain.RoleCode;
import com.ticketing.system.auth.domain.RoleEntity;
import com.ticketing.system.auth.domain.UserEntity;
import com.ticketing.system.auth.domain.UserStatus;
import com.ticketing.system.entity.BaseEntity;
import com.ticketing.system.ticket.domain.TicketEntity;
import com.ticketing.system.ticket.domain.TicketPriority;
import com.ticketing.system.ticket.domain.TicketStatus;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.UUID;

public final class TestEntityFactory {

    private TestEntityFactory() {
    }

    public static RoleEntity role(RoleCode code) {
        RoleEntity role = new RoleEntity(code, code.name(), code.name());
        assignBaseEntity(role, UUID.randomUUID());
        return role;
    }

    public static UserEntity user(RoleCode code) {
        UserEntity user = new UserEntity(role(code), code.name().toLowerCase() + "@example.com", "{noop}Password123!", code.name() + " User", null);
        assignBaseEntity(user, UUID.randomUUID());
        return user;
    }

    public static UserEntity user(RoleCode code, String passwordHash) {
        UserEntity user = new UserEntity(role(code), code.name().toLowerCase() + "@example.com", passwordHash, code.name() + " User", null);
        assignBaseEntity(user, UUID.randomUUID());
        return user;
    }

    public static void setStatus(UserEntity user, UserStatus status) {
        setField(user, "status", status);
    }

    public static TicketEntity ticket(TicketStatus status) {
        TicketEntity ticket = new TicketEntity(
                "TCK-2026-TEST",
                "Portal login issue",
                "Customer cannot access the service portal after login.",
                TicketPriority.HIGH,
                "ACCESS",
                UUID.randomUUID()
        );
        assignBaseEntity(ticket, UUID.randomUUID());
        if (status != TicketStatus.OPEN) {
            ticket.changeStatus(status);
        }
        return ticket;
    }

    public static void assignBaseEntity(BaseEntity entity, UUID id) {
        try {
            Method setter = BaseEntity.class.getDeclaredMethod("setId", UUID.class);
            setter.setAccessible(true);
            setter.invoke(entity, id);
            Method onCreate = BaseEntity.class.getDeclaredMethod("onCreate");
            onCreate.setAccessible(true);
            onCreate.invoke(entity);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
