CREATE TABLE reward_ledger (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id     CHAR(36)     NOT NULL,
    mission_id     BIGINT       NOT NULL,
    type           VARCHAR(10)  NOT NULL,
    amount_or_code VARCHAR(100) NOT NULL,
    issued_at      DATETIME(6)  NOT NULL,

    CONSTRAINT fk_reward_session FOREIGN KEY (session_id) REFERENCES sessions (id),
    CONSTRAINT fk_reward_mission FOREIGN KEY (mission_id) REFERENCES mission_definitions (id),

    -- 같은 (세션 + 미션)에 리워드 중복 발급 방지
    -- M1/M2처럼 매일 반복 지급이 필요하면 이 제약을 제거하고 애플리케이션 레벨에서 처리
    UNIQUE KEY uq_reward_per_mission (session_id, mission_id),

    INDEX idx_reward_session (session_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
