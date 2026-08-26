# FDS: Open Banking 기반 실시간 이상 거래 탐지 시스템

## 1. 프로젝트 개요
금융 거래 데이터를 실시간으로 분석하여 이상 거래를 탐지하는 시스템입니다.

### 1.1️ 기술 스택
- **백엔드**: Spring Boot 2.7.0
- **데이터베이스**: MySQL
- **실시간 통신**: WebSocket
- **캐싱**: Redis
- **배포**: AWS EC2, RDS

### 1.2 프로젝트 일정
- **완성 목표**: 2026년 10월 18일 (95% 이상 완성)
- **개발 기간**: 2026년 8월 22일 ~ 10월 18일 (8주)

## 2. 주요 기능
###  ️2.1 이상 거래 탐지
- 거래액 이상 (기술통계: Z-score)
- 시간대 이상 (새벽 거래 등)
- 거래처 이상 (블랙리스트)
- 거래 빈도 이상 (자동화 의심)
- 신규 거래처 이상

### 2.2 실시간 모니터링
- WebSocket 기반 실시간 거래 스트림
- 대시보드 시각화
- 즉각적 알림

### 2.3 데이터 분석
- 사용자별 거래 패턴 학습
- 신뢰도 점수 계산
- 통계 기반 이상치 탐지

## 3. 구조
### 3.1 시스템 아키텍처
Open Banking API
↓
Spring Boot Backend
├─ 기술통계 분석
├─ 규칙 기반 탐지
└─ 실시간 처리
↓
MySQL + Redis
↓
WebSocket → Dashboard

### 3.2 프로젝트 구조
```
src/
├── main/java/com/fds/
│ ├── controller/ (API 엔드포인트)
│ ├── service/ (비즈니스 로직)
│ ├── repository/ (DB 접근)
│ ├── entity/ (데이터 모델)
│ └── config/ (설정 파일)
└── main/resources/
docs/
├── architecture.md (시스템 설계)
├── database-schema.md (DB 스키마)
├── api-spec.md (API 명세)
└── anomaly-rules.md (탐지 규칙)
```

## 4. 목표
### 4.1 성능 목표
- **탐지 정확도**: 95% 이상
- **반응 시간**: 1초 이내
- **동시 처리**: 1000+ 거래/초

### 4.2 사용 방법
(9월부터 개발 후 작성 예정)

### 설치
```bash
git clone https://github.com/naheeseong/fds-anomaly-detection.git
cd fds-anomaly-detection
mvn clean install
```

### 실행
```bash
mvn spring-boot:run
```

## 5. 기타
### 5.1 참고 문서
- [시스템 아키텍처](docs/architecture.md)
- [데이터베이스 설계](docs/database-schema.md)
- [API 명세](docs/api-spec.md)
- [이상 거래 탐지 규칙](docs/anomaly-rules.md)

### 5.2 라이선스
MIT

---