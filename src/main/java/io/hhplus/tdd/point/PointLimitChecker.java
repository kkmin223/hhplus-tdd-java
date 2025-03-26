package io.hhplus.tdd.point;

import io.hhplus.tdd.config.PointLimit;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class PointLimitChecker {

    private final PointLimit pointLimit;

    public void checkMaxPointLimit(long point) {
        if (pointLimit.max() < point) {
            throw new RuntimeException("포인트는 최대 포인트 정책 금액보다 클 수 없습니다.");
        }
    }

    public void checkMinPointLimit(long point) {
        if (point < pointLimit.min()) {
            throw new RuntimeException("포인트는 최소 포인트 정책 금액보다 작을 수 없습니다.");
        }
    }
}
