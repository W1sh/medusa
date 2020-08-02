package com.w1sh.medusa.listeners;

import com.w1sh.medusa.AudioConnectionManager;
import com.w1sh.medusa.events.PauseTrackEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public final class PauseTrackListener implements EventListener<PauseTrackEvent> {

    private final AudioConnectionManager audioConnectionManager;

    @Override
    public Mono<Void> execute(PauseTrackEvent event) {
        return audioConnectionManager.getAudioConnection(event)
                .doOnNext(con -> con.getTrackScheduler().pause())
                .then();
    }
}
