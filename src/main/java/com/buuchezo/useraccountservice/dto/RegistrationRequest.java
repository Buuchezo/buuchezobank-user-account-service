package com.buuchezo.useraccountservice.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegistrationRequest {
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String role;
}
