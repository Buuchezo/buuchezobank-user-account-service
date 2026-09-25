package com.buuchezo.useraccountservice.controller;

import com.buuchezo.useraccountservice.dto.BusinessDto;
import com.buuchezo.useraccountservice.dto.BusinessMembershipDto;
import com.buuchezo.useraccountservice.dto.CreateBusinessRequest;
import com.buuchezo.useraccountservice.service.BusinessService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/businesses")
@RequiredArgsConstructor
public class BusinessController {

    private final BusinessService businessService;

    @PostMapping
    public ResponseEntity<BusinessDto> createBusiness(
            @Valid @RequestBody CreateBusinessRequest request,
            Authentication authentication
    ) {
        BusinessDto business = businessService.createBusiness(
                request,
                authentication.getName()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(business);
    }

    @GetMapping("/{businessId:\\d+}")
    public ResponseEntity<BusinessDto> getBusiness(
            @PathVariable Long businessId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                businessService.getBusinessById(
                        businessId,
                        authentication.getName()
                )
        );
    }

    @GetMapping("/me")
    public ResponseEntity<List<BusinessDto>> getMyBusinesses(
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                businessService.getBusinessesForUser(
                        authentication.getName()
                )
        );
    }

    @PostMapping("/{businessId}/members")
    public ResponseEntity<BusinessMembershipDto> addMember(
            @PathVariable Long businessId,
            @RequestParam String memberEmail,
            @RequestParam String role,
            Authentication authentication
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        businessService.addMember(
                                businessId,
                                authentication.getName(),
                                memberEmail,
                                role
                        )
                );
    }

    @GetMapping("/{businessId}/members")
    public ResponseEntity<List<BusinessMembershipDto>> getMembers(
            @PathVariable Long businessId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                businessService.getMembers(
                        businessId,
                        authentication.getName()
                )
        );
    }

    @PatchMapping("/{businessId}/members/{membershipId}")
    public ResponseEntity<BusinessMembershipDto> updateMemberRole(
            @PathVariable Long businessId,
            @PathVariable Long membershipId,
            @RequestParam String role,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                businessService.updateMemberRole(
                        businessId,
                        membershipId,
                        authentication.getName(),
                        role
                )
        );
    }

    @DeleteMapping("/{businessId}/members/{membershipId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable Long businessId,
            @PathVariable Long membershipId,
            Authentication authentication
    ) {
        businessService.removeMember(
                businessId,
                membershipId,
                authentication.getName()
        );

        return ResponseEntity.noContent().build();
    }
}
