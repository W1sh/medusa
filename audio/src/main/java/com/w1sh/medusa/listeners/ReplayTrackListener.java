package com.w1sh.medusa.listeners;

import com.w1sh.medusa.AudioConnectionManager;
import com.w1sh.medusa.events.ReplayTrackEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public final class ReplayTrackListener implements EventListener<ReplayTrackEvent> {

    private final AudioConnectionManager audioConnectionManager;

    @Override
    public Class<ReplayTrackEvent> getEventType() {
        return ReplayTrackEvent.class;
    }

    @Override
    public Mono<Void> execute(ReplayTrackEvent event) {
        return audioConnectionManager.getAudioConnection(event)
                .doOnNext(audioConnection -> audioConnection.getTrackScheduler().replay())
                .then();
    }
}
