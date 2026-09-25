package com.buuchezo.useraccountservice.entity;

import com.buuchezo.useraccountservice.enums.AccountOwnershipType;
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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String accountNumber;

    @Column(nullable = false)
    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Currency currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountType accountType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus accountStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "ownership_type")
    @Builder.Default
    private AccountOwnershipType ownershipType = AccountOwnershipType.PERSONAL;

    /*
     * Personal account owner.
     *
     * A user can have multiple accounts, so this is intentionally
     * ManyToOne rather than OneToOne.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /*
     * Business account owner.
     *
     * A business can have multiple accounts.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id")
    private Business business;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    /*
     * Optimistic locking version.
     *
     * Hibernate increments this value whenever the account is updated.
     * This prevents concurrent balance/account updates from silently
     * overwriting each other.
     */
    @Version
    @Column(nullable = false)
    @Builder.Default
    private Long version = 0L;
}
