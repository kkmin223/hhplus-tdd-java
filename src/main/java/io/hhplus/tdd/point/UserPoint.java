package io.hhplus.tdd.point;

public record UserPoint(
    long id,
    long point,
    long updateMillis
) {

    public static UserPoint empty(long id) {
        return new UserPoint(id, 0, System.currentTimeMillis());
    }

    public long chargePoint(long amount) {
        if (amount <= 0) {
            throw new RuntimeException("포인트 충전 금액은 양수여야합니다.");
        }

        return this.point() + amount;
    }

    public long usePoint(long amount) {
        if (amount <= 0) {
            throw new RuntimeException("포인트 사용 금액은 양수여야합니다.");
        }

        return this.point() - amount;
    }
}
