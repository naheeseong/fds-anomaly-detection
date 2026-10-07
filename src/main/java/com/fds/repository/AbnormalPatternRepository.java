package com.fds.repository;

import com.fds.entity.AbnormalPattern;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AbnormalPatternRepository extends JpaRepository<AbnormalPattern, Long> {

    List<AbnormalPattern> findByTransactionId(Long transactionId);
}
