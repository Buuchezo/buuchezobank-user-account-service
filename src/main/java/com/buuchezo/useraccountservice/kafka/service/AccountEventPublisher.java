package com.buuchezo.useraccountservice.kafka.service;

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
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishUserRegistrationEvent(UserRegistrationEvent event) {
        try {
            kafkaTemplate.send(USER_TOPIC, event.getEmail(), event);
            log.info("User registration event published successfully {}", event.getEmail());
        } catch (Exception e) {
            log.error("Error publishing user registration event: {}", e.getMessage());
        }
    }
}
