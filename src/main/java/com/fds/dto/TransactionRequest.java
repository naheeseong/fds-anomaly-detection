package com.fds.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class TransactionRequest {

    @NotNull
    @Positive
    private Long userId;

    @NotNull
    @DecimalMin(value = "0", inclusive = false)
    @DecimalMax(value = "999999999")
    private BigDecimal amount;

    @NotNull
    @PastOrPresent
    private LocalDateTime transactionTime;

    @NotBlank
    @Size(min = 1, max = 50)
    private String merchantId;

    @NotBlank
    @Size(min = 1, max = 100)
    private String merchantName;

    @Size(max = 50)
    private String category;

    @Size(max = 200)
    private String description;
}
