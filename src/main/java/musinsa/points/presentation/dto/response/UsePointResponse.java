package musinsa.points.presentation.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * 포인트 사용 결과 응답 DTO (Record 버전)
 *
 * @param useId       포인트 사용 트랜잭션 ID
 * @param memberSeq   회원 식별자
 * @param orderNo     주문번호
 * @param usedAmount  사용된 총 포인트 금액
 * @param status      사용 상태 (USED, PARTIAL_CANCELLED, CANCELLED)
 * @param usedAt      사용 시각
 * @param details     각 적립 로트별 차감 상세 내역
 */
public record UsePointResponse(
        UUID useId,
        Long memberSeq,
        String orderNo,
        Long usedAmount,
        String status,
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
            String grantType,
            Instant expiresAt
    ) {}
}
