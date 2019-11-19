package com.w1sh.medusa.api.audio.listeners;

import com.w1sh.medusa.api.audio.events.PlayTrackEvent;
import com.w1sh.medusa.core.dispatchers.CommandEventDispatcher;
import com.w1sh.medusa.core.listeners.EventListener;
import com.w1sh.medusa.core.managers.AudioConnectionManager;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class PlayTrackListener implements EventListener<PlayTrackEvent> {

    public PlayTrackListener(CommandEventDispatcher eventDispatcher) {
        eventDispatcher.registerListener(this);
    }

    @Override
    public Class<PlayTrackEvent> getEventType() {
        return PlayTrackEvent.class;
    }

    @Override
    public Mono<Void> execute(PlayTrackEvent event) {
        return Mono.justOrEmpty(event.getMessage().getContent())
                .zipWith(Mono.justOrEmpty(event.getGuildId()))
                .doOnNext(tuple -> AudioConnectionManager.getInstance().requestTrack(tuple.getT1(), tuple.getT2()))
                .then();
    }
}
