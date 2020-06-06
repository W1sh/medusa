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
    public Class<ShuffleQueueEvent> getEventType() {
        return ShuffleQueueEvent.class;
    }

    @Override
    public Mono<Void> execute(ShuffleQueueEvent event) {
        return Mono.justOrEmpty(event.getGuildId())
                .flatMap(audioConnectionManager::getAudioConnection)
                .zipWith(event.getMessage().getChannel(), (con, mc) -> con.getTrackScheduler().shuffle(mc))
                .then();
    }
}
