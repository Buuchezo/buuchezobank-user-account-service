package com.buuchezo.useraccountservice.service;

import com.buuchezo.useraccountservice.dto.*;

public interface AuthService {

    ApiResponse<AuthResponse> registerUser(RegistrationRequest registrationRequest);

    ApiResponse<AuthResponse> loginUser(LoginRequest loginRequest);

    ApiResponse<TwoFactorSetupResponse> setupTwoFactor(String email);

    ApiResponse<TwoFactorSetupResponse> verifyTwoFactorSetup(
            String email,
            TwoFactorVerifyRequest request
    );

    ApiResponse<Void> disableTwoFactor(
            String email,
            TwoFactorVerifyRequest request
    );

    ApiResponse<AuthResponse> verifyTwoFactorLogin(
            TwoFactorLoginRequest request
    );
}