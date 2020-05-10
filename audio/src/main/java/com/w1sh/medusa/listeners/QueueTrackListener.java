package com.w1sh.medusa.listeners;

import com.w1sh.medusa.AudioConnectionManager;
import com.w1sh.medusa.events.QueueTrackEvent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public final class QueueTrackListener implements EventListener<QueueTrackEvent> {

    private final AudioConnectionManager audioConnectionManager;

    public QueueTrackListener(AudioConnectionManager audioConnectionManager) {
        this.audioConnectionManager = audioConnectionManager;
    }

    @Override
    public Class<QueueTrackEvent> getEventType() {
        return QueueTrackEvent.class;
    }

    @Override
    public Mono<Void> execute(QueueTrackEvent event) {
        return Mono.justOrEmpty(event.getGuildId())
                .flatMap(audioConnectionManager::getAudioConnection)
                .zipWith(event.getMessage().getChannel(), (ac, channel) -> ac.getTrackScheduler().printQueue(channel))
                .then();
    }
}
