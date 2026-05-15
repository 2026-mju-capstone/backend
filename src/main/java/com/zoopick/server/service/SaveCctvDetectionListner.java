package com.zoopick.server.service;


import com.zoopick.server.dto.match.SaveCctvDetectionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class SaveCctvDetectionListner {
    private final CctvMatchService cctvMatchService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMatchCctvToLostItems(SaveCctvDetectionEvent event) {
        cctvMatchService.matchCctvToLostItems(event.detectionId());
    }
}
