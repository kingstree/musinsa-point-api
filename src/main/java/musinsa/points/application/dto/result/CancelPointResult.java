package musinsa.points.application.dto.result;

import lombok.Builder;
import java.time.Instant;
import java.util.UUID;
import musinsa.points.domain.enums.PointStatus;

@Builder
public record CancelPointResult(
        UUID grantId,
        Long memberSeq,
        Long cancelAmount,
        PointStatus status,
        String reason,
        Instant cancelledAt
) { }
