package com.buuchezo.useraccountservice.controller;

import com.buuchezo.useraccountservice.dto.ApiResponse;
import com.buuchezo.useraccountservice.dto.BusinessOnboardingRequest;
import com.buuchezo.useraccountservice.dto.BusinessOnboardingResponse;
import com.buuchezo.useraccountservice.service.BusinessOnboardingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/businesses")
@RequiredArgsConstructor
public class BusinessOnboardingController {

    private final BusinessOnboardingService businessOnboardingService;

    @PostMapping("/onboarding")
    public ResponseEntity<ApiResponse<BusinessOnboardingResponse>> onboard(
            @Valid @RequestBody BusinessOnboardingRequest request
    ) {

        return ResponseEntity.status(201)
                .body(
                        businessOnboardingService.onboard(request)
                );
    }
}
