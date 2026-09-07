package com.fds.exception;

public class TransactionNotFoundException extends RuntimeException {

    public TransactionNotFoundException(Long id) {
        super("거래 ID " + id + "을(를) 찾을 수 없습니다");
    }
}
