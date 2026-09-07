package com.fds.entity;

import lombok.*;
import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions", indexes = {
        @Index(name = "idx_user_time", columnList = "user_id,transaction_time"),
        @Index(name = "idx_abnormal", columnList = "is_abnormal")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDateTime transactionTime;

    private String merchantId;
    private String merchantName;
    private String category;

    private Boolean isAbnormal = false;
    private String abnormalReason;

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}