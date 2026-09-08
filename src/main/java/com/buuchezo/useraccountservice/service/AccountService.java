package com.buuchezo.useraccountservice.service;

import com.buuchezo.useraccountservice.dto.AccountDto;
import com.buuchezo.useraccountservice.dto.ApiResponse;
import com.buuchezo.useraccountservice.enums.AccountStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AccountService {
    ApiResponse<AccountDto> getMyAccount();

    ApiResponse<AccountDto> getAccountNumber(String accountNumber);

    ApiResponse<AccountDto> changeAccountStatus(String accountNumber, AccountStatus status);

    ApiResponse<Page<AccountDto>> getAllAccount(Pageable pageable);

}
