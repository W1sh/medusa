package com.w1sh.medusa.listeners;

import com.w1sh.medusa.AudioConnectionManager;
import com.w1sh.medusa.events.RemoveTrackEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public final class RemoveTrackListener implements CustomEventListener<RemoveTrackEvent> {

    private final AudioConnectionManager audioConnectionManager;

    @Override
    public Mono<Void> execute(RemoveTrackEvent event) {
        return Mono.justOrEmpty(event.getArguments().get(0))
                .map(Integer::parseInt)
                .zipWith(audioConnectionManager.getAudioConnection(event),
                        (index, audioConnection) -> Optional.ofNullable(audioConnection.getTrackScheduler().remove(index)))
                .then();
    }
}
