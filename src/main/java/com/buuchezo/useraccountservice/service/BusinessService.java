package com.buuchezo.useraccountservice.service;

import com.buuchezo.useraccountservice.dto.BusinessDto;
import com.buuchezo.useraccountservice.dto.BusinessMembershipDto;
import com.buuchezo.useraccountservice.dto.CreateBusinessRequest;

import java.util.List;

public interface BusinessService {

    BusinessDto createBusiness(
            CreateBusinessRequest request,
            String userEmail
    );

    BusinessDto getBusinessById(Long businessId);

    List<BusinessDto> getBusinessesForUser(String userEmail);

    BusinessMembershipDto addMember(
            Long businessId,
            String userEmail,
            String memberEmail,
            String role
    );

    List<BusinessMembershipDto> getMembers(
            Long businessId,
            String userEmail
    );

    BusinessMembershipDto updateMemberRole(
            Long businessId,
            Long membershipId,
            String userEmail,
            String role
    );

    void removeMember(
            Long businessId,
            Long membershipId,
            String userEmail
    );
}
