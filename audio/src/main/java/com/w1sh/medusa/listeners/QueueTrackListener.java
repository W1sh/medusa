package com.w1sh.medusa.listeners;

import com.w1sh.medusa.AudioConnectionManager;
import com.w1sh.medusa.events.QueueTrackEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public final class QueueTrackListener implements EventListener<QueueTrackEvent> {

    private final AudioConnectionManager audioConnectionManager;

    @Override
    public Class<QueueTrackEvent> getEventType() {
        return QueueTrackEvent.class;
    }

    // TODO: move playlist print logic to here
    @Override
    public Mono<Void> execute(QueueTrackEvent event) {
        return audioConnectionManager.getAudioConnection(event)
                .doOnNext(con -> con.getTrackScheduler().getFullQueue())
                .then();
    }
}
