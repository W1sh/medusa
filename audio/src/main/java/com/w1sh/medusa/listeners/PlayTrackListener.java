package com.w1sh.medusa.listeners;

import com.w1sh.medusa.AudioConnectionManager;
import com.w1sh.medusa.events.PlayTrackEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public final class PlayTrackListener implements EventListener<PlayTrackEvent> {

    private final AudioConnectionManager audioConnectionManager;

    @Override
    public Mono<Void> execute(PlayTrackEvent event) {
        return Mono.justOrEmpty(event)
                .flatMap(tuple -> audioConnectionManager.requestTrack(event))
                .onErrorResume(throwable -> Mono.fromRunnable(() -> log.error("Failed to play track", throwable)))
                .then();
    }
}
