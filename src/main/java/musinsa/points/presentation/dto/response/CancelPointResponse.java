package musinsa.points.presentation.dto.response;

import lombok.Builder;
import musinsa.points.application.dto.result.CancelPointResult;

import java.time.Instant;
import java.util.UUID;

@Builder
public record CancelPointResponse(
        UUID cancelId,
        Long memberSeq,
        String reason,
        Instant createdDate,
        Instant modifiedDate,
        String status
) {
    public static CancelPointResponse from(CancelPointResult r) {
        return CancelPointResponse.builder()
                .memberSeq(r.memberSeq())
                .reason(r.reason())
                .build();
    }
}
