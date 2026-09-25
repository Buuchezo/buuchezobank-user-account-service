package com.buuchezo.useraccountservice.repository;

import com.buuchezo.useraccountservice.entity.Business;
import com.buuchezo.useraccountservice.entity.BusinessMembership;
import com.buuchezo.useraccountservice.entity.User;
import com.buuchezo.useraccountservice.enums.BusinessRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BusinessMembershipRepository
        extends JpaRepository<BusinessMembership, Long> {

    List<BusinessMembership> findAllByBusiness(Business business);

    List<BusinessMembership> findAllByUser(User user);

    Optional<BusinessMembership> findByBusinessAndUser(
            Business business,
            User user
    );

    boolean existsByBusinessAndUser(
            Business business,
            User user
    );

    boolean existsByBusinessAndUserAndRole(
            Business business,
            User user,
            BusinessRole role
    );

    List<BusinessMembership> findAllByBusinessAndActiveTrue(
            Business business
    );

    List<BusinessMembership> findAllByUserAndActiveTrue(
            User user
    );
}
