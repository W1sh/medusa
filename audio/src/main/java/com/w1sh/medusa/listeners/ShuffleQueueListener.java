package com.w1sh.medusa.listeners;

import com.w1sh.medusa.AudioConnectionManager;
import com.w1sh.medusa.events.ShuffleQueueEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public final class ShuffleQueueListener implements EventListener<ShuffleQueueEvent> {

    private final AudioConnectionManager audioConnectionManager;

    @Override
    public Mono<Void> execute(ShuffleQueueEvent event) {
        return audioConnectionManager.getAudioConnection(event)
                .doOnNext(con -> con.getTrackScheduler().shuffle())
                .then();
    }
}
