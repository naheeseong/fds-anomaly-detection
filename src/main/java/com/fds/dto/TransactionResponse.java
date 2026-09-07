package com.fds.dto;

import com.fds.entity.Transaction;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class TransactionResponse {

    private Long id;
    private Long userId;
    private BigDecimal amount;
    private LocalDateTime transactionTime;
    private String merchantId;
    private String merchantName;
    private String category;
    private Boolean isAbnormal;
    private String abnormalReason;
    private String message;
    private LocalDateTime timestamp;

    public static TransactionResponse from(Transaction transaction) {
        boolean abnormal = Boolean.TRUE.equals(transaction.getIsAbnormal());
        return TransactionResponse.builder()
                .id(transaction.getId())
                .userId(transaction.getUserId())
                .amount(transaction.getAmount())
                .transactionTime(transaction.getTransactionTime())
                .merchantId(transaction.getMerchantId())
                .merchantName(transaction.getMerchantName())
                .category(transaction.getCategory())
                .isAbnormal(transaction.getIsAbnormal())
                .abnormalReason(transaction.getAbnormalReason())
                .message(abnormal ? "이상 거래 탐지" : "정상 거래")
                .timestamp(LocalDateTime.now())
                .build();
    }
}
