package com.fds.repository;

import com.fds.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Page<Transaction> findByUserId(Long userId, Pageable pageable);

    Page<Transaction> findByIsAbnormal(Boolean isAbnormal, Pageable pageable);

    Page<Transaction> findByUserIdAndIsAbnormal(Long userId, Boolean isAbnormal, Pageable pageable);

    boolean existsByUserIdAndMerchantId(Long userId, String merchantId);
}
