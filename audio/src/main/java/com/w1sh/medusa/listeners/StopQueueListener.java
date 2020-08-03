package com.w1sh.medusa.listeners;

import com.w1sh.medusa.AudioConnectionManager;
import com.w1sh.medusa.events.StopQueueEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public final class StopQueueListener implements EventListener<StopQueueEvent> {

    private final AudioConnectionManager audioConnectionManager;

    @Override
    public Mono<Void> execute(StopQueueEvent event) {
        return Mono.justOrEmpty(event)
                .flatMap(audioConnectionManager::scheduleLeave)
                .then();
    }
}
