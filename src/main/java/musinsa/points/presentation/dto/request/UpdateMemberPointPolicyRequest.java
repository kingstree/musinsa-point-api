package musinsa.points.presentation.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record UpdateMemberPointPolicyRequest(

        @NotNull(message = "회원 번호는 필수입니다.")
        Long memberSeq,

        @Min(value = 0, message = "보유 상한액은 0 이상이어야 합니다.")
        Long maxBalance

) {}
