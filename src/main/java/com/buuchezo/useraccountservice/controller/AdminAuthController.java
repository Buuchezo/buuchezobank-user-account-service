package com.buuchezo.useraccountservice.controller;

import com.buuchezo.useraccountservice.dto.AdminLoginRequest;
import com.buuchezo.useraccountservice.dto.ApiResponse;
import com.buuchezo.useraccountservice.dto.AuthResponse;
import com.buuchezo.useraccountservice.service.AdminAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/auth")
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> loginAdmin(
            @Valid @RequestBody AdminLoginRequest request
    ) {
        return ResponseEntity.ok(
                adminAuthService.loginAdmin(request)
        );
    }
}
