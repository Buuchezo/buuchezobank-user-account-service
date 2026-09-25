package com.buuchezo.useraccountservice.repository;

import com.buuchezo.useraccountservice.entity.Business;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BusinessRepository extends JpaRepository<Business, Long> {

    Optional<Business> findByRegistrationNumber(String registrationNumber);

    boolean existsByRegistrationNumber(String registrationNumber);

    boolean existsByLegalNameIgnoreCase(String legalName);
}
