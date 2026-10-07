package com.fds.service;

import com.fds.entity.Transaction;
import com.fds.entity.UserStatistics;
import com.fds.repository.TransactionRepository;
import com.fds.repository.UserStatisticsRepository;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AnomalyDetectionServiceTest {

    private static final LocalDateTime DAYTIME = LocalDateTime.of(2026, 10, 12, 14, 0);
    private static final LocalDateTime SUNDAY_EARLY = LocalDateTime.of(2026, 10, 1, 12, 0)
            .with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).withHour(1).withMinute(0);
    private static final LocalDateTime SATURDAY_EARLY = LocalDateTime.of(2026, 10, 1, 12, 0)
            .with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY)).withHour(2).withMinute(0);

    private record Case(String name, Transaction transaction, boolean statsPresent, BigDecimal avg,
                         BigDecimal std, boolean merchantExists, long frequencyCount, boolean expectedAbnormal) {
    }

    private AnomalyDetectionService buildService(Case c) {
        UserStatisticsRepository statisticsRepository = mock(UserStatisticsRepository.class);
        TransactionRepository transactionRepository = mock(TransactionRepository.class);

        if (c.statsPresent()) {
            when(statisticsRepository.findById(c.transaction().getUserId())).thenReturn(Optional.of(
                    UserStatistics.builder()
                            .userId(c.transaction().getUserId())
                            .avgAmount(c.avg())
                            .stdAmount(c.std())
                            .build()));
        } else {
            when(statisticsRepository.findById(anyLong())).thenReturn(Optional.empty());
        }

        when(transactionRepository.existsByUserIdAndMerchantId(anyLong(), any())).thenReturn(c.merchantExists());
        when(transactionRepository.countByUserIdAndTransactionTimeBetween(anyLong(), any(), any()))
                .thenReturn(c.frequencyCount());

        return new AnomalyDetectionService(statisticsRepository, transactionRepository);
    }

    private Transaction tx(Long userId, BigDecimal amount, LocalDateTime time, String merchantId) {
        return Transaction.builder()
                .userId(userId)
                .amount(amount)
                .transactionTime(time)
                .merchantId(merchantId)
                .merchantName("merchant")
                .build();
    }

    private List<Case> labeledDataset() {
        List<Case> cases = new ArrayList<>();

        // --- 정상 거래 (10건) ---
        cases.add(new Case("N1_no_stats_normal",
                tx(1L, BigDecimal.valueOf(50_000), DAYTIME, "M001"),
                false, null, null, true, 1, false));
        cases.add(new Case("N2_within_zscore",
                tx(2L, BigDecimal.valueOf(550_000), DAYTIME, "M002"),
                true, BigDecimal.valueOf(500_000), BigDecimal.valueOf(200_000), true, 1, false));
        cases.add(new Case("N3_small_amount",
                tx(3L, BigDecimal.valueOf(100_000), DAYTIME, "M003"),
                false, null, null, true, 0, false));
        cases.add(new Case("N4_daytime_large_amount",
                tx(4L, BigDecimal.valueOf(2_000_000), DAYTIME, "M004"),
                false, null, null, true, 1, false));
        cases.add(new Case("N5_oddtime_low_amount",
                tx(5L, BigDecimal.valueOf(500_000), DAYTIME.withHour(3), "M005"),
                false, null, null, true, 1, false));
        cases.add(new Case("N6_late_evening",
                tx(6L, BigDecimal.valueOf(100), DAYTIME.withHour(23), "M006"),
                false, null, null, true, 1, false));
        cases.add(new Case("N7_existing_merchant_moderate_freq",
                tx(7L, BigDecimal.valueOf(300_000), DAYTIME, "M007"),
                false, null, null, true, 5, false));
        cases.add(new Case("N8_zscore_boundary",
                tx(8L, BigDecimal.valueOf(1_100_000), DAYTIME, "M008"),
                true, BigDecimal.valueOf(500_000), BigDecimal.valueOf(200_000), true, 1, false));
        cases.add(new Case("N9_saturday_low_amount",
                tx(9L, BigDecimal.valueOf(500_000), SATURDAY_EARLY, "M009"),
                false, null, null, true, 1, false));
        cases.add(new Case("N10_normal_small",
                tx(10L, BigDecimal.valueOf(20_000), DAYTIME, "M010"),
                false, null, null, true, 2, false));

        // --- 이상 거래 (10건) ---
        cases.add(new Case("A1_high_amount",
                tx(11L, BigDecimal.valueOf(2_000_000), DAYTIME, "M011"),
                true, BigDecimal.valueOf(500_000), BigDecimal.valueOf(200_000), true, 1, true));
        cases.add(new Case("A2_odd_time_high_amount",
                tx(12L, BigDecimal.valueOf(5_000_000), DAYTIME.withHour(2), "M012"),
                false, null, null, true, 1, true));
        cases.add(new Case("A3_blacklist_merchant",
                tx(13L, BigDecimal.valueOf(50_000), DAYTIME, "SCAM_001"),
                false, null, null, true, 1, true));
        cases.add(new Case("A4_high_frequency",
                tx(14L, BigDecimal.valueOf(50_000), DAYTIME, "M014"),
                false, null, null, true, 10, true));
        cases.add(new Case("A5_new_merchant",
                tx(15L, BigDecimal.valueOf(50_000), DAYTIME, "M015"),
                false, null, null, false, 1, true));
        cases.add(new Case("A6_sunday_after_midnight",
                tx(16L, BigDecimal.valueOf(100_000), SUNDAY_EARLY, "M016"),
                false, null, null, true, 1, true));
        cases.add(new Case("A7_blacklist_merchant2",
                tx(17L, BigDecimal.valueOf(10_000), DAYTIME, "FRAUD_002"),
                false, null, null, true, 1, true));
        cases.add(new Case("A8_very_high_frequency",
                tx(18L, BigDecimal.valueOf(50_000), DAYTIME, "M018"),
                false, null, null, true, 15, true));
        cases.add(new Case("A9_high_amount_small_stats",
                tx(19L, BigDecimal.valueOf(500_000), DAYTIME, "M019"),
                true, BigDecimal.valueOf(100_000), BigDecimal.valueOf(10_000), true, 1, true));
        cases.add(new Case("A10_new_merchant2",
                tx(20L, BigDecimal.valueOf(10_000), DAYTIME, "M020"),
                false, null, null, false, 1, true));

        return cases;
    }

    @TestFactory
    List<DynamicTest> ruleDetectionCases() {
        List<DynamicTest> tests = new ArrayList<>();
        for (Case c : labeledDataset()) {
            tests.add(DynamicTest.dynamicTest(c.name(), () -> {
                AnomalyDetectionService service = buildService(c);
                AnomalyDetectionResult result = service.detect(c.transaction());
                assertThat(result.isAbnormal())
                        .as("case %s expected isAbnormal=%s", c.name(), c.expectedAbnormal())
                        .isEqualTo(c.expectedAbnormal());
            }));
        }
        return tests;
    }

    @Test
    void accuracyOnLabeledDataset_isAtLeast90Percent() {
        List<Case> dataset = labeledDataset();
        int correct = 0;
        for (Case c : dataset) {
            AnomalyDetectionService service = buildService(c);
            AnomalyDetectionResult result = service.detect(c.transaction());
            if (result.isAbnormal() == c.expectedAbnormal()) {
                correct++;
            }
        }

        double accuracy = (double) correct / dataset.size();
        assertThat(accuracy).isGreaterThanOrEqualTo(0.9);
    }
}
