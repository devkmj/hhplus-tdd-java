package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class PointServiceTest {

    @Mock
    UserPointTable userPointTable;

    @Mock
    PointHistoryTable pointHistoryTable;

    @InjectMocks
    PointService pointService;

    @Test
    void 존재하지_않는_유저는_포인트0으로_조회된다() {
        // given
        long userId = 1L;
        Mockito.when(userPointTable.selectById(userId))
                .thenReturn(UserPoint.empty(userId));

        // when
        UserPoint result = pointService.getPoint(userId);

        // then
        assertThat(result.id()).isEqualTo(userId);
        assertThat(result.point()).isEqualTo(0L);
    }

    @Test
    void 충전_금액이_0이하면_예외가_발생한다() {
        // given
        long userId = 1L;
        long invalidAmount = 0L;

        // when & then
        assertThatThrownBy(() -> pointService.charge(userId, invalidAmount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("충전 금액은 0보다 커야 합니다");
    }

    @Test
    void 포인트를_정상적으로_충전한다() {
        // given
        long userId = 1L;
        long amount = 1000L;
        long initialPoint = 500L;

        UserPoint before = new UserPoint(userId, initialPoint, System.currentTimeMillis());
        UserPoint after = new UserPoint(userId, initialPoint + amount, System.currentTimeMillis());

        Mockito.when(userPointTable.selectById(userId)).thenReturn(before);
        Mockito.when(userPointTable.insertOrUpdate(userId, initialPoint + amount)).thenReturn(after);

        // when
        UserPoint result = pointService.charge(userId, amount);

        // then
        assertThat(result.point()).isEqualTo(1500L);
    }

    @Test
    void 최대_포인트_초과시_예외가_발생한다() {
        long userId = 1L;
        long amount = 60_000L;
        UserPoint before = new UserPoint(userId, 80_000L , System.currentTimeMillis());

        Mockito.when(userPointTable.selectById(userId)).thenReturn(before);

        assertThatThrownBy(() -> pointService.charge(userId, amount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("최대 보유 포인트를 초과할 수 없습니다.");
    }

}
