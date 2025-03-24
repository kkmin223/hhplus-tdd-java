package io.hhplus.tdd.point;

import io.hhplus.tdd.config.PointLimit;
import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.dto.point.ChargeUserPointRequestDto;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

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
}
