package musinsa.points.application.dto.result;

import lombok.Builder;
import java.time.Instant;
import java.util.UUID;
import musinsa.points.domain.enums.GrantType;
import musinsa.points.domain.enums.PointStatus;

@Builder
public record GrantPointResult(
        UUID grantId,
        Long memberSeq,
        Long amount,
        Long remainingAmount,
        GrantType grantType,
        PointStatus status,
        Instant expiresAt,
        Instant createdAt
) { }
