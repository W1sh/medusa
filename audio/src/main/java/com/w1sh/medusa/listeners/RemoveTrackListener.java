package com.w1sh.medusa.listeners;

import com.w1sh.medusa.events.RemoveTrackEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class RemoveTrackListener implements EventListener<RemoveTrackEvent> {

    @Override
    public Class<RemoveTrackEvent> getEventType() {
        return RemoveTrackEvent.class;
    }

    @Override
    public Mono<Void> execute(RemoveTrackEvent event) {
        return Mono.empty();
    }
}
