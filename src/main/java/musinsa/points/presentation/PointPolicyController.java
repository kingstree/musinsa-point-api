package musinsa.points.presentation;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import musinsa.points.application.service.PointPolicyService;
import musinsa.points.common.exception.ErrorCode;
import musinsa.points.common.response.ApiResult;
import musinsa.points.common.response.ResponseUtil;
import musinsa.points.common.security.service.CustomUserDetailsInfo;
import musinsa.points.presentation.dto.request.UpdateMemberPointPolicyRequest;
import musinsa.points.presentation.dto.response.UpdateMemberPointPolicyResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/points/policies")
@RequiredArgsConstructor
@Validated
public class PointPolicyController {

    private final PointPolicyService pointPolicyService;

    /**
     * 멤버별 무료포인트 보유 상한액 정책 업데이트(+ 캐시 즉시 반영)
     * - MANAGER: 누구의 정책이든 변경 가능
     * - 일반회원: 자신의 것만 변경 가능(원하지 않으면 권한을 MANAGER로만 제한하면 됨)
     */
    @Operation(summary = "멤버별 보유 상한액 정책 수정(캐시 즉시 반영)")
    @PutMapping("/members/{memberSeq}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResult<UpdateMemberPointPolicyResponse>> updateMemberPolicy(
            @AuthenticationPrincipal CustomUserDetailsInfo user,
            @PathVariable("memberSeq") Long memberSeq,
            @Valid @RequestBody UpdateMemberPointPolicyRequest request
    ) {

        // 요청 바디의 memberSeq가 path와 다르면 방어
        if (request.memberSeq() == null || !request.memberSeq().equals(memberSeq)) {
            return ResponseUtil.failure(
                    ErrorCode.VALIDATION_FAILED,
                    "요청 경로의 memberSeq와 본문 memberSeq가 일치하지 않습니다."
            );
        }

        // 서비스: DB 업데이트 → 캐시(pointPolicy) 갱신까지 수행
        UpdateMemberPointPolicyResponse response =
                pointPolicyService.updateMemberMaxBalanceAndRefreshCache(
                        memberSeq,
                        request.maxBalance()
                );

        return ResponseUtil.success("정책이 업데이트되었고 캐시가 새로고침되었습니다. 대상 : " + memberSeq, response);
    }
}
