package io.hhplus.tdd.dto.point;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class UseUserPointRequestDto {
    private Long amount;

    public static UseUserPointRequestDto createdBy(Long amount) {
        return UseUserPointRequestDto.builder()
            .amount(amount)
            .build();
    }

}
