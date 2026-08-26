# 프로젝트 구조 가이드

## 1. 전체 폴더 구조
```
fds-anomaly-detection/
├── src/
│ ├── main/
│ │ ├── java/com/fds/
│ │ │ ├── FdsApplication.java (메인 클래스)
│ │ │ ├── controller/ (API)
│ │ │ ├── service/ (비즈니스 로직)
│ │ │ ├── repository/ (DB 접근)
│ │ │ ├── entity/ (데이터 모델)
│ │ │ ├── dto/ (요청/응답 객체)
│ │ │ ├── util/ (유틸리티)
│ │ │ └── config/ (설정)
│ │ └── resources/
│ │ ├── application.properties (설정)
│ │ ├── static/ (HTML, CSS, JS)
│ │ └── templates/ (Thymeleaf 템플릿)
│ └── test/
│ └── java/com/fds/ (테스트)
├── docs/ (문서)
│ ├── architecture.md
│ ├── database-schema.md
│ ├── api-spec.md
│ ├── anomaly-rules.md
│ └── project-structure.md
├── README.md
├── pom.xml (Maven 의존성)
├── .gitignore
└── .idea/ (IntelliJ 설정)
```

## 2. 각 폴더 역할

### controller/
- **역할**: API 엔드포인트 정의
- **파일 예시**:
    - TransactionController.java (거래 API)
    - MonitoringController.java (모니터링 API)
    - WebSocketHandler.java (WebSocket)

### service/
- **역할**: 핵심 비즈니스 로직
- **파일 예시**:
    - TransactionService.java (거래 처리)
    - AnomalyDetectionService.java (이상 탐지 - 가장 중요!)
    - StatisticsService.java (통계 계산)

### repository/
- **역할**: 데이터베이스 접근 (JPA)
- **파일 예시**:
    - TransactionRepository.java
    - AbnormalPatternRepository.java
    - UserStatisticsRepository.java

### entity/
- **역할**: 데이터 모델 (DB 테이블 대응)
- **파일 예시**:
    - Transaction.java
    - AbnormalPattern.java
    - UserStatistics.java
    - Alert.java

### dto/
- **역할**: 요청/응답 객체
- **파일 예시**:
    - TransactionRequest.java
    - TransactionResponse.java
    - MonitoringData.java

### config/
- **역할**: Spring 설정
- **파일 예시**:
    - WebSocketConfig.java
    - DatabaseConfig.java
    - RedisConfig.java

### util/
- **역할**: 유틸리티 함수
- **파일 예시**:
    - AnomalyDetectionUtils.java (탐지 관련 유틸)
    - StatisticsUtils.java (통계 유틸)

## 3. 개발 순서
```
Week 1-2: controller + entity + repository
Week 3: database (MySQL 연동)
Week 4-5: service (AnomalyDetectionService - 핵심!)
Week 5-6: WebSocket + Dashboard
week 7-8: refactoring
```