package com.buuchezo.useraccountservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdminDto {

    private Long id;

    private String email;

    private String firstName;

    private String lastName;

    private boolean enabled;

    private String role;

    private LocalDateTime createdAt;
}
