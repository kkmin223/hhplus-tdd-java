package io.hhplus.tdd.point;

import io.hhplus.tdd.dto.point.ChargeUserPointRequestDto;
import io.hhplus.tdd.dto.point.UseUserPointRequestDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/point")
@AllArgsConstructor
@Validated
public class PointController {

    private static final Logger log = LoggerFactory.getLogger(PointController.class);
    private final PointService pointService;

    /**
     * TODO - 특정 유저의 포인트를 조회하는 기능을 작성해주세요.
     */
    @GetMapping("{id}")
    public UserPoint point(
        @PathVariable @Positive(message = "유저 Id는 양수여야 합니다.") long id
    ) {
        return pointService.getUserPoint(id);
    }

    /**
     * TODO - 특정 유저의 포인트 충전/이용 내역을 조회하는 기능을 작성해주세요.
     */
    @GetMapping("{id}/histories")
    public List<PointHistory> history(
        @PathVariable @Positive(message = "유저 Id는 양수여야 합니다.") long id
    ) {
        return pointService.listPointHistory(id);
    }

    /**
     * TODO - 특정 유저의 포인트를 충전하는 기능을 작성해주세요.
     */
    @PatchMapping("{id}/charge")
    public UserPoint charge(
        @PathVariable @Positive(message = "유저 Id는 양수여야 합니다.") long id,
        @Valid @RequestBody ChargeUserPointRequestDto request
    ) {
        return pointService.chargeUserPoint(id, request);
    }

    /**
     * TODO - 특정 유저의 포인트를 사용하는 기능을 작성해주세요.
     */
    @PatchMapping("{id}/use")
    public UserPoint use(
        @PathVariable @Positive(message = "유저 Id는 양수여야 합니다.") long id,
        @Valid @RequestBody UseUserPointRequestDto request
    ) {
        return pointService.useUserPoint(id, request);
    }
}
