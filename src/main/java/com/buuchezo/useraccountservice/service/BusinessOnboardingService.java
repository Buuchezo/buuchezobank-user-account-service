package com.buuchezo.useraccountservice.service;

import com.buuchezo.useraccountservice.dto.ApiResponse;
import com.buuchezo.useraccountservice.dto.BusinessOnboardingRequest;
import com.buuchezo.useraccountservice.dto.BusinessOnboardingResponse;

public interface BusinessOnboardingService {

    ApiResponse<BusinessOnboardingResponse> onboard(
            BusinessOnboardingRequest request
    );
}
