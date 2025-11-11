package musinsa.points.presentation.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * 포인트 사용 취소 응답 DTO
 */
public record CancelUsedPointResponse(
        UUID useId,
        Long memberSeq,
        long cancelAmount,
        long regrantNeededAmount,
        Instant cancelledAt,
        List<CancelDetail> details
) {
    /**
     * 취소 상세 내역 (각 적립 로트 기준 복원 금액 등)
     */
    public record CancelDetail(
            UUID grantId,
            long restoredAmount,
            boolean regranted,
            Instant regrantExpiresAt
    ) {}
}
