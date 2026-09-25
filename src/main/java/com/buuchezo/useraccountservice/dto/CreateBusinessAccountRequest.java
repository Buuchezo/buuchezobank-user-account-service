package com.buuchezo.useraccountservice.dto;

import com.buuchezo.useraccountservice.enums.AccountType;
import com.buuchezo.useraccountservice.enums.Currency;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBusinessAccountRequest {

    @NotNull
    private AccountType accountType;

    @NotNull
    private Currency currency;
}
