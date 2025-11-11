package musinsa.points.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import musinsa.points.domain.entity.*;
import musinsa.points.domain.repository.PointRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 주문 리포지터리 어댑터
 * 
 * 어댑터 패턴을 사용하여:
 * 1. 도메인 인터페이스 (OrderRepository)와 JPA 인터페이스 (JpaOrderRepository) 연결
 * 2. 도메인 계층과 인프라스트럭처 계층의 분리
 * 3. 의존성 역전 원칙 (DIP) 적용
 * 
 * 역할:
 * - 도메인 인터페이스를 구현
 * - JPA 리포지터리를 위임하여 실제 데이터베이스 작업 수행
 */
@Component
@RequiredArgsConstructor
public class PointRepositoryAdapter implements PointRepository {
    
    private final PointCancelJpaRepository pointCancelJpaRepository;
    private final PointGrantJpaRepository pointGrantJpaRepository;
    private final PointUseJpaRepository pointUseJpaRepository;
    private final PointUseDetailJpaRepository pointUseDetailJpaRepository;
    private final PointHistoryJpaRepository pointHistoryJpaRepository;


    @Override
    public Optional<PointUse> findUseById(UUID useId) {
        return pointUseJpaRepository.findById(useId);
    }

    @Override
    public List<PointUseDetail> findUseDetailsByUseId(UUID useId) {
        return pointUseDetailJpaRepository.findOrderedByUseIdForCancel(useId);
    }

    @Override
    public List<PointGrant> findSpendableGrantsOrderByExpiry(long memberSeq) {
        return pointGrantJpaRepository.findSpendableGrantsOrderByExpiry(memberSeq);
    }

    @Override
    public Optional<PointGrant> findGrantById(UUID grantId) {
        return pointGrantJpaRepository.findById(grantId);
    }

    @Override
    public PointGrant saveGrant(PointGrant grant) {
        return pointGrantJpaRepository.save(grant);
    }

    @Override
    public PointUse saveUse(PointUse use) {
        return pointUseJpaRepository.save(use);
    }

    @Override
    public PointUseDetail saveUseDetail(PointUseDetail detail) {
        return pointUseDetailJpaRepository.save(detail);
    }

    @Override
    public List<PointUseDetail> saveUseDetails(List<PointUseDetail> details) {
        return pointUseDetailJpaRepository.saveAll(details);
    }

    @Override
    public PointCancel saveCancel(PointCancel cancel) {
        return pointCancelJpaRepository.save(cancel);
    }

    @Override
    public UUID saveCancelRecord(PointCancel cancelRecord) {
        PointCancel saved = pointCancelJpaRepository.save(cancelRecord);
        return saved.getCancelId();
    }

    @Override
    public PointHistory saveHistory(PointHistory history) {
        return pointHistoryJpaRepository.save(history);
    }

    @Override
    public void adjustGrantRemaining(UUID grantId, long delta) {
        int updated = pointGrantJpaRepository.adjustRemaining(grantId, delta);
        if (updated != 1) {
            throw new IllegalStateException("adjustGrantRemaining failed: grant=" + grantId + ", delta=" + delta);
        }
    }

    @Override
    public long sumRemainingByMember(long memberSeq) {
        return pointGrantJpaRepository.sumRemainingByMember(memberSeq);
    }

    @Override
    public long sumCanceledAmountByUseId(UUID useId) {
        return pointCancelJpaRepository.sumCanceledAmountByUseId(useId);
    }

    @Override
    public long sumCanceledAmountByUseDetailId(UUID useDetailId) {
        if (useDetailId == null) return 0L;
        return pointCancelJpaRepository.sumCanceledByUseDetailId(useDetailId);
    }
}
