package com.w1sh.medusa.listeners;

import com.w1sh.medusa.AudioConnectionManager;
import com.w1sh.medusa.core.dispatchers.CommandEventDispatcher;
import com.w1sh.medusa.core.events.EventFactory;
import com.w1sh.medusa.core.listeners.EventListener;
import com.w1sh.medusa.events.PauseTrackEvent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class PauseTrackListener implements EventListener<PauseTrackEvent> {

    public PauseTrackListener(CommandEventDispatcher eventDispatcher) {
        EventFactory.registerEvent(PauseTrackEvent.KEYWORD, PauseTrackEvent.class);
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
