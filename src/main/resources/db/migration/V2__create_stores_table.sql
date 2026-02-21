CREATE TABLE stores (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    lat        DOUBLE       NOT NULL,
    lng        DOUBLE       NOT NULL,
    radius_m   INT          NOT NULL DEFAULT 100,
    address    VARCHAR(255),
    created_at DATETIME(6)  NOT NULL,
    updated_at DATETIME(6)  NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 더미 매장 데이터 (MVP)
INSERT INTO stores (name, lat, lng, radius_m, address, created_at, updated_at)
VALUES
    ('와플 카페 강남점', 37.4979, 127.0276, 100, '서울시 강남구 테헤란로 123', NOW(6), NOW(6)),
    ('와플 카페 홍대점', 37.5563, 126.9237, 100, '서울시 마포구 어울마당로 65', NOW(6), NOW(6)),
    ('와플 카페 잠실점', 37.5133, 127.1028, 100, '서울시 송파구 올림픽로 300', NOW(6), NOW(6));
