package com.buuchezo.useraccountservice.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Builder
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserStatisticsDto {

    private Long totalUsers;
    private Long activeUsers;
    private Long inactiveUsers;
    private Long totalAccounts;
    private Long averageAccountPerUser;
    private Long customersCount;
    private Long adminsCount;

}
