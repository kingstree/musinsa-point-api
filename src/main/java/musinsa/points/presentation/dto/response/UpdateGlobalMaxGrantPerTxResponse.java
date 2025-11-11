package musinsa.points.presentation.dto.response;

import lombok.Builder;
import musinsa.points.domain.enums.PointPolicyType;


import java.time.Instant;
import java.util.UUID;

@Builder
public record UpdateGlobalMaxGrantPerTxResponse(
        UUID policyId,
        PointPolicyType pointPolicyType,
        Long value,
        Instant updatedAt
) {}
