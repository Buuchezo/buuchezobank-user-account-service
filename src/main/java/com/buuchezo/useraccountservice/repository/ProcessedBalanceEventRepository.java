package com.buuchezo.useraccountservice.repository;

import com.buuchezo.useraccountservice.entity.ProcessedBalanceEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProcessedBalanceEventRepository
        extends JpaRepository<ProcessedBalanceEvent, Long> {

    boolean existsByEventId(UUID eventId);
}