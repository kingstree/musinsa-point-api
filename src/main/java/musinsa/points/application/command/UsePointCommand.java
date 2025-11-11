package musinsa.points.application.command;

import lombok.Builder;

/**
 * 포인트 사용 Command 객체
 * - 주문 결제 시 포인트 차감에 사용됨
 */
@Builder
public record UsePointCommand(
        Long memberSeq,     // 포인트를 사용할 회원 번호
        String orderNo,     // 주문 번호 (중복 불가)
        Long useAmount,     // 사용 금액
        Long createdBy      // 요청자 (감사 로그용)
) { }
