package com.buuchezo.useraccountservice.service;

import com.buuchezo.useraccountservice.dto.AdminLoginRequest;
import com.buuchezo.useraccountservice.dto.ApiResponse;
import com.buuchezo.useraccountservice.dto.AuthResponse;

public interface AdminAuthService {

    ApiResponse<AuthResponse> loginAdmin(AdminLoginRequest request);
}
