package com.buuchezo.useraccountservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TwoFactorLoginRequest {

    @NotBlank(message = "Challenge token is required")
    private String challengeToken;

    @NotBlank(message = "Verification code is required")
    @Pattern(
            regexp = "\\d{6}",
            message = "Verification code must contain exactly 6 digits"
    )
    private String code;
}