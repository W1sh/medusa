package com.w1sh.medusa.api.audio.listeners;

import com.w1sh.medusa.api.audio.events.ResumeTrackEvent;
import com.w1sh.medusa.core.dispatchers.CommandEventDispatcher;
import com.w1sh.medusa.core.listeners.EventListener;
import com.w1sh.medusa.core.managers.AudioConnectionManager;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class ResumeTrackListener implements EventListener<ResumeTrackEvent> {

    public ResumeTrackListener(CommandEventDispatcher eventDispatcher) {
        eventDispatcher.registerListener(this);
    }

    @Override
    public Class<ResumeTrackEvent> getEventType() {
        return ResumeTrackEvent.class;
    }

    @Override
    public Mono<Void> execute(ResumeTrackEvent event) {
        return Mono.justOrEmpty(event.getGuildId())
                .flatMap(AudioConnectionManager.getInstance()::getAudioConnection)
                .doOnNext(audioConnection -> audioConnection.getTrackScheduler().resume())
                .then();
    }
}
