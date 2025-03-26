package io.hhplus.tdd.point;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.hhplus.tdd.common.PointConstants;
import io.hhplus.tdd.common.PointErrorMessages;
import io.hhplus.tdd.common.TestDataInitializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class PointIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    TestDataInitializer initializer;

    @BeforeEach
    void resetTestData() {
        initializer.resetAll(); // 테스트 실행 전에 상태 초기화
    }


    @Autowired
    ObjectMapper objectMapper;

    private static final long TEST_USER_ID = 999L;
    private static final long TEST_AMOUNT1 = 500L;
    private static final long TEST_AMOUNT2 = 700L;

    @Test
    void 존재하지_않는_유저의_포인트는_0이다() throws Exception {
        // when & then
        mvc.perform(get("/point/{id}", TEST_USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TEST_USER_ID))
                .andExpect(jsonPath("$.point").value(0));
    }

    @Test
    void 포인트_충전_금액이_최소_충전_금액_이하면_예외가_발생한다() throws Exception {
        // when & then
        mvc.perform(patch("/point/{id}/charge", TEST_USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.valueOf(PointConstants.MIN_CHARGE_AMOUNT)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                    .value(PointErrorMessages.AMOUNT_MUST_BE_POSITIVE.message(PointConstants.MIN_CHARGE_AMOUNT)));
    }

    @Test
    void 포인트를_정상적으로_충전한다() throws Exception {
        // when & then
        mvc.perform(patch("/point/{id}/charge", TEST_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.valueOf(TEST_AMOUNT1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TEST_USER_ID));

    }

    @Test
    void 포인트를_두_번_충전하면_누적_포인트가_정상이다() throws Exception {
        // given - 첫 번째 충전

        // given - 두 번째 충전

        // when & then - 최종 포인트 조회 결과가 누적된 값인지 확인

    }

    @Test
    void 충전_후_최대_포인트_초과시_예외가_발생한다() throws Exception {
        // given - 충전 전 포인트
        mvc.perform(patch("/point/{id}/charge", TEST_USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.valueOf(TEST_AMOUNT1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TEST_USER_ID))
                .andExpect(jsonPath("$.point").value(TEST_AMOUNT1));

        // given - 최대 포인트 이상 충전 시도
        mvc.perform(patch("/point/{id}/charge", TEST_USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.valueOf(PointConstants.MAX_POINT)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                    .value(PointErrorMessages.MAX_POINT_EXCEEDED.message(PointConstants.MAX_POINT)));
    }

    @Test
    void 포인트_사용_금액이_최소_사용_금액_이하면_예외가_발생한다() throws Exception {
        // when & then
        mvc.perform(patch("/point/{id}/use", TEST_USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.valueOf(PointConstants.MIN_USE_AMOUNT)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                    .value(PointErrorMessages.USE_AMOUNT_MUST_BE_POSITIVE.message(PointConstants.MIN_USE_AMOUNT)));
    }

    @Test
    void 포인트를_정상적으로_사용한다() throws Exception {
        // given - 포인트 충전
        long chargeAmount = 3000L;
        long useAmount = 500L;
        mvc.perform(patch("/point/{id}/charge", TEST_USER_ID)
        .contentType(MediaType.APPLICATION_JSON)
                .content(String.valueOf(chargeAmount)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TEST_USER_ID))
                .andExpect(jsonPath("$.point").value(chargeAmount))
                .andExpect(jsonPath("$.updateMillis").isNumber());

        // when & then - 포인트 사용
        mvc.perform(patch("/point/{id}/use", TEST_USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.valueOf(TEST_AMOUNT1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TEST_USER_ID))
                .andExpect(jsonPath("$.point").isNumber())
                .andExpect(jsonPath("$.updateMillis").isNumber());

        // when & then - 사용 후 잔액 확인
        mvc.perform(get("/point/{id}", TEST_USER_ID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TEST_USER_ID))
                .andExpect(jsonPath("$.point").value(chargeAmount - useAmount));
    }

    @Test
    void 포인트가_부족하면_예외가_발생한다() throws Exception {
        // given - 포인트 충전
        mvc.perform(patch("/point/{id}/charge", TEST_USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.valueOf(TEST_AMOUNT1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TEST_USER_ID))
                .andExpect(jsonPath("$.point").value(TEST_AMOUNT1))
                .andExpect(jsonPath("$.updateMillis").isNumber());

        // when & then - 보유 포인트보다 많은 3100포인트 사용 시도
        mvc.perform(patch("/point/{id}/use", TEST_USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.valueOf(TEST_AMOUNT2)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                    .value(PointErrorMessages.INSUFFICIENT_POINT.message(TEST_AMOUNT1)));
    }

    @Test
    void 포인트_내역을_정상적으로_조회한다() throws Exception {
        // given - 포인트 충전 & 사용
        mvc.perform(patch("/point/{id}/charge", TEST_USER_ID)
        .contentType(MediaType.APPLICATION_JSON)
                .content(String.valueOf(TEST_AMOUNT2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TEST_USER_ID))
                .andExpect(jsonPath("$.point").value(TEST_AMOUNT2))
                .andExpect(jsonPath("$.updateMillis").isNumber());

        mvc.perform(patch("/point/{id}/use", TEST_USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.valueOf(TEST_AMOUNT1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TEST_USER_ID))
                .andExpect(jsonPath("$.point").value(TEST_AMOUNT2 - TEST_AMOUNT1))
                .andExpect(jsonPath("$.updateMillis").isNumber());

        // when & then - 충전 & 사용된 내역 조회
        mvc.perform(get("/point/{id}/histories", TEST_USER_ID)
        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].userId").value(TEST_USER_ID))
                .andExpect(jsonPath("$[0].type").value(TransactionType.CHARGE.toString()))
                .andExpect(jsonPath("$[0].amount").value(TEST_AMOUNT2))
                .andExpect(jsonPath("$[1].userId").value(TEST_USER_ID))
                .andExpect(jsonPath("$[1].type").value(TransactionType.USE.toString()))
                .andExpect(jsonPath("$[1].amount").value(TEST_AMOUNT1));

    }
}
