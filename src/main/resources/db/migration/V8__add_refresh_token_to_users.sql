ALTER TABLE users ADD COLUMN refresh_token VARCHAR(255) NULL;
ALTER TABLE users ADD COLUMN refresh_token_expiry TIMESTAMP NULL;

CREATE INDEX idx_users_refresh_token ON users (refresh_token);
