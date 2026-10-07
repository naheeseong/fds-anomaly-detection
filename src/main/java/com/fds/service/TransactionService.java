package com.fds.service;

import com.fds.dto.TransactionRequest;
import com.fds.entity.AbnormalPattern;
import com.fds.entity.Alert;
import com.fds.entity.Transaction;
import com.fds.exception.TransactionNotFoundException;
import com.fds.repository.AbnormalPatternRepository;
import com.fds.repository.AlertRepository;
import com.fds.repository.TransactionRepository;
import com.fds.websocket.TransactionWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private static final double CRITICAL_THRESHOLD = 0.6;
    private static final double WARNING_THRESHOLD = 0.4;

    private final TransactionRepository transactionRepository;
    private final AnomalyDetectionService anomalyDetectionService;
    private final AbnormalPatternRepository abnormalPatternRepository;
    private final AlertRepository alertRepository;
    private final TransactionWebSocketHandler webSocketHandler;

    @Transactional
    public Transaction createTransaction(TransactionRequest request) {
        Transaction transaction = Transaction.builder()
                .userId(request.getUserId())
                .amount(request.getAmount())
                .transactionTime(request.getTransactionTime())
                .merchantId(request.getMerchantId())
                .merchantName(request.getMerchantName())
                .category(request.getCategory())
                .build();

        AnomalyDetectionResult result = anomalyDetectionService.detect(transaction);
        if (result.isAbnormal()) {
            transaction.setIsAbnormal(true);
            transaction.setAbnormalReason(result.reasonSummary());
        }

        Transaction saved = transactionRepository.save(transaction);

        if (result.isAbnormal()) {
            saveAbnormalPatterns(saved, result);
            createAlert(saved, result);
        }

        webSocketHandler.broadcastTransactionCreated(saved);
        if (result.isAbnormal()) {
            webSocketHandler.broadcastAnomalyDetected(saved, result);
        }

        return saved;
    }

    private void saveAbnormalPatterns(Transaction transaction, AnomalyDetectionResult result) {
        List<AbnormalPattern> patterns = result.getPatternTypes().stream()
                .map(patternType -> AbnormalPattern.builder()
                        .transactionId(transaction.getId())
                        .patternType(patternType)
                        .confidenceScore((float) result.getConfidenceScore())
                        .detectedAt(LocalDateTime.now())
                        .build())
                .toList();
        abnormalPatternRepository.saveAll(patterns);
    }

    private void createAlert(Transaction transaction, AnomalyDetectionResult result) {
        Alert alert = Alert.builder()
                .transactionId(transaction.getId())
                .alertLevel(resolveAlertLevel(result.getConfidenceScore()))
                .message(result.reasonSummary() + " 탐지")
                .isResolved(false)
                .build();
        alertRepository.save(alert);
    }

    private String resolveAlertLevel(double confidenceScore) {
        if (confidenceScore >= CRITICAL_THRESHOLD) {
            return "CRITICAL";
        }
        if (confidenceScore >= WARNING_THRESHOLD) {
            return "WARNING";
        }
        return "INFO";
    }

    public Transaction getTransaction(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException(id));
    }

    public Page<Transaction> getTransactions(Long userId, Boolean isAbnormal, Pageable pageable) {
        if (userId != null && isAbnormal != null) {
            return transactionRepository.findByUserIdAndIsAbnormal(userId, isAbnormal, pageable);
        }
        if (userId != null) {
            return transactionRepository.findByUserId(userId, pageable);
        }
        if (isAbnormal != null) {
            return transactionRepository.findByIsAbnormal(isAbnormal, pageable);
        }
        return transactionRepository.findAll(pageable);
    }
}
