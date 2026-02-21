CREATE TABLE users (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(12)  NOT NULL UNIQUE,
    password      VARCHAR(12)  NOT NULL,
    role          VARCHAR(10)  NOT NULL DEFAULT 'USER',  -- USER | OWNER
    point         INT          NOT NULL,
    created_at    DATETIME(6)  NOT NULL,
    updated_at    DATETIME(6)  NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
