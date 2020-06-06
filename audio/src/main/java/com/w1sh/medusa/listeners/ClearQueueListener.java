package com.w1sh.medusa.listeners;

import com.w1sh.medusa.AudioConnectionManager;
import com.w1sh.medusa.events.ClearQueueEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public final class ClearQueueListener implements EventListener<ClearQueueEvent> {

    private final AudioConnectionManager audioConnectionManager;

    @Override
    public Class<ClearQueueEvent> getEventType() {
        return ClearQueueEvent.class;
    }

    @Override
    public Mono<Void> execute(ClearQueueEvent event) {
        return Mono.justOrEmpty(event.getGuildId())
                .flatMap(audioConnectionManager::getAudioConnection)
                .zipWith(event.getMessage().getChannel(), (con, mc) -> con.getTrackScheduler().clearQueue(mc))
                .then();
    }
}
