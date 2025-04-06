package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.dto.point.ChargeUserPointRequestDto;
import io.hhplus.tdd.dto.point.UseUserPointRequestDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class PointService {

    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;
    private final PointLimitChecker pointLimitChecker;

    /**
     * 포인트 충전 <br>
     * 1. 충전 금액에 대한 포인트 최대 정책 검사<br>
     * 2. 유저 ID로 현재 포인트 조회<br>
     * 3. 충전 이후 금액 계산<br>
     * 4. 계산한 금액에 대한 포인트 최대 정책 검사<br>
     * 5. 포인트 업데이트<br>
     * 6. 포인트 충전 히스토리 추가<br>
     * 7. 최종 유저 포인트 현황 반환<br>
     * @param id 충전할 유저 ID
     * @param request 충전 금액을 담은 DTO
     * @return 충전 이후에 유저 포인트 현황
     */
    public UserPoint chargeUserPoint(Long id, ChargeUserPointRequestDto request) {

        pointLimitChecker.checkMaxPointLimit(request.getAmount());

        UserPoint userPoint = userPointTable.selectById(id);

        long updatePoint = userPoint.chargePoint(request.getAmount());
        pointLimitChecker.checkMaxPointLimit(updatePoint);

        UserPoint updatedUserPoint = userPointTable.insertOrUpdate(id, updatePoint);

        try {
            pointHistoryTable.insert(id, request.getAmount(), TransactionType.CHARGE, updatedUserPoint.updateMillis());
        } catch (Exception ex) {
            log.error("chargeUserPoint 히스토리 추가 실패: " + ex.getMessage());
        }


        return updatedUserPoint;
    }

    /**
     * 포인트 조회<br>
     * 1. 유저 ID로 포인트 히스토리 조회<br>
     * 2. 포인트 히스토리가 없을 경우, 유저 유효성 에러 발생<br>
     * 3. 유저 ID로 현재 포인트 조회<br>
     * @param id 조회할 유저 ID
     * @return 유저 포인트 현황
     */
    public UserPoint getUserPoint(Long id) {

        List<PointHistory> pointHistoryList = pointHistoryTable.selectAllByUserId(id);

        if (pointHistoryList.isEmpty()) {
            throw new RuntimeException("유효하지 않은 유저입니다.");
        }

        UserPoint userPoint = userPointTable.selectById(id);

        return userPoint;
    }

    /**
     * 포인트 사용<br>
     * 1. 유저 ID로 현재 포인트 조회<br>
     * 2. 포인트 사용 이후 금액 계산<br>
     * 3. 계산한 금액에 대한 포인트 최소 정책 검사<br>
     * 4. 포인트 사용 히스토리 추가<br>
     * 5. 사용 이후 유저 포인트 현황 반환<br>
     * @param id 유저 ID
     * @param request 사용 금액
     * @return 사용 이후에 유저 포인트 현황
     */
    public UserPoint useUserPoint(Long id, UseUserPointRequestDto request) {

        UserPoint userPoint = userPointTable.selectById(id);
        long updatePoint = userPoint.usePoint(request.getAmount());

        pointLimitChecker.checkMinPointLimit(updatePoint);

        UserPoint updatedUserPoint = userPointTable.insertOrUpdate(id, updatePoint);

        try {
            pointHistoryTable.insert(id, request.getAmount(), TransactionType.USE, updatedUserPoint.updateMillis());
        } catch (Exception ex) {
            log.error("useUserPoint 히스토리 추가 실패: " + ex.getMessage());
        }


        return updatedUserPoint;
    }

    /**
     * 1. 유저 ID로 포인트 히스토리 조회<br>
     * 2. 포인트 히스토리가 없을 경우 예외 반환<br>
     * 3. 포인트 히스토리 반환<br>
     * @param id 유저 ID
     * @return 유저 포인트 히스토리
     */
    public List<PointHistory> listPointHistory(Long id) {
        List<PointHistory> pointHistories = pointHistoryTable.selectAllByUserId(id);

        if (pointHistories.isEmpty()) {
            throw new RuntimeException("유효하지 않은 유저입니다");
        }

        return pointHistories;
    }
}
