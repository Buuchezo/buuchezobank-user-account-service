package com.buuchezo.useraccountservice.dto;

import com.buuchezo.useraccountservice.enums.BusinessRole;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessMembershipDto {

    private Long id;

    private Long businessId;

    private Long userId;

    private String userEmail;

    private BusinessRole role;

    private boolean active;

    private LocalDateTime createdAt;
}
