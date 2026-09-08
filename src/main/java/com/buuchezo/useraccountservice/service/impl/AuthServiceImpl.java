package com.buuchezo.useraccountservice.service.impl;

import com.buuchezo.useraccountservice.dto.*;
import com.buuchezo.useraccountservice.entity.Account;
import com.buuchezo.useraccountservice.entity.Role;
import com.buuchezo.useraccountservice.entity.User;
import com.buuchezo.useraccountservice.enums.AccountStatus;
import com.buuchezo.useraccountservice.enums.AccountType;
import com.buuchezo.useraccountservice.enums.Currency;
import com.buuchezo.useraccountservice.exceptions.BadRequestException;
import com.buuchezo.useraccountservice.exceptions.NotFoundException;
import com.buuchezo.useraccountservice.kafka.dto.UserRegistrationEvent;
import com.buuchezo.useraccountservice.kafka.service.AccountEventPublisher;
import com.buuchezo.useraccountservice.repository.AccountRepository;
import com.buuchezo.useraccountservice.repository.RoleRepository;
import com.buuchezo.useraccountservice.repository.UserRepository;
import com.buuchezo.useraccountservice.security.JwtService;
import com.buuchezo.useraccountservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final JwtService jwtService;
    private final ModelMapper modelMapper;
    private final AccountEventPublisher accountEventPublisher;

    @Override
    public ApiResponse<AuthResponse> registerUser(RegistrationRequest registrationRequest) {
        log.info("We are inside the register user service method");
        if (userRepository.existsByEmail(registrationRequest.getEmail())) {
            throw new BadRequestException("User with this email already exists");
        }
        Set<Role> roles = new HashSet<>();
        String roleName = (registrationRequest.getRole() != null && !registrationRequest
                .getRole()
                .isBlank())
                ? registrationRequest.getRole().toUpperCase() : "CUSTOMER";

        var assignedRole = roleRepository.findByName(roleName)
                .orElseThrow(() -> new NotFoundException("Role with name " + roleName + " not found"));

        roles.add(assignedRole);

        var registeredUser = User.builder()
                .email(registrationRequest.getEmail())
                .password(passwordEncoder.encode(registrationRequest.getPassword()))
                .firstName(registrationRequest.getFirstName())
                .lastName(registrationRequest.getLastName())
                .enabled(true)
                .roles(roles)
                .build();

        var newUser = userRepository.save(registeredUser);

        // TODO Generate a unique account number and send and an email out to him/her account details
        //  and the save the account number to the database


        //Generate a unique number for the user
        String accountNumber = generateUniqueAccountNumber();
        var accountToSaveToDb = Account.builder()
                .accountNumber(accountNumber)
                .balance(BigDecimal.ZERO)
                .currency(Currency.USD)
                .accountType(AccountType.SAVINGS)
                .accountStatus(AccountStatus.ACTIVE)
                .user(newUser)
                .build();
        accountRepository.save(accountToSaveToDb);

        //Publish event out to the notification service
        var userRegistrationEvent = UserRegistrationEvent.builder()
                .email(newUser.getEmail())
                .firstName(newUser.getFirstName())
                .lastName(newUser.getLastName())
                .accountNumber(accountNumber)
                .bankName("Buuchezo Bank")
                .build();
        //it´s going to publish through kafka
        accountEventPublisher.publishUserRegistrationEvent(userRegistrationEvent);

        // Generate token for te user


        // Convert to dto

        var userDto = modelMapper.map(newUser, UserDto.class);
        var authResponse = AuthResponse.builder()
                .user(userDto)
                .build();

        return new ApiResponse<>(HttpStatus.CREATED.value(), "User registered successfully", authResponse);
    }

    @Override
    public ApiResponse<AuthResponse> loginUser(LoginRequest loginRequest) {
        log.info("inside login user service method");
        var user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() ->
                        new NotFoundException("User with email " + loginRequest.getEmail() + " not found"));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new BadRequestException("Password doesn´t match");
        }
        if (!user.isEnabled()) {
            throw new BadRequestException("User is not enabled");
        }

        List<String> roles = user.getRoles().stream().map(Role::getName).toList();

        String token = jwtService.generateToken(user.getEmail(), roles);
        var userDto = modelMapper.map(user, UserDto.class);
        AuthResponse authResponse = AuthResponse
                .builder()
                .token(token)
                .user(userDto)
                .build();
        return new ApiResponse<>(HttpStatus.OK.value(), "User logged in successfully", authResponse);
    }

    private String generateUniqueAccountNumber() {
        String accountNumber;
        var random = ThreadLocalRandom.current();
        do {
            int randomPart = random.nextInt(100_000_000);
            accountNumber = String.format("00%08d", randomPart);
        } while (accountRepository.existsByAccountNumber(accountNumber));
        return accountNumber;
    }
}