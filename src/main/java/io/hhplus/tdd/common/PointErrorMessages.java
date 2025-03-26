package io.hhplus.tdd.common;

public enum PointErrorMessages {
    AMOUNT_MUST_BE_POSITIVE("충전 금액은 %d보다 커야 합니다."),
    MAX_POINT_EXCEEDED("최대 보유 포인트(%d)를 초과할 수 없습니다."),
    INSUFFICIENT_POINT("포인트가 부족합니다. 현재 보유 포인트: %d"),
    USE_AMOUNT_MUST_BE_POSITIVE("사용 금액은 %d보다 커야 합니다.");

    private final String template;

    PointErrorMessages(String template) {
        this.template = template;
    }

    public String message() {
        return template;
    }

    public String message(Object... args) {
        return String.format(template, args);
    }
}