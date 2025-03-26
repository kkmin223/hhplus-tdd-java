package io.hhplus.tdd.point;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class UserPointTest {

    @Test
    void 양수_포인트로_포인트를_충전하면_현재_잔액과_입력한_포인트를_더한후_반환한다() {
        // given
        UserPoint userPoint = new UserPoint(1L, 100L, System.currentTimeMillis());
        long point = 100L;

        // when
        long resultPoint = userPoint.chargePoint(100L);

        // then
        Assertions.assertThat(resultPoint).isEqualTo(userPoint.point() + point);
    }

    @Test
    void 양수가_아닌_포인트를_충전하면_포인트_충전_금액_유효성_에러가_발생한다() {
        // given
        UserPoint userPoint = new UserPoint(1L, 100L, System.currentTimeMillis());
        long point = 0L;

        // when
        RuntimeException runtimeException = assertThrows(RuntimeException.class, () -> userPoint.chargePoint(point));

        // then
        Assertions.assertThat(runtimeException.getMessage())
            .isEqualTo("포인트 충전 금액은 양수여야합니다.");
    }

    @Test
    void 양수_포인트로_포인트를_사용하면_현재_잔액과_입력한_포인트를_뺀후_반환한다() {
        // given
        UserPoint userPoint = new UserPoint(1L, 100L, System.currentTimeMillis());
        long point = 100L;

        // when
        long resultPoint = userPoint.usePoint(100L);

        // then
        Assertions.assertThat(resultPoint).isEqualTo(userPoint.point() - point);
    }

    @Test
    void 양수가_아닌_포인트를_사용하면_포인트_사용_금액_유효성_에러가_발생한다() {
        // given
        UserPoint userPoint = new UserPoint(1L, 100L, System.currentTimeMillis());
        long point = 0L;

        // when
        RuntimeException runtimeException = assertThrows(RuntimeException.class, () -> userPoint.usePoint(point));

        // then
        Assertions.assertThat(runtimeException.getMessage())
            .isEqualTo("포인트 사용 금액은 양수여야합니다.");
    }
}
