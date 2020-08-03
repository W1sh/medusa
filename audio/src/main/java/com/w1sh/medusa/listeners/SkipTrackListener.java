package com.w1sh.medusa.listeners;

import com.w1sh.medusa.AudioConnectionManager;
import com.w1sh.medusa.events.SkipTrackEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public final class SkipTrackListener implements EventListener<SkipTrackEvent> {

    private final AudioConnectionManager audioConnectionManager;

    @Override
    public Mono<Void> execute(SkipTrackEvent event) {
        return audioConnectionManager.getAudioConnection(event)
                .doOnNext(con -> con.getTrackScheduler().skip())
                .then();
    }
}
