package io.hhplus.tdd.dto.point;

import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class UseUserPointRequestDto {
    @Positive(message = "사용 금액은 양수여야 합니다.")
    private Long amount;

    public static UseUserPointRequestDto createdBy(Long amount) {
        return UseUserPointRequestDto.builder()
            .amount(amount)
            .build();
    }

}
