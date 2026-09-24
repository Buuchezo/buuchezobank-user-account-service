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
import com.buuchezo.useraccountservice.security.TotpService;
import com.buuchezo.useraccountservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final int TWO_FACTOR_CHALLENGE_LENGTH = 32;
    private static final int TWO_FACTOR_CHALLENGE_MINUTES = 5;

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final JwtService jwtService;
    private final ModelMapper modelMapper;
    private final AccountEventPublisher accountEventPublisher;
    private final TotpService totpService;

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public ApiResponse<AuthResponse> registerUser(
            RegistrationRequest registrationRequest
    ) {

        log.info("We are inside the register user service method");

        if (userRepository.existsByEmail(registrationRequest.getEmail())) {
            throw new BadRequestException(
                    "User with this email already exists"
            );
        }

        /*
         * Public registration can NEVER create an ADMIN account.
         * Administrative accounts are created and authenticated
         * exclusively through the separate admins table.
         */
        Set<Role> roles = new HashSet<>();

        var customerRole = roleRepository.findByName("CUSTOMER")
                .orElseThrow(() ->
                        new NotFoundException(
                                "Role with name CUSTOMER not found"
                        )
                );

        roles.add(customerRole);

        var registeredUser = User.builder()
                .email(registrationRequest.getEmail())
                .password(
                        passwordEncoder.encode(
                                registrationRequest.getPassword()
                        )
                )
                .firstName(registrationRequest.getFirstName())
                .lastName(registrationRequest.getLastName())
                .enabled(true)
                .roles(roles)
                .build();

        var newUser = userRepository.save(registeredUser);

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

        var userRegistrationEvent = UserRegistrationEvent.builder()
                .email(newUser.getEmail())
                .firstName(newUser.getFirstName())
                .lastName(newUser.getLastName())
                .accountNumber(accountNumber)
                .bankName("Buuchezo Bank")
                .build();

        accountEventPublisher.publishUserRegistrationEvent(
                userRegistrationEvent
        );

        var userDto = modelMapper.map(
                newUser,
                UserDto.class
        );

        var authResponse = AuthResponse.builder()
                .user(userDto)
                .build();

        return new ApiResponse<>(
                HttpStatus.CREATED.value(),
                "User registered successfully",
                authResponse
        );
    }

    @Override
    public ApiResponse<AuthResponse> loginUser(
            LoginRequest loginRequest
    ) {

        log.info("inside login user service method");

        var user = userRepository.findByEmail(
                        loginRequest.getEmail()
                )
                .orElseThrow(() ->
                        new NotFoundException(
                                "User with email "
                                        + loginRequest.getEmail()
                                        + " not found"
                        )
                );

        if (!passwordEncoder.matches(
                loginRequest.getPassword(),
                user.getPassword()
        )) {
            throw new BadRequestException(
                    "Password doesn´t match"
            );
        }

        if (!user.isEnabled()) {
            throw new BadRequestException(
                    "User is not enabled"
            );
        }

        /*
         * Customer authentication can NEVER issue ADMIN authority.
         * Administrative authentication is handled exclusively by
         * AdminAuthService using the admins table.
         */
        List<String> roles = List.of("CUSTOMER");

        var userDto = modelMapper.map(
                user,
                UserDto.class
        );

        /*
         * If 2FA is enabled, do not issue the real JWT yet.
         * Generate a short-lived login challenge instead.
         */
        if (user.isTwoFactorEnabled()) {

            String challengeToken = generateChallengeToken();

            user.setTwoFactorChallengeHash(
                    hashChallengeToken(challengeToken)
            );

            user.setTwoFactorChallengeExpiresAt(
                    LocalDateTime.now()
                            .plusMinutes(TWO_FACTOR_CHALLENGE_MINUTES)
            );

            userRepository.save(user);

            AuthResponse authResponse = AuthResponse.builder()
                    .user(userDto)
                    .requiresTwoFactor(true)
                    .challengeToken(challengeToken)
                    .build();

            return new ApiResponse<>(
                    HttpStatus.OK.value(),
                    "Two-factor authentication required",
                    authResponse
            );
        }

        /*
         * If 2FA is disabled, issue the JWT immediately.
         */
        String token = jwtService.generateToken(
                user.getEmail(),
                roles
        );

        AuthResponse authResponse = AuthResponse.builder()
                .token(token)
                .user(userDto)
                .requiresTwoFactor(false)
                .build();

        return new ApiResponse<>(
                HttpStatus.OK.value(),
                "User logged in successfully",
                authResponse
        );
    }

    @Override
    public ApiResponse<AuthResponse> verifyTwoFactorLogin(
            TwoFactorLoginRequest request
    ) {

        log.info("Verifying two-factor login challenge");

        String challengeHash = hashChallengeToken(
                request.getChallengeToken()
        );

        var user = userRepository
                .findByTwoFactorChallengeHash(challengeHash)
                .orElseThrow(() ->
                        new BadRequestException(
                                "Invalid or expired two-factor challenge"
                        )
                );

        if (!user.isEnabled()) {
            throw new BadRequestException(
                    "User is not enabled"
            );
        }

        if (!user.isTwoFactorEnabled()) {
            throw new BadRequestException(
                    "Two-factor authentication is not enabled"
            );
        }

        if (user.getTwoFactorChallengeExpiresAt() == null
                || user.getTwoFactorChallengeExpiresAt()
                .isBefore(LocalDateTime.now())) {

            clearTwoFactorChallenge(user);

            userRepository.save(user);

            throw new BadRequestException(
                    "Two-factor challenge has expired"
            );
        }

        boolean valid = totpService.verifyCode(
                user.getTotpSecret(),
                request.getCode()
        );

        if (!valid) {
            throw new BadRequestException(
                    "Invalid verification code"
            );
        }

        /*
         * Customer 2FA authentication can NEVER issue ADMIN authority.
         */
        List<String> roles = List.of("CUSTOMER");

        /*
         * Consume the challenge before issuing the JWT.
         * This makes the challenge single-use.
         */
        clearTwoFactorChallenge(user);

        userRepository.save(user);

        String token = jwtService.generateToken(
                user.getEmail(),
                roles
        );

        var userDto = modelMapper.map(
                user,
                UserDto.class
        );

        AuthResponse authResponse = AuthResponse.builder()
                .token(token)
                .user(userDto)
                .requiresTwoFactor(false)
                .build();

        return new ApiResponse<>(
                HttpStatus.OK.value(),
                "User logged in successfully",
                authResponse
        );
    }

    @Override
    public ApiResponse<TwoFactorSetupResponse> setupTwoFactor(
            String email
    ) {

        log.info(
                "Starting two-factor authentication setup for user {}",
                email
        );

        var user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new NotFoundException(
                                "User with email " + email + " not found"
                        )
                );

        if (user.isTwoFactorEnabled()) {
            throw new BadRequestException(
                    "Two-factor authentication is already enabled"
            );
        }

        String secret = totpService.generateSecret();

        user.setTotpSecret(secret);
        user.setTwoFactorEnabled(false);

        userRepository.save(user);

        String otpAuthUri = totpService.generateOtpAuthUri(
                user.getEmail(),
                secret
        );

        var response = TwoFactorSetupResponse.builder()
                .enabled(false)
                .secret(secret)
                .otpAuthUri(otpAuthUri)
                .build();

        log.info(
                "Two-factor authentication setup generated for user {}",
                email
        );

        return new ApiResponse<>(
                HttpStatus.OK.value(),
                "Two-factor authentication setup initiated",
                response
        );
    }

    @Override
    public ApiResponse<TwoFactorSetupResponse> verifyTwoFactorSetup(
            String email,
            TwoFactorVerifyRequest request
    ) {

        log.info(
                "Verifying two-factor authentication setup for user {}",
                email
        );

        var user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new NotFoundException(
                                "User with email " + email + " not found"
                        )
                );

        if (user.isTwoFactorEnabled()) {
            throw new BadRequestException(
                    "Two-factor authentication is already enabled"
            );
        }

        if (user.getTotpSecret() == null
                || user.getTotpSecret().isBlank()) {

            throw new BadRequestException(
                    "Two-factor authentication setup has not been initiated"
            );
        }

        boolean valid = totpService.verifyCode(
                user.getTotpSecret(),
                request.getCode()
        );

        if (!valid) {
            throw new BadRequestException(
                    "Invalid verification code"
            );
        }

        user.setTwoFactorEnabled(true);

        userRepository.save(user);

        var response = TwoFactorSetupResponse.builder()
                .enabled(true)
                .build();

        log.info(
                "Two-factor authentication successfully enabled for user {}",
                email
        );

        return new ApiResponse<>(
                HttpStatus.OK.value(),
                "Two-factor authentication enabled successfully",
                response
        );
    }

    @Override
    public ApiResponse<Void> disableTwoFactor(
            String email,
            TwoFactorVerifyRequest request
    ) {

        log.info(
                "Attempting to disable two-factor authentication for user {}",
                email
        );

        var user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new NotFoundException(
                                "User with email " + email + " not found"
                        )
                );

        if (!user.isTwoFactorEnabled()) {
            throw new BadRequestException(
                    "Two-factor authentication is not enabled"
            );
        }

        boolean valid = totpService.verifyCode(
                user.getTotpSecret(),
                request.getCode()
        );

        if (!valid) {
            throw new BadRequestException(
                    "Invalid verification code"
            );
        }

        user.setTwoFactorEnabled(false);
        user.setTotpSecret(null);

        clearTwoFactorChallenge(user);

        userRepository.save(user);

        log.info(
                "Two-factor authentication disabled for user {}",
                email
        );

        return new ApiResponse<>(
                HttpStatus.OK.value(),
                "Two-factor authentication disabled successfully",
                null
        );
    }

    private String generateChallengeToken() {

        byte[] bytes = new byte[TWO_FACTOR_CHALLENGE_LENGTH];

        secureRandom.nextBytes(bytes);

        StringBuilder token = new StringBuilder(
                TWO_FACTOR_CHALLENGE_LENGTH * 2
        );

        for (byte b : bytes) {
            token.append(String.format("%02x", b));
        }

        return token.toString();
    }

    private String hashChallengeToken(String challengeToken) {

        if (challengeToken == null || challengeToken.isBlank()) {
            throw new BadRequestException(
                    "Challenge token is required"
            );
        }

        try {

            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    challengeToken.getBytes(StandardCharsets.UTF_8)
            );

            StringBuilder hex = new StringBuilder(
                    hash.length * 2
            );

            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }

            return hex.toString();

        } catch (NoSuchAlgorithmException e) {

            throw new IllegalStateException(
                    "SHA-256 algorithm is not available",
                    e
            );
        }
    }

    private void clearTwoFactorChallenge(User user) {

        user.setTwoFactorChallengeHash(null);
        user.setTwoFactorChallengeExpiresAt(null);
    }

    private String generateUniqueAccountNumber() {

        String accountNumber;

        var random = ThreadLocalRandom.current();

        do {

            int randomPart = random.nextInt(100_000_000);

            accountNumber = String.format(
                    "00%08d",
                    randomPart
            );

        } while (
                accountRepository.existsByAccountNumber(accountNumber)
        );

        return accountNumber;
    }
}