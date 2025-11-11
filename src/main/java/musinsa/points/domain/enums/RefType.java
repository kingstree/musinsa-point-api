package musinsa.points.domain.enums;

import musinsa.points.common.exception.BusinessException;
import musinsa.points.common.exception.ErrorCode;

/**
 * 포인트 이력의 참조 타입을 나타내는 Enum
 * 어떤 엔티티(PointGrant, PointUse, PointCancel 등)에서 발생했는지를 구분한다.
 */
public enum RefType {
    GRANT,     // 포인트 적립
    USE,       // 포인트 사용
    CANCEL,    // 포인트 사용 취소
    EXPIRE;    // 포인트 만료

    public static RefType from(String name) {
        for (RefType type : values()) {
            if (type.name().equalsIgnoreCase(name)) {
                return type;
            }
        }
        throw new BusinessException(ErrorCode.INVALID_TYPE, "알 수 없는 참조 타입입니다: " + name);
    }
}
