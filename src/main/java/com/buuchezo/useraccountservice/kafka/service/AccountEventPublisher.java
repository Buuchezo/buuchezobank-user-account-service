package com.buuchezo.useraccountservice.kafka.service;

import com.buuchezo.useraccountservice.kafka.dto.BalanceUpdateEvent;
import com.buuchezo.useraccountservice.kafka.dto.UserRegistrationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountEventPublisher {

    //TOPIC
    private static final String USER_TOPIC = "user-registered-event";
    private static final String BALANCE_UPDATE_TOPIC = "balance-update-notification-event";
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishUserRegistrationEvent(UserRegistrationEvent event) {
        try {
            kafkaTemplate.send(USER_TOPIC, event.getEmail(), event);
            log.info("User registration event published successfully {}", event.getEmail());
        } catch (Exception e) {
            log.error("Error publishing user registration event: {}", e.getMessage());
        }
    }
    

    public void publishTransactionNotificationEvent(BalanceUpdateEvent event) {
        try {
            kafkaTemplate.send(BALANCE_UPDATE_TOPIC, event.getEmail(), event);
            log.info("Balance update event sent out {}", event.getEmail());
        } catch (Exception e) {
            log.error("Failed to publish Balance update event: {}", e.getMessage());
        }
    }
}
