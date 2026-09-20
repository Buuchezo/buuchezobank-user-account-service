package com.buuchezo.useraccountservice.kafka.service;

import com.buuchezo.useraccountservice.entity.Account;
import com.buuchezo.useraccountservice.entity.ProcessedBalanceEvent;
import com.buuchezo.useraccountservice.enums.transaction.TransactionDirection;
import com.buuchezo.useraccountservice.exceptions.NotFoundException;
import com.buuchezo.useraccountservice.kafka.dto.BalanceUpdateEvent;
import com.buuchezo.useraccountservice.repository.AccountRepository;
import com.buuchezo.useraccountservice.repository.ProcessedBalanceEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountBalanceConsumer {

    private final AccountRepository accountRepository;
    private final ProcessedBalanceEventRepository processedBalanceEventRepository;
    private final AccountEventPublisher accountEventPublisher;

    @KafkaListener(
            topics = "balance-update-events",
            groupId = "account-group"
    )
    @Transactional
    public void consumerBalanceUpdate(
            BalanceUpdateEvent event
    ) {

        log.info(
                "INCOMING BALANCE EVENT: eventId={}, account={}, amount={}, direction={}, type={}, reference={}",
                event.getEventId(),
                event.getAccountNumber(),
                event.getAmount(),
                event.getTransactionDirection(),
                event.getTransactionType(),
                event.getReference()
        );

        /*
         * Every balance event must have a unique event ID.
         */
        if (event.getEventId() == null) {

            log.error(
                    "Balance event rejected because eventId is missing. reference={}",
                    event.getReference()
            );

            throw new IllegalArgumentException(
                    "Balance update event is missing eventId"
            );
        }

        /*
         * Idempotency check.
         *
         * Kafka can deliver the same message more than once.
         * We must never apply the same balance change twice.
         */
        if (processedBalanceEventRepository.existsByEventId(
                event.getEventId()
        )) {

            log.warn(
                    "Duplicate balance event ignored. eventId={}, reference={}",
                    event.getEventId(),
                    event.getReference()
            );

            return;
        }

        log.info(
                "Processing balance update for account number: {}",
                event.getAccountNumber()
        );

        Account account = accountRepository
                .findByAccountNumber(event.getAccountNumber())
                .orElseThrow(() ->
                        new NotFoundException(
                                "Account not found"
                        )
                );

        BigDecimal currentBalance =
                account.getBalance() == null
                        ? BigDecimal.ZERO
                        : account.getBalance();

        /*
         * CREDIT means money enters the account.
         */
        if (event.getTransactionDirection()
                == TransactionDirection.CREDIT) {

            account.setBalance(
                    currentBalance.add(event.getAmount())
            );

            /*
             * DEBIT means money leaves the account.
             */
        } else if (event.getTransactionDirection()
                == TransactionDirection.DEBIT) {

            account.setBalance(
                    currentBalance.subtract(event.getAmount())
            );

        } else {

            log.error(
                    "Unknown transaction direction. eventId={}, direction={}",
                    event.getEventId(),
                    event.getTransactionDirection()
            );

            throw new IllegalArgumentException(
                    "Unknown transaction direction"
            );
        }

        /*
         * Save the new balance.
         *
         * @Version on Account means concurrent modifications
         * are detected by JPA.
         */
        accountRepository.save(account);

        /*
         * Record the event as processed in the SAME database
         * transaction as the balance update.
         */
        ProcessedBalanceEvent processedEvent =
                ProcessedBalanceEvent.builder()
                        .eventId(event.getEventId())
                        .processedAt(LocalDateTime.now())
                        .build();

        try {

            processedBalanceEventRepository.save(
                    processedEvent
            );

        } catch (DataIntegrityViolationException e) {

            /*
             * Another delivery already recorded this event.
             *
             * Because this method is transactional, the balance
             * change will also be rolled back.
             */
            log.warn(
                    "Duplicate balance event detected while recording event. eventId={}",
                    event.getEventId()
            );

            throw e;
        }

        log.info(
                "Balance updated successfully. " +
                        "Account: {}, Old balance: {}, Amount: {}, " +
                        "Direction: {}, New balance: {}, eventId={}",
                account.getAccountNumber(),
                currentBalance,
                event.getAmount(),
                event.getTransactionDirection(),
                account.getBalance(),
                event.getEventId()
        );

        /*
         * Publish the resulting balance to the notification service.
         *
         * Existing notification behaviour is preserved.
         */
        BalanceUpdateEvent balanceUpdateEventToPublishNotification =
                BalanceUpdateEvent.builder()
                        .eventId(event.getEventId())
                        .email(account.getUser().getEmail())
                        .firstName(account.getUser().getFirstName())
                        .lastName(account.getUser().getLastName())
                        .accountNumber(account.getAccountNumber())
                        .amount(event.getAmount())
                        .transactionDirection(
                                event.getTransactionDirection()
                        )
                        .transactionType(
                                event.getTransactionType()
                        )
                        .transactionStatus(
                                event.getTransactionStatus()
                        )
                        .reference(event.getReference())
                        .description(event.getDescription())
                        .currentBalance(account.getBalance())
                        .currency(event.getCurrency())
                        .build();

        accountEventPublisher
                .publishTransactionNotificationEvent(
                        balanceUpdateEventToPublishNotification
                );
    }
}