package com.buuchezo.useraccountservice.dto;

import com.buuchezo.useraccountservice.enums.AccountOwnershipType;
import com.buuchezo.useraccountservice.enums.AccountStatus;
import com.buuchezo.useraccountservice.enums.AccountType;
import com.buuchezo.useraccountservice.enums.Currency;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDto {

    private Long id;

    private String accountNumber;

    private BigDecimal balance;

    private Currency currency;

    private AccountType accountType;

    private AccountStatus accountStatus;

    private AccountOwnershipType ownershipType;

    private String ownerEmail;

    private Long businessId;

    private String businessName;

    private LocalDateTime createdAt;
}
