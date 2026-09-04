package com.buuchezo.useraccountservice.repository;

import com.buuchezo.useraccountservice.entity.Account;
import com.buuchezo.useraccountservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByAccountNumber(String accountNumber);

    boolean existsByAccountNumber(String accountNumber);

    Optional<Account> findByUser(User user);
}
