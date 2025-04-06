package io.hhplus.tdd.point;

import io.hhplus.tdd.config.PointLimit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class PointLimitCheckerTest {

    @Autowired
    private PointLimitChecker pointLimitChecker;

    @Autowired
    private PointLimit pointLimit;

    @Test
    void 포인트_최대_금액_포인트를_입력하면_최대_포인트_정책_검사를_통과한다() {
        // given
        long point = pointLimit.max();

        // when & then
        assertDoesNotThrow(() -> pointLimitChecker.checkMaxPointLimit(point));
    }

    @Test
    void 포인트_최대_금액_포인트보다_큰_포인트를_입력하면_최대_포인트_정책_검사에서_에러가_발생한다() {
        // given
        long point = pointLimit.max() + 1L;

        // when
        RuntimeException runtimeException = assertThrows(RuntimeException.class, () -> pointLimitChecker.checkMaxPointLimit(point));

        // then
        assertThat(runtimeException.getMessage())
            .isEqualTo("포인트는 최대 포인트 정책 금액보다 클 수 없습니다.");
    }

    @Test
    void 포인트_최소_금액_포인트를_입력하면_최소_포인트_정책_검사를_통과한다() {
        // given
        long point = pointLimit.min();

        // when & then
        assertDoesNotThrow(() -> pointLimitChecker.checkMinPointLimit(point));
    }



    @Test
    void 포인트_최소_금액_포인트보다_작은_포인트를_입력하면_최소_포인트_정책_검사에서_에러가_발생한다() {
        // given
        long point = pointLimit.min() - 1L;

        // when
        RuntimeException runtimeException = assertThrows(RuntimeException.class, () -> pointLimitChecker.checkMinPointLimit(point));

        // then
        assertThat(runtimeException.getMessage())
            .isEqualTo("포인트는 최소 포인트 정책 금액보다 작을 수 없습니다.");
    }


}
