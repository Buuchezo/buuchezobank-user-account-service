package com.buuchezo.useraccountservice.controller;


import com.buuchezo.useraccountservice.dto.AccountDto;
import com.buuchezo.useraccountservice.dto.ApiResponse;
import com.buuchezo.useraccountservice.enums.AccountStatus;
import com.buuchezo.useraccountservice.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
@RequestMapping("/api/accounts/admin")
public class AdminAccountController {

    private final AccountService accountService;


    @GetMapping("/all")
    public ResponseEntity<ApiResponse<Page<AccountDto>>> listAllAccounts(
            @PageableDefault(page = 0, size = 100) Pageable pageable
    ) {
        return ResponseEntity.ok(accountService.getAllAccount(pageable));
    }

    @PatchMapping("/status")
    public ResponseEntity<ApiResponse<AccountDto>> changeAccountStatus(
            @RequestParam String accountNumber,
            @RequestParam AccountStatus status
    ) {
        return ResponseEntity.ok(accountService.changeAccountStatus(accountNumber, status));
    }
}
