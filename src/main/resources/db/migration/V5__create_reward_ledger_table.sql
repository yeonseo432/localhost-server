CREATE TABLE reward_ledger (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id        BIGINT       NOT NULL,
    mission_id     BIGINT       NOT NULL,
    amount         INT          NOT NULL,
    created_at     DATETIME(6)  NOT NULL,

    CONSTRAINT fk_reward_user    FOREIGN KEY (user_id)    REFERENCES users (id),
    CONSTRAINT fk_reward_mission FOREIGN KEY (mission_id) REFERENCES mission_definitions (id),

    -- 같은 (유저 + 미션)에 리워드 중복 발급 방지
    -- M1/M2처럼 매일 반복 지급이 필요하면 이 제약을 제거하고 애플리케이션 레벨에서 처리
    UNIQUE KEY uq_reward_per_mission (user_id, mission_id),

    INDEX idx_reward_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
