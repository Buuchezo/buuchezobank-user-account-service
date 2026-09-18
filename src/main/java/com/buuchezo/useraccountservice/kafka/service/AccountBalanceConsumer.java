package com.buuchezo.useraccountservice.kafka.service;

import com.buuchezo.useraccountservice.enums.transaction.TransactionDirection;
import com.buuchezo.useraccountservice.exceptions.NotFoundException;
import com.buuchezo.useraccountservice.kafka.dto.BalanceUpdateEvent;
import com.buuchezo.useraccountservice.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountBalanceConsumer {

    private final AccountRepository accountRepository;
    private final AccountEventPublisher accountEventPublisher;

    @KafkaListener(
            topics = "balance-update-events",
            groupId = "account-group"
    )
    @Transactional
    public void consumerBalanceUpdate(BalanceUpdateEvent event) {

        log.info(
                "Processing balance update for account number: {}",
                event.getAccountNumber()
        );

        var account = accountRepository
                .findByAccountNumber(event.getAccountNumber())
                .orElseThrow(() ->
                        new NotFoundException("Account not found")
                );

        // Get the current balance directly from the database.
        // The Account Service owns the account balance.
        var currentBalance = account.getBalance();

        if (currentBalance == null) {
            currentBalance = java.math.BigDecimal.ZERO;
        }

        // Update the balance based on the transaction direction.
        if (event.getTransactionDirection() == TransactionDirection.CREDIT) {

            account.setBalance(
                    currentBalance.add(event.getAmount())
            );

        } else if (event.getTransactionDirection() == TransactionDirection.DEBIT) {

            account.setBalance(
                    currentBalance.subtract(event.getAmount())
            );

        } else {

            log.warn(
                    "Unknown transaction direction for reference {}",
                    event.getReference()
            );

            return;
        }

        accountRepository.save(account);

        log.info(
                "Balance updated successfully. Account: {}, Old balance: {}, Amount: {}, Direction: {}, New balance: {}",
                account.getAccountNumber(),
                currentBalance,
                event.getAmount(),
                event.getTransactionDirection(),
                account.getBalance()
        );

        // Publish the resulting balance to the notification service.
        BalanceUpdateEvent balanceUpdateEventToPublishNotification =
                BalanceUpdateEvent.builder()
                        .email(account.getUser().getEmail())
                        .firstName(account.getUser().getFirstName())
                        .accountNumber(account.getAccountNumber())
                        .amount(event.getAmount())
                        .transactionDirection(event.getTransactionDirection())
                        .reference(event.getReference())
                        .description(event.getDescription())
                        .currentBalance(account.getBalance())
                        .build();

        accountEventPublisher.publishTransactionNotificationEvent(
                balanceUpdateEventToPublishNotification
        );
    }
}