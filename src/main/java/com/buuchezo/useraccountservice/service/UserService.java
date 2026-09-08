package com.buuchezo.useraccountservice.service;

import com.buuchezo.useraccountservice.dto.ApiResponse;
import com.buuchezo.useraccountservice.dto.UserDto;
import com.buuchezo.useraccountservice.dto.UserStatisticsDto;
import com.buuchezo.useraccountservice.dto.UserWithAccountDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    ApiResponse<UserWithAccountDto> getMyDetails();

    ApiResponse<UserWithAccountDto> searchUser(String email, String accountNumber);

    ApiResponse<Page<UserDto>> getAllUsers(String roleName, Pageable pageable);

    ApiResponse<UserStatisticsDto> getEntireUserStatistics();

    ApiResponse<String> toggleUserStatus(Long id);
}
