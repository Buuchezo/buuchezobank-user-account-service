package com.buuchezo.useraccountservice.service.impl;

import com.buuchezo.useraccountservice.dto.BusinessDto;
import com.buuchezo.useraccountservice.dto.BusinessMembershipDto;
import com.buuchezo.useraccountservice.dto.CreateBusinessRequest;
import com.buuchezo.useraccountservice.entity.Business;
import com.buuchezo.useraccountservice.entity.BusinessMembership;
import com.buuchezo.useraccountservice.entity.User;
import com.buuchezo.useraccountservice.enums.BusinessRole;
import com.buuchezo.useraccountservice.repository.BusinessMembershipRepository;
import com.buuchezo.useraccountservice.repository.BusinessRepository;
import com.buuchezo.useraccountservice.repository.UserRepository;
import com.buuchezo.useraccountservice.service.BusinessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BusinessServiceImpl implements BusinessService {

    private final BusinessRepository businessRepository;
    private final BusinessMembershipRepository membershipRepository;
    private final UserRepository userRepository;

    @Override
    public BusinessDto createBusiness(
            CreateBusinessRequest request,
            String userEmail
    ) {
        User user = getUser(userEmail);

        if (businessRepository.existsByRegistrationNumber(
                request.getRegistrationNumber())) {
            throw new IllegalArgumentException(
                    "A business with this registration number already exists"
            );
        }

        if (businessRepository.existsByLegalNameIgnoreCase(
                request.getLegalName())) {
            throw new IllegalArgumentException(
                    "A business with this legal name already exists"
            );
        }

        Business business = Business.builder()
                .legalName(request.getLegalName())
                .tradingName(request.getTradingName())
                .registrationNumber(request.getRegistrationNumber())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .city(request.getCity())
                .country(request.getCountry())
                .active(true)
                .build();

        business = businessRepository.save(business);

        BusinessMembership membership = BusinessMembership.builder()
                .business(business)
                .user(user)
                .role(BusinessRole.OWNER)
                .active(true)
                .build();

        membershipRepository.save(membership);

        return toDto(business);
    }

    @Override
    @Transactional(readOnly = true)
    public BusinessDto getBusinessById(Long businessId) {

        Business business = businessRepository.findById(businessId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Business not found: " + businessId
                        )
                );

        return toDto(business);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BusinessDto> getBusinessesForUser(String userEmail) {

        User user = getUser(userEmail);

        return membershipRepository.findAllByUserAndActiveTrue(user)
                .stream()
                .map(BusinessMembership::getBusiness)
                .map(this::toDto)
                .toList();
    }

    @Override
    public BusinessMembershipDto addMember(
            Long businessId,
            String userEmail,
            String memberEmail,
            String role
    ) {
        Business business = getBusiness(businessId);
        User requester = getUser(userEmail);
        User member = getUser(memberEmail);

        requireOwnerOrAdmin(business, requester);

        if (membershipRepository.existsByBusinessAndUser(
                business,
                member)) {
            throw new IllegalArgumentException(
                    "User is already a member of this business"
            );
        }

        BusinessRole businessRole = parseRole(role);

        if (businessRole == BusinessRole.OWNER) {
            throw new IllegalArgumentException(
                    "OWNER cannot be assigned through this endpoint"
            );
        }

        BusinessMembership membership = BusinessMembership.builder()
                .business(business)
                .user(member)
                .role(businessRole)
                .active(true)
                .build();

        return toMembershipDto(
                membershipRepository.save(membership)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<BusinessMembershipDto> getMembers(
            Long businessId,
            String userEmail
    ) {
        Business business = getBusiness(businessId);
        User requester = getUser(userEmail);

        requireMember(business, requester);

        return membershipRepository.findAllByBusiness(business)
                .stream()
                .map(this::toMembershipDto)
                .toList();
    }

    @Override
    public BusinessMembershipDto updateMemberRole(
            Long businessId,
            Long membershipId,
            String userEmail,
            String role
    ) {
        Business business = getBusiness(businessId);
        User requester = getUser(userEmail);

        requireOwnerOrAdmin(business, requester);

        BusinessMembership membership =
                membershipRepository.findById(membershipId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Membership not found: "
                                                + membershipId
                                )
                        );

        if (!membership.getBusiness().getId().equals(business.getId())) {
            throw new IllegalArgumentException(
                    "Membership does not belong to this business"
            );
        }

        if (membership.getRole() == BusinessRole.OWNER) {
            throw new IllegalArgumentException(
                    "OWNER role cannot be changed through this endpoint"
            );
        }

        BusinessRole newRole = parseRole(role);

        if (newRole == BusinessRole.OWNER) {
            throw new IllegalArgumentException(
                    "OWNER role cannot be assigned through this endpoint"
            );
        }

        membership.setRole(newRole);

        return toMembershipDto(
                membershipRepository.save(membership)
        );
    }

    @Override
    public void removeMember(
            Long businessId,
            Long membershipId,
            String userEmail
    ) {
        Business business = getBusiness(businessId);
        User requester = getUser(userEmail);

        requireOwnerOrAdmin(business, requester);

        BusinessMembership membership =
                membershipRepository.findById(membershipId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Membership not found: "
                                                + membershipId
                                )
                        );

        if (!membership.getBusiness().getId().equals(business.getId())) {
            throw new IllegalArgumentException(
                    "Membership does not belong to this business"
            );
        }

        if (membership.getRole() == BusinessRole.OWNER) {
            throw new IllegalArgumentException(
                    "Business owner cannot be removed"
            );
        }

        membership.setActive(false);
        membershipRepository.save(membership);
    }

    private Business getBusiness(Long businessId) {
        return businessRepository.findById(businessId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Business not found: " + businessId
                        )
                );
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found: " + email
                        )
                );
    }

    private void requireMember(
            Business business,
            User user
    ) {
        membershipRepository.findByBusinessAndUser(
                        business,
                        user
                )
                .filter(BusinessMembership::isActive)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User is not an active member of this business"
                        )
                );
    }

    private void requireOwnerOrAdmin(
            Business business,
            User user
    ) {
        BusinessMembership membership =
                membershipRepository.findByBusinessAndUser(
                        business,
                        user
                )
                .filter(BusinessMembership::isActive)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User is not an active member of this business"
                        )
                );

        if (membership.getRole() != BusinessRole.OWNER
                && membership.getRole() != BusinessRole.ADMIN) {
            throw new IllegalArgumentException(
                    "Only OWNER or ADMIN can perform this operation"
            );
        }
    }

    private BusinessRole parseRole(String role) {
        try {
            return BusinessRole.valueOf(role.toUpperCase());
        } catch (Exception exception) {
            throw new IllegalArgumentException(
                    "Invalid business role: " + role
            );
        }
    }

    private BusinessDto toDto(Business business) {
        return BusinessDto.builder()
                .id(business.getId())
                .legalName(business.getLegalName())
                .tradingName(business.getTradingName())
                .registrationNumber(business.getRegistrationNumber())
                .email(business.getEmail())
                .phone(business.getPhone())
                .address(business.getAddress())
                .city(business.getCity())
                .country(business.getCountry())
                .active(business.isActive())
                .createdAt(business.getCreatedAt())
                .build();
    }

    private BusinessMembershipDto toMembershipDto(
            BusinessMembership membership
    ) {
        return BusinessMembershipDto.builder()
                .id(membership.getId())
                .businessId(membership.getBusiness().getId())
                .userId(membership.getUser().getId())
                .userEmail(membership.getUser().getEmail())
                .role(membership.getRole())
                .active(membership.isActive())
                .createdAt(membership.getCreatedAt())
                .build();
    }
}
