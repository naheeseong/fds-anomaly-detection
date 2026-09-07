package com.fds.service;

import com.fds.dto.TransactionRequest;
import com.fds.entity.Transaction;
import com.fds.exception.TransactionNotFoundException;
import com.fds.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;

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
        return transactionRepository.save(transaction);
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
