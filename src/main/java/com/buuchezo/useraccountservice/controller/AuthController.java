package com.buuchezo.useraccountservice.controller;


import com.buuchezo.useraccountservice.dto.ApiResponse;
import com.buuchezo.useraccountservice.dto.AuthResponse;
import com.buuchezo.useraccountservice.dto.LoginRequest;
import com.buuchezo.useraccountservice.dto.RegistrationRequest;
import com.buuchezo.useraccountservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
            @Valid @RequestBody
            RegistrationRequest registrationRequest) {
        return ResponseEntity.ok(authService.registerUser(registrationRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> loginUser(
            @Valid @RequestBody
            LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.loginUser(loginRequest));
    }
}
