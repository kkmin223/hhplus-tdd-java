package io.hhplus.tdd.point;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.hhplus.tdd.dto.point.ChargeUserPointRequestDto;
import io.hhplus.tdd.dto.point.UseUserPointRequestDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.core.StringContains.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PointController.class)
class PointControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockBean
    private PointService pointService;

    @Test
    void 양수인_ID로_조회하면_포인트_조회를_성공한다() throws Exception {
        // given
        Long id = 1L;

        // when & then
        mockMvc.perform(get("/point/{id}", id))
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    void 양수가아닌_ID로_포인트를_조회하면_유저ID_검증_에러가_발생한다() throws Exception {
        // given
        Long id = 0L;

        // when & then
        mockMvc.perform(get("/point/{id}", id))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value(containsString("유저 Id는 양수여야 합니다.")));
    }

    @Test
    void 양수인_ID로_조회하면_포인트_내역_조회를_성공한다() throws Exception {
        // given
        Long id = 1L;

        // when & then
        mockMvc.perform(get("/point/{id}/histories", id))
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    void 양수가아닌_ID로_포인트내역을_조회하면_유저ID_검증_에러가_발생한다() throws Exception {
        // given
        Long id = 0L;

        // when & then
        mockMvc.perform(
                get("/point/{id}/histories", id)
            )
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value(containsString("유저 Id는 양수여야 합니다.")));
    }

    @Test
    void 양수인_ID와_포인트를_충전하면_충전을_성공한다() throws Exception {
        // given
        Long id = 1L;
        ChargeUserPointRequestDto requestDto = new ChargeUserPointRequestDto(100L);

        //when & then
        mockMvc.perform(
                patch("/point/{id}/charge", id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto))
            )
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    void 양수가_아닌_ID로_포인트를_충전하면_유저ID_검증_에러가_발생한다() throws Exception {
        // given
        Long id = 0L;
        ChargeUserPointRequestDto requestDto = new ChargeUserPointRequestDto(100L);

        //when & then
        mockMvc.perform(
                patch("/point/{id}/charge", id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto))
            )
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value(containsString("유저 Id는 양수여야 합니다.")));
    }

    @Test
    void 양수가_아닌_포인트를_충전하면_amount_검증_에러가_발생한다() throws Exception {
        // given
        Long id = 1L;
        ChargeUserPointRequestDto requestDto = new ChargeUserPointRequestDto(0L);

        //when & then
        mockMvc.perform(
                patch("/point/{id}/charge", id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto))
            )
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value(containsString("충전 금액은 양수여야 합니다.")));
    }

    @Test
    void 양수인_ID와_포인트를_사용하면_사용을_성공한다() throws Exception {
        // given
        Long id = 1L;
        UseUserPointRequestDto requestDto = UseUserPointRequestDto.createdBy(100L);

        //when & then
        mockMvc.perform(
                patch("/point/{id}/use", id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto))
            )
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    void 양수가_아닌_ID로_포인트를_사용하면_유저ID_검증_에러가_발생한다() throws Exception {
        // given
        Long id = 0L;
        UseUserPointRequestDto requestDto = UseUserPointRequestDto.createdBy(100L);

        //when & then
        mockMvc.perform(
                patch("/point/{id}/use", id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto))
            )
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value(containsString("유저 Id는 양수여야 합니다.")));
    }

    @Test
    void 양수가_아닌_포인트를_사용하면_amount_검증_에러가_발생한다() throws Exception {
        // given
        Long id = 1L;
        UseUserPointRequestDto requestDto = UseUserPointRequestDto.createdBy(0L);

        //when & then
        mockMvc.perform(
                patch("/point/{id}/use", id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto))
            )
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value(containsString("사용 금액은 양수여야 합니다.")));
    }

}
