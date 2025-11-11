package musinsa.points.application.dto.result;

import lombok.Builder;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import musinsa.points.domain.enums.UseStatus;

@Builder
public record CancelUsedPointResult(
        UUID cancelId,
        UUID useId,
        Long memberSeq,
        Long cancelAmount,
        Long regrantNeededAmount,
        UseStatus status,
        String reason,
        Instant cancelledAt,
        List<CancelDetail> CancelDetails
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
