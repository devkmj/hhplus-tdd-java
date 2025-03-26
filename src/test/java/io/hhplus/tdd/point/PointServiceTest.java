package io.hhplus.tdd.point;

import io.hhplus.tdd.common.PointConstants;
import io.hhplus.tdd.common.PointErrorMessages;
import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
public class PointServiceTest {

    @Mock
    UserPointTable userPointTable;

    @Mock
    PointHistoryTable pointHistoryTable;

    @InjectMocks
    PointService pointService;

    private static final long TEST_USER_ID = 1L;

    @Test
    void 존재하지_않는_유저는_포인트0으로_조회된다() {
        // given
        Mockito.when(userPointTable.selectById(TEST_USER_ID))
                .thenReturn(UserPoint.empty(TEST_USER_ID));

        // when
        UserPoint result = pointService.getPoint(TEST_USER_ID);

        // then
        assertThat(result.id()).isEqualTo(TEST_USER_ID);
        assertThat(result.point()).isEqualTo(0L);
    }

    @Test
    void 충전_금액이_최소_충전_금액_이하면_예외가_발생한다() {
        // given
        long invalidAmount = PointConstants.MIN_CHARGE_AMOUNT;

        // when & then
        assertThatThrownBy(() -> pointService.charge(TEST_USER_ID, invalidAmount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(PointErrorMessages.AMOUNT_MUST_BE_POSITIVE.message(PointConstants.MIN_CHARGE_AMOUNT));
    }

    @Test
    void 포인트를_정상적으로_충전한다() {
        // given
        long amount = 1000L;
        long initialPoint = 500L;

        UserPoint before = new UserPoint(TEST_USER_ID, initialPoint, System.currentTimeMillis());
        UserPoint after = new UserPoint(TEST_USER_ID, initialPoint + amount, System.currentTimeMillis());

        Mockito.when(userPointTable.selectById(TEST_USER_ID)).thenReturn(before);
        Mockito.when(userPointTable.insertOrUpdate(TEST_USER_ID, initialPoint + amount)).thenReturn(after);

        // when
        UserPoint result = pointService.charge(TEST_USER_ID, amount);

        // then
        assertThat(result.point()).isEqualTo(1500L);

        Mockito.verify(pointHistoryTable).insert(
                eq(TEST_USER_ID), eq(amount), eq(TransactionType.CHARGE), anyLong()
        );
    }

    @Test
    void 최대_포인트_초과시_예외가_발생한다() {
        // given
        long amount = 60_000L;
        UserPoint before = new UserPoint(TEST_USER_ID, PointConstants.MAX_POINT , System.currentTimeMillis());

        Mockito.when(userPointTable.selectById(TEST_USER_ID)).thenReturn(before);

        // when & then
        assertThatThrownBy(() -> pointService.charge(TEST_USER_ID, amount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(PointErrorMessages.MAX_POINT_EXCEEDED.message(PointConstants.MAX_POINT));
    }

    @Test
    void 사용_금액이_최소_사용_금엑_이하면_예외가_발생한다() {
        // given
        long payAmount = PointConstants.MIN_USE_AMOUNT;

        // when & then
        assertThatThrownBy(() -> pointService.use(TEST_USER_ID, payAmount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(PointErrorMessages.USE_AMOUNT_MUST_BE_POSITIVE.message(PointConstants.MIN_USE_AMOUNT));

    }

    @Test
    void 포인트가_부족하면_예외가_발생한다(){
        // given
        long amount = 1000L;
        UserPoint current= new UserPoint(TEST_USER_ID, 500L, System.currentTimeMillis());

        Mockito.when(userPointTable.selectById(TEST_USER_ID)).thenReturn(current);

        // when & then
        assertThatThrownBy(() -> pointService.use(TEST_USER_ID, amount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(PointErrorMessages.INSUFFICIENT_POINT.message(current.point()));
    }

    @Test
    void 포인트를_정상적으로_사용한다() {
        // given
        long amount = 450L;
        long initial = 680L;

        UserPoint current = new UserPoint(TEST_USER_ID, initial, System.currentTimeMillis());
        UserPoint after = new UserPoint(TEST_USER_ID, initial - amount, System.currentTimeMillis() + amount);

        Mockito.when(userPointTable.selectById(TEST_USER_ID)).thenReturn(current);
        Mockito.when(userPointTable.insertOrUpdate(TEST_USER_ID, initial - amount)).thenReturn(after);

        // when
        UserPoint result = pointService.use(TEST_USER_ID, amount);

        // then
        assertThat(result.point()).isEqualTo(230L);

        Mockito.verify(pointHistoryTable).insert(
                eq(TEST_USER_ID), eq(amount), eq(TransactionType.USE), anyLong()
        );
    }

    @Test
    void 포인트_내역이_없으면_빈_리스트를_반환한다() {
        // given
        Mockito.when(pointHistoryTable.selectAllByUserId(TEST_USER_ID)).thenReturn(List.of());

        // when
        List<PointHistory> result = pointService.getHistories(TEST_USER_ID);

        //then
        assertThat(result).isEmpty();
    }

    @Test
    void 포인트_내역이_존재하면_리스트를_반환한다() {
        // given
        List<PointHistory> histories = List.of(
                new PointHistory(1, TEST_USER_ID, 1000, TransactionType.CHARGE, System.currentTimeMillis()),
                new PointHistory(2, TEST_USER_ID, 500, TransactionType.USE, System.currentTimeMillis())
        );

        Mockito.when(pointHistoryTable.selectAllByUserId(TEST_USER_ID)).thenReturn(histories);

        // when
        List<PointHistory> result = pointService.getHistories(TEST_USER_ID);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).type()).isEqualTo(TransactionType.CHARGE);
        assertThat(result.get(1).type()).isEqualTo(TransactionType.USE);
    }
}
