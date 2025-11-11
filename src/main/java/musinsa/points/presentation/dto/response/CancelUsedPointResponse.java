package musinsa.points.presentation.dto.response;

import lombok.Builder;
import musinsa.points.application.dto.result.CancelUsedPointResult;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * 포인트 사용 취소 응답 DTO
 */
@Builder
public record CancelUsedPointResponse(
        UUID useId,
        Long memberSeq,
        long cancelAmount,
        long regrantNeededAmount,
        Instant cancelledAt,
        List<CancelDetail> details
) {
    public static CancelUsedPointResponse from(CancelUsedPointResult r) {
        return CancelUsedPointResponse.builder()
                .useId(r.useId())
                .memberSeq(r.memberSeq())
                .cancelAmount(r.cancelAmount())
                .regrantNeededAmount(r.regrantNeededAmount())
                .cancelledAt(r.cancelledAt())
                .build();
    }
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
