package musinsa.points.presentation.dto.response;
import lombok.Builder;
import musinsa.points.application.dto.result.GrantPointResult;

import java.time.Instant;
import java.util.UUID;



/**
 * 주문 우선순위 응답 DTO (Presentation Layer)
 *
 * 특징:
 * - API 클라이언트 친화적 구조
 * - JSON 직렬화 최적화
 */
@Builder
public record GrantPointResponse(
        UUID grantId,
        Long memberSeq,
        Long grantedAmount,
        Long remainingAmount,
        Instant expiresAt,
        String grantType,
        String status,
        Instant createdDate
) {
    public static GrantPointResponse from(GrantPointResult r) {
        return GrantPointResponse.builder()
                .grantId(r.grantId())
                .memberSeq(r.memberSeq())
                .remainingAmount(r.remainingAmount())
                .expiresAt(r.expiresAt())
                .build();
    }
}
