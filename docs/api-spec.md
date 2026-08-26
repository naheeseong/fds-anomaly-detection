# API 명세서

## 1. 기본 정보

- **Base URL**: http://localhost:8080/api
- **모든 응답**: JSON
- **인증**: (나중에 추가)

## 2. 거래 API

### POST /transactions
거래 데이터를 받아서 저장하고 이상 거래 판정

**요청:**
```json
{
  "userId": 1,
  "amount": 500000,
  "transactionTime": "2026-09-15T14:30:00",
  "merchantId": "MERCHANT_001",
  "merchantName": "편의점",
  "category": "retail"
}
```

**응답 (200 OK):**
```json
{
  "id": 1001,
  "userId": 1,
  "amount": 500000,
  "transactionTime": "2026-09-15T14:30:00",
  "isAbnormal": false,
  "abnormalReason": null,
  "confidenceScore": 0.0,
  "message": "정상 거래"
}
```

**응답 (이상 거래):**
```json
{
  "id": 1002,
  "userId": 1,
  "amount": 5000000,
  "isAbnormal": true,
  "abnormalReason": "HIGH_AMOUNT, ODD_TIME",
  "confidenceScore": 0.8,
  "message": "이상 거래 탐지됨"
}
```

## 3. 모니터링 API

### GET /monitoring/statistics
실시간 통계 조회

**응답:**
```json
{
  "totalTransactions": 10250,
  "abnormalTransactions": 512,
  "accuracy": 0.95,
  "detectionRate": "50건/오늘"
}
```

### GET /monitoring/recent-alerts
최근 이상 거래 목록

**응답:**
```json
[
  {
    "id": 1,
    "transactionId": 1001,
    "alertLevel": "WARNING",
    "message": "거래액이 비정상 높습니다 (Z-score: 3.2)",
    "createdAt": "2026-09-15T14:35:00"
  }
]
```

## 4. WebSocket

### ws://localhost:8080/ws/transactions
실시간 거래 데이터 스트림

**수신 메시지 (정상 거래):**
```json
{
  "type": "NEW_TRANSACTION",
  "transaction": {
    "id": 1001,
    "userId": 1,
    "amount": 500000,
    "isAbnormal": false
  }
}
```

**수신 메시지 (이상 거래):**
```json
{
  "type": "ABNORMAL_DETECTED",
  "transaction": {
    "id": 1002,
    "userId": 1,
    "amount": 5000000,
    "isAbnormal": true,
    "abnormalReason": "HIGH_AMOUNT"
  }
}
```

## 5. 상태 코드

| 코드 | 의미 |
|------|------|
| 200 | 성공 |
| 400 | 잘못된 요청 |
| 401 | 미인증 |
| 404 | 리소스 없음 |
| 500 | 서버 오류 |