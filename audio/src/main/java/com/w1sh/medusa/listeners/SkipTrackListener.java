package com.w1sh.medusa.listeners;

import com.w1sh.medusa.AudioConnectionManager;
import com.w1sh.medusa.events.SkipTrackEvent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public final class SkipTrackListener implements EventListener<SkipTrackEvent> {

    private final AudioConnectionManager audioConnectionManager;

    public SkipTrackListener(AudioConnectionManager audioConnectionManager) {
        this.audioConnectionManager = audioConnectionManager;
    }

    @Override
    public Class<SkipTrackEvent> getEventType() {
        return SkipTrackEvent.class;
    }

    @Override
    public Mono<Void> execute(SkipTrackEvent event) {
        return Mono.justOrEmpty(event.getGuildId())
                .flatMap(audioConnectionManager::getAudioConnection)
                .doOnNext(audioConnection -> audioConnection.getTrackScheduler().nextTrack(true))
                .then();
    }
}
