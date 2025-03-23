# 🧪 HHPlus TDD 과제 - 포인트 시스템

Spring Boot 기반의 포인트 적립/사용 시스템 구현 과제입니다.  
TDD(Test-Driven Development)를 기반으로 설계하며, 동시성 이슈 제어 및 테스트 가능한 구조를 목표로 합니다.

---

## ✅ 구현 기능

- 포인트 조회 `GET /points/{userId}`
- 포인트 충전 `POST /points/charge`
- 포인트 사용 `POST /points/use`
- 포인트 내역 조회 `GET /points/history/{userId}`

---

## 🧪 테스트

- 각 기능에 대해 단위 테스트 (성공/예외 케이스 포함)
- 기능별 통합 테스트
- 포인트 사용 시 동시성 제어 테스트 (멀티스레드 환경)
