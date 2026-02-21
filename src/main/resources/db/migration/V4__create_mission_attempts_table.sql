CREATE TABLE mission_attempts (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id     CHAR(36)     NOT NULL,
    mission_id     BIGINT       NOT NULL,
    status         VARCHAR(20)  NOT NULL,

    -- M1/M5: 날짜별 중복 방지 (오늘 이미 성공했는지 체크)
    attempt_date   DATE         NOT NULL,

    -- M3/M4: 업로드된 원본 이미지 URL (재촬영/운영 검토용)
    image_url      VARCHAR(512),

    -- M2: 체류 GPS 체크인
    checkin_at     DATETIME(6),
    checkin_lat    DOUBLE,
    checkin_lng    DOUBLE,

    -- M2: 체류 GPS 체크아웃
    checkout_at    DATETIME(6),
    checkout_lat   DOUBLE,
    checkout_lng   DOUBLE,

    -- M3/M4: FastAPI AI 판정 결과
    ai_result_json TEXT,

    created_at     DATETIME(6)  NOT NULL,
    updated_at     DATETIME(6)  NOT NULL,

    CONSTRAINT fk_attempt_session FOREIGN KEY (session_id) REFERENCES sessions (id),
    CONSTRAINT fk_attempt_mission FOREIGN KEY (mission_id) REFERENCES mission_definitions (id),

    -- 조회 성능 인덱스
    INDEX idx_attempt_session_mission (session_id, mission_id),
    INDEX idx_attempt_session_date    (session_id, mission_id, attempt_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
