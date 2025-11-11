package musinsa.points.application.command;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

/**
 * Command: 포인트 부여 취소 요청 정보를 캡슐화
 * - 외부 DTO → Command 변환 후 서비스/도메인 계층에서 사용
 */
@Getter
@Builder
public class CancelPointCommand {

    private final UUID grantId;               // 취소 대상 grant 식별자
    private final UUID useId;                 // (옵션) 사용 이력 참조 시
    private final Long memberSeq;             // 요청자 (또는 대상 회원)
    private final Long cancelAmount;          // 취소할 금액
    private final Long regrantNeededAmount;   // 재적립 필요 금액 (일부 취소 시)
    private final String reason;              // 취소 사유
    private final Instant requestedAt;        // 취소 요청 시각

    public static CancelPointCommand of(
            UUID grantId,
            UUID useId,
            Long memberSeq,
            Long cancelAmount,
            Long regrantNeededAmount,
            String reason
    ) {
        return CancelPointCommand.builder()
                .grantId(grantId)
                .useId(useId)
                .memberSeq(memberSeq)
                .cancelAmount(cancelAmount)
                .regrantNeededAmount(regrantNeededAmount != null ? regrantNeededAmount : 0L)
                .reason(reason)
                .requestedAt(Instant.now())
                .build();
    }
}
