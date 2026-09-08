package com.buuchezo.useraccountservice.dto;


import com.buuchezo.useraccountservice.enums.AccountStatus;
import com.buuchezo.useraccountservice.enums.AccountType;
import com.buuchezo.useraccountservice.enums.Currency;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountDto {

    private Long id;
    private String accountNumber;
    private BigDecimal balance;
    private Currency currency;   // USD EUR
    private AccountType accountType; // SAVINGS CURRENT CHECKING
    private AccountStatus accountStatus; // ACTIVE INACTIVE CLOSE
    private String ownerEmail;
    private LocalDateTime createdAt;
}
