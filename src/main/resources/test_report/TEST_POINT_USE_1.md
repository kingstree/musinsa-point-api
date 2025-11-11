## 테스트 환경 
    주문시에만 포인트를 사용할 수 있다고 가정한다.

----
## 테스트 요건
#### 포인트 사용시에는 주문번호를 함께 기록하여 어떤 주문에서 얼마의 포인트를 사용했는지 식별할 수 있어야 한다.

##### 포인트 사용시 주문번호 insert
![Pasted image 20251111183933](./attachments/Pasted%20image%2020251111183933.png)


----
## 테스트 요건
#### 1. 포인트 사용시에는 관리자가 수기 지급한 포인트가 우선 사용되어야 하며, 만료일이 짧게 남은 순서로 사용해야 한다.
#### 2. 사용한 금액중 전제 또는 일부를 사용취소 할수 있다.
#### 3. 사용취소 시점에 이미 만료된 포인트를 사용취소 해야 한다면 그 금액만큼 신규적립 처리 한다.


##### 최초 지급 내역
![Pasted image 20251111190832](./attachments/Pasted%20image%2020251111190832.png)

##### 주문 내역
(주문 완료 11,500 )
![Pasted image 20251111190932](./attachments/Pasted%20image%2020251111190932.png)
관리자순, 만료일이 짧은 순으로 포인트가 사용됨을 확인
![Pasted image 20251111190958](./attachments/Pasted%20image%2020251111190958.png)


##### 총 5,500원 취소 (취소 내역)
(사용된 역순으로 취소됨)
![Pasted image 20251111191258](./attachments/Pasted%20image%2020251111191258.png)
(사용된 순으로 복원됨)
![Pasted image 20251111191317](./attachments/Pasted%20image%2020251111191317.png)


##### 1,000원 추가 취소( 관리자도 역순으로 잘 취소됨)
![Pasted image 20251111191753](./attachments/Pasted%20image%2020251111191753.png)
![Pasted image 20251111191809](./attachments/Pasted%20image%2020251111191809.png)

#####  포인트 만료 처리 (grant_id: 4560c77c-2156-47c3-a6f0-5f63ee3e894f)
(만료일자가 변경됨)![Pasted image 20251111192351](./attachments/Pasted%20image%2020251111192351.png)

#### 4,000원 추가 취소(4천원이 취소되며 7일 추가하여 재 부여처리)
![Pasted image 20251111192543](./attachments/Pasted%20image%2020251111192543.png)
![Pasted image 20251111192624](./attachments/Pasted%20image%2020251111192624.png)


#### 모든 내역이 감사되고 있음
![Pasted image 20251111193318](./attachments/Pasted%20image%2020251111193318.png)
