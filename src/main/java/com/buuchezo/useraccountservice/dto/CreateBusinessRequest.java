package com.buuchezo.useraccountservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBusinessRequest {

    @NotBlank
    private String legalName;

    private String tradingName;

    @NotBlank
    private String registrationNumber;

    @NotBlank
    @Email
    private String email;

    private String phone;

    private String address;

    private String city;

    private String country;
}
