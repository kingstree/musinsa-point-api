package musinsa.points.presentation.dto.response;

import lombok.Builder;

@Builder
public record UpdateMemberPointPolicyResponse(
        Long memberSeq,
        Long maxBalance,
        boolean cacheRefreshed,
        String message
) {}
