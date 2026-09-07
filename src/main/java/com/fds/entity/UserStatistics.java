package com.fds.entity;

import lombok.*;
import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_statistics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStatistics {

    @Id
    private Long userId;

    private BigDecimal avgAmount;
    private BigDecimal stdAmount;
    private Integer avgTransactionsPerDay;
    private LocalDateTime lastUpdated;
}
