package com.buuchezo.useraccountservice.entity;

import com.buuchezo.useraccountservice.enums.AccountStatus;
import com.buuchezo.useraccountservice.enums.AccountType;
import com.buuchezo.useraccountservice.enums.Currency;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity
@Table(name = "accounts")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Account {

    private final LocalDateTime createdAt = LocalDateTime.now();
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String accountNumber;

    @Column(nullable = false)
    private BigDecimal balance;

    @Version
    @Column(nullable = false)
    private Long version;

    @Enumerated(EnumType.STRING)
    private Currency currency;   // USD EUR

    @Enumerated(EnumType.STRING)
    private AccountType accountType; // SAVINGS CURRENT CHECKING

    @Enumerated(EnumType.STRING)
    private AccountStatus accountStatus; // ACTIVE INACTIVE CLOSED

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;


}
