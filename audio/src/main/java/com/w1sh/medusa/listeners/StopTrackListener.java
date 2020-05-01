package com.w1sh.medusa.listeners;

import com.w1sh.medusa.AudioConnectionManager;
import com.w1sh.medusa.events.StopTrackEvent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public final class StopTrackListener implements EventListener<StopTrackEvent> {

    private final AudioConnectionManager audioConnectionManager;

    public StopTrackListener(AudioConnectionManager audioConnectionManager) {
        this.audioConnectionManager = audioConnectionManager;
    }

    @Override
    public Class<StopTrackEvent> getEventType() {
        return StopTrackEvent.class;
    }

    @Override
    public Mono<Void> execute(StopTrackEvent event) {
        return Mono.justOrEmpty(event)
                .flatMap(audioConnectionManager::scheduleLeave)
                .then();
    }
}
