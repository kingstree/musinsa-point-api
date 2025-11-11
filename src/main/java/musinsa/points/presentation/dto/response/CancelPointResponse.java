package musinsa.points.presentation.dto.response;

import java.time.Instant;
import java.util.UUID;

public record CancelPointResponse(
        UUID cancelId,
        Long memberSeq,
        String reason,
        Instant createdDate,
        Instant modifiedDate,
        String status
) {}
