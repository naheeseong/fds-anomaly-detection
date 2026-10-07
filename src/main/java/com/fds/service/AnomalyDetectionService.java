package com.fds.service;

import com.fds.entity.Transaction;
import com.fds.entity.UserStatistics;
import com.fds.repository.TransactionRepository;
import com.fds.repository.UserStatisticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * docs/anomaly-rules.md 의 5가지 규칙을 적용해 거래의 이상 여부를 판정한다.
 */
@Service
@RequiredArgsConstructor
public class AnomalyDetectionService {

    private static final BigDecimal Z_SCORE_THRESHOLD = BigDecimal.valueOf(3);
    private static final BigDecimal ODD_TIME_AMOUNT_THRESHOLD = BigDecimal.valueOf(1_000_000);
    private static final int ODD_TIME_START_HOUR = 0;
    private static final int ODD_TIME_END_HOUR = 6;
    private static final int HIGH_FREQUENCY_THRESHOLD = 10;
    private static final double CONFIDENCE_PER_RULE = 0.2;

    private static final Set<String> BLACKLISTED_MERCHANTS = Set.of("SCAM_001", "FRAUD_002");

    public static final String HIGH_AMOUNT = "HIGH_AMOUNT";
    public static final String ODD_TIME = "ODD_TIME";
    public static final String BLACKLIST_MERCHANT = "BLACKLIST_MERCHANT";
    public static final String HIGH_FREQUENCY = "HIGH_FREQUENCY";
    public static final String NEW_MERCHANT = "NEW_MERCHANT";

    private final UserStatisticsRepository userStatisticsRepository;
    private final TransactionRepository transactionRepository;

    public AnomalyDetectionResult detect(Transaction transaction) {
        List<String> patterns = new ArrayList<>();

        if (isHighAmount(transaction)) {
            patterns.add(HIGH_AMOUNT);
        }
        if (isOddTime(transaction)) {
            patterns.add(ODD_TIME);
        }
        if (isBlacklistedMerchant(transaction)) {
            patterns.add(BLACKLIST_MERCHANT);
        }
        if (isHighFrequency(transaction)) {
            patterns.add(HIGH_FREQUENCY);
        }
        if (isNewMerchant(transaction)) {
            patterns.add(NEW_MERCHANT);
        }

        double confidenceScore = Math.min(1.0, patterns.size() * CONFIDENCE_PER_RULE);
        return new AnomalyDetectionResult(patterns, confidenceScore);
    }

    /** Rule 1: 거래액 이상 (Z-score) */
    boolean isHighAmount(Transaction transaction) {
        Optional<UserStatistics> statsOptional = userStatisticsRepository.findById(transaction.getUserId());
        if (statsOptional.isEmpty()) {
            return false;
        }

        UserStatistics stats = statsOptional.get();
        BigDecimal mean = stats.getAvgAmount();
        BigDecimal std = stats.getStdAmount();
        if (mean == null || std == null || std.compareTo(BigDecimal.ZERO) == 0) {
            return false;
        }

        BigDecimal threshold = std.multiply(Z_SCORE_THRESHOLD);
        BigDecimal diff = transaction.getAmount().subtract(mean).abs();
        return diff.compareTo(threshold) > 0;
    }

    /** Rule 2: 시간대 이상 (새벽 대액 거래, 일요일 자정 이후 거래) */
    boolean isOddTime(Transaction transaction) {
        LocalDateTime time = transaction.getTransactionTime();
        int hour = time.getHour();
        boolean lateNight = hour >= ODD_TIME_START_HOUR && hour < ODD_TIME_END_HOUR;

        boolean lateNightHighAmount = lateNight
                && transaction.getAmount().compareTo(ODD_TIME_AMOUNT_THRESHOLD) > 0;
        boolean sundayAfterMidnight = lateNight && time.getDayOfWeek() == DayOfWeek.SUNDAY;

        return lateNightHighAmount || sundayAfterMidnight;
    }

    /** Rule 3: 거래처 이상 (블랙리스트) */
    boolean isBlacklistedMerchant(Transaction transaction) {
        return BLACKLISTED_MERCHANTS.contains(transaction.getMerchantId());
    }

    /** Rule 4: 거래 빈도 이상 (1시간 내 10회 이상) */
    boolean isHighFrequency(Transaction transaction) {
        LocalDateTime time = transaction.getTransactionTime();
        long count = transactionRepository.countByUserIdAndTransactionTimeBetween(
                transaction.getUserId(), time.minusHours(1), time);
        return count >= HIGH_FREQUENCY_THRESHOLD;
    }

    /** Rule 5: 신규 거래처 이상 */
    boolean isNewMerchant(Transaction transaction) {
        return !transactionRepository.existsByUserIdAndMerchantId(
                transaction.getUserId(), transaction.getMerchantId());
    }
}
