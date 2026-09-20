package com.buuchezo.useraccountservice.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TwoFactorSetupResponse {

    private boolean enabled;
    private String secret;
    private String otpAuthUri;
}