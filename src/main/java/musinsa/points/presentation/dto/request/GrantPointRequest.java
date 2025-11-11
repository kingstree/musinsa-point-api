package musinsa.points.presentation.dto.request;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import musinsa.points.domain.enums.GrantType;

import java.time.Instant;

/**
 * 포인트 적립 요청 DTO
 */
public record GrantPointRequest(

        @Schema(description = "대상 회원 시퀀스", example = "1")
        @NotNull(message = "memberSeq는 필수입니다.")
        Long memberSeq,

        @Schema(description = "적립 금액 (1 이상, 정책 상한 이하)", example = "5000")
        @Positive(message = "적립 금액은 1 이상이어야 합니다.")
        long amount,

        @Schema(description = "설정 만료일 (선택, null이면 기본 365일)", example = "2025-12-31T23:59:59Z")
        Instant expiresAt,

        @Schema(description = "적립 타입 (MANUAL / AUTO / REGRANT)", example = "MANUAL")
        @NotNull(message = "grantType은 필수입니다.")
        GrantType grantType,

        @Schema(description = "비고 / 관리자 메모", example = "이벤트 보상 지급")
        @Size(max = 255, message = "비고는 255자를 초과할 수 없습니다.")
        String note


) {}
