package com.fds.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ErrorResponse {

    private String error;
    private String message;
    private Object details;
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
