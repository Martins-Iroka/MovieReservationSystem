CREATE TABLE IF NOT EXISTS user_verification_tracking(
    token TEXT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,

    FOREIGN KEY (user_id) REFERENCES users(id)
);