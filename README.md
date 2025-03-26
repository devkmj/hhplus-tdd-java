## HHPlus Chapter. 1-1 TDD로 개발하기
  
### 사용 기술 스택 및 개발환경
| **항목** | **내용** |
| --- | --- |
| Language | Java 17 |
| Framework | Spring Boot 3.2.0 |
| Build Tool | Gradle |
| Test Framework | JUnit5, Mockito, MockMvc |
| 개발 환경 | IntelliJ IDEA, macOS |
| 데이터 저장소 | In-Memory (Map 기반) - UserPointTable, PointHistoryTable |

---

### 구현 기능

- 포인트 조회 `GET /points/{userId}`
- 포인트 충전 `POST /points/charge`
- 포인트 사용 `POST /points/use`
- 포인트 내역 조회 `GET /points/history/{userId}`

---

### 테스트

- 단위 테스트 : 각 서비스 메서드에 대해 정상/예외 케이스 테스트
- 통합 테스트 : Controller -> Service -> Table 흐름 테스트
- 동시성 테스트 : 충전/사용 API에 대한 병렬 요청 시나리오 작성

---

### 동시성 제어 전략

포인트 충전 및 사용 시 Race Condition을 방지하고 데이터 정합성을 보장하기 위해 동시성 제어가 필요했습니다.

- **환경** : 분산 환경이 아닌 단일 WAS환경
- **해결방식** : synchronized 기반의 사용자별 락 적용

--- 

### 적용 방식

- 유저 ID 별로 고유한 락 객체를 생성
- ConcurrentHashMap<Long, Object>을 통해 관리
- synchronized (lockMap.get(userId)) 으로 충전/사용 블록 보호

---

### 장점

- 구현이 간단하고 직관적임
- 유저 단위로 락을 관리하여 병렬성 확보
- 테스트 및 디버깅이 용이함

### 단점

- 멀티 인스턴스 환경에서는 락이 공유되지 않아 데이터 정합성 문제가 발생할 수 있음
- 동일 유저에 요청이 집중될 경우 병목 발생 가능
- 락 객체는 한번 생성되면 계속 Map에 남아 GC 대상이 되지 않으므로, 유저 수가 많아질수록 메모리 누수 가능성 존재함

--- 

### 실제 구현 위치

| 클래스 | 설명 |
|--------|------|
| `PointService` | 사용자 포인트 충전 및 사용 시 Lock 적용 |
| `PointIntegrationTest` | 다중 요청 환경에서 충돌 없는 처리를 위한 테스트 시나리오 작성 |

---
