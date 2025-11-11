package musinsa.points.application.dto.result;

import lombok.Builder;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import musinsa.points.domain.enums.GrantType;
import musinsa.points.domain.enums.UseStatus;
import musinsa.points.presentation.dto.response.UsePointResponse;

@Builder
public record UsePointResult(
        UUID useId,
        Long memberSeq,
        String orderNo,
        Long useAmount,
        UseStatus status,
        Instant usedAt,
        List<UseDetail> details
) {
    /**
     * 적립 로트별 사용 상세 내역
     *
     * @param grantId             사용된 적립 로트 ID
     * @param usedFromGrant       해당 로트에서 차감된 포인트 금액
     * @param remainingAfterUse   차감 후 잔여 포인트
     * @param grantType           적립 타입 (MANUAL / AUTO / REGRANT)
     * @param expiresAt           해당 로트의 만료일
     */
    public record UseDetail(
            UUID grantId,
            Long usedFromGrant,
            Long remainingAfterUse,
            GrantType grantType,
            Instant expiresAt
    ) {}
}
