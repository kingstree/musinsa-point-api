package musinsa.points.application.event;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 도메인 이벤트 핸들러
 *
 * 장점:
 * 1. 관심사의 분리 (Separation of Concerns)
 * 2. 느슨한 결합 (Loose Coupling)
 * 3. 확장성 (Extensibility)
 * 4. 재사용성 (Reusability)
 *
 * 주의:
 * - @TransactionalEventListener를 사용하여 트랜잭션 커밋 후 이벤트 처리
 * - @Async를 사용하여 비동기 처리 가능
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PointEventHandler {

}

/**
 * 인프라스트럭처 서비스들 (간략화)
 */

