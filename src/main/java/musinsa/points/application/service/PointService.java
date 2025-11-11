package musinsa.points.application.service;

import musinsa.points.application.command.CancelUsedPointCommand;
import musinsa.points.application.command.UsePointCommand;
import musinsa.points.domain.entity.*;
import musinsa.points.domain.enums.*;
import musinsa.points.presentation.dto.response.CancelUsedPointResponse;
import musinsa.points.presentation.dto.response.UsePointResponse;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

import musinsa.points.common.exception.BusinessException;
import musinsa.points.common.exception.ErrorCode;
import musinsa.points.presentation.dto.request.CancelPointRequest;
import musinsa.points.presentation.dto.response.CancelPointResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import musinsa.points.application.command.GrantPointCommand;
import musinsa.points.common.security.service.CustomUserDetailsInfo;
import musinsa.points.domain.enums.EventType;

import musinsa.points.domain.repository.PointRepository;
import musinsa.points.infrastructure.repository.MemberRepository;
import musinsa.points.presentation.dto.response.GrantPointResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class PointService {

    private final MemberRepository memberRepository;
    private final PointRepository pointRepository;
    private final PointPolicyService policyService;

    /**
     * 포인트 적립 서비스
     * 1. 정책 확인 및 검증
     * 2. PointGrant 엔티티 생성
     * 3. 원장(point_grant) 및 이력(point_history) 저장
     */
    @Transactional
    public GrantPointResponse grantPoints(GrantPointCommand command) {

        command.validate();

        // 1️⃣ 회원 조회
        Member member = memberRepository.findById(command.getMemberSeq())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REQUEST,"Member not found: " + command.getMemberSeq()));

        // 2️⃣ 정책 검증
        long maxPerTx   = policyService.getGlobalMaxGrantPerTx();           // GLOBAL 정책에서 조회
        long maxBalance = policyService.getMaxBalancePerMember(command.getMemberSeq()); // MEMBER 정책에서 조회

        if (command.getAmount() > maxPerTx) {
            throw new IllegalStateException("1회 최대 적립 한도 초과: 요청=%d, 한도=%d"
                    .formatted(command.getAmount(), maxPerTx));
        }

        long currentRemain = pointRepository.sumRemainingByMember(member.getMemberSeq());
        if (currentRemain + command.getAmount() > maxBalance) {
            throw new IllegalStateException(
                    "보유 한도 초과: 현재=%d, 요청=%d, 한도=%d"
                            .formatted(currentRemain, command.getAmount(), maxBalance)
            );
        }


        // 만료일 계산 (기본 365일, 최소 1일, 최대 5년)
        Instant expiresAt = command.getExpiresAt();
        Instant now = Instant.now();

        if (expiresAt == null) {
            long defaultExpiryDays = 365L;
            expiresAt = Instant.now().plus(defaultExpiryDays, ChronoUnit.DAYS);
        } else {
            // 최소 만료일 검증: 현재 시점보다 +1일 이상이어야 함
            Instant minAllowed = now.plus(1, ChronoUnit.DAYS);
            if (expiresAt.isBefore(minAllowed)) {
                throw new BusinessException(ErrorCode.INVALID_REQUEST,"만료일은 최소 1일 이후여야 합니다." + minAllowed + "이후 가능");
            }

            // 최대 만료일 검증: 현재 시점 기준 5년 미만이어야 함
            //Instant maxAllowed = now.plus(5 * 365, ChronoUnit.DAYS); -> 윤년 계산이 안됨
            Instant maxAllowed = plusYearsUtc(now,5);

            if (expiresAt.isAfter(maxAllowed)) {
                throw new BusinessException(ErrorCode.INVALID_REQUEST,"만료일은 최대 5년 미만이어야 합니다. (윤년 계산)5년후 :"+ maxAllowed);
            }
        }

        // 엔티티 생성
        PointGrant grant = PointGrant.create(
                member,
                command.getAmount(),
                command.getGrantType(),
                Instant.now(),
                expiresAt,
                command.getNote()
        );

        // 저장
        pointRepository.saveGrant(grant);

        // 히스토리 기록
        PointHistory history = PointHistory.builder()
                .member(member)
                .eventType(EventType.GRANT)
                .refType(RefType.GRANT)
                .refId(grant.getGrantId())
                .delta(command.getAmount())
                .balanceAfter(currentRemain + command.getAmount())
                .build();
        pointRepository.saveHistory(history);

        log.info("✅ Grant completed: member={}, amount={}, expires={}", member.getMemberSeq(), command.getAmount(), expiresAt);

        // 7️⃣ 응답 DTO 반환
        return new GrantPointResponse(
                grant.getGrantId(),
                member.getMemberSeq(),
                grant.getAmount(),
                grant.getRemainingAmount(),
                grant.getExpiresAt(),
                grant.getGrantType().name(),
                PointStatus.ACTIVE.name(),
                grant.getCreatedDate()
        );
    }
    static Instant plusYearsUtc(Instant base, int years) {
        return OffsetDateTime.ofInstant(base, ZoneOffset.UTC)
                .plusYears(years)
                .toInstant();
    }
    /**
     * 포인트 부여 취소 (Grant Cancel)
     * 조건:
     *  - 대상 grant는 ACTIVE 상태여야 함
     *  - remainingAmount == amount (일절 사용되지 않은 원장만 취소 가능)
     * 처리:
     *  - grant.status = CANCELLED, remainingAmount = 0
     *  - PointHistory에 GRANT_CANCEL 기록 (delta 음수)
     */
    @Transactional
    public CancelPointResponse cancelGrant(final CancelPointRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        final UUID grantId = request.grantId();

        // 1) 대상 원장 조회
        PointGrant grant = pointRepository.findGrantById(grantId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REQUEST,"Grant not found: " + grantId));
        Member owner = grant.getMember();

        // 2) 상태/사용 여부 검증
        if (!grant.isActive()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "이미 취소되었거나 만료된 적립은 취소할 수 없습니다.");
        }

        // 현재 잔액 조회 (취소 전)
        long currentRemain = pointRepository.sumRemainingByMember(owner.getMemberSeq());
        long cancelPoint = grant.getRemainingAmount();
        // 3) 상태 변경 & 저장
        grant.cancel();
        PointGrant canceledPointGrant = pointRepository.saveGrant(grant);

        // 4) 히스토리 기록 (delta 음수)
        PointHistory history = PointHistory.builder()
                .member(owner)
                .eventType(EventType.GRANT_CANCEL)
                .refType(RefType.CANCEL)
                .refId(grant.getGrantId())
                .delta(-grant.getRemainingAmount())
                .balanceAfter(currentRemain - cancelPoint)
                .build();
        PointHistory pointHistory = pointRepository.saveHistory(history);

        // 5) 응답 구성 (point_cancel 테이블은 '사용 취소'용이라 여기서는 생성하지 않음)
        return new CancelPointResponse(
                canceledPointGrant.getGrantId(),
                owner.getMemberSeq(),
                request.reason(),
                history.getCreatedDate(),
                history.getModifiedDate(),
                PointStatus.CANCELLED.name()
        );
    }

    /**
     * 주문에서 포인트 사용
     * 규칙:
     *  - MANUAL로 수기 지급된 포인트 우선
     *  - 그 다음 만료 임박(만료일 오름차순)
     *  - 동일 만료일이면 선입선출(FIFO)
     */
    @Transactional
    public UsePointResponse usePoints(final UsePointCommand command) {

        // 1) 회원 조회
        Member member = memberRepository.findById(command.memberSeq())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REQUEST,"Member not found: " + command.memberSeq()));

        // 2) 잔액 검증(잔액이 0이면 사용 X)
        long totalRemain = pointRepository.sumRemainingByMember(member.getMemberSeq());
        if (totalRemain <= 0) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "잔액 부족: 보유=" + totalRemain);
        }
        long usePoint = command.useAmount();
        if (command.useAmount() > totalRemain) {
            usePoint = totalRemain;
        }

        // 3) 사용 헤더 생성
        PointUse use = PointUse.builder()
                .member(member)
                .orderNo(command.orderNo())
                .usedAmount(usePoint)
                .status(UseStatus.USED)
                .build();
        pointRepository.saveUse(use);

        List<UsePointResponse.UseDetail> detailViews = new ArrayList<>();

        long remainToUse = usePoint;
        // 4) 차감 대상 그랜트 조회 (우선순위 정렬 반영된 쿼리)
        List<PointGrant> grants = pointRepository.findSpendableGrantsOrderByExpiry(member.getMemberSeq());
        for (PointGrant grant : grants) {
            if (remainToUse <= 0) break;
            if (!grant.isActive()) continue; // 안전장치

            long available = grant.getRemainingAmount();
            if (available <= 0) continue;

            long deduct = Math.min(available, remainToUse);
            grant.consume(deduct);
            pointRepository.saveGrant(grant);

            // 상세 저장 (매핑 테이블)
            PointUseDetail detail = PointUseDetail.builder()
                    .pointUse(use)
                    .pointGrant(grant)
                    .amount(deduct)
                    .build();
            pointRepository.saveUseDetail(detail);

            // 응답용 뷰 누적
            detailViews.add(new UsePointResponse.UseDetail(
                    grant.getGrantId(),
                    deduct,
                    grant.getRemainingAmount(),
                    grant.getGrantType().name(),
                    grant.getExpiresAt()
            ));

            remainToUse -= deduct;
        }

        if (remainToUse > 0) {
            // 이론상 도달 불가(잔액 검증 통과)하지만, 동시성 이슈 방어
            throw new BusinessException(ErrorCode.CONFLICT, "동시성으로 인해 충분히 차감하지 못했습니다. 남은금액=" + remainToUse);
        }

        // 5) 히스토리 기록 (USE)
        PointHistory history = PointHistory.builder()
                .member(member)
                .eventType(EventType.USE)
                .refType(RefType.USE)
                .refId(use.getUseId())
                .delta(-command.useAmount())
                .balanceAfter(totalRemain - command.useAmount())
                .build();
        pointRepository.saveHistory(history);

        // 6) 응답 조립
        return new UsePointResponse(
                use.getUseId(),
                member.getMemberSeq(),
                use.getOrderNo(),
                use.getUsedAmount(),
                use.getStatus().name(),
                use.getCreatedDate(),
                detailViews
        );
    }

    /**
     * 포인트 사용 취소 (Use Cancel)
     * 규칙:
     *  - 사용건이 존재해야 함
     *  - 취소 금액은 사용 금액 이하
     *  - 이미 만료된 적립분은 재적립 처리
     */
    @Transactional
    public CancelUsedPointResponse cancelUsedPoints(final CancelUsedPointCommand command) {
        Objects.requireNonNull(command, "command must not be null");

        // 1️⃣ 사용 이력 조회
        PointUse use = pointRepository.findUseById(command.useId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Point use not found: " + command.useId()));

        Member member = use.getMember();
        long usedAmount = use.getUsedAmount();
        long cancelAmount = command.cancelAmount();

        // 이미 취소된 금액 조회(부분취소 누적)
        long canceledSoFar = pointRepository.sumCanceledAmountByUseId(use.getUseId());
        long cancellableLeft = usedAmount - canceledSoFar;
        if (cancellableLeft <= 0) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "더 이상 취소할 수 있는 금액이 없습니다. (누적취소=%d)".formatted(canceledSoFar));
        }
        if (cancelAmount <= 0 || cancelAmount > cancellableLeft) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "취소 금액이 잘못되었습니다. (요청=%d, 사용=%d, 누적취소=%d, 잔여취소가능=%d)"
                            .formatted(cancelAmount, usedAmount, canceledSoFar, cancellableLeft)
            );
        }

        // 2️⃣ 사용 상세 조회 (어떤 grant에서 사용했는지)
        List<PointUseDetail> details = pointRepository.findUseDetailsByUseId(use.getUseId()).stream()
                .sorted(Comparator.comparing(PointUseDetail::getCreatedDate).reversed())
                .toList();

        long remainingCancel = cancelAmount;
        List<CancelUsedPointResponse.CancelDetail> cancelDetails = new ArrayList<>();
        long regrantTotal = 0L;

        for (PointUseDetail detail : details) {
            if (remainingCancel <= 0) break;

            PointGrant grant = detail.getPointGrant();
            long canceledForDetail = pointRepository.sumCanceledAmountByUseDetailId(detail.getUseDetailId()); // 새로 추가

            long cancellableForDetail = detail.getAmount() - canceledForDetail;
            if (cancellableForDetail <= 0) {
                // 이 상세는 더 이상 취소 불가 → 다음 상세로
                continue;
            }

            long refund = Math.min(remainingCancel, cancellableForDetail);
            long grantRestorableNow = grant.getAmount() - grant.getRemainingAmount();
            refund = Math.min(refund, grantRestorableNow);

            boolean regranted = false;
            Instant regrantExpire = null;

            if (refund <= 0) {
                continue;
            }

            // 만료 여부 판단
            if (grant.getExpiresAt().isBefore(Instant.now())) {
                // 만료된 경우 재적립
                regranted = true;
                regrantExpire = Instant.now().plus(7, ChronoUnit.DAYS);
                PointGrant newGrant = PointGrant.create(
                        member,
                        refund,
                        GrantType.REGRANT,
                        Instant.now(),
                        regrantExpire,
                        "만료된 포인트 취소분 재적립"
                );
                pointRepository.saveGrant(newGrant);
                regrantTotal += refund;
            } else {
                // 만료되지 않은 경우 원복
                grant.restore(refund);
                pointRepository.saveGrant(grant);
            }
            // point_cancel 레코드 생성
            PointCancel cancelRecord = PointCancel.builder()
                    .pointUse(use)
                    .pointUseDetail(detail)
                    .cancelAmount(refund)
                    .regrantNeededAmount(regranted ? refund : 0L)
                    .build();

            pointRepository.saveCancelRecord(cancelRecord);

            cancelDetails.add(new CancelUsedPointResponse.CancelDetail(
                    grant.getGrantId(),
                    refund,
                    regranted,
                    regrantExpire
            ));

            remainingCancel -= refund;
        }

        if (remainingCancel > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "취소 잔액 처리 중 오류 발생 (남은금액=" + remainingCancel + ")");
        }

        // 4️⃣ history 기록
        long memberBalance = pointRepository.sumRemainingByMember(member.getMemberSeq());
        PointHistory history = PointHistory.builder()
                .member(member)
                .eventType(EventType.USE_CANCEL)
                .refType(RefType.CANCEL)
                .refId(use.getUseId())
                .delta(cancelAmount)
                .balanceAfter(memberBalance)
                .build();
        pointRepository.saveHistory(history);

        // 5️⃣ 응답
        return new CancelUsedPointResponse(
                use.getUseId(),
                member.getMemberSeq(),
                cancelAmount,
                regrantTotal,
                history.getCreatedDate(),
                cancelDetails
        );
    }

}
