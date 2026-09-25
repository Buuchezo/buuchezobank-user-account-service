package com.buuchezo.useraccountservice.repository;

import com.buuchezo.useraccountservice.entity.Account;
import com.buuchezo.useraccountservice.entity.Business;
import com.buuchezo.useraccountservice.entity.User;
import com.buuchezo.useraccountservice.enums.AccountOwnershipType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByAccountNumber(String accountNumber);

    boolean existsByAccountNumber(String accountNumber);

    /*
     * Existing method kept for backward compatibility.
     */
    Optional<Account> findByUser(User user);

    /*
     * Personal accounts.
     */
    List<Account> findAllByUser(User user);

    List<Account> findAllByUserAndOwnershipType(
            User user,
            AccountOwnershipType ownershipType
    );

    Optional<Account> findFirstByUserAndOwnershipTypeOrderByCreatedAtAsc(
            User user,
            AccountOwnershipType ownershipType
    );

    /*
     * Business accounts.
     */
    List<Account> findAllByBusiness(Business business);

    List<Account> findAllByBusinessAndOwnershipType(
            Business business,
            AccountOwnershipType ownershipType
    );

    Optional<Account> findFirstByBusinessAndOwnershipTypeOrderByCreatedAtAsc(
            Business business,
            AccountOwnershipType ownershipType
    );
}
