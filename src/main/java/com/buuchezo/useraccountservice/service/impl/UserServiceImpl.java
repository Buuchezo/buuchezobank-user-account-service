package com.buuchezo.useraccountservice.service.impl;

import com.buuchezo.useraccountservice.dto.*;
import com.buuchezo.useraccountservice.entity.Account;
import com.buuchezo.useraccountservice.entity.User;
import com.buuchezo.useraccountservice.exceptions.BadRequestException;
import com.buuchezo.useraccountservice.exceptions.NotFoundException;
import com.buuchezo.useraccountservice.repository.AccountRepository;
import com.buuchezo.useraccountservice.repository.UserRepository;
import com.buuchezo.useraccountservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;


@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final ModelMapper modelMapper;


    @Override
    public ApiResponse<UserWithAccountDto> getMyDetails() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Inside getUserDetails user email from authentication is : {}", email);

        var user = userRepository.findByEmail(email).orElseThrow(() -> new NotFoundException("User not found"));
        var account = accountRepository.findByUser(user).orElseThrow(() -> new NotFoundException("Account not found"));

        var userWithAccountDto = mapToUserWithAccount(user, account);
        return new ApiResponse<>(
                HttpStatus.OK.value(),
                "Profile retrieved",
                userWithAccountDto);
    }

    @Override
    public ApiResponse<UserWithAccountDto> searchUser(String email, String accountNumber) {
        log.info("Searching for a user");

        User user;
        Account account;
        if (email != null && !email.isBlank()) {
            user = userRepository
                    .findByEmail(email)
                    .orElseThrow(() -> new NotFoundException("User not found."));
            account = accountRepository.findByUser(user).orElseThrow(() -> new NotFoundException("Account not found"));
        } else if (accountNumber != null && !accountNumber.isBlank()) {
            account = accountRepository
                    .findByAccountNumber(accountNumber)
                    .orElseThrow(() -> new NotFoundException("User not found."));
            user = account.getUser();
        } else {
            throw new BadRequestException("An Email or Account Number is required");
        }
        var userWithAccountDto = mapToUserWithAccount(user, account);
        return new ApiResponse<>(
                HttpStatus.OK.value(),
                "Profile retrieved successfully",
                userWithAccountDto);
    }

    @Override
    public ApiResponse<Page<UserDto>> getAllUsers(String roleName, Pageable pageable) {
        log.info("Getting all users");
        var sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by("createdAt").descending()
        );

        Page<User> userPage;
        if (roleName != null && !roleName.isBlank()) {
            userPage = userRepository.findByRoleName(roleName.toUpperCase(), sortedPageable);
        } else {
            userPage = userRepository.findAll(sortedPageable);
        }

        Page<UserDto> dtoPage = userPage.map(user -> modelMapper.map(user, UserDto.class));
        return new ApiResponse<>(
                HttpStatus.OK.value(),
                "Users fetched successfully",
                dtoPage
        );
    }

    @Override
    public ApiResponse<UserStatisticsDto> getEntireUserStatistics() {
        Long total = userRepository.count();
        Long enabled = userRepository.countByEnabledTrue();

        var stats = UserStatisticsDto
                .builder()
                .totalUsers(total)
                .activeUsers(enabled)
                .inactiveUsers(total - enabled)
                .totalAccounts(accountRepository.count())
                .averageAccountPerUser(accountRepository.count())
                .customersCount(userRepository.countByRoleName("CUSTOMER"))
                .adminsCount(userRepository.countByRoleName("ADMIN"))
                .build();
        return new ApiResponse<>(
                HttpStatus.OK.value(),
                "Statistics fetched successfully",
                stats
        );
    }

    @Override
    public ApiResponse<String> toggleUserStatus(Long userId) {
        var user = userRepository
                .findById(userId)
                .orElseThrow(() -> new NotFoundException("User with not found"));

        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
        String status = user.isEnabled() ? "Enabled" : "Disabled";
        log.info("User has been {}", status);

        return new ApiResponse<>(
                HttpStatus.OK.value(),
                "User status changed to " + status,
                null
        );
    }

    private UserWithAccountDto mapToUserWithAccount(User user, Account account) {
        return UserWithAccountDto
                .builder()
                .user(modelMapper.map(user, UserDto.class))
                .account(modelMapper.map(account, AccountDto.class))
                .build();
    }
}
