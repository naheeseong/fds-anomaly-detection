# API 명세서

## 1. 기본 정보

- **Base URL**: http://localhost:8080/api
- **모든 응답**: JSON
- **Content-Type**: application/json
- **인증**: Bearer Token (JWT, 나중에 추가)

---

## 2. 거래 API

### 2.1 POST /transactions
거래 데이터를 받아서 저장하고 이상 거래 판정

**요청 헤더:**

Content-Type: application/json
Authorization: Bearer {token} (선택사항)


**요청 바디:**
```json
{
  "userId": 1,                          // 필수, BIGINT
  "amount": 500000,                     // 필수, 0 < amount <= 999999999
  "transactionTime": "2026-09-15T14:30:00",  // 필수, ISO 8601
  "merchantId": "MERCHANT_001",         // 필수, VARCHAR(50)
  "merchantName": "편의점",             // 필수, VARCHAR(100)
  "category": "retail",                 // 선택, VARCHAR(50)
  "description": "구매"                 // 선택, VARCHAR(200)
}
```

**요청 검증:**
```
✓ userId > 0
✓ amount 범위: 0 < amount <= 999,999,999
✓ transactionTime: 현재 시간 이후 불가
✓ merchantId 길이: 1~50
✓ merchantName 길이: 1~100
```

**응답 (200 OK - 정상 거래):**
```json
{
  "id": 1001,
  "userId": 1,
  "amount": 500000,
  "transactionTime": "2026-09-15T14:30:00",
  "merchantId": "MERCHANT_001",
  "merchantName": "편의점",
  "category": "retail",
  "isAbnormal": false,
  "abnormalReason": null,
  "confidenceScore": 0.0,
  "patterns": [],
  "message": "정상 거래",
  "timestamp": "2026-09-15T14:30:05"
}
```

**응답 (200 OK - 이상 거래):**
```json
{
  "id": 1002,
  "userId": 1,
  "amount": 5000000,
  "transactionTime": "2026-09-15T02:30:00",
  "merchantId": "MERCHANT_002",
  "merchantName": "명품점",
  "category": "luxury",
  "isAbnormal": true,
  "abnormalReason": "HIGH_AMOUNT, ODD_TIME",
  "confidenceScore": 0.8,
  "patterns": [
    {
      "type": "HIGH_AMOUNT",
      "zscore": 3.5,
      "threshold": 1100000,
      "actual": 5000000
    },
    {
      "type": "ODD_TIME",
      "hour": 2,
      "message": "새벽 2시 대액 거래"
    }
  ],
  "message": "이상 거래 탐지",
  "timestamp": "2026-09-15T02:30:05"
}
```

**응답 (400 Bad Request):**
```json
{
  "error": "BAD_REQUEST",
  "message": "amount가 범위를 벗어났습니다",
  "details": {
    "field": "amount",
    "value": -1000,
    "constraint": "0 < amount <= 999999999"
  },
  "timestamp": "2026-09-15T14:30:05"
}
```

**응답 (500 Internal Server Error):**
```json
{
  "error": "INTERNAL_SERVER_ERROR",
  "message": "데이터베이스 연동 중 오류 발생",
  "traceId": "abc123def456",
  "timestamp": "2026-09-15T14:30:05"
}
```

**상태 코드:**

| 코드 | 의미 | 예시 |
|------|------|------|
| 200 | 거래 처리 성공 | 정상/이상 거래 모두 저장됨 |
| 400 | 잘못된 요청 | userId가 음수 |
| 401 | 미인증 | 토큰 없음 |
| 409 | 중복 거래 | 같은 거래 ID 중복 |
| 500 | 서버 오류 | DB 연결 실패 |

---

### 2.2 GET /transactions?page=0&size=20&userId=1
거래 목록 조회 (페이징)

**요청 쿼리:**
```
page=0 // 선택, 기본값 0
size=20 // 선택, 기본값 20, 최대 100
userId=1 // 선택, 사용자별 필터
isAbnormal=true // 선택, 이상 거래만 조회
startDate=2026-09-01T00:00:00 // 선택, ISO 8601
endDate=2026-09-30T23:59:59 // 선택, ISO 8601
sortBy=transaction_time // 선택, 정렬 기준
sortOrder=DESC // 선택, ASC or DESC
```

**응답 (200 OK):**
```json
{
  "content": [
    {
      "id": 1001,
      "userId": 1,
      "amount": 500000,
      "isAbnormal": false,
      "transactionTime": "2026-09-15T14:30:00"
    }
  ],
  "pagination": {
    "page": 0,
    "size": 20,
    "totalElements": 1250,
    "totalPages": 63,
    "hasNext": true,
    "hasPrevious": false
  },
  "timestamp": "2026-09-15T14:35:00"
}
```

---

### 2.3 GET /transactions/{id}
개별 거래 조회

**요청 경로:**

GET /transactions/1001


**응답 (200 OK):**
```json
{
  "id": 1001,
  "userId": 1,
  "amount": 500000,
  ...
}
```

**응답 (404 Not Found):**
```json
{
  "error": "NOT_FOUND",
  "message": "거래 ID 1001을 찾을 수 없습니다",
  "timestamp": "2026-09-15T14:30:05"
}
```

---

## 3. 모니터링 API

### 3.1 GET /monitoring/statistics
실시간 통계 조회

**쿼리 파라미터:**

period=1d // 1d(일), 7d(주), 30d(월)


**응답 (200 OK):**
```json
{
  "period": "1d",
  "totalTransactions": 10250,
  "abnormalTransactions": 512,
  "normalTransactions": 9738,
  "detectionRate": 0.0499,        // 5.0%
  "accuracy": 0.95,               // 95% (테스트 데이터 기준)
  "averageConfidenceScore": 0.72,
  "topAbnormalReasons": [
    {
      "reason": "HIGH_AMOUNT",
      "count": 250,
      "percentage": 48.8
    },
    {
      "reason": "ODD_TIME",
      "count": 150,
      "percentage": 29.3
    },
    {
      "reason": "NEW_MERCHANT",
      "count": 80,
      "percentage": 15.6
    }
  ],
  "timestamp": "2026-09-15T14:35:00"
}
```

### 3.2 GET /monitoring/alerts?severity=WARNING&limit=10
최근 알림 조회

**쿼리 파라미터:**
```
severity=WARNING // INFO, WARNING, CRITICAL
limit=10 // 조회 건수
offset=0 // 페이징 오프셋
```

**응답 (200 OK):**
```json
{
  "alerts": [
    {
      "id": 1,
      "transactionId": 1002,
      "userId": 1,
      "severity": "WARNING",
      "message": "거래액이 비정상 높습니다 (Z-score: 3.5)",
      "patterns": ["HIGH_AMOUNT", "ODD_TIME"],
      "confidenceScore": 0.8,
      "isResolved": false,
      "createdAt": "2026-09-15T02:30:05",
      "resolvedAt": null
    }
  ],
  "pagination": {
    "total": 512,
    "limit": 10,
    "offset": 0,
    "hasMore": true
  },
  "timestamp": "2026-09-15T14:35:00"
}
```

### 3.3 PATCH /monitoring/alerts/{id}
알림 상태 업데이트

**요청:**
```json
{
  "isResolved": true,
  "resolution": "거래 승인"
}
```

**응답 (200 OK):**
```json
{
  "id": 1,
  "isResolved": true,
  "resolvedAt": "2026-09-15T14:35:00"
}
```

---

## 4. 통계 API

### 4.1 GET /statistics/user/{userId}
사용자별 통계

**응답 (200 OK):**
```json
{
  "userId": 1,
  "totalTransactions": 250,
  "totalAmount": 125000000,
  "avgAmount": 500000,
  "stdAmount": 200000,
  "minAmount": 1000,
  "maxAmount": 2000000,
  "avgTransactionsPerDay": 12,
  "mostUsedCategory": "retail",
  "lastTransaction": "2026-09-15T14:30:00",
  "timestamp": "2026-09-15T14:35:00"
}
```

---

## 5. WebSocket

### 5.1 ws://localhost:8080/ws/transactions

**연결 요청:**
```json
{
  "type": "SUBSCRIBE",
  "userId": 1          // 선택, 특정 사용자만 구독
}
```

**수신 메시지 (정상 거래):**
```json
{
  "type": "TRANSACTION_CREATED",
  "transaction": {
    "id": 1001,
    "userId": 1,
    "amount": 500000,
    "isAbnormal": false,
    "timestamp": "2026-09-15T14:30:00"
  }
}
```

**수신 메시지 (이상 거래 - 즉시 전송):**
```json
{
  "type": "ANOMALY_DETECTED",
  "alert": {
    "severity": "WARNING",
    "message": "HIGH_AMOUNT, ODD_TIME 탐지",
    "confidenceScore": 0.8,
    "transaction": {
      "id": 1002,
      "userId": 1,
      "amount": 5000000,
      "timestamp": "2026-09-15T02:30:00"
    }
  }
}
```

**수신 메시지 (통계 업데이트):**
```json
{
  "type": "STATISTICS_UPDATED",
  "statistics": {
    "totalProcessed": 10250,
    "abnormalCount": 512,
    "avgProcessingTime": "0.8s"
  }
}
```

---

## 6. 에러 처리

### 공통 에러 응답 형식
```json
{
  "error": "ERROR_CODE",
  "message": "사용자 친화적 메시지",
  "details": {
    "field": "필드명",
    "constraint": "제약조건",
    "value": "잘못된 값"
  },
  "traceId": "abc123def456",  // 서버 로깅용
  "timestamp": "2026-09-15T14:30:05"
}
```

### 에러 코드 정의
| 코드 | HTTP | 의미 |
|------|------|------|
| BAD_REQUEST | 400 | 요청 형식 오류 |
| INVALID_AMOUNT | 400 | 거래액 범위 오류 |
| INVALID_USER | 400 | 사용자 ID 오류 |
| UNAUTHORIZED | 401 | 인증 실패 |
| DUPLICATE_TRANSACTION | 409 | 중복 거래 |
| NOT_FOUND | 404 | 리소스 없음 |
| INTERNAL_SERVER_ERROR | 500 | 서버 오류 |

---

## 7. 성능 요구사항

| 항목 | 목표 |
|------|------|
| 거래 처리 시간 | < 100ms |
| 이상 탐지 시간 | < 1s |
| 대시보드 업데이트 | < 500ms |
| 동시 처리 능력 | 1000+ TPS |
| 가용성 | 99.9% |

---

## 8. 버전 관리

API 버전: v1
URL: /api/v1/transactions

향후 v2 출시 시:

/api/v2/transactions
하위 호환성 유지

---

## 9. Rate Limiting

요청 제한:

사용자당: 1000 requests/hour
IP당: 5000 requests/hour

응답 헤더:
X-RateLimit-Limit: 1000
X-RateLimit-Remaining: 999
X-RateLimit-Reset: 1694774400


---

## 10. 캐싱 전략

GET 요청:

Cache-Control: max-age=300 (5분)

WebSocket: 실시간이므로 캐싱 없음

통계: Cache-Control: max-age=60 (1분)