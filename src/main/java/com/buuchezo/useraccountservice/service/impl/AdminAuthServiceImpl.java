package com.buuchezo.useraccountservice.service.impl;

import com.buuchezo.useraccountservice.dto.AdminDto;
import com.buuchezo.useraccountservice.dto.AdminLoginRequest;
import com.buuchezo.useraccountservice.dto.ApiResponse;
import com.buuchezo.useraccountservice.dto.AuthResponse;
import com.buuchezo.useraccountservice.entity.Admin;
import com.buuchezo.useraccountservice.exceptions.BadRequestException;
import com.buuchezo.useraccountservice.exceptions.NotFoundException;
import com.buuchezo.useraccountservice.repository.AdminRepository;
import com.buuchezo.useraccountservice.security.JwtService;
import com.buuchezo.useraccountservice.service.AdminAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class AdminAuthServiceImpl implements AdminAuthService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public ApiResponse<AuthResponse> loginAdmin(AdminLoginRequest request) {

        Admin admin = adminRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new NotFoundException(
                                "Admin with email " + request.getEmail() + " not found"
                        )
                );

        if (!passwordEncoder.matches(
                request.getPassword(),
                admin.getPassword()
        )) {
            throw new BadRequestException("Password doesn´t match");
        }

        if (!admin.isEnabled()) {
            throw new BadRequestException("Admin account is not enabled");
        }

        String token = jwtService.generateAdminToken(admin.getEmail());

        AdminDto adminDto = AdminDto.builder()
                .id(admin.getId())
                .email(admin.getEmail())
                .firstName(admin.getFirstName())
                .lastName(admin.getLastName())
                .enabled(admin.isEnabled())
                .role(admin.getRole())
                .createdAt(admin.getCreatedAt())
                .build();

        AuthResponse authResponse = AuthResponse.builder()
                .token(token)
                .build();

        return new ApiResponse<>(
                HttpStatus.OK.value(),
                "Admin logged in successfully",
                authResponse
        );
    }
}
