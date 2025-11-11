package musinsa.points.application.dto.command;

import lombok.Builder;

import java.util.UUID;

/**
 * 포인트 사용 취소 Command
 * 서비스 계층에서 사용되는 불변 명령 객체
 */
@Builder
public record CancelUsedPointCommand(
        UUID useId,          // 취소할 사용 ID
        long cancelAmount,   // 취소 금액
        String reason,       // 취소 사유
        Long memberSeq,      // 요청자 회원번호
        Long createdBy       // 등록자 (관리자 또는 시스템)
) {
}
