## 1. 모든 api는 권한별로 인가가 되어야 사용할수 있습니다.

![[Pasted image 20251111163220.png]](./attachments/Pasted_image_20251111163220.png)
## 2. 1회 적립당, 멤버별 포인트 최대 보유 한도 등 정책은 DB 및 cache로 관리된다.
   #### 캐쉬 조회
![[Pasted image 20251111164017.png]](./attachments/Pasted image 20251111164017.png)
#### 시스템 스타트 시 캐쉬조회 내역
```json
{
  "name": "pointPolicy",
  "size": 6,
  "stats": "CacheStats{hitCount=2, missCount=1, loadSuccessCount=1, loadFailureCount=0, totalLoadTime=273625, evictionCount=0, evictionWeight=0}",
  "entries": {
    "1": {
      "MAX_BALANCE_PER_MEMBER": {
        "scope": "MEMBER",
        "policyType": "MAX_BALANCE_PER_MEMBER",
        "memberSeq": 1,
        "policyValue": 110000,
        "policyId": "25440d8a-ef89-438d-b323-0eebcb99f6f4"
      }
    },
    "2": {
      "MAX_BALANCE_PER_MEMBER": {
        "scope": "MEMBER",
        "policyType": "MAX_BALANCE_PER_MEMBER",
        "memberSeq": 2,
        "policyValue": 111000,
        "policyId": "bfe36651-4690-4adb-aabf-a88597b7f2dc"
      }
    },
    "3": {
      "MAX_BALANCE_PER_MEMBER": {
        "scope": "MEMBER",
        "policyType": "MAX_BALANCE_PER_MEMBER",
        "memberSeq": 3,
        "policyValue": 111100,
        "policyId": "c79fe378-141e-4148-8801-df5dddd04f73"
      }
    },
    "4": {
      "MAX_BALANCE_PER_MEMBER": {
        "scope": "MEMBER",
        "policyType": "MAX_BALANCE_PER_MEMBER",
        "memberSeq": 4,
        "policyValue": 111110,
        "policyId": "9f83c2a8-9111-4e35-a121-3b2f228293ee"
      }
    },
    "5": {
      "MAX_BALANCE_PER_MEMBER": {
        "scope": "MEMBER",
        "policyType": "MAX_BALANCE_PER_MEMBER",
        "memberSeq": 5,
        "policyValue": 111111,
        "policyId": "1b06bfdf-5b73-4e93-bca4-ba8076367add"
      }
    },
    "GLOBAL": {
      "MAX_GRANT_PER_TX": {
        "scope": "GLOBAL",
        "policyType": "MAX_GRANT_PER_TX",
        "memberSeq": null,
        "policyValue": 100000,
        "policyId": "06377988-43af-4580-bb2e-6a040ac207ef"
      }
    }
  }
}
```
