package com.waffle.marketing.mission.model

enum class AttemptStatus {
    /** 시작됨 (체크인 등 1단계 완료, 아직 최종 검증 대기) */
    PENDING,

    /** 검증 성공 → 리워드 지급 대상 */
    SUCCESS,

    /** 검증 실패 */
    FAILED,

    /** 재시도 필요 (AI 신뢰도 부족 등) */
    RETRY,
}
