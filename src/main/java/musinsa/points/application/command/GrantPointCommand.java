package musinsa.points.application.command;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import musinsa.points.common.exception.BusinessException;
import musinsa.points.common.exception.ErrorCode;
import musinsa.points.domain.enums.GrantType;

import java.time.Instant;

/**
 * ✅ 포인트 적립 명령(Command)
 * Controller → Application(Service) 계층으로 전달되는 불변 명령 객체.
 *
 * 엔터티(PointGrant)와 1:1 필드 호환을 지향하되, 엔터티에 직접 의존하지 않고
 * 필요한 최소한의 도메인 타입(GrantType)만 참조한다.
 */
@Getter
@ToString
@Builder(toBuilder = true)
public final class GrantPointCommand {

    /** 적립 대상 회원 식별자 (FK: members.member_seq) */
    private final Long memberSeq;

    /** 적립 금액 (> 0) */
    private final Long amount;

    /** 적립 유형 (MANUAL | AUTO | REGRANT) */
    private final GrantType grantType;

    /** 만료 시각(UTC). null이면 서비스 레이어에서 정책 기본 일수로 계산하여 설정 */
    private final Instant expiresAt;

    /** 관리자 메모/비고 (nullable) */
    private final String note;

    /**
     * 기본 유효성 검사: 필수 필드 및 금액 규칙
     * (상한/만하, 만료 범위 등 정책 검증은 서비스에서 처리)
     */
    public void validate() {
        if (memberSeq == null) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "회원 식별자(memberSeq)는 필수 값입니다.");
        }
        if (amount == null || amount <= 0) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "적립 금액(amount)은 1 이상이어야 합니다.");
        }
        if (grantType == null) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "적립 유형(grantType)은 필수 값입니다.");
        }
    }

    /** 수기 지급 여부 */
    public boolean isManualGrant() {
        return grantType == GrantType.MANUAL;
    }

    /** 만료 시각을 커스텀으로 제공했는지 여부 */
    public boolean hasCustomExpiry() {
        return expiresAt != null;
    }

    /** 간결한 생성용 팩토리 */
    public static GrantPointCommand of(Long memberSeq,
                                       Long amount,
                                       GrantType grantType,
                                       Instant expiresAt,
                                       String note) {
        GrantPointCommand cmd = GrantPointCommand.builder()
                .memberSeq(memberSeq)
                .amount(amount)
                .grantType(grantType)
                .expiresAt(expiresAt)
                .note(note)
                .build();
        cmd.validate();
        return cmd;
    }
}
