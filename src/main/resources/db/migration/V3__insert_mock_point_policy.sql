-- =========================================================
-- V3__insert_mock_point_policy.sql
-- 목적: 무료포인트 기능 테스트용 정책 데이터 사전 세팅
-- 스키마 전제:
--   - point_policy(scope, policy_type, member_seq, active, policy_value, ...)
--   - policy_scope: 'GLOBAL' | 'MEMBER'
--   - policy_type : 'MAX_GRANT_PER_TX' | 'MAX_BALANCE_PER_MEMBER'
-- 규칙:
--   - GLOBAL: policy_type별 활성 1건만
--   - MEMBER: (member_seq, policy_type)별 활성 1건만
-- =========================================================

-- 0) 기존 활성 GLOBAL 정책이 있다면 비활성화 (유니크 인덱스 충돌 방지)
UPDATE point_policy
SET active = FALSE, modified_date = CURRENT_TIMESTAMP, modified_by = 0
WHERE scope = 'GLOBAL'
  AND active = TRUE
  AND policy_type IN ('MAX_GRANT_PER_TX','MAX_BALANCE_PER_MEMBER');


-- 1) GLOBAL 정책 2건 (유형별 1건)
--    - 1회 최대 적립: 100,000
--    - 회원 최대 보유: 2,000,000
INSERT INTO point_policy (
    policy_id, scope, policy_type, member_seq, active, policy_value,
    created_date, created_by
) VALUES
    (RANDOM_UUID(), 'GLOBAL', 'MAX_GRANT_PER_TX',       NULL, TRUE, 100000,  CURRENT_TIMESTAMP, 1);


-- 2) 기존 활성 MEMBER(1~5) 정책이 있다면 비활성화
UPDATE point_policy
SET active = FALSE, modified_date = CURRENT_TIMESTAMP, modified_by = 0
WHERE scope = 'MEMBER'
  AND active = TRUE
  AND member_seq IN (1,2,3,4,5)
  AND policy_type IN ('MAX_GRANT_PER_TX','MAX_BALANCE_PER_MEMBER');

-- 3) MEMBER별 오버라이드 (member1~5)
--    member1: 보유 5,000,000 / 1회 500,000
--    member2: 보유 3,000,000 / 1회 300,000
--    member3~5: 보유 1,000,000 / 1회 100,000
INSERT INTO point_policy (
    policy_id, scope, policy_type, member_seq, active, policy_value,
    created_date, created_by
) VALUES
    -- member1
    (RANDOM_UUID(), 'MEMBER', 'MAX_BALANCE_PER_MEMBER', 1, TRUE, 110000, CURRENT_TIMESTAMP, 1),
    -- member2
    (RANDOM_UUID(), 'MEMBER', 'MAX_BALANCE_PER_MEMBER', 2, TRUE, 111000, CURRENT_TIMESTAMP, 1),
    -- member3
    (RANDOM_UUID(), 'MEMBER', 'MAX_BALANCE_PER_MEMBER', 3, TRUE, 111100, CURRENT_TIMESTAMP, 1),
    -- member4
    (RANDOM_UUID(), 'MEMBER', 'MAX_BALANCE_PER_MEMBER', 4, TRUE, 111110, CURRENT_TIMESTAMP, 1),
    -- member5
    (RANDOM_UUID(), 'MEMBER', 'MAX_BALANCE_PER_MEMBER', 5, TRUE, 111111, CURRENT_TIMESTAMP, 1);

