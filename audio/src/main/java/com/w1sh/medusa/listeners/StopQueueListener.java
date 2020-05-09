package com.w1sh.medusa.listeners;

import com.w1sh.medusa.AudioConnectionManager;
import com.w1sh.medusa.events.StopQueueEvent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public final class StopQueueListener implements EventListener<StopQueueEvent> {

    private final AudioConnectionManager audioConnectionManager;

    public StopQueueListener(AudioConnectionManager audioConnectionManager) {
        this.audioConnectionManager = audioConnectionManager;
    }

    @Override
    public Class<StopQueueEvent> getEventType() {
        return StopQueueEvent.class;
    }

    @Override
    public Mono<Void> execute(StopQueueEvent event) {
        return Mono.justOrEmpty(event)
                .flatMap(audioConnectionManager::scheduleLeave)
                .then();
    }
}
