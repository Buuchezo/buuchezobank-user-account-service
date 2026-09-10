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


    @KafkaListener(topics = "balance-update-events", groupId = "account-group")
    @Transactional
    public void consumerBalanceUpdate(BalanceUpdateEvent event) {

        log.info("processing balance for account number: {}", event.getAccountNumber());
        var account = accountRepository.findByAccountNumber(event.getAccountNumber())
                .orElseThrow(()-> new NotFoundException("Account not found"));

        // update balance of the account

        if(event.getTransactionDirection().equals(TransactionDirection.CREDIT)){
        account.setBalance(event.getCurrentBalance().add(event.getAmount()));

    }else if(event.getTransactionDirection().equals(TransactionDirection.DEBIT)){
        account.setBalance(event.getCurrentBalance().subtract(event.getAmount()));

    }
        accountRepository.save(account);

        BalanceUpdateEvent balanceUpdateEventToPublishNotification = BalanceUpdateEvent.builder()
                .email(account.getUser().getEmail())
                .firstName(account.getUser().getFirstName())
                .accountNumber(account.getAccountNumber())
                .amount(event.getAmount())
                .transactionDirection(event.getTransactionDirection())
                .reference(event.getReference())
                .description(event.getDescription())
                .currentBalance(account.getBalance())
                .build();
        accountEventPublisher.publishTransactionNotificationEvent(balanceUpdateEventToPublishNotification);
    }
}
