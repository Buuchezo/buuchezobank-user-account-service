package com.buuchezo.useraccountservice.controller;

import com.buuchezo.useraccountservice.dto.*;
import com.buuchezo.useraccountservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> registerUser(
            @Valid @RequestBody RegistrationRequest registrationRequest
    ) {

        return ResponseEntity.ok(
                authService.registerUser(registrationRequest)
        );
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> loginUser(
            @Valid @RequestBody LoginRequest loginRequest
    ) {

        return ResponseEntity.ok(
                authService.loginUser(loginRequest)
        );
    }

    @PostMapping("/2fa/setup")
    public ResponseEntity<ApiResponse<TwoFactorSetupResponse>> setupTwoFactor(
            Authentication authentication
    ) {

        String email = authentication.getName();

        return ResponseEntity.ok(
                authService.setupTwoFactor(email)
        );
    }

    @PostMapping("/2fa/verify")
    public ResponseEntity<ApiResponse<TwoFactorSetupResponse>> verifyTwoFactor(
            Authentication authentication,
            @Valid @RequestBody TwoFactorVerifyRequest request
    ) {

        String email = authentication.getName();

        return ResponseEntity.ok(
                authService.verifyTwoFactorSetup(
                        email,
                        request
                )
        );
    }

    @PostMapping("/2fa/disable")
    public ResponseEntity<ApiResponse<Void>> disableTwoFactor(
            Authentication authentication,
            @Valid @RequestBody TwoFactorVerifyRequest request
    ) {

        String email = authentication.getName();

        return ResponseEntity.ok(
                authService.disableTwoFactor(
                        email,
                        request
                )
        );
    }

    @PostMapping("/2fa/login")
    public ResponseEntity<ApiResponse<AuthResponse>> loginTwoFactor(
            @Valid @RequestBody TwoFactorLoginRequest request
    ) {

        return ResponseEntity.ok(
                authService.verifyTwoFactorLogin(request)
        );
    }
}