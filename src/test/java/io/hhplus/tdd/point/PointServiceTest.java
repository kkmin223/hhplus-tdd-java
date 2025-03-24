package io.hhplus.tdd.point;

import io.hhplus.tdd.config.PointLimit;
import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.dto.point.ChargeUserPointRequestDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;

@ExtendWith(MockitoExtension.class)
class PointServiceTest {

    @InjectMocks
    private PointService pointService;

    @Mock
    private UserPointTable userPointTable;

    @Mock
    private PointHistoryTable pointHistoryTable;

    @Mock
    private PointLimit pointLimit;

    @Test
    void 포인트충전_테스트() {
        // given
        Long userId = 1L;
        ChargeUserPointRequestDto request = new ChargeUserPointRequestDto(100L);
        UserPoint existUserPoint = UserPoint.empty(userId);

        Mockito.when(userPointTable.insertOrUpdate(userId, request.getAmount()))
            .thenReturn(new UserPoint(userId, existUserPoint.point() + request.getAmount(), System.currentTimeMillis()));

        Mockito.when(userPointTable.selectById(userId))
            .thenReturn(existUserPoint);

        Mockito.when(pointHistoryTable.insert(anyLong(), anyLong(), any(), anyLong()))
            .thenReturn(new PointHistory(1, userId, request.getAmount(), TransactionType.CHARGE, System.currentTimeMillis()));

        Mockito.when(pointLimit.max()).thenReturn(10000L);
        // when
        UserPoint result = pointService.chargeUserPoint(userId, request);

        // then
        assertThat(result)
            .extracting("id", "point")
            .contains(userId, request.getAmount());
    }

    @Test
    void 최대금액을_넘는_포인트를_충전하면_에러가_발생한다() {
        //given
        Long userId = 1L;
        ChargeUserPointRequestDto request = new ChargeUserPointRequestDto(110L);

        Mockito.when(pointLimit.max()).thenReturn(100L);

        //when & then
        assertThatThrownBy(() -> pointService.chargeUserPoint(userId, request))
            .isInstanceOf(RuntimeException.class);
    }

    @Test
    void 포인트를_충전한_값이_최대_포인트를_넘으면_에러가_발생한다() {
        //given
        Long userId = 1L;
        ChargeUserPointRequestDto request = new ChargeUserPointRequestDto(20L);
        UserPoint existUserPoint = new UserPoint(userId, 90L, System.currentTimeMillis());

        Mockito.when(userPointTable.selectById(userId))
            .thenReturn(existUserPoint);

        Mockito.when(pointLimit.max()).thenReturn(100L);

        //when & then
        assertThatThrownBy(() -> pointService.chargeUserPoint(userId, request))
            .isInstanceOf(RuntimeException.class);
    }

    @Test
    void 포인트를_충전한_값이_최대_포인트와_같으면_충전을_성공한다() {
        //given
        Long userId = 1L;
        ChargeUserPointRequestDto request = new ChargeUserPointRequestDto(10L);
        UserPoint existUserPoint = new UserPoint(userId, 90L, System.currentTimeMillis());

        Mockito.when(userPointTable.insertOrUpdate(userId, request.getAmount()))
            .thenReturn(new UserPoint(userId, existUserPoint.point() + request.getAmount(), System.currentTimeMillis()));

        Mockito.when(userPointTable.selectById(userId))
            .thenReturn(existUserPoint);

        Mockito.when(pointHistoryTable.insert(anyLong(), anyLong(), any(), anyLong()))
            .thenReturn(new PointHistory(1, userId, request.getAmount(), TransactionType.CHARGE, System.currentTimeMillis()));

        Mockito.when(pointLimit.max()).thenReturn(100L);

        //when & then
        assertThatThrownBy(() -> pointService.chargeUserPoint(userId, request))
            .isInstanceOf(RuntimeException.class);
    }
}
