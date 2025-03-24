package io.hhplus.tdd.point;

import io.hhplus.tdd.common.PointErrorMessages;
import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PointService {

    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;

    private static final long MAX_POINT = 100_000L; // 최대 보유 포인트

    public PointService(UserPointTable userPointTable, PointHistoryTable pointHistoryTable) {
        this.userPointTable = userPointTable;
        this.pointHistoryTable = pointHistoryTable;
    }

    /**
     * 포인트 조회
     */
    public UserPoint getPoint(long userId) {
        return userPointTable.selectById(userId);
    }

    /**
     * 포인트 충전
     */
    public UserPoint charge(long userId, long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException(PointErrorMessages.AMOUNT_MUST_BE_POSITIVE);
        }

        UserPoint current = findUserPoint(userId);
        long newAmount = current.point() + amount;

        if(newAmount > MAX_POINT) {
            throw new IllegalArgumentException(PointErrorMessages.MAX_POINT_EXCEEDED);
        }

        UserPoint updated = userPointTable.insertOrUpdate(userId, newAmount);
        pointHistoryTable.insert(userId, amount, TransactionType.CHARGE, now());

        return updated;
    }

    /**
     * 포인트 사용
     */
    public UserPoint use(long userId, long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException(PointErrorMessages.USE_AMOUNT_MUST_BE_POSITIVE);
        }

        UserPoint current = findUserPoint(userId);
        long newAmount = current.point() - amount;

        if(newAmount < 0) {
            throw new IllegalArgumentException(PointErrorMessages.INSUFFICIENT_POINT);
        }

        UserPoint updated = userPointTable.insertOrUpdate(userId, newAmount);
        pointHistoryTable.insert(userId, amount, TransactionType.USE, now());
        return updated;
    }

    /**
     * 포인트 내역
     */
    public List<PointHistory> getHistories(long userId) {
        return pointHistoryTable.selectAllByUserId(userId);
    }

    private UserPoint findUserPoint(long userId) {
        return userPointTable.selectById(userId);
    }

    private long now() {
        return System.currentTimeMillis();
    }
}
