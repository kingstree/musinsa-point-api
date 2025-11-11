package musinsa.points.domain.repository;

import musinsa.points.domain.entity.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 포인트 도메인 리포지터리 포트(도메인 레벨)
 *
 * 인프라 구현은 infrastructure 계층에서 제공한다.
 */
public interface PointRepository {

    // ---- 조회 (쿼리) ----

    /**
     * 포인트 사용 헤더를 ID로 조회한다.
     */
    Optional<PointUse> findUseById(UUID useId);

    /**
     * 포인트 사용 상세내역을 useId 기준으로 조회한다.
     */
    List<PointUseDetail> findUseDetailsByUseId(UUID useId);

    /**
     * 회원의 사용 가능한(남은 금액 > 0, 만료 미도래) 그랜트들을 만료일 오름차순(FIFO)으로 조회한다.
     */
    List<PointGrant> findSpendableGrantsOrderByExpiry(long memberSeq);

    /**
     * 특정 그랜트를 ID로 조회한다.
     */
    Optional<PointGrant> findGrantById(UUID grantId);

    // ---- 저장 (커맨드) ----
    /** 적립 원장 저장 */
    PointGrant saveGrant(PointGrant grant);

    /** 사용 헤더 저장 */
    PointUse saveUse(PointUse use);

    /** 사용 상세 저장 */
    PointUseDetail saveUseDetail(PointUseDetail detail);

    /** 사용 상세 일괄 저장 */
    List<PointUseDetail> saveUseDetails(List<PointUseDetail> details);

    /** 사용 취소 헤더 저장 */
    PointCancel saveCancel(PointCancel cancel);

    /**
     * point_cancel 레코드 저장 (JPA로 저장 후 생성된 cancelId 반환)
     */
    UUID saveCancelRecord(PointCancel cancelRecord);

    /** 감사 로그 저장 */
    PointHistory saveHistory(PointHistory history);

    // ---- 갱신 (남은금액 차감/증가) ----
    /**
     * 남은 금액을 증감한다. (양수=증가, 음수=차감)
     * 구현체는 동시성 안전하게 처리(낙관적 락/쿼리 업데이트 등)해야 한다.
     */
    void adjustGrantRemaining(UUID grantId, long delta);
    // 포인트 잔액 //
    long sumRemainingByMember(long memberSeq);

    /**
     * 해당 사용건(useId)의 누적 취소 금액 합계를 조회한다.
     */
    long sumCanceledAmountByUseId(UUID useId);

    long sumCanceledAmountByUseDetailId(UUID useDetailId);
}
