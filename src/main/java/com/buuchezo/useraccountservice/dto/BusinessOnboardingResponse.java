package com.buuchezo.useraccountservice.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessOnboardingResponse {

    private AuthResponse authentication;

    private BusinessDto business;

    private AccountDto account;
}
