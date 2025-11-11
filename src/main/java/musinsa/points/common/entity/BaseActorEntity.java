package musinsa.points.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseActorEntity extends BaseTimeEntity {

    // Integer(사용자 PK)나 String(아이디) 등 "스칼라"로 두세요. User 엔티티 연관관계로 두면 순환/부트스트랩 이슈가 생깁니다.
    //스칼라 필드는 단순 정수 저장임으로 부하를 신경쓸 필요가 없습니다.
    @CreatedBy
    @Column(name = "created_by")
    private Long createdBy;

    @LastModifiedBy
    @Column(name = "modified_by")
    private Long modifiedBy;

}
