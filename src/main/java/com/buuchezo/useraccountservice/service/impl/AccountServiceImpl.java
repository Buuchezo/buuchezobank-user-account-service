package com.buuchezo.useraccountservice.service.impl;

import com.buuchezo.useraccountservice.dto.AccountDto;
import com.buuchezo.useraccountservice.dto.ApiResponse;
import com.buuchezo.useraccountservice.entity.Account;
import com.buuchezo.useraccountservice.enums.AccountStatus;
import com.buuchezo.useraccountservice.exceptions.NotFoundException;
import com.buuchezo.useraccountservice.repository.AccountRepository;
import com.buuchezo.useraccountservice.repository.UserRepository;
import com.buuchezo.useraccountservice.service.AccountService;
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

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Override
    public ApiResponse<AccountDto> getMyAccount() {

        log.info("fetching account for logged in user");

        String userEmail = Objects.requireNonNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        ).getName();

        var user = userRepository
                .findByEmail(userEmail)
                .orElseThrow(() ->
                        new NotFoundException("User not found"));

        var account = accountRepository
                .findByUser(user)
                .orElseThrow(() ->
                        new NotFoundException("Account not found"));

        return new ApiResponse<>(
                HttpStatus.OK.value(),
                "Account retrieved",
                toDto(account)
        );
    }

    @Override
    public ApiResponse<AccountDto> getAccountNumber(
            String accountNumber
    ) {

        var account = accountRepository
                .findByAccountNumber(accountNumber)
                .orElseThrow(() ->
                        new NotFoundException("Account not found"));

        return new ApiResponse<>(
                HttpStatus.OK.value(),
                "Account retrieved",
                toDto(account)
        );
    }

    @Override
    public ApiResponse<AccountDto> changeAccountStatus(
            String accountNumber,
            AccountStatus status
    ) {

        var account = accountRepository
                .findByAccountNumber(accountNumber)
                .orElseThrow(() ->
                        new NotFoundException("Account not found"));

        account.setAccountStatus(status);

        var changedAccount = accountRepository.save(account);

        return new ApiResponse<>(
                HttpStatus.OK.value(),
                "Account status changed",
                toDto(changedAccount)
        );
    }

    @Override
    public ApiResponse<Page<AccountDto>> getAllAccount(
            Pageable pageable
    ) {

        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by("createdAt").descending()
        );

        Page<Account> accounts =
                accountRepository.findAll(sortedPageable);

        Page<AccountDto> dtoPage =
                accounts.map(this::toDto);

        return new ApiResponse<>(
                HttpStatus.OK.value(),
                "Accounts retrieved",
                dtoPage
        );
    }

    private AccountDto toDto(Account account) {

        AccountDto accountDto =
                modelMapper.map(account, AccountDto.class);

        /*
         * Personal account.
         */
        if (account.getUser() != null) {
            accountDto.setOwnerEmail(
                    account.getUser().getEmail()
            );
        }

        /*
         * Business account.
         */
        if (account.getBusiness() != null) {
            accountDto.setBusinessId(
                    account.getBusiness().getId()
            );

            accountDto.setBusinessName(
                    account.getBusiness().getTradingName() != null
                            ? account.getBusiness().getTradingName()
                            : account.getBusiness().getLegalName()
            );
        }

        return accountDto;
    }
}
