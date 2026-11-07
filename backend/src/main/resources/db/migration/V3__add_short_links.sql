CREATE TABLE short_links (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    target_url VARCHAR(500) NOT NULL,
    created_at TIMESTAMP NOT NULL
);
