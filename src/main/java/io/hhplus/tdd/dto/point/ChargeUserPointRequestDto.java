package io.hhplus.tdd.dto.point;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ChargeUserPointRequestDto {
    @Positive(message = "충전 금액은 양수여야 합니다.")
    private Long amount;
}
