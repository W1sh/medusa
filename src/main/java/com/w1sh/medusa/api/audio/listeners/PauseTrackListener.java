package com.w1sh.medusa.api.audio.listeners;

import com.w1sh.medusa.api.audio.events.PauseTrackEvent;
import com.w1sh.medusa.core.dispatchers.CommandEventDispatcher;
import com.w1sh.medusa.core.listeners.EventListener;
import com.w1sh.medusa.core.managers.AudioConnectionManager;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class PauseTrackListener implements EventListener<PauseTrackEvent> {

    public PauseTrackListener(CommandEventDispatcher eventDispatcher) {
        eventDispatcher.registerListener(this);
    }

    @Override
    public Class<PauseTrackEvent> getEventType() {
        return PauseTrackEvent.class;
    }

    @Override
    public Mono<Void> execute(PauseTrackEvent event) {
        return Mono.justOrEmpty(event.getGuildId())
                .flatMap(AudioConnectionManager.getInstance()::getAudioConnection)
                .doOnNext(audioConnection -> audioConnection.getTrackScheduler().pause())
                .then();
    }
}
