package com.onebank.transaction.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "transaction")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int transactionId;
    private BigDecimal debitAmount;
    private BigDecimal creditAmount;
    private String accountNumber;
    private String currentBalance;
    @CreationTimestamp
    private LocalDateTime checkedIn;

}

