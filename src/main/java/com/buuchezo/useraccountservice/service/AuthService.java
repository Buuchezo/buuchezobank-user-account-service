package com.buuchezo.useraccountservice.service;

import com.buuchezo.useraccountservice.dto.ApiResponse;
import com.buuchezo.useraccountservice.dto.AuthResponse;
import com.buuchezo.useraccountservice.dto.LoginRequest;
import com.buuchezo.useraccountservice.dto.RegistrationRequest;

public interface AuthService {

    ApiResponse<AuthResponse> registerUser(RegistrationRequest registrationRequest);

    ApiResponse<AuthResponse> loginUser(LoginRequest loginRequest);
}
