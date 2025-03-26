package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.dto.point.ChargeUserPointRequestDto;
import io.hhplus.tdd.dto.point.UseUserPointRequestDto;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    private PointLimitChecker pointLimitChecker;

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

        Mockito.when(pointLimitChecker.checkPointLimit(anyLong())).thenReturn(true);
        // when
        UserPoint result = pointService.chargeUserPoint(userId, request);

        // then
        assertThat(result)
            .extracting("id", "point")
            .contains(userId, request.getAmount());
    }

    @Test
    void 포인트를_조회한다() {
        //given
        Long userId = 1L;
        UserPoint existUserPoint = new UserPoint(userId, 10L, System.currentTimeMillis());

        Mockito.when(pointHistoryTable.selectAllByUserId(userId))
            .thenReturn(List.of(new PointHistory(1, userId, existUserPoint.point(), TransactionType.CHARGE, existUserPoint.updateMillis())));

        Mockito.when(userPointTable.selectById(userId))
            .thenReturn(existUserPoint);

        //when
        UserPoint result = pointService.getUserPoint(userId);

        //then
        assertThat(result)
            .extracting("id", "point", "updateMillis")
            .contains(existUserPoint.id(), existUserPoint.point(), existUserPoint.updateMillis());
    }

    @Test
    void 포인트_히스토리_내역이_없는_유저를_조회하면_실패한다() {
        //given
        Long userId = 1L;

        Mockito.when(pointHistoryTable.selectAllByUserId(userId))
            .thenReturn(new ArrayList<>());

        //when
        RuntimeException runtimeException = assertThrows(RuntimeException.class, () -> pointService.getUserPoint(userId));

        assertThat(runtimeException.getMessage())
            .isEqualTo("유효하지 않은 유저입니다.");
    }

    @Test
    void 포인트를_사용한다() {
        //given
        Long userId = 1L;
        Long amount = 50L;
        UseUserPointRequestDto request = UseUserPointRequestDto.createdBy(amount);

        UserPoint existUserPoint = new UserPoint(userId, 100L, System.currentTimeMillis());

        Mockito.when(userPointTable.selectById(userId))
            .thenReturn(existUserPoint);

        Mockito.when(userPointTable.insertOrUpdate(anyLong(), anyLong()))
            .thenReturn(new UserPoint(userId, existUserPoint.point() - request.getAmount(), System.currentTimeMillis()));

        Mockito.when(pointLimitChecker.checkPointLimit(anyLong())).thenReturn(true);
        //when
        UserPoint updatedUserPoint = pointService.useUserPoint(userId, request);

        //then
        assertThat(updatedUserPoint)
            .extracting("id", "point")
            .contains(userId, existUserPoint.point() - request.getAmount());

    }

    @Test
    void 사용후_포인트가_최소_포인트보다_작으면_에러가_발생한다() {
        //given
        Long userId = 1L;
        Long amount = 150L;
        UseUserPointRequestDto request = UseUserPointRequestDto.createdBy(amount);

        UserPoint existUserPoint = new UserPoint(userId, 100L, System.currentTimeMillis());

        Mockito.when(userPointTable.selectById(userId))
            .thenReturn(existUserPoint);

        //when
        RuntimeException runtimeException = assertThrows(RuntimeException.class, () -> pointService.useUserPoint(userId, request));

        //then
        assertThat(runtimeException.getMessage())
            .isEqualTo("잔액이 충분하지 않습니다.");
    }

    @Test
    void 포인트_히스토리를_조회한다() {
        // given
        Long userId = 1L;
        PointHistory pointHistory = new PointHistory(1L, userId, 100, TransactionType.CHARGE, System.currentTimeMillis());

        Mockito.when(pointHistoryTable.selectAllByUserId(userId))
            .thenReturn(List.of(pointHistory));

        // when
        List<PointHistory> pointHistories = pointService.listPointHistory(userId);

        // then
        assertThat(pointHistories).hasSize(1)
            .extracting("id", "userId", "amount", "type")
            .contains(new Tuple(1L, userId, pointHistory.amount(), pointHistory.type()));
    }

    @Test
    void 포인트_히스토리가_없는_유저를_조회하면_에러가_발생한다() {
        // given
        Long userId = 1L;
        Mockito.when(pointHistoryTable.selectAllByUserId(userId))
            .thenReturn(new ArrayList<>());

        // when
        RuntimeException runtimeException = assertThrows(RuntimeException.class, () -> pointService.listPointHistory(userId));

        // then
        assertThat(runtimeException.getMessage())
            .isEqualTo("유효하지 않은 유저입니다");
    }
}
