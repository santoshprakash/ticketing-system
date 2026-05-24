CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    token_hash VARCHAR(128) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ,
    replaced_by_token_hash VARCHAR(128),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by UUID,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_by UUID,
    deleted_at TIMESTAMPTZ,
    deleted_by UUID,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_refresh_tokens_created_by FOREIGN KEY (created_by) REFERENCES users (id),
    CONSTRAINT fk_refresh_tokens_updated_by FOREIGN KEY (updated_by) REFERENCES users (id),
    CONSTRAINT fk_refresh_tokens_deleted_by FOREIGN KEY (deleted_by) REFERENCES users (id),
    CONSTRAINT uk_refresh_tokens_token_hash UNIQUE (token_hash),
    CONSTRAINT chk_refresh_tokens_expiry CHECK (expires_at > created_at)
);

CREATE TABLE password_reset_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    token_hash VARCHAR(128) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    used_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by UUID,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_by UUID,
    deleted_at TIMESTAMPTZ,
    deleted_by UUID,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_password_reset_tokens_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_password_reset_tokens_created_by FOREIGN KEY (created_by) REFERENCES users (id),
    CONSTRAINT fk_password_reset_tokens_updated_by FOREIGN KEY (updated_by) REFERENCES users (id),
    CONSTRAINT fk_password_reset_tokens_deleted_by FOREIGN KEY (deleted_by) REFERENCES users (id),
    CONSTRAINT uk_password_reset_tokens_token_hash UNIQUE (token_hash),
    CONSTRAINT chk_password_reset_tokens_expiry CHECK (expires_at > created_at)
);

CREATE INDEX ix_refresh_tokens_user_active ON refresh_tokens (user_id, expires_at DESC)
    WHERE revoked_at IS NULL AND deleted_at IS NULL;
CREATE INDEX ix_refresh_tokens_hash_active ON refresh_tokens (token_hash)
    WHERE revoked_at IS NULL AND deleted_at IS NULL;
CREATE INDEX ix_password_reset_tokens_hash_active ON password_reset_tokens (token_hash)
    WHERE used_at IS NULL AND deleted_at IS NULL;
CREATE INDEX ix_password_reset_tokens_user_created ON password_reset_tokens (user_id, created_at DESC)
    WHERE deleted_at IS NULL;
