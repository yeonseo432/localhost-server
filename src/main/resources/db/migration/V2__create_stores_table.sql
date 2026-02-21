CREATE TABLE stores (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    owner_id        BIGINT       NULL,
    name            VARCHAR(100) NOT NULL,
    lat             DOUBLE       NOT NULL,
    lng             DOUBLE       NOT NULL,
    radius_m        INT          NOT NULL DEFAULT 100,
    business_number VARCHAR(20)  NULL,   -- 사업자번호 (저장만, 검증 없음)
    image_url       VARCHAR(512) NULL,   -- 매장 대표 이미지 URL
    created_at      DATETIME(6)  NOT NULL,
    updated_at      DATETIME(6)  NOT NULL,
    CONSTRAINT fk_store_owner FOREIGN KEY (owner_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;