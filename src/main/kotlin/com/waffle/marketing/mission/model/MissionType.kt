package com.waffle.marketing.mission.model

enum class MissionType {
    /** M1: 특정 시간대 방문 */
    TIME_WINDOW,

    /** M2: N분 이상 체류 (GPS 체크인/체크아웃) */
    DWELL,

    /** M3: 특정 제품 구매 (영수증 OCR + AI) */
    RECEIPT,

    /** M4: 특정 재고 상품 찾아보기 (사진 비교) */
    INVENTORY,

    /** M5: 반복 방문 (스탬프) */
    STAMP,
}
