package com.buuchezo.useraccountservice.service.impl;

import com.buuchezo.useraccountservice.dto.AccountDto;
import com.buuchezo.useraccountservice.dto.CreateBusinessAccountRequest;
import com.buuchezo.useraccountservice.dto.ApiResponse;
import com.buuchezo.useraccountservice.entity.Account;
import com.buuchezo.useraccountservice.entity.BusinessMembership;
import com.buuchezo.useraccountservice.enums.AccountOwnershipType;
import com.buuchezo.useraccountservice.enums.AccountStatus;
import com.buuchezo.useraccountservice.enums.BusinessRole;
import com.buuchezo.useraccountservice.exceptions.NotFoundException;
import com.buuchezo.useraccountservice.repository.AccountRepository;
import com.buuchezo.useraccountservice.repository.BusinessMembershipRepository;
import com.buuchezo.useraccountservice.repository.BusinessRepository;
import com.buuchezo.useraccountservice.repository.UserRepository;
import com.buuchezo.useraccountservice.service.AccountNumberGenerator;
import com.buuchezo.useraccountservice.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final BusinessMembershipRepository businessMembershipRepository;
    private final BusinessRepository businessRepository;
    private final AccountNumberGenerator accountNumberGenerator;
    private final ModelMapper modelMapper;

    @Override
    public ApiResponse<AccountDto> getMyAccount() {
        log.info("fetching primary personal account for logged in user");

        var user = getAuthenticatedUser();

        var account = accountRepository
                .findFirstByUserAndOwnershipTypeOrderByCreatedAtAsc(
                        user,
                        AccountOwnershipType.PERSONAL
                )
                .orElseThrow(() -> new NotFoundException(
                        "Personal account not found"
                ));

        return new ApiResponse<>(
                HttpStatus.OK.value(),
                "Account retrieved",
                toDto(account)
        );
    }

    @Override
    public ApiResponse<List<AccountDto>> getMyAccounts() {
        log.info("fetching all personal accounts for logged in user");

        var user = getAuthenticatedUser();

        var accounts = accountRepository.findAllByUserAndOwnershipType(
                user,
                AccountOwnershipType.PERSONAL
        );

        var accountDtos = accounts.stream()
                .map(this::toDto)
                .toList();

        return new ApiResponse<>(
                HttpStatus.OK.value(),
                "Personal accounts retrieved",
                accountDtos
        );
    }

    @Override
    public ApiResponse<AccountDto> getAccountNumber(String accountNumber) {
        var authenticatedUser = getAuthenticatedUser();

        var account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new NotFoundException("Account not found"));

        authorizeAccountAccess(account, authenticatedUser);

        return new ApiResponse<>(
                HttpStatus.OK.value(),
                "Account retrieved",
                toDto(account)
        );
    }

    @Override
    public ApiResponse<AccountDto> createBusinessAccount(
            Long businessId,
            CreateBusinessAccountRequest request,
            String userEmail
    ) {
        var business = businessRepository.findById(businessId)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Business not found"
                        )
                );

        var user = userRepository.findByEmail(userEmail)
                .orElseThrow(() ->
                        new NotFoundException(
                                "User not found"
                        )
                );

        var membership = businessMembershipRepository
                .findByBusinessAndUser(business, user)
                .orElseThrow(() ->
                        new org.springframework.security.access.AccessDeniedException(
                                "You are not a member of this business"
                        )
                );

        if (!membership.isActive()) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Your business membership is inactive"
            );
        }

        if (membership.getRole() != BusinessRole.OWNER
                && membership.getRole() != BusinessRole.ADMIN
                && membership.getRole() != BusinessRole.ACCOUNTANT) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You are not authorized to create business accounts"
            );
        }

        if (!business.isActive()) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Business is inactive"
            );
        }

        Account account = Account.builder()
                .accountNumber(
                        accountNumberGenerator.generateUniqueAccountNumber()
                )
                .balance(java.math.BigDecimal.ZERO)
                .currency(request.getCurrency())
                .accountType(request.getAccountType())
                .accountStatus(AccountStatus.ACTIVE)
                .ownershipType(AccountOwnershipType.BUSINESS)
                .business(business)
                .user(null)
                .build();

        Account savedAccount = accountRepository.save(account);

        return new ApiResponse<>(
                HttpStatus.CREATED.value(),
                "Business account created",
                toDto(savedAccount)
        );
    }

    @Override
    public ApiResponse<List<AccountDto>> getBusinessAccounts(
            Long businessId,
            String userEmail
    ) {
        var business = businessRepository.findById(businessId)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Business not found"
                        )
                );

        var user = userRepository.findByEmail(userEmail)
                .orElseThrow(() ->
                        new NotFoundException(
                                "User not found"
                        )
                );

        var membership = businessMembershipRepository
                .findByBusinessAndUser(business, user)
                .orElseThrow(() ->
                        new org.springframework.security.access.AccessDeniedException(
                                "You are not a member of this business"
                        )
                );

        if (!membership.isActive()) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Your business membership is inactive"
            );
        }

        var accounts = accountRepository
                .findAllByBusinessAndOwnershipType(
                        business,
                        AccountOwnershipType.BUSINESS
                );

        var accountDtos = accounts.stream()
                .map(this::toDto)
                .toList();

        return new ApiResponse<>(
                HttpStatus.OK.value(),
                "Business accounts retrieved",
                accountDtos
        );
    }

    @Override
    public ApiResponse<AccountDto> changeAccountStatus(
            String accountNumber,
            AccountStatus status
    ) {
        var account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new NotFoundException("Account not found"));

        account.setAccountStatus(status);
        var changedAccount = accountRepository.save(account);

        return new ApiResponse<>(
                HttpStatus.OK.value(),
                "Account status changed",
                toDto(changedAccount)
        );
    }

    @Override
    public ApiResponse<Page<AccountDto>> getAllAccount(Pageable pageable) {
        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by("createdAt").descending()
        );

        Page<Account> accounts = accountRepository.findAll(sortedPageable);
        Page<AccountDto> dtoPage = accounts.map(this::toDto);

        return new ApiResponse<>(
                HttpStatus.OK.value(),
                "Accounts retrieved",
                dtoPage
        );
    }

    private com.buuchezo.useraccountservice.entity.User getAuthenticatedUser() {
        String userEmail = Objects.requireNonNull(
                SecurityContextHolder.getContext().getAuthentication()
        ).getName();

        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private void authorizeAccountAccess(
            Account account,
            com.buuchezo.useraccountservice.entity.User authenticatedUser
    ) {
        if (account.getOwnershipType() == AccountOwnershipType.PERSONAL) {
            if (account.getUser() == null
                    || !account.getUser().getId().equals(authenticatedUser.getId())) {
                throw new AccessDeniedException("You are not authorized to access this account");
            }

            return;
        }

        if (account.getOwnershipType() == AccountOwnershipType.BUSINESS) {
            if (account.getBusiness() == null) {
                throw new AccessDeniedException("Business account has no business owner");
            }

            BusinessMembership membership =
                    businessMembershipRepository
                            .findByBusinessAndUser(
                                    account.getBusiness(),
                                    authenticatedUser
                            )
                            .orElseThrow(() ->
                                    new AccessDeniedException(
                                            "You are not authorized to access this business account"
                                    )
                            );

            if (!membership.isActive()) {
                throw new AccessDeniedException(
                        "Your business membership is inactive"
                );
            }

            return;
        }

        throw new AccessDeniedException(
                "Account ownership type is not configured"
        );
    }

    private AccountDto toDto(Account account) {
        AccountDto accountDto = modelMapper.map(account, AccountDto.class);

        if (account.getUser() != null) {
            accountDto.setOwnerEmail(account.getUser().getEmail());
        }

        if (account.getBusiness() != null) {
            accountDto.setBusinessId(account.getBusiness().getId());
            accountDto.setBusinessName(
                    account.getBusiness().getTradingName() != null
                            ? account.getBusiness().getTradingName()
                            : account.getBusiness().getLegalName()
            );
        }

        return accountDto;
    }
}
