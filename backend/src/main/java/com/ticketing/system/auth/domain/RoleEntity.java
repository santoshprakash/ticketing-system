package com.ticketing.system.auth.domain;

import com.ticketing.system.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "roles")
public class RoleEntity extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 50)
    private RoleCode code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    protected RoleEntity() {
    }

    public RoleEntity(RoleCode code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }

    public RoleCode getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
