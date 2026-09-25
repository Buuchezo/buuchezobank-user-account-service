package com.buuchezo.useraccountservice.service;

import com.buuchezo.useraccountservice.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
public class AccountNumberGenerator {

    private final AccountRepository accountRepository;

    public String generateUniqueAccountNumber() {
        String accountNumber;

        do {
            int randomPart = ThreadLocalRandom.current()
                    .nextInt(100_000_000);

            accountNumber = String.format(
                    "00%08d",
                    randomPart
            );

        } while (accountRepository.existsByAccountNumber(accountNumber));

        return accountNumber;
    }
}
