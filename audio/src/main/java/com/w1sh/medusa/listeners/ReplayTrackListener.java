package com.w1sh.medusa.listeners;

import com.w1sh.medusa.AudioConnectionManager;
import com.w1sh.medusa.events.ReplayTrackEvent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public final class ReplayTrackListener implements EventListener<ReplayTrackEvent> {

    private final AudioConnectionManager audioConnectionManager;

    public ReplayTrackListener(AudioConnectionManager audioConnectionManager) {
        this.audioConnectionManager = audioConnectionManager;
    }

    @Override
    public Class<ReplayTrackEvent> getEventType() {
        return ReplayTrackEvent.class;
    }

    @Override
    public Mono<Void> execute(ReplayTrackEvent event) {
        return Mono.justOrEmpty(event.getGuildId())
                .flatMap(audioConnectionManager::getAudioConnection)
                .doOnNext(audioConnection -> audioConnection.getTrackScheduler().replay())
                .then();
    }
}
