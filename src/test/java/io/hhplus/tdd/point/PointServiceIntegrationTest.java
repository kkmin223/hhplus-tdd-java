package io.hhplus.tdd.point;

import io.hhplus.tdd.config.PointLimit;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.dto.point.ChargeUserPointRequestDto;
import io.hhplus.tdd.dto.point.UseUserPointRequestDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class PointServiceIntegrationTest {

    @Autowired
    private PointService pointService;

    @Autowired
    private UserPointTable userPointTable;

    @Autowired
    private PointLimit pointLimit;

    private static Long userId = 0L;

    private static Long getUserId() {
        return ++userId;
    }

    @Test
    void 포인트를_충전하면_사용자의_포인트_현황을_반환한다() {
        // given
        Long userId = getUserId();
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
        Long userId = getUserId();
        ChargeUserPointRequestDto chargeUserPointRequestDto = new ChargeUserPointRequestDto(pointLimit.max() + 1L);

        // when
        RuntimeException runtimeException = assertThrows(RuntimeException.class, () -> pointService.chargeUserPoint(userId, chargeUserPointRequestDto));

        // then
        assertThat(runtimeException.getMessage())
            .isEqualTo("포인트는 최대 포인트 정책 금액보다 클 수 없습니다.");
    }

    @Test
    void 포인트_충전_이후에_포인트_총액이_포인트_최대_정책을_넘으면_포인트_충전_불가_에러가_발생한다() {
        // given
        Long userId = getUserId();
        ChargeUserPointRequestDto chargeUserPointRequestDto1 = new ChargeUserPointRequestDto(100L);
        ChargeUserPointRequestDto chargeUserPointRequestDto2 = new ChargeUserPointRequestDto(pointLimit.max() - chargeUserPointRequestDto1.getAmount() + 1L);

        // when
        RuntimeException runtimeException = assertThrows(RuntimeException.class, () -> {
            pointService.chargeUserPoint(userId, chargeUserPointRequestDto1);
            pointService.chargeUserPoint(userId, chargeUserPointRequestDto2);
        });

        // then
        assertThat(runtimeException.getMessage())
            .isEqualTo("포인트는 최대 포인트 정책 금액보다 클 수 없습니다.");
    }

    @Test
    void 포인트를_충전한_이후에_포인트_현황을_조회할_수_있다() {
        // given
        Long userId = getUserId();
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
        Long userId = getUserId();
        // when
        RuntimeException runtimeException = assertThrows(RuntimeException.class, () -> pointService.getUserPoint(userId));

        // then
        assertThat(runtimeException.getMessage())
            .isEqualTo("유효하지 않은 유저입니다.");
    }

    @Test
    void 포인트를_충전한_이후에_포인트를_사용할_수_있다() {
        // given
        Long userId = getUserId();
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
        Long userId = getUserId();
        ChargeUserPointRequestDto chargeUserPointRequestDto = new ChargeUserPointRequestDto(100L);
        UseUserPointRequestDto useUserPointRequestDto = UseUserPointRequestDto.createdBy(chargeUserPointRequestDto.getAmount() + 1L);

        // when
        RuntimeException runtimeException = assertThrows(RuntimeException.class, () -> {
            pointService.chargeUserPoint(userId, chargeUserPointRequestDto);
            pointService.useUserPoint(userId, useUserPointRequestDto);
        });

        // then
        assertThat(runtimeException.getMessage())
            .isEqualTo("포인트는 최소 포인트 정책 금액보다 작을 수 없습니다.");
    }

    @Test
    void 포인트를_충전하면_사용자의_포인트_충전_히스토리를_조회할_수_있다() {
        // given
        Long userId = getUserId();
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
        Long userId = getUserId();
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
        Long userId = getUserId();

        // when
        RuntimeException runtimeException = assertThrows(RuntimeException.class, () -> pointService.listPointHistory(userId));

        // then
        assertThat(runtimeException.getMessage())
            .isEqualTo("유효하지 않은 유저입니다");
    }



    @Test
    void 동일한_유저가_N번_포인트를_충전하면_N번의_포인트가_충전된다() throws InterruptedException {
        // given
        Long userId = getUserId();
        Long amount = 100L;
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    ChargeUserPointRequestDto request = new ChargeUserPointRequestDto(amount);
                    pointService.chargeUserPoint(userId, request);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        // then
        UserPoint userPoint = pointService.getUserPoint(userId);

        assertThat(userPoint)
            .extracting("id", "point")
            .contains(userId, amount * threadCount);
    }

    @Test
    void 동일한_유저가_N번_포인트를_충전할때_최대_포인트를_넘으면_이후_충전에_대해서_포인트_충전_불가_에러가_발생한다() throws InterruptedException {
        // given
        Long userId = getUserId();
        int threadCount = 10;
        int expectFailCount = 3;

        Long amount = pointLimit.max() / (threadCount - expectFailCount);
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    ChargeUserPointRequestDto request = new ChargeUserPointRequestDto(amount);
                    pointService.chargeUserPoint(userId, request);
                    successCount.getAndIncrement();
                } catch (Exception ex) {
                    failCount.getAndIncrement();
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        // then
        UserPoint userPoint = pointService.getUserPoint(userId);

        assertThat(successCount.get()).isEqualTo(threadCount - expectFailCount);
        assertThat(failCount.get()).isEqualTo(expectFailCount);

        assertThat(userPoint)
            .extracting("id", "point")
            .contains(userId, amount * successCount.get());
    }

    @Test
    void 동일한_유저가_N번_포인트를_사용하면_N번의_포인트가_사용된다() throws InterruptedException {
        // given
        Long userId = getUserId();

        Long amount = 100L;
        int threadCount = 10;
        Long existPoint = threadCount * amount;
        userPointTable.insertOrUpdate(userId, existPoint);
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    UseUserPointRequestDto request = UseUserPointRequestDto.createdBy(amount);
                    pointService.useUserPoint(userId, request);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        // then
        UserPoint userPoint = pointService.getUserPoint(userId);

        assertThat(userPoint)
            .extracting("id", "point")
            .contains(userId, existPoint - amount * threadCount);
    }

    @Test
    void 동일한_유저가_N번_포인트를_사용할때_잔액이_최소_포인트보다_작으면_이후_사용에_대해서_포인트_사용_불가_에러가_발생한다() throws InterruptedException {
        // given
        Long userId = getUserId();
        int threadCount = 10;
        int expectFailCount = 3;
        Long existPoint = 1000L;

        userPointTable.insertOrUpdate(userId, existPoint);


        Long amount = existPoint / (threadCount - expectFailCount);
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    UseUserPointRequestDto request = UseUserPointRequestDto.createdBy(amount);
                    pointService.useUserPoint(userId, request);
                    successCount.getAndIncrement();
                } catch (Exception ex) {
                    failCount.getAndIncrement();
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        // then
        UserPoint userPoint = pointService.getUserPoint(userId);

        assertThat(successCount.get()).isEqualTo(threadCount - expectFailCount);
        assertThat(failCount.get()).isEqualTo(expectFailCount);

        assertThat(userPoint)
            .extracting("id", "point")
            .contains(userId, existPoint - (amount * successCount.get()));
    }
}
