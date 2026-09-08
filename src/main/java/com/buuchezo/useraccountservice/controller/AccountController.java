package com.buuchezo.useraccountservice.controller;


import com.buuchezo.useraccountservice.dto.AccountDto;
import com.buuchezo.useraccountservice.dto.ApiResponse;
import com.buuchezo.useraccountservice.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;


    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AccountDto>> getMyAccount() {
        return ResponseEntity.ok(accountService.getMyAccount());
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<ApiResponse<AccountDto>> getAccountByAccountNumber(
            @PathVariable String accountNumber
    ) {
        return ResponseEntity.ok(accountService.getAccountNumber(accountNumber));
    }
}
