# 이상 거래 탐지 규칙

## 1. Rule 1: 거래액 이상 (HIGH_AMOUNT)

### 방법: 기술통계 (Z-score)

**수식:**

Z-score = (거래액 - 평균) / 표준편차

if |Z-score| > 3:
이상 거래로 판정 (신뢰도: 99.7%)


**예시:**
- 평균 거래액: 500,000원
- 표준편차: 200,000원
- 임계값: 500,000 ± (200,000 × 3) = [-100,000 ~ 1,100,000원]
- 2,000,000원 거래 → **이상** (Z = 7.5)

**구현:**
```java
public boolean isAbnormalAmount(Long userId, BigDecimal amount) {
    UserStatistics stats = statisticsRepository.findByUserId(userId);
    BigDecimal mean = stats.getAvgAmount();
    BigDecimal std = stats.getStdAmount();
    BigDecimal threshold = std.multiply(BigDecimal.valueOf(3));
    BigDecimal diff = amount.subtract(mean).abs();
    return diff.compareTo(threshold) > 0;
}
```

## 2. Rule 2: 시간대 이상 (ODD_TIME)

**규칙:**
- 새벽 00~06시 + 대액(>1,000,000원) = 의심
- 일요일 자정 후 거래 = 의심

**예시:**
- 오전 2시 300만원 거래 → **이상**
- 오후 2시 300만원 거래 → **정상**

## 3. Rule 3: 거래처 이상 (BLACKLIST_MERCHANT)

**규칙:**
- 블랙리스트 가맹점 = 즉시 차단

**예시:**

블랙리스트 = ["SCAM_001", "FRAUD_002"]
if merchant_id in 블랙리스트 → 이상


## 4. Rule 4: 거래 빈도 이상 (HIGH_FREQUENCY)

**규칙:**
- 1시간 내 10회 이상 거래 = 자동화 의심

**예시:**

14:00~15:00 거래 15건 → 자동화 거래 의심


## 5. Rule 5: 신규 거래처 (NEW_MERCHANT)

**규칙:**
- 해당 사용자가 처음 거래하는 가맹점

**예시:**

if 거래처가 사용자의 거래 이력에 없으면 → 신규 거래처


## 6. 신뢰도 점수 계산

**수식:**

신뢰도 = (감지된 규칙 수 × 0.2) × 100%

예시:

Rule 1 + Rule 5: 신뢰도 40%
Rule 1 + Rule 2 + Rule 4: 신뢰도 60%
4개 이상: 신뢰도 80% 이상

## 7. 데이터분석개론 연계

| 규칙 | 학습 개념 | 수강 시기 |
|------|---------|---------|
| Rule 1 | 기술통계, Z-score | 10월 중순 |
| Rule 2 | 시계열 패턴 분석 | 10월 말 |
| Rule 3 | 범주형 데이터 | 진행 중 |
| Rule 4 | 빈도 분석 | 진행 중 |
| Rule 5 | 신규성 분석 | 진행 중 |