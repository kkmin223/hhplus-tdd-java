package io.hhplus.tdd.point;

import io.hhplus.tdd.config.PointLimit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PointLimitCheckerTest {

    @Autowired
    private PointLimitChecker pointLimitChecker;

    @Autowired
    private PointLimit pointLimit;

    @Test
    void 포인트_최대_금액_포인트를_입력하면_TRUE를_반환한다() {
        // given
        long point = pointLimit.max();

        // when
        boolean result = pointLimitChecker.checkPointLimit(point);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void 포인트_최소_금액_포인트를_입력하면_TRUE를_반환한다() {
        // given
        long point = pointLimit.min();

        // when
        boolean result = pointLimitChecker.checkPointLimit(point);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void 포인트_최대_금액_포인트보다_큰_포인트를_입력하면_FALSE를_반환한다() {
        // given
        long point = pointLimit.max() + 1L;

        // when
        boolean result = pointLimitChecker.checkPointLimit(point);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void 포인트_최소_금액_포인트보다_작은_포인트를_입력하면_FALSE를_반환한다() {
        // given
        long point = pointLimit.min() - 1L;

        // when
        boolean result = pointLimitChecker.checkPointLimit(point);

        // then
        assertThat(result).isFalse();
    }


}
