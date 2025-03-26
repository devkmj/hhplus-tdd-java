package io.hhplus.tdd.common;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

@Component
public class TestDataInitializer {

    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;

    public TestDataInitializer(UserPointTable userPointTable, PointHistoryTable pointHistoryTable) {
        this.userPointTable = userPointTable;
        this.pointHistoryTable = pointHistoryTable;
    }

    public void resetAll() {
        resetUserPointTable();
        resetPointHistoryTable();
    }

    private void resetUserPointTable() {
        try {
            Field tableField = UserPointTable.class.getDeclaredField("table");
            tableField.setAccessible(true);
            Map<?, ?> map = (Map<?, ?>) tableField.get(userPointTable);
            map.clear();
        } catch (Exception e) {
            throw new RuntimeException("UserPointTable 초기화 실패", e);
        }
    }

    private void resetPointHistoryTable() {
        try {
            Field tableField = PointHistoryTable.class.getDeclaredField("table");
            tableField.setAccessible(true);
            Field cursorField = PointHistoryTable.class.getDeclaredField("cursor");
            cursorField.setAccessible(true);

            ((List<?>) tableField.get(pointHistoryTable)).clear();
            cursorField.setLong(pointHistoryTable, 1L);
        } catch (Exception e) {
            throw new RuntimeException("PointHistoryTable 초기화 실패", e);
        }
    }
}