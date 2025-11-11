package musinsa.points.domain.snapshot;

import musinsa.points.domain.enums.PointPolicyType;
import musinsa.points.domain.enums.PolicyScope;

import java.util.UUID;

public record PolicySnapshot(
        PolicyScope scope,
        PointPolicyType policyType,
        Long memberSeq,
        Long policyValue,
        UUID policyId
) {}
