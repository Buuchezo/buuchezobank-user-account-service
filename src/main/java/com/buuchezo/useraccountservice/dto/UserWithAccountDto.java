package com.buuchezo.useraccountservice.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Builder
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserWithAccountDto {

    private UserDto user;
    private AccountDto account;

}
