package com.ticketing.system.admin.domain;

import com.ticketing.system.auth.domain.UserEntity;
import com.ticketing.system.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_module_access")
public class UserModuleAccessEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Enumerated(EnumType.STRING)
    @Column(name = "module_code", nullable = false, length = 50)
    private ModuleCode moduleCode;

    @Column(nullable = false)
    private boolean enabled;

    protected UserModuleAccessEntity() {
    }

    public UserModuleAccessEntity(UserEntity user, ModuleCode moduleCode, boolean enabled) {
        this.user = user;
        this.moduleCode = moduleCode;
        this.enabled = enabled;
    }

    public UserEntity getUser() {
        return user;
    }

    public ModuleCode getModuleCode() {
        return moduleCode;
    }

    public boolean isEnabled() {
        return enabled && getDeletedAt() == null;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
