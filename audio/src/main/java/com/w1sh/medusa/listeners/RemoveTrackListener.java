package com.w1sh.medusa.listeners;

import com.w1sh.medusa.AudioConnectionManager;
import com.w1sh.medusa.events.RemoveTrackEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class RemoveTrackListener implements EventListener<RemoveTrackEvent> {

    private final AudioConnectionManager audioConnectionManager;

    @Override
    public Class<RemoveTrackEvent> getEventType() {
        return RemoveTrackEvent.class;
    }

    @Override
    public Mono<Void> execute(RemoveTrackEvent event) {
        return Mono.justOrEmpty(event.getArguments().get(0))
                .map(Integer::parseInt)
                .zipWith(audioConnectionManager.getAudioConnection(event),
                        (index, audioConnection) -> audioConnection.getTrackScheduler().remove(index))
                .then();
    }
}
