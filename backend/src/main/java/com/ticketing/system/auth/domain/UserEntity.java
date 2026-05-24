package com.ticketing.system.auth.domain;

import com.ticketing.system.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "users")
public class UserEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private RoleEntity role;

    @Column(nullable = false, unique = true, length = 320)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(name = "phone_number", length = 30)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private UserStatus status;

    @Column(name = "last_login_at")
    private OffsetDateTime lastLoginAt;

    @Column(name = "super_admin", nullable = false)
    private boolean superAdmin;

    protected UserEntity() {
    }

    public UserEntity(RoleEntity role, String email, String passwordHash, String fullName, String phoneNumber) {
        this.role = role;
        this.email = email.toLowerCase();
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.status = UserStatus.ACTIVE;
        this.superAdmin = false;
    }

    public RoleEntity getRole() {
        return role;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public UserStatus getStatus() {
        return status;
    }

    public OffsetDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public boolean isSuperAdmin() {
        return superAdmin;
    }

    public boolean isActive() {
        return status == UserStatus.ACTIVE && getDeletedAt() == null;
    }

    public void recordLogin() {
        this.lastLoginAt = OffsetDateTime.now();
    }

    public void updatePassword(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void updateProfile(RoleEntity role, String fullName, String phoneNumber, UserStatus status) {
        this.role = role;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.status = status;
    }

    public void markSuperAdmin() {
        this.superAdmin = true;
    }
}
