package com.buuchezo.useraccountservice.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessDto {

    private Long id;

    private String legalName;

    private String tradingName;

    private String registrationNumber;

    private String email;

    private String phone;

    private String address;

    private String city;

    private String country;

    private boolean active;

    private LocalDateTime createdAt;
}
