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
                "INCOMING EVENT: account={}, type={}, direction={}, status={}, currency={}, reference={}",
                event.getAccountNumber(),
                event.getTransactionType(),
                event.getTransactionDirection(),
                event.getTransactionStatus(),
                event.getCurrency(),
                event.getReference()
        );

<<<<<<< HEAD
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

=======
 log.info(
            "INCOMING BALANCE EVENT: account={}, amount={}, direction={}, type={}, reference={}",
            event.getAccountNumber(),
            event.getAmount(),
            event.getTransactionDirection(),
            event.getTransactionType(),
            event.getReference()
    );

        log.info(
                "Processing balance update for account number: {}",
                event.getAccountNumber()
        );

        var account = accountRepository
                .findByAccountNumber(event.getAccountNumber())
                .orElseThrow(() ->
                        new NotFoundException("Account not found")
                );

        /*
         * The Account Service owns the account balance.
         * Therefore, read the current balance directly
         * from the database.
         */
        var currentBalance = account.getBalance();

>>>>>>> 0a389f0 (Add balance event transaction type diagnostic)
        if (currentBalance == null) {
            currentBalance = java.math.BigDecimal.ZERO;
        }

<<<<<<< HEAD
        // Update the balance based on the transaction direction.
=======
        /*
         * CREDIT means money is coming into the account.
         */
>>>>>>> 0a389f0 (Add balance event transaction type diagnostic)
        if (event.getTransactionDirection() == TransactionDirection.CREDIT) {

            account.setBalance(
                    currentBalance.add(event.getAmount())
            );

<<<<<<< HEAD
=======
        /*
         * DEBIT means money is leaving the account.
         */
>>>>>>> 0a389f0 (Add balance event transaction type diagnostic)
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
<<<<<<< HEAD
                "Balance updated successfully. Account: {}, Old balance: {}, Amount: {}, Direction: {}, New balance: {}",
=======
                "Balance updated successfully. " +
                "Account: {}, Old balance: {}, Amount: {}, " +
                "Direction: {}, New balance: {}",
>>>>>>> 0a389f0 (Add balance event transaction type diagnostic)
                account.getAccountNumber(),
                currentBalance,
                event.getAmount(),
                event.getTransactionDirection(),
                account.getBalance()
        );

<<<<<<< HEAD
        // Publish the resulting balance to the notification service.
=======
        /*
         * Publish the resulting balance to the notification service.
         */
>>>>>>> 0a389f0 (Add balance event transaction type diagnostic)
        BalanceUpdateEvent balanceUpdateEventToPublishNotification =
                BalanceUpdateEvent.builder()
                        .email(account.getUser().getEmail())
                        .firstName(account.getUser().getFirstName())
                        .accountNumber(account.getAccountNumber())
                        .amount(event.getAmount())
                        .transactionDirection(event.getTransactionDirection())
<<<<<<< HEAD
                        .transactionType(event.getTransactionType())
=======
>>>>>>> 0a389f0 (Add balance event transaction type diagnostic)
                        .reference(event.getReference())
                        .description(event.getDescription())
                        .currentBalance(account.getBalance())
                        .build();

        accountEventPublisher.publishTransactionNotificationEvent(
                balanceUpdateEventToPublishNotification
        );
    }


}