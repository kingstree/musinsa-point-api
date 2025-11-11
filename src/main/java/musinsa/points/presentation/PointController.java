package musinsa.points.presentation;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import musinsa.points.application.command.GrantPointCommand;
import musinsa.points.application.command.UsePointCommand;
import musinsa.points.application.command.CancelUsedPointCommand;
import musinsa.points.application.service.PointService;
import musinsa.points.common.exception.ErrorCode;
import musinsa.points.common.response.ApiResult;
import musinsa.points.common.response.ResponseUtil;
import musinsa.points.common.security.service.CustomUserDetailsInfo;
import musinsa.points.domain.enums.GrantType;
import musinsa.points.domain.enums.UserRole;
import musinsa.points.presentation.dto.request.CancelPointRequest;
import musinsa.points.presentation.dto.request.GrantPointRequest;
import musinsa.points.presentation.dto.request.UsePointRequest;
import musinsa.points.presentation.dto.request.CancelUsedPointRequest;
import musinsa.points.presentation.dto.response.CancelPointResponse;
import musinsa.points.presentation.dto.response.GrantPointResponse;
import musinsa.points.presentation.dto.response.UsePointResponse;
import musinsa.points.presentation.dto.response.CancelUsedPointResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/points")
@RequiredArgsConstructor
@Validated
public class PointController {

    private final PointService pointService;

    /**
     * 포인트 적립 (MANUAL/AUTO/REGRANT)
     * 1) 요청 DTO → Command 매핑
     * 2) 서비스에서 정책 검증/만료일 계산 후 적립 수행
     */
    @Operation(summary = "포인트 적립")
    @PostMapping("/grant")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResult<GrantPointResponse>> grantPoints(
            @AuthenticationPrincipal CustomUserDetailsInfo userDetailsInfo,
            @Valid @RequestBody GrantPointRequest request
    ) {
        Long currentMemberSeq = userDetailsInfo.getMemberSeq();
        UserRole currentRole = userDetailsInfo.getRole();

        // 권한 검증
        if (currentRole != UserRole.MANAGER && !currentMemberSeq.equals(request.memberSeq())) {
            return ResponseUtil.failure(ErrorCode.VALIDATION_FAILED, ErrorCode.VALIDATION_FAILED.getMessage() + "회원 생성 실패");
        }

        GrantType setGrantType = request.grantType();
        if (userDetailsInfo.getRole() == UserRole.MANAGER) {
            if(request.grantType() == GrantType.AUTO){// 테스트 용
                setGrantType = GrantType.AUTO;
            }else {
                setGrantType = GrantType.MANUAL;
            }
        }
        if (userDetailsInfo.getRole() != UserRole.MANAGER && setGrantType != GrantType.REGRANT  ) {
            setGrantType = GrantType.AUTO;
        }

        /**
         * * 1.취소 로직을 확인하는 절차가 있으면 좋음
         * * 2.재 부여는 이벤트 발생 시 이벤트 핸들러로 요청이 들어오면 좋음
        */
        GrantPointCommand command = GrantPointCommand.builder()
                .memberSeq(request.memberSeq())
                .amount(request.amount())
                .grantType(setGrantType)
                .expiresAt(request.expiresAt())
                .note(request.note())
                .build();
        command.validate();

        GrantPointResponse response = pointService.grantPoints(command);
        return ResponseUtil.success("성공", response);
    }


    /**
     * 포인트 부여 취소 (Grant Cancel)
     * - MANAGER: 누구의 부여든 취소 가능
     * - 일반회원: 자신의 부여만 취소 가능
     */
    @Operation(summary = "포인트 부여 취소")
    @PostMapping("/grant/cancel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResult<CancelPointResponse>> cancelGrant(
            @AuthenticationPrincipal CustomUserDetailsInfo user,
            @Valid @RequestBody CancelPointRequest request
    ) {
        Long currentMemberSeq = user.getMemberSeq();
        UserRole role = user.getRole();

        // 권한 검증: 매니저만 타인 건 취소 허용
        if (role != UserRole.MANAGER && !currentMemberSeq.equals(request.memberSeq())) {
            return ResponseUtil.failure(
                    ErrorCode.VALIDATION_FAILED, // 프로젝트 공통 코드를 따름 (기존 PointController와 동일 패턴)
                    "권한이 없습니다: 다른 회원의 포인트 부여를 취소할 수 없습니다."
            );
        }

        CancelPointResponse response = pointService.cancelGrant(request);
        return ResponseUtil.success("성공", response);
    }

    /**
     * 포인트 사용
     * - 일반 회원은 자신의 주문에 대해서만 사용 가능
     * - 매니저는 대리 사용 허용(운영툴 등)
     */
    @Operation(summary = "포인트 사용")
    @PostMapping("/use")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResult<UsePointResponse>> usePoints(
            @AuthenticationPrincipal CustomUserDetailsInfo user,
            @Valid @RequestBody UsePointRequest request
    ) {
        Long currentMemberSeq = user.getMemberSeq();
        UserRole role = user.getRole();

        // 권한 검증: 매니저만 타인 건 대리 사용 허용
        if (role != UserRole.MANAGER && !currentMemberSeq.equals(request.memberSeq())) {
            return ResponseUtil.failure(
                    ErrorCode.VALIDATION_FAILED,
                    "권한이 없습니다: 다른 회원의 포인트를 사용할 수 없습니다."
            );
        }

        UsePointCommand command = UsePointCommand.builder()
                .memberSeq(request.memberSeq())
                .orderNo(request.orderNo())
                .useAmount(request.useAmount())
                .createdBy(currentMemberSeq)
                .build();

        UsePointResponse response = pointService.usePoints(command);
        return ResponseUtil.success("성공", response);
    }

    /**
     * 포인트 사용 취소
     * - MANAGER: 누구의 사용건이든 취소 가능
     * - 일반회원: 자신의 사용건만 취소 가능 (소유자 검증은 서비스에서 방어적으로 재확인)
     */
    @Operation(summary = "포인트 사용 취소")
    @PostMapping("/use/cancel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResult<CancelUsedPointResponse>> cancelUsedPoints(
            @AuthenticationPrincipal CustomUserDetailsInfo user,
            @Valid @RequestBody CancelUsedPointRequest request
    ) {
        Long currentMemberSeq = user.getMemberSeq();

        // Command 구성 (서비스에서 소유자 검증 및 권한 재확인)
        CancelUsedPointCommand command = CancelUsedPointCommand.builder()
                .useId(request.useId())
                .cancelAmount(request.cancelAmount())
                .reason(request.reason())
                .memberSeq(currentMemberSeq)
                .createdBy(currentMemberSeq)
                .build();

        CancelUsedPointResponse response = pointService.cancelUsedPoints(command);
        return ResponseUtil.success("성공", response);
    }




}
