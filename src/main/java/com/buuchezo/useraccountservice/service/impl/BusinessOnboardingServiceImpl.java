package com.buuchezo.useraccountservice.service.impl;

import com.buuchezo.useraccountservice.dto.*;
import com.buuchezo.useraccountservice.entity.Role;
import com.buuchezo.useraccountservice.entity.User;
import com.buuchezo.useraccountservice.exceptions.BadRequestException;
import com.buuchezo.useraccountservice.exceptions.NotFoundException;
import com.buuchezo.useraccountservice.repository.RoleRepository;
import com.buuchezo.useraccountservice.repository.UserRepository;
import com.buuchezo.useraccountservice.security.JwtService;
import com.buuchezo.useraccountservice.service.AccountService;
import com.buuchezo.useraccountservice.service.BusinessOnboardingService;
import com.buuchezo.useraccountservice.service.BusinessService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BusinessOnboardingServiceImpl
        implements BusinessOnboardingService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final ModelMapper modelMapper;

    private final BusinessService businessService;
    private final AccountService accountService;

    @Override
    @Transactional
    public ApiResponse<BusinessOnboardingResponse> onboard(
            BusinessOnboardingRequest request
    ) {

        BusinessOwnerRegistrationRequest owner = request.getOwner();

        /*
         * ---------------------------------------------------------
         * 1. Validate owner
         * ---------------------------------------------------------
         */

        if (userRepository.existsByEmail(owner.getEmail())) {
            throw new BadRequestException(
                    "User with this email already exists"
            );
        }

        /*
         * ---------------------------------------------------------
         * 2. Create CUSTOMER user
         *
         * IMPORTANT:
         * The public business onboarding flow can NEVER create
         * ADMIN authority.
         * ---------------------------------------------------------
         */

        Role customerRole = roleRepository.findByName("CUSTOMER")
                .orElseThrow(() ->
                        new NotFoundException(
                                "Role with name CUSTOMER not found"
                        )
                );

        Set<Role> roles = new HashSet<>();
        roles.add(customerRole);

        User user = User.builder()
                .email(owner.getEmail())
                .password(
                        passwordEncoder.encode(owner.getPassword())
                )
                .firstName(owner.getFirstName())
                .lastName(owner.getLastName())
                .enabled(true)
                .twoFactorEnabled(false)
                .roles(roles)
                .build();

        User savedUser = userRepository.save(user);

        /*
         * ---------------------------------------------------------
         * 3. Create Business + OWNER membership
         *
         * Existing BusinessService creates the OWNER membership.
         * ---------------------------------------------------------
         */

        BusinessDto business = businessService.createBusiness(
                request.getBusiness(),
                savedUser.getEmail()
        );

        /*
         * ---------------------------------------------------------
         * 4. Create BUSINESS account
         *
         * Existing AccountService checks the OWNER membership and
         * creates an account with:
         *
         * ownershipType = BUSINESS
         * user          = NULL
         * business      = business
         * ---------------------------------------------------------
         */

        ApiResponse<AccountDto> accountResponse =
                accountService.createBusinessAccount(
                        business.getId(),
                        request.getAccount(),
                        savedUser.getEmail()
                );

        AccountDto account = accountResponse.data();

        /*
         * ---------------------------------------------------------
         * 5. Generate CUSTOMER JWT
         * ---------------------------------------------------------
         */

        String token = jwtService.generateToken(
                savedUser.getEmail(),
                List.of("CUSTOMER")
        );

        UserDto userDto = modelMapper.map(
                savedUser,
                UserDto.class
        );

        AuthResponse authentication = AuthResponse.builder()
                .token(token)
                .user(userDto)
                .requiresTwoFactor(false)
                .build();

        BusinessOnboardingResponse response =
                BusinessOnboardingResponse.builder()
                        .authentication(authentication)
                        .business(business)
                        .account(account)
                        .build();

        return new ApiResponse<>(
                HttpStatus.CREATED.value(),
                "Business account onboarding completed successfully",
                response
        );
    }
}
