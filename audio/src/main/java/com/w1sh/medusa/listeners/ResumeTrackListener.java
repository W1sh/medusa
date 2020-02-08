package com.w1sh.medusa.listeners;

import com.w1sh.medusa.AudioConnectionManager;
import com.w1sh.medusa.data.events.EventFactory;
import com.w1sh.medusa.events.ResumeTrackEvent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public final class ResumeTrackListener implements EventListener<ResumeTrackEvent> {

    public ResumeTrackListener() {
        EventFactory.registerEvent(ResumeTrackEvent.KEYWORD, ResumeTrackEvent.class);
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
