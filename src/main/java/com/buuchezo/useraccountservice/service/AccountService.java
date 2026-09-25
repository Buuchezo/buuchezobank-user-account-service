package com.buuchezo.useraccountservice.service;

import com.buuchezo.useraccountservice.dto.AccountDto;
import com.buuchezo.useraccountservice.dto.CreateBusinessAccountRequest;
import com.buuchezo.useraccountservice.dto.ApiResponse;
import com.buuchezo.useraccountservice.enums.AccountStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AccountService {

    ApiResponse<AccountDto> getMyAccount();

    ApiResponse<List<AccountDto>> getMyAccounts();

    ApiResponse<AccountDto> getAccountNumber(String accountNumber);

    ApiResponse<AccountDto> changeAccountStatus(
            String accountNumber,
            AccountStatus status
    );

    ApiResponse<Page<AccountDto>> getAllAccount(Pageable pageable);

    ApiResponse<AccountDto> createBusinessAccount(
            Long businessId,
            CreateBusinessAccountRequest request,
            String userEmail
    );

    ApiResponse<List<AccountDto>> getBusinessAccounts(
            Long businessId,
            String userEmail
    );
}
