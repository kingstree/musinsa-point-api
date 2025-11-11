package musinsa.points.presentation.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * 포인트 사용 취소 요청 DTO
 */
public record CancelUsedPointRequest(
        @NotNull(message = "취소할 사용 ID는 필수입니다.")
        UUID useId,

        @Min(value = 1, message = "취소 금액은 1 이상이어야 합니다.")
        long cancelAmount,

        @NotBlank(message = "취소 사유를 입력해주세요.")
        String reason
) {
}
