package com.buuchezo.useraccountservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "businesses",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_business_legal_name",
                        columnNames = "legal_name"
                ),
                @UniqueConstraint(
                        name = "uk_business_registration_number",
                        columnNames = "registration_number"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Business {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "legal_name", nullable = false)
    private String legalName;

    @Column(name = "trading_name")
    private String tradingName;

    @Column(name = "registration_number", unique = true)
    private String registrationNumber;

    @Column(nullable = false)
    private String email;

    private String phone;

    private String address;

    private String city;

    private String country;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
