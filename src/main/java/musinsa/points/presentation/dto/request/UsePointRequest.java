package musinsa.points.presentation.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 주문에서 포인트를 사용하는 요청 DTO
 * - memberSeq: 대상 회원 시퀀스 (필수)
 * - orderNo  : 주문번호, 중복 사용 방지를 위해 64자 제한 (필수)
 * - useAmount: 사용 포인트 금액(원), 1 이상 (필수)
 * - reason   : 사용 사유(선택)
 */
public record UsePointRequest(

        @NotNull(message = "memberSeq는 필수입니다.")
        Long memberSeq,

        @NotBlank(message = "orderNo는 필수입니다.")
        @Size(max = 64, message = "orderNo는 최대 64자입니다.")
        String orderNo,

        @NotNull(message = "useAmount는 필수입니다.")
        @Min(value = 1, message = "useAmount는 1 이상이어야 합니다.")
        Long useAmount,

        @Size(max = 255, message = "reason은 최대 255자입니다.")
        String reason
) {}
