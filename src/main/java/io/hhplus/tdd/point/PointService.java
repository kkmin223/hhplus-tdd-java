package io.hhplus.tdd.point;

import io.hhplus.tdd.config.PointLimit;
import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.dto.point.ChargeUserPointRequestDto;
import io.hhplus.tdd.dto.point.UseUserPointRequestDto;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class PointService {

    private final PointLimit pointLimit;
    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;

    public UserPoint chargeUserPoint(Long id, ChargeUserPointRequestDto request) {

        if (pointLimit.max() < request.getAmount()) {
            throw new RuntimeException("최대 포인트를 넘는 금액은 충전할 수 없습니다.");
        }

        UserPoint userPoint = userPointTable.selectById(id);

        Long updatePoint = userPoint.point() + request.getAmount();
        if (pointLimit.max() < updatePoint) {
            throw new RuntimeException("최대 포인트를 넘어서 충전할 수 없습니다.");
        }

        UserPoint updatedUserPoint = userPointTable.insertOrUpdate(id, updatePoint);

        pointHistoryTable.insert(id, request.getAmount(), TransactionType.CHARGE, updatedUserPoint.updateMillis());

        return updatedUserPoint;
    }

    public UserPoint getUserPoint(Long id) {

        List<PointHistory> pointHistoryList = pointHistoryTable.selectAllByUserId(id);

        if (pointHistoryList.isEmpty()) {
            throw new RuntimeException("유효하지 않은 유저입니다.");
        }

        UserPoint userPoint = userPointTable.selectById(id);

        return userPoint;
    }

    public UserPoint useUserPoint(UseUserPointRequestDto request) {

        UserPoint userPoint = userPointTable.selectById(request.getId());

        if (userPoint.point() - request.getAmount() < pointLimit.min()) {
            throw new RuntimeException("잔액이 충분하지 않습니다.");
        }

        UserPoint updatedUserPoint = userPointTable.insertOrUpdate(request.getId(), userPoint.point() - request.getAmount());

        pointHistoryTable.insert(request.getId(), request.getAmount(), TransactionType.CHARGE, updatedUserPoint.updateMillis());

        return updatedUserPoint;
    }
}
