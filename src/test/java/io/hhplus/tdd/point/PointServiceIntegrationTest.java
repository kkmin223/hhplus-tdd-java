package io.hhplus.tdd.point;

import io.hhplus.tdd.config.PointLimit;
import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.dto.point.ChargeUserPointRequestDto;
import io.hhplus.tdd.dto.point.UseUserPointRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class PointServiceIntegrationTest {

    @Autowired
    private PointService pointService;

    @Autowired
    private PointHistoryTable pointHistoryTable;

    @Autowired
    private UserPointTable userPointTable;

    @Autowired
    private PointLimitChecker pointLimitChecker;

    @Autowired
    private PointLimit pointLimit;

    @BeforeEach
    void clearTable() {
        userPointTable.clear();
        pointHistoryTable.clear();
    }

    @Test
    void 포인트를_충전하면_사용자의_포인트_현황을_반환한다() {
        // given
        Long userId = 1L;
        ChargeUserPointRequestDto chargeUserPointRequestDto = new ChargeUserPointRequestDto(100L);

        // when
        UserPoint userPoint = pointService.chargeUserPoint(userId, chargeUserPointRequestDto);

        // then
        assertThat(userPoint)
            .extracting("id", "point")
            .contains(userId, chargeUserPointRequestDto.getAmount());
    }

    @Test
    void 포인트_최대_정책을_넘는_금액를_충전하면_포인트_충전_불가_에러가_발생한다() {
        // given
        Long userId = 1L;
        ChargeUserPointRequestDto chargeUserPointRequestDto = new ChargeUserPointRequestDto(pointLimit.max() + 1L);

        // when
        RuntimeException runtimeException = assertThrows(RuntimeException.class, () -> pointService.chargeUserPoint(userId, chargeUserPointRequestDto));

        // then
        assertThat(runtimeException.getMessage())
            .isEqualTo("최대 포인트를 넘는 금액은 충전할 수 없습니다.");
    }

    @Test
    void 포인트_충전_이후에_포인트_총액이_포인트_최대_정책을_넘으면_포인트_충전_불가_에러가_발생한다() {
        // given
        Long userId = 1L;
        ChargeUserPointRequestDto chargeUserPointRequestDto1 = new ChargeUserPointRequestDto(100L);
        ChargeUserPointRequestDto chargeUserPointRequestDto2 = new ChargeUserPointRequestDto(pointLimit.max() - chargeUserPointRequestDto1.getAmount() + 1L);

        // when
        RuntimeException runtimeException = assertThrows(RuntimeException.class, () -> {
            pointService.chargeUserPoint(userId, chargeUserPointRequestDto1);
            pointService.chargeUserPoint(userId, chargeUserPointRequestDto2);
        });

        // then
        assertThat(runtimeException.getMessage())
            .isEqualTo("최대 포인트를 넘어서 충전할 수 없습니다.");
    }

    @Test
    void 포인트를_충전한_이후에_포인트_현황을_조회할_수_있다() {
        // given
        Long userId = 1L;
        ChargeUserPointRequestDto chargeUserPointRequestDto = new ChargeUserPointRequestDto(100L);

        UserPoint userPoint = pointService.chargeUserPoint(userId, chargeUserPointRequestDto);
        // when
        UserPoint getUserPoint = pointService.getUserPoint(userId);

        // then
        assertThat(getUserPoint)
            .extracting("id", "point", "updateMillis")
            .contains(userPoint.id(), userPoint.point(), userPoint.updateMillis());
    }

    @Test
    void 포인트를_충전한적이_없는_유저가_포인트를_조회하면_유저_유효성_에러가_발생한다() {
        // given
        Long userId = 1L;
        // when
        RuntimeException runtimeException = assertThrows(RuntimeException.class, () -> pointService.getUserPoint(userId));

        // then
        assertThat(runtimeException.getMessage())
            .isEqualTo("유효하지 않은 유저입니다.");
    }

    @Test
    void 포인트를_충전한_이후에_포인트를_사용할_수_있다() {
        // given
        Long userId = 1L;
        ChargeUserPointRequestDto chargeUserPointRequestDto = new ChargeUserPointRequestDto(100L);
        UseUserPointRequestDto useUserPointRequestDto = UseUserPointRequestDto.createdBy(100L);

        // when
        pointService.chargeUserPoint(userId, chargeUserPointRequestDto);
        UserPoint userPoint = pointService.useUserPoint(userId, useUserPointRequestDto);

        // then
        assertThat(userPoint)
            .extracting("id", "point")
            .contains(userId, chargeUserPointRequestDto.getAmount() - useUserPointRequestDto.getAmount());
    }

    @Test
    void 포인트를_사용한_이후_잔액이_포인트_최소_정책보다_작으면_잔액_유효성_에러가_발생한다() {
        // given
        Long userId = 1L;
        ChargeUserPointRequestDto chargeUserPointRequestDto = new ChargeUserPointRequestDto(100L);
        UseUserPointRequestDto useUserPointRequestDto = UseUserPointRequestDto.createdBy(chargeUserPointRequestDto.getAmount() + 1L);

        // when
        RuntimeException runtimeException = assertThrows(RuntimeException.class, () -> {
            pointService.chargeUserPoint(userId, chargeUserPointRequestDto);
            pointService.useUserPoint(userId, useUserPointRequestDto);
        });

        // then
        assertThat(runtimeException.getMessage())
            .isEqualTo("잔액이 충분하지 않습니다.");
    }

    @Test
    void 포인트를_충전하면_사용자의_포인트_충전_히스토리를_조회할_수_있다() {
        // given
        Long userId = 1L;
        ChargeUserPointRequestDto chargeUserPointRequestDto = new ChargeUserPointRequestDto(100L);

        // when
        pointService.chargeUserPoint(userId, chargeUserPointRequestDto);
        List<PointHistory> pointHistories = pointService.listPointHistory(userId);

        assertThat(pointHistories)
            .hasSize(1)
            .extracting("userId", "amount", "type")
            .contains(
                tuple(userId, chargeUserPointRequestDto.getAmount(), TransactionType.CHARGE)
            );
    }

    @Test
    void 포인트_충전하고_사용하면_사용자의_포인트_충전_사용_히스토리를_조회할_수_있다() {
        // given
        Long userId = 1L;
        ChargeUserPointRequestDto chargeUserPointRequestDto = new ChargeUserPointRequestDto(100L);
        UseUserPointRequestDto useUserPointRequestDto = UseUserPointRequestDto.createdBy(100L);

        // when
        pointService.chargeUserPoint(userId, chargeUserPointRequestDto);
        pointService.useUserPoint(userId, useUserPointRequestDto);

        List<PointHistory> pointHistories = pointService.listPointHistory(userId);

        // then
        assertThat(pointHistories)
            .hasSize(2)
            .extracting("userId", "amount", "type")
            .containsExactlyInAnyOrder(
                tuple(userId, chargeUserPointRequestDto.getAmount(), TransactionType.CHARGE)
                , tuple(userId, useUserPointRequestDto.getAmount(), TransactionType.USE)
            );
    }

    @Test
    void 포인트를_충전하지_않은_사용자가_포인트_히스토리를_조회하면_유저_유효성_에러가_발생한다() {
        // given
        Long userId = 1L;

        // when
        RuntimeException runtimeException = assertThrows(RuntimeException.class, () -> pointService.listPointHistory(userId));

        // then
        assertThat(runtimeException.getMessage())
            .isEqualTo("유효하지 않은 유저입니다");
    }

}
