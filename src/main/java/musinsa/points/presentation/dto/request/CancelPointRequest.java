package musinsa.points.presentation.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CancelPointRequest(

    @NotNull(message = "취소할 부여 ID는 필수입니다.")
    UUID grantId,

    @NotNull(message = "회원 식별자는 필수입니다.")
    Long memberSeq,

    @Size(max = 255, message = "취소 사유는 255자를 초과할 수 없습니다.")
    String reason
) {}
