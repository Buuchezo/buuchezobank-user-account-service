package com.buuchezo.useraccountservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessOnboardingRequest {

    @Valid
    @NotNull(message = "Owner information is required")
    private BusinessOwnerRegistrationRequest owner;

    @Valid
    @NotNull(message = "Business information is required")
    private CreateBusinessRequest business;

    @Valid
    @NotNull(message = "Account information is required")
    private CreateBusinessAccountRequest account;
}
