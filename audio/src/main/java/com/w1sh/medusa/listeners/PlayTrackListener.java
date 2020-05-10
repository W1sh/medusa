package com.w1sh.medusa.listeners;

import com.w1sh.medusa.AudioConnectionManager;
import com.w1sh.medusa.events.PlayTrackEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public final class PlayTrackListener implements EventListener<PlayTrackEvent> {

    private static final Logger logger = LoggerFactory.getLogger(PlayTrackListener.class);

    private final AudioConnectionManager audioConnectionManager;

    public PlayTrackListener(AudioConnectionManager audioConnectionManager) {
        this.audioConnectionManager = audioConnectionManager;
    }

    @Override
    public Class<PlayTrackEvent> getEventType() {
        return PlayTrackEvent.class;
    }

    @Override
    public Mono<Void> execute(PlayTrackEvent event) {
        return Mono.justOrEmpty(event)
                .flatMap(tuple -> audioConnectionManager.requestTrack(event))
                .onErrorResume(throwable -> Mono.fromRunnable(() -> logger.error("Failed to play track", throwable)))
                .then();
    }
}
