package com.buuchezo.useraccountservice.controller;

import com.buuchezo.useraccountservice.dto.AccountDto;
import com.buuchezo.useraccountservice.dto.ApiResponse;
import com.buuchezo.useraccountservice.dto.CreateBusinessAccountRequest;
import com.buuchezo.useraccountservice.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/businesses/{businessId}/accounts")
@RequiredArgsConstructor
public class BusinessAccountController {

    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<ApiResponse<AccountDto>> createBusinessAccount(
            @PathVariable Long businessId,
            @Valid @RequestBody CreateBusinessAccountRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.status(201).body(
                accountService.createBusinessAccount(
                        businessId,
                        request,
                        authentication.getName()
                )
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AccountDto>>> getBusinessAccounts(
            @PathVariable Long businessId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                accountService.getBusinessAccounts(
                        businessId,
                        authentication.getName()
                )
        );
    }
}
