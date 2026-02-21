CREATE TABLE sessions (
    id         CHAR(36)    NOT NULL PRIMARY KEY,
    created_at DATETIME(6) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
