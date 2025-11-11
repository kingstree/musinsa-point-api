package musinsa.points.presentation.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** 글로벌 정책: 1회 최대 적립 한도 수정 요청 */
public record UpdateGlobalMaxGrantPerTxRequest(
        @NotNull(message = "maxGrantPerTx 값은 필수입니다.")
        @Min(value = 1, message = "1회 최대 적립 한도는 1 이상이어야 합니다.")
        Long maxGrantPerTx
) {}
