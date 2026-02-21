CREATE TABLE mission_definitions (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    store_id    BIGINT       NOT NULL,
    type        VARCHAR(20)  NOT NULL,
    config_json TEXT         NOT NULL,
    reward_json TEXT         NOT NULL,
    is_active   TINYINT(1)   NOT NULL DEFAULT 1,
    created_at  DATETIME(6)  NOT NULL,
    updated_at  DATETIME(6)  NOT NULL,
    CONSTRAINT fk_mission_store FOREIGN KEY (store_id) REFERENCES stores (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 더미 미션 데이터 (강남점 기준)
INSERT INTO mission_definitions (store_id, type, config_json, reward_json, created_at, updated_at)
VALUES
    -- M1: 평일 15~17시 방문
    (1, 'TIME_WINDOW',
     '{"startHour":15,"endHour":17,"days":["MON","TUE","WED","THU","FRI"]}',
     '{"type":"POINT","amount":100}',
     NOW(6), NOW(6)),
    -- M2: 10분 체류
    (1, 'DWELL',
     '{"durationMinutes":10}',
     '{"type":"POINT","amount":150}',
     NOW(6), NOW(6)),
    -- M3: 아메리카노 구매
    (1, 'RECEIPT',
     '{"targetProductKey":"아메리카노","confidenceThreshold":0.8}',
     '{"type":"POINT","amount":120}',
     NOW(6), NOW(6)),
    -- M4: 신제품 쿠키 찾기
    (1, 'INVENTORY',
     '{"answerImageUrl":"https://example.com/cookie.jpg","confidenceThreshold":0.75}',
     '{"type":"POINT","amount":80}',
     NOW(6), NOW(6)),
    -- M5: 3일 방문 스탬프
    (1, 'STAMP',
     '{"requiredCount":3}',
     '{"type":"COUPON","code":"WAFFLE3DAY"}',
     NOW(6), NOW(6));
