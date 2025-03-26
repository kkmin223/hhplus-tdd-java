package io.hhplus.tdd.point;

import io.hhplus.tdd.config.PointLimit;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class PointLimitChecker {

    private final PointLimit pointLimit;

    public boolean checkPointLimit(long point) {
        return pointLimit.min() <= point && point <= pointLimit.max();
    }
}
