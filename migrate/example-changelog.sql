--liquibase formatted sql

--changeset your.name:1 labels:create-proxies context:create-proxies
--comment: Create table proxies with specific columns
CREATE TABLE proxies (
    id serial primary key,
    conn_string VARCHAR(32) UNIQUE,
    ip_address VARCHAR(32),
    port VARCHAR(16),
    is_socks BOOLEAN,
    usage_count int DEFAULT 0 NOT NULL CHECK (usage_count >= 0),
    next_available TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    guest_token VARCHAR(20),
    guest_token_updated TIMESTAMP DEFAULT 'epoch',
    success_delta INT DEFAULT 0 CHECK (success_delta >= -50000 AND success_delta <= 50000),
    failed_count INT DEFAULT 0 CHECK (failed_count >= 0 AND failed_count <= 1000)
);
--rollback DROP TABLE proxies;