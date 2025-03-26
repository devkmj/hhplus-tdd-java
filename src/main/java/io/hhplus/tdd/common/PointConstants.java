package io.hhplus.tdd.common;

/**
 * 포인트 관련 상수들을 모아둔 클래스
 */
public class PointConstants {

    private PointConstants() {
        // 인스턴스화 방지
    }

    public static final long MAX_POINT = 100_000L;               // 최대 보유 포인트
    public static final long MIN_CHARGE_AMOUNT = 100L;           // 최소 충전 금액
    public static final long MIN_USE_AMOUNT = 100L;              // 최소 사용 금액

}
