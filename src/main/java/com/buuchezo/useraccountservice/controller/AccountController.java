package com.buuchezo.useraccountservice.controller;

import com.buuchezo.useraccountservice.dto.AccountDto;
import com.buuchezo.useraccountservice.dto.ApiResponse;
import com.buuchezo.useraccountservice.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AccountDto>> getMyAccount() {
        return ResponseEntity.ok(accountService.getMyAccount());
    }

    @GetMapping("/me/all")
    public ResponseEntity<ApiResponse<List<AccountDto>>> getMyAccounts() {
        return ResponseEntity.ok(accountService.getMyAccounts());
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<ApiResponse<AccountDto>> getAccountByAccountNumber(
            @PathVariable String accountNumber
    ) {
        return ResponseEntity.ok(
                accountService.getAccountNumber(accountNumber)
        );
    }
}
