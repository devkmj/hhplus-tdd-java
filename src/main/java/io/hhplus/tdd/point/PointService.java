package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.springframework.stereotype.Service;

@Service
public class PointService {

    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;

    private static final long MAX_POINT = 100_000L;     // 최대 보유 포인트

    public PointService(UserPointTable userPointTable, PointHistoryTable pointHistoryTable) {
        this.userPointTable = userPointTable;
        this.pointHistoryTable = pointHistoryTable;
    }

    /**
     * 포인트 조회
     * */
    public UserPoint getPoint(long userId) {
        return userPointTable.selectById(userId);
    }


    public UserPoint charge(long userId, long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("충전 금액은 0보다 커야 합니다");
        }
        // 1. 현재 포인트 조히
        UserPoint current = userPointTable.selectById(userId);

        // 2. 누적 충전
        long newAmount = current.point() + amount;

        if(newAmount > MAX_POINT) {
            throw new IllegalArgumentException("최대 보유 포인트를 초과할 수 없습니다.");
        }

        // 3. 포인트 테이블 업데이트
        UserPoint updated = userPointTable.insertOrUpdate(userId, newAmount);

        // 4. 충전 히스토리 기록
        pointHistoryTable.insert(userId, amount, TransactionType.CHARGE, System.currentTimeMillis());

        return updated;
    }
}
